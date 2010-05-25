/**
 * Created Jan 5, 2007
 */
package org.openl.rules.ui;

import java.lang.reflect.Array;

import org.openl.base.INamedThing;
import org.openl.meta.DoubleValue;
import org.openl.meta.IMetaHolder;
import org.openl.rules.calc.SpreadsheetResult;
import org.openl.rules.table.FormattedCell;
import org.openl.rules.table.GridTable;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.ui.FilteredGrid;
import org.openl.rules.table.ui.IGridSelector;
import org.openl.rules.table.ui.filters.IGridFilter;
import org.openl.rules.table.ui.filters.SimpleHtmlFilter;
import org.openl.rules.table.ui.filters.TableValueFilter;
import org.openl.rules.table.ui.filters.XlsSimpleFilter;
import org.openl.rules.table.xls.formatters.AXlsFormatter;
import org.openl.rules.webstudio.web.jsf.WebContext;
import org.openl.rules.webtools.WebTool;
import org.openl.util.StringTool;

/**
 * @author snshor
 * 
 * @deprecated
 */
public class ObjectViewer {

    public ObjectViewer() {
    }

    public static Object displaySpreadsheetResult(final SpreadsheetResult res) {
        ILogicalTable table = res.getLogicalTable();
        IGridTable gt = table.getGridTable();

        final int firstRowHeight = table.getLogicalRow(0).getGridTable().getGridHeight();
        final int firstColWidth = table.getLogicalColumn(0).getGridTable().getGridWidth();

        TableValueFilter.Model model = new TableValueFilter.Model() {

            public Object getValue(int col, int row) {
                if (row < firstRowHeight) {
                    return null; // the row 0 contains column headers
                }
                if (col < firstColWidth) {
                    return null;
                }
                if (res.width() <= col - firstColWidth || res.height() <= row - firstRowHeight) {
                    return null;
                }

                return res.getValue(row - firstRowHeight, col - firstColWidth);
            }

        };

        TableValueFilter tvf = new TableValueFilter(gt, model);
        IGridFilter[] filters = { tvf, new XlsSimpleFilter(), new SimpleHtmlFilter(), new LinkMaker(tvf) };

        FilteredGrid fg = new FilteredGrid(gt.getGrid(), filters);

        // AB: show only results
        // return new Object[]{gt, new GridTable(gt.getRegion(), fg)};
        return new GridTable(gt.getRegion(), fg);
    }

    private String displayArray(Object res) {
        int len = Array.getLength(res);
        if (len == 0) {
            return "[]";
        }

        Object el = Array.get(res, 0);
        if (el == null) {
            return "[]";
        }

        if (el.getClass().isArray()) {
            return displayMatrix(res);
        }

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < len; i++) {
            buf.append("<p>\n");
            buf.append(displayResult(Array.get(res, i)));
        }

        return buf.toString();
    }

    private String displayMatrix(Object res) {
        int len = Array.getLength(res);

        StringBuffer buf = new StringBuffer();

        buf.append("<table class='ov-matrix'>\n");

        for (int i = 0; i < len; i++) {

            Object row = Array.get(res, i);
            if (row == null) {
                continue;
            }

            buf.append("<tr>\n");
            int l2 = Array.getLength(row);

            for (int j = 0; j < l2; j++) {
                Object x = Array.get(row, j);
                buf.append("<td class='ov-matrix'>");
                buf.append(displayResult(x));
                buf.append("</td>");
            }

            buf.append("</tr>\n");

        }

        buf.append("</table>\n");

        return buf.toString();
    }

    public StringBuffer displayMetaHolder(IMetaHolder mh, String display, StringBuffer buf) {
        buf.append("<a ");
        makeXlsOrDocUrl(mh.getMetaInfo().getSourceUrl(), buf);
        buf.append(">");
        StringTool.encodeHTMLBody(display, buf);
        buf.append("</a>");

        return buf;

    }

    public String displayResult(Object res) {
        if (res == null) {
            return "<b>null</b>";
        }

        if (res.getClass().isArray()) {
            return displayArray(res);
        }

        if (res instanceof IMetaHolder) {
            return displayMetaHolder((IMetaHolder) res, String.valueOf(res), new StringBuffer()).toString();
        }

        if (res instanceof IGridTable) {
            IGridTable tt = (IGridTable) res;

            return ProjectModel.showTable(tt, false);
        }

        if (res instanceof INamedThing) {
            return ((INamedThing) res).getName();
        }

        if (res instanceof SpreadsheetResult) {
            SpreadsheetResult sres = (SpreadsheetResult) res;
            Object d = displaySpreadsheetResult(sres);
            return displayResult(d);
        }

        String value = res.toString();

        return value;
    }

    private void makeXlsOrDocUrl(String uri, StringBuffer buf) {
        String url = WebTool.makeXlsOrDocUrl(uri);
        buf.append("href='" + WebContext.getContextPath() + "/jsp/showLinks.jsp?").append(url).append("'");
        buf.append(" target='show_app_hidden'");
    }

    private static class LinkMaker implements IGridFilter, IGridSelector {

        private String url;

        private TableValueFilter dataAdapter;

        public LinkMaker(TableValueFilter dataAdapter) {
            super();
            this.dataAdapter = dataAdapter;
        }

        public FormattedCell filterFormat(FormattedCell cell) {

            String fontStyle = WebTool.fontToHtml(cell.getFont(), new StringBuilder()).toString();

            cell.setFormattedValue("<a href=\"" + url + "\" class=\"nounderline\" style=\"" + fontStyle + "\"  >"
                    + cell.getFormattedValue() + "</a>");
            return cell;
        }

        public IGridSelector getGridSelector() {
            return this;
        }

        private String makeUrl(int col, int row, TableValueFilter dataAdapter) {
            Object obj = dataAdapter.getCellValue(col, row);

            if (obj == null || !(obj instanceof DoubleValue)) {
                return null;
            }

            DoubleValue dv = (DoubleValue) obj;
            if (Math.abs(dv.doubleValue()) < 0.005) {
                return null;
            }

            return getURL(dv);
        }

        public static String getURL(DoubleValue dv) {
            int rootID = Explanator.getCurrent().getUniqueId(dv);
            return "javascript: open_explain_win(\'?rootID=" + rootID + "&header=Explanation')";
        }

        public Object parse(String value) {
            return value;
        }

        public boolean selectCoords(int col, int row) {
            url = makeUrl(col, row, dataAdapter);

            return url != null;
        }

        public AXlsFormatter getFormatter() {
            return null;
        }

    }

}
