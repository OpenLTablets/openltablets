package org.openl.rules.tableeditor.event;

import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.mapper.MapperException;

import org.openl.rules.service.TableServiceImpl;
import org.openl.rules.table.ui.CellStyle;
import org.openl.rules.table.ui.ICellStyle;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.IGridTable;
import org.openl.rules.tableeditor.model.CellEditorSelector;
import org.openl.rules.tableeditor.model.EditorHelper;
import org.openl.rules.tableeditor.model.ICellEditor;
import org.openl.rules.tableeditor.model.TableEditorModel;
import org.openl.rules.tableeditor.renderkit.HTMLRenderer;
import org.openl.rules.tableeditor.util.Constants;
import org.openl.rules.util.net.NetUtils;
import org.openl.rules.web.jsf.util.FacesUtils;
import org.openl.rules.webtools.XlsUrlParser;

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;

/**
 * Table editor controller. It should be a managed bean with <b>request</b>
 * scope.
 *
 * @author Andrey Naumenko
 */
public class TableEditorController extends BaseTableEditorController implements JSTableEditor {

    public static class EditorTypeResponse {
        private String editor;
        private Object params;

        public EditorTypeResponse(String editor) {
            this.editor = editor;
        }

        public String getEditor() {
            return editor;
        }

        public Object getParams() {
            return params;
        }

        public void setEditor(String editor) {
            this.editor = editor;
        }

        public void setParams(Object params) {
            this.params = params;
        }
    }

    public static class LoadResponse {
        private String tableHTML;
        private String topLeftCell;
        private TableEditorModel model;

        public LoadResponse(String tableHTML, String topLeftCell, TableEditorModel model) {
            this.tableHTML = tableHTML;
            this.topLeftCell = topLeftCell;
            this.model = model;
        }

        public String getTableHTML() {
            return tableHTML;
        }

        public String getTopLeftCell() {
            return topLeftCell;
        }

        public boolean isHasRedo() {
            return model != null && model.hasRedo();
        }

        public boolean isHasUndo() {
            return model != null && model.hasUndo();
        }

        @SuppressWarnings("unused")
        public void setHasRedo(boolean hasRedo) {
        }

        @SuppressWarnings("unused")
        public void setHasUndo(boolean hasUndo) {
        }

        public void setTableHTML(String tableHTML) {
            this.tableHTML = tableHTML;
        }

        public void setTopLeftCell(String topLeftCell) {
            this.topLeftCell = topLeftCell;
        }

    }

    public static class RangeParam {
        private Long min, max;

        public RangeParam(Long min, Long max) {
            this.min = min;
            this.max = max;
        }

        public Long getMax() {
            return max;
        }

        public Long getMin() {
            return min;
        }

        public void setMax(Long max) {
            this.max = max;
        }

        public void setMin(Long min) {
            this.min = min;
        }
    }

    public static class SuggestParam {
        private Integer minChars;
        private Integer delay;

        public SuggestParam(Integer minChars, Integer delay) {
            this.minChars = minChars;
            this.delay = delay;
        }

        public Integer getDelay() {
            return delay;
        }

        public Integer getMinChars() {
            return minChars;
        }

        public void setDelay(Integer delay) {
            this.delay = delay;
        }

        public void setMinChars(Integer minChars) {
            this.minChars = minChars;
        }
    }

    public static class TableModificationResponse {
        private String response;
        private String status;
        private TableEditorModel model;

        public TableModificationResponse(String response, String status, TableEditorModel model) {
            this.response = response;
            this.status = status;
            this.model = model;
        }

        public TableModificationResponse(String response, TableEditorModel model) {
            this.response = response;
            this.model = model;
        }

        public String getResponse() {
            return response;
        }

        public String getStatus() {
            return status;
        }

        public boolean isHasRedo() {
            return model != null && model.hasRedo();
        }

        public boolean isHasUndo() {
            return model != null && model.hasUndo();
        }

        @SuppressWarnings("unused")
        public void setHasRedo(boolean hasRedo) {
        }

        @SuppressWarnings("unused")
        public void setHasUndo(boolean hasUndo) {
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public void setStatus(String status) {
            this.status = status;
        }

    }

    private static String pojo2json(Object pojo) {
        try {
            return new StringBuilder().append("(").append(JSONMapper.toJSON(pojo).render(true)).append(")").toString();
        } catch (MapperException e) {
            return null;
        }
    }

