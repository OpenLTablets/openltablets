package org.openl.rules.ui;

import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openl.rules.lang.xls.ITableNodeTypes;
import org.openl.rules.lang.xls.XlsSheetSourceCodeModule;
import org.openl.rules.lang.xls.binding.TableProperties;
import org.openl.rules.lang.xls.binding.TableProperties.Property;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ui.ICellStyle;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.rules.table.xls.builder.CreateTableException;
import org.openl.rules.table.xls.builder.TableBuilder;
import org.openl.rules.ui.tablewizard.WizardBase;
import org.openl.rules.web.jsf.util.FacesUtils;
import org.openl.rules.webstudio.web.util.WebStudioUtils;

/**
 * Backing bean for table coping.
 * 
 * @author Andrei Astrouski.
 */
public class TableCopier extends WizardBase {

    /** Logger */
    private static final Log log = LogFactory.getLog(TableCopier.class);
    /** Table identifier */
    private int elementId;
    /** Table technical name */
    private String tableTechnicalName;
    /** Table business name */
    private String tableBusinessName;

    public TableCopier() {
        start();
        init();
    }

    /**
     * Initializes table properties.
     */
    private void init() {
        String elementIdParam = FacesUtils.getRequestParameter("elementID");
        WebStudio studio = WebStudioUtils.getWebStudio();
        if (StringUtils.isNotBlank(elementIdParam)) {
            elementId = Integer.parseInt(elementIdParam);
            studio.setTableID(elementId);
            ProjectModel model = studio.getModel();
            TableSyntaxNode node = model.getNode(elementId);
            tableTechnicalName = parseTechnicalName(
                    node.getHeaderLineValue().getValue(),
                    node.getType());
            tableBusinessName = node == null ? null : node.getProperty(
                    TableBuilder.TABLE_PROPERTIES_NAME);
        } else {
            elementId = studio.getTableID();
        }
    }
    
    @Override
    protected void onStart() {
        reset();
        initWorkbooks();
    }

    @Override
    protected void onFinish(boolean cancelled) {
        reset();
    }

    /**
     * Cleans table properties.
     */
    protected void reset() {
        super.reset();
        elementId = -1;
        tableTechnicalName = null;
        tableBusinessName = null;
    }

    public String getTableBusinessName() {
        return tableBusinessName;
    }

    public void setTableBusinessName(String tableBusinessName) {
        this.tableBusinessName = tableBusinessName;
    }

    public String getTableTechnicalName() {
        return tableTechnicalName;
    }

    public void setTableTechnicalName(String tableTechnicalName) {
        this.tableTechnicalName = tableTechnicalName;
    }

    /**
     * Parses table header for technical name
     * 
     * @param header table header to parse
     * @param tableType type of table
     * @return technical name of table
     */
    private String parseTechnicalName(String header, String tableType) {
        if (tableType.equals(ITableNodeTypes.XLS_ENVIRONMENT)
                || tableType.equals(ITableNodeTypes.XLS_OTHER)) {
            return null;
        }
        header = header.replaceFirst("\\(.*\\)", "");
        String[] headerTokens = StringUtils.split(header);
        return headerTokens[headerTokens.length - 1];
    }

    /**
     * Copy table handler.
     */
    public String copy() {
        boolean success = false;
        try {
            doCopy();
            success = true;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not copy table", e.getMessage()));
            log.error("Could not copy table: ", e);
        }
        if (success) {
            FacesContext.getCurrentInstance().addMessage(
                    null, new FacesMessage("Table was copied successful"));
        }
        return null;
    }

    /**
     * Copies table.
     * 
     * @throws CreateTableException
     */
    private void doCopy() throws CreateTableException {
        WebStudio studio = WebStudioUtils.getWebStudio();
        ProjectModel model = studio.getModel();
        XlsSheetSourceCodeModule sourceCodeModule = getDestinationSheet();
        buildTable(sourceCodeModule, model);
    }

    /**
     * Creates new table.
     * @param sourceCodeModule excel sheet to save in
     * @param model table model
     * @throws CreateTableException
     */
    private void buildTable(XlsSheetSourceCodeModule sourceCodeModule,
            ProjectModel model) throws CreateTableException {
        IGridTable table = model.getTable(elementId);
        TableSyntaxNode node = model.getNode(elementId);
        String tableType = node.getType();
        
        TableBuilder builder = new TableBuilder(
                new XlsSheetGridModel(sourceCodeModule));

        int tableWidth = table.getGridWidth();
        int tableHeight = table.getGridHeight();
        int logicTableStartRow = 0;
        
        builder.beginTable(tableWidth, tableHeight);

        if (!tableType.equals(ITableNodeTypes.XLS_ENVIRONMENT)
                && !tableType.equals(ITableNodeTypes.XLS_OTHER)) {
            String newHeader = buildHeader(
                    node.getHeaderLineValue().getValue(), tableType);
            ICellStyle headerStyle = table.getCellStyle(0, 0);
            builder.writeHeader(newHeader, headerStyle);
            logicTableStartRow++;

            TableProperties tableProperties = node.getTableProperties();
            Property[] properties = null;
            ICellStyle propertiesStyle = null;
            if (tableProperties != null) {
                IGridTable propertiesTable = tableProperties.getTable()
                    .getGridTable();
                propertiesStyle = propertiesTable.getCellStyle(0, 0) == null ?
                        null : propertiesTable.getCellStyle(0, 0);
                properties = tableProperties.getProperties();
            }
            builder.writeProperties(buildProperties(properties), propertiesStyle);
            logicTableStartRow += properties == null ? 0 : properties.length;
        }

        builder.writeGridTable(table.getLogicalRegion(
                0, logicTableStartRow, tableWidth,
                tableHeight - logicTableStartRow).getGridTable());

        builder.endTable();
    }

    /**
     * Creates new header. 
     * @param header old header
     * @param tableType type of table
     * @return new header
     */
    private String buildHeader(String header, String tableType) {
        String tableOldTechnicalName = parseTechnicalName(header, tableType);
        return header.replaceFirst(tableOldTechnicalName,
                tableTechnicalName.trim());
    }

    /**
     * Creates new properties. 
     * 
     * @param properties old properties
     * @return new properties
     */
    private Map<String, String> buildProperties(Property[] properties) {
        Map<String, String> newProperties = new HashMap<String, String>();
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                String key = properties[i].getKey().getValue();
                String value = properties[i].getValue().getValue();
                newProperties.put(key.trim(), value.trim());
            }
        }
        if (StringUtils.isBlank(tableBusinessName)
                && newProperties.containsKey(TableBuilder.TABLE_PROPERTIES_NAME)) {
            newProperties.remove(TableBuilder.TABLE_PROPERTIES_NAME);
        } else if (StringUtils.isNotBlank(tableBusinessName)) {
            newProperties.put(TableBuilder.TABLE_PROPERTIES_NAME, tableBusinessName);
        }
        return newProperties;
    }

}
