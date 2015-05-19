/**
 * Created Jan 5, 2007
 */
package org.openl.rules.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openl.meta.explanation.ExplanationNumberValue;
import org.openl.rules.calc.SpreadsheetResult;
import org.openl.rules.table.FormattedCell;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.Point;
import org.openl.rules.table.ui.filters.AGridFilter;
import org.openl.rules.table.ui.filters.CollectionCellFilter;
import org.openl.rules.table.ui.filters.ExpectedResultFilter;
import org.openl.rules.table.ui.filters.IGridFilter;
import org.openl.rules.table.ui.filters.TableValueFilter;
import org.openl.rules.tableeditor.model.ui.TableModel;
import org.openl.rules.tableeditor.renderkit.HTMLRenderer;
import org.openl.rules.testmethod.result.ComparedResult;

/**
 * @author snshor
 * 
 * @deprecated
 */
public final class ObjectViewer {

    private ObjectViewer() {
    }

    /** Display SpreadsheetResult with added filter for given fields as expected result and passed/failed icon**/
    public static String displaySpreadsheetResult(final SpreadsheetResult res, Map<Point, ComparedResult> spreadsheetCellsForTest) {
        return display(res, spreadsheetCellsForTest, true);
    }

    /** Display SpreadsheetResult with filter for links to explanation for values*/
    public static String displaySpreadsheetResult(final SpreadsheetResult res) {
        return display(res, null, true);
    }

    /** Display SpreadsheetResult without any filters in the table**/
    public static String displaySpreadsheetResultNoFilters(final SpreadsheetResult res) {
        return display(res, null, false);
    }

    private static String display(final SpreadsheetResult res, Map<Point, ComparedResult> spreadsheetCellsForTest, boolean filter) {
        ILogicalTable table = res.getLogicalTable();

        final int firstRowHeight = table.getRow(0).getSource().getHeight();
        final int firstColWidth = table.getColumn(0).getSource().getWidth();

        TableValueFilter.Model model = new TableValueFilter.Model() {

            public Object getValue(int col, int row) {
                if (row < firstRowHeight) {
                    return null; // the row 0 contains column headers
                }
                if (col < firstColWidth) {
                    return null;
                }
                if (res.getWidth() <= col - firstColWidth || res.getHeight() <= row - firstRowHeight) {
                    return null;
                }

                return res.getValue(row - firstRowHeight, col - firstColWidth);
            }

        };

        IGridTable gridtable = table.getSource();
        TableValueFilter tableValueFilter = new TableValueFilter(gridtable, model);
        CollectionCellFilter collectionFilter = new CollectionCellFilter();

        List<IGridFilter> filters = new ArrayList<IGridFilter>();
        filters.add(tableValueFilter);
        filters.add(collectionFilter);

        if (filter) {
            filters.add(new LinkMaker());

            // Check if the cells for test are initialized,
            // Means Spreadsheet should be displayed with expected values for tests
            //
            if (spreadsheetCellsForTest != null) {
                ExpectedResultFilter expResFilter = new ExpectedResultFilter(spreadsheetCellsForTest);
                filters.add(expResFilter);
            }
        }

        TableModel tableModel = TableModel.initializeTableModel(gridtable, filters.toArray(new IGridFilter[filters.size()]));
        return new HTMLRenderer.TableRenderer(tableModel).render(false);
    }

    private static class LinkMaker extends AGridFilter {

        public FormattedCell filterFormat(FormattedCell cell) {
            Object value = cell.getObjectValue();
            if (value instanceof ExplanationNumberValue<?>) {
                int rootID = Explanator.getCurrent().getUniqueId((ExplanationNumberValue<?>) value);
                String url = "javascript: explain(\'?rootID=" + rootID + "')";
                cell.setFormattedValue("<a href=\"" + url + "\">" + cell.getFormattedValue() + "</a>");
            }
            return cell;
        }
    }

}