    public String insertRowColBefore() throws Exception {
        int row = getRow();
        int col = getCol();
        String editorId = getEditorId();
        EditorHelper editorHelper = getHelper(editorId);
        if (editorHelper != null) {
            TableEditorModel editorModel = editorHelper.getModel();

            TableModificationResponse tmResponse = new TableModificationResponse(null, editorHelper.getModel());
            try {
                if (row >= 0) {
                    if (editorModel.canAddRows(1)) {
                        editorModel.insertRows(1, row);
                    } else {
                        IGridRegion newRegion = new TableServiceImpl(false).moveTable(editorModel.getUpdatedTable(), null);                        
                        editorModel.setRegion(newRegion);
                        editorModel.insertRows(1, row);
                    }
                } else if (editorModel.canAddCols(1)) {
                    editorModel.insertColumns(1, col);
                } else {
                    tmResponse.setStatus("Can not add column");
                }
            } catch (Exception e) {
                tmResponse.setStatus("Internal server error");
                e.printStackTrace();
            }

            tmResponse.setResponse(render(editorId));
            return pojo2json(tmResponse);
        }
        return null;
    }

    public String edit() {
        String editorId = getRequestParam(Constants.REQUEST_PARAM_EDITOR_ID);
        String cellToEdit = getRequestParam(Constants.REQUEST_PARAM_CELL);
        EditorHelper editorHelper = getHelper(editorId);
        return new HTMLRenderer().render("edit", editorHelper.getTable(), null, null, false, cellToEdit, true, editorId, null);
    }

    public String editXls() {
        String cellUri = getRequestParam(Constants.REQUEST_PARAM_CELL_URI);
        boolean local = NetUtils.isLocalRequest((ServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                .getRequest());
        boolean wantURI = (cellUri == null || cellUri.equals("")) ? false : true;
        if (local) {
            if (wantURI) {
                XlsUrlParser parser = new XlsUrlParser();
                parser.parse(cellUri);
                try {
                    org.openl.rules.webtools.ExcelLauncher.launch("LaunchExcel.vbs", parser.wbPath, parser.wbName,
                            parser.wsName, parser.range);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (wantURI) {
            return "<script type='text/javascript'>"
                    + "alert('This action is available only from the machine server runs at.')</script>";
        }
        return null;
    }

    /**
     * Generates JSON response for cell type: editor type and editor specific
     * setup javascript object.
     *
     * @return {@link #OUTCOME_SUCCESS} jsf navigation case outcome
     *
     * Modified by snshor to reflect new Cell Editor creation/selection
     * framework
     */

    public String getCellType() {
        EditorHelper editorHelper = getHelper(getEditorId());
        if (editorHelper != null) {
            TableEditorModel model = editorHelper.getModel();
            ICellEditor editor = new CellEditorSelector().selectEditor(getRow(), getCol(), model);
            EditorTypeResponse typeResponse = editor.getEditorTypeAndMetadata();
            return pojo2json(typeResponse);
        }
        return "";
    }

    private int getCol() {
        return getRequestIntParam(Constants.REQUEST_PARAM_COL) - 1;
    }

    private String getEditorId() {
        return getRequestParam(Constants.REQUEST_PARAM_EDITOR_ID);
    }

    private int getRequestIntParam(String name) {
        int param = -1;
        try {
            String requestParam = getRequestParam(name);
            if (requestParam != null) {
                param = Integer.parseInt(requestParam);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return param;
    }

    private String getRequestParam(String name) {
        return FacesUtils.getRequestParameter(name);
    }

    private int getRow() {
        EditorHelper editorHelper = getHelper(getEditorId());
        int numberOfNonShownRows = editorHelper.getModel().getNumberOfNonShownRows();
        return getRequestIntParam(Constants.REQUEST_PARAM_ROW) - 1 + numberOfNonShownRows;
    }

    public String load() throws Exception {
        String editorId = getEditorId();
        String response = render(editorId);
        IGridTable gridTable = getGridTable(editorId);
        response = pojo2json(new LoadResponse(response, gridTable.getGrid().getCellUri(gridTable.getGridColumn(0, 0),
                gridTable.getGridRow(0, 0)), getEditorModel(editorId)));
        return response;
    }

    public String removeRowCol() throws Exception {
        int row = getRow();
        int col = getCol();
        String editorId = getEditorId();
        EditorHelper editorHelper = getHelper(editorId);
        if (editorHelper != null) {
            TableEditorModel editorModel = editorHelper.getModel();
            boolean move = Boolean.valueOf(getRequestParam(Constants.REQUEST_PARAM_MOVE));

            if (row >= 0) {
                if (move) {
                    ;
                } else {
                    editorModel.removeRows(1, row);
                }
            } else {
                if (move) {
                    ;
                } else {
                    editorModel.removeColumns(1, col);
                }
            }
            return pojo2json(new TableModificationResponse(render(editorId), editorHelper.getModel()));
        }
        return null;
    }

    public String setIndent() throws Exception {
        int row = getRow();
        int col = getCol();
        String editorId = getEditorId();
        EditorHelper editorHelper = getHelper(editorId);
        if (editorHelper != null) {
            int indent = getRequestIntParam(Constants.REQUEST_PARAM_INDENT);
            ICellStyle style = editorHelper.getModel().getCellStyle(row, col);
            int currentIndent = style.getIdent();
            int resultIndent = currentIndent + indent;
            CellStyle newStyle = new CellStyle(style);
            newStyle.setIdent(resultIndent >= 0 ? resultIndent : 0);
            editorHelper.getModel().setStyle(row, col, newStyle);
            return pojo2json(new TableModificationResponse(null, editorHelper.getModel()));
        }
        return null;
    }

    /**
     * Handles request saving new cell value.
     *
     * @return {@link #OUTCOME_SUCCESS} jsf navigation case outcome
     */
    public String setCellValue() {
        String value = getRequestParam(Constants.REQUEST_PARAM_VALUE);
        EditorHelper editorHelper = getHelper(getEditorId());
        if (editorHelper != null) {
            editorHelper.getModel().setCellValue(getRow(), getCol(), value);
            return pojo2json(new TableModificationResponse(null, editorHelper.getModel()));
        }
        return null;
    }

    public String setProp() throws Exception {
        String name = getRequestParam(Constants.REQUEST_PARAM_PROP_NAME);
        String value = getRequestParam(Constants.REQUEST_PARAM_PROP_VALUE);
        String editorId = getEditorId();
        EditorHelper editorHelper = getHelper(editorId);
        if (editorHelper != null) {
            TableEditorModel editorModel = editorHelper.getModel();
            if (editorModel.canAddRows(1)) {
                editorModel.insertProp(name, value);
            } else {
                IGridRegion newRegion = new TableServiceImpl(false).moveTable(editorModel.getUpdatedTable(), null);
                editorModel.setRegion(newRegion);
                editorModel.insertProp(name, value);
            }
            TableModificationResponse tmResponse = new TableModificationResponse(null, editorHelper.getModel());
            tmResponse.setResponse(render(editorId));
            return pojo2json(tmResponse);
        }
        return null;
    }

    public String setAlign() throws Exception {
        int row = getRow();
        int col = getCol();
        String editorId = getEditorId();
        EditorHelper editorHelper = getHelper(editorId);
        if (editorHelper != null) {
            String align = getRequestParam(Constants.REQUEST_PARAM_ALIGN);
            int halign = -1;
            if ("left".equalsIgnoreCase(align)) {
                halign = ICellStyle.ALIGN_LEFT;
            } else if ("center".equalsIgnoreCase(align)) {
                halign = ICellStyle.ALIGN_CENTER;
            } else if ("right".equalsIgnoreCase(align)) {
                halign = ICellStyle.ALIGN_RIGHT;
            } else if ("justify".equalsIgnoreCase(align)) {
                halign = ICellStyle.ALIGN_JUSTIFY;
            }

            if (halign != -1) {
                ICellStyle style = editorHelper.getModel().getCellStyle(row, col);
                if (style.getHorizontalAlignment() != halign) {
                    CellStyle newStyle = new CellStyle(style);
                    newStyle.setHorizontalAlignment(halign);
                    editorHelper.getModel().setStyle(row, col, newStyle);
                }
            }
            return pojo2json(new TableModificationResponse(null, editorHelper.getModel()));
        }
        return null;
    }

    public String undo() throws Exception {
        String editorId = getEditorId();
        EditorHelper editorHelper = getHelper(editorId);
        if (editorHelper != null) {
            TableModificationResponse tmResponse = new TableModificationResponse(null, editorHelper.getModel());
            if (editorHelper.getModel().hasUndo()) {
                editorHelper.getModel().undo();
                tmResponse.setResponse(render(editorId));
            } else {
                tmResponse.setStatus("No actions to undo");
            }
            return pojo2json(tmResponse);
        }
        return null;
    }

    public String redo() throws Exception {
        String editorId = getEditorId();
        EditorHelper editorHelper = getHelper(editorId);
        if (editorHelper != null) {
            TableModificationResponse tmResponse = new TableModificationResponse(null, editorHelper.getModel());
            if (editorHelper.getModel().hasRedo()) {
                editorHelper.getModel().redo();
                tmResponse.setResponse(render(editorId));
            } else {
                tmResponse.setStatus("No actions to redo");
            }
            return pojo2json(tmResponse);
        }
        return null;
    }

    public String saveTable() throws Exception {
        EditorHelper editorHelper = getHelper(getEditorId());
        if (editorHelper != null) {
            editorHelper.getModel().save();
            return pojo2json(new TableModificationResponse("", editorHelper.getModel()));
        }
        return null;
    }

}
