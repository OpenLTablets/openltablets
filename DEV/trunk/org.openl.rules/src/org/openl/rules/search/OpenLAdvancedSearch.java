/**
 * Created May 3, 2007
 */
package org.openl.rules.search;

import java.util.ArrayList;

import org.openl.rules.lang.xls.ITableNodeTypes;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.lang.xls.syntax.XlsModuleSyntaxNode;
import org.openl.syntax.ISyntaxError;
import org.openl.util.AStringBoolOperator;
import org.openl.util.ArrayTool;

/**
 * @author snshor
 *
 *
 */
public class OpenLAdvancedSearch implements ITableNodeTypes, ISearchConstants, IOpenLSearch {

    static public String[] typeButtons = { "Rules", "Spreadsheet", "TBasic", "Column Match", "Data", "Method",
            "Datatype", "Test", "Run", "Env", "Other" };

    static public String[] types = { XLS_DT, XLS_SPREADSHEET, XLS_TBASIC, XLS_COLUMN_MATCH, XLS_DATA, XLS_METHOD,
            XLS_DATATYPE, XLS_TEST_METHOD, XLS_RUN_METHOD, XLS_ENVIRONMENT, XLS_OTHER };

    static public String[] nfValues = { "", "NOT" };

    static public final boolean[] typeNeedValue1 = { false, true };

    boolean[] selectedType = new boolean[typeButtons.length];

    SearchElement[] tableElements = { new SearchElement(HEADER), new SearchElement(PROPERTY) };
    SearchElement[] columnElements = { new SearchElement(COLUMN_NAME), new SearchElement(COLUMN_TYPE) };

    private void addColumnPropertyAfter(int i) {
        columnElements = (SearchElement[]) ArrayTool.insertValue(i + 1, columnElements, columnElements[i].copy());
    }

    /**
     * @param i
     */
    private void addTablePropertyAfter(int i) {
        tableElements = (SearchElement[]) ArrayTool.insertValue(i + 1, tableElements, tableElements[i].copy());
    }

    private void deleteColumnPropertyAt(int i) {
        columnElements = (SearchElement[]) ArrayTool.removeValue(i, columnElements);
    }

    /**
     * @param i
     */
    private void deleteTablePropertyAt(int i) {
        tableElements = (SearchElement[]) ArrayTool.removeValue(i, tableElements);
    }

    /**
     * @param tableElements2
     * @param i
     */
    // private void makeLengthTableElements(int len)
    // {
    // SearchElement[] xx = new SearchElement[len];
    // System.arraycopy(tableElements, 0, xx, 0, Math.min(len,
    // tableElements.length));
    // tableElements = xx;
    // }

    public void editAction(String action) {
        if (action.startsWith(ADD_ACTION)) {
            addTablePropertyAfter(Integer.parseInt(action.substring(ADD_ACTION.length())));
        } else if (action.startsWith(DELETE_ACTION)) {
            deleteTablePropertyAt(Integer.parseInt(action.substring(DELETE_ACTION.length())));
        } else if (action.startsWith(COL_ADD_ACTION)) {
            addColumnPropertyAfter(Integer.parseInt(action.substring(COL_ADD_ACTION.length())));
        } else if (action.startsWith(COL_DELETE_ACTION)) {
            deleteColumnPropertyAt(Integer.parseInt(action.substring(COL_DELETE_ACTION.length())));
        }

    }

    public void fillColumnElement(int i, String gopID, String nfID, String typeID, String opType1ID, String value1ID,
            String opType2ID, String value2ID) {
        if (i >= columnElements.length) {
            return;
        }

        if (typeID == null) {
            typeID = COLUMN_NAME;
        }

        SearchElement se = new SearchElement(typeID);

        se.setOperator(GroupOperator.find(gopID));
        se.setNotFlag(nfValues[1].equals(nfID));

        se.setOpType1(opType1ID);
        if (value1ID != null) {
            se.setValue1(value1ID);
        }
        se.setOpType2(opType2ID);
        se.setValue2(value2ID);

        columnElements[i] = se;

    }

    public void fillTableElement(int i, String gopID, String nfID, String typeID, String value1ID, String opTypeID,
            String value2ID) {
        if (i >= tableElements.length) {
            return;
        }

        if (typeID == null) {
            typeID = PROPERTY;
        }

        SearchElement se = new SearchElement(typeID);

        se.setOperator(GroupOperator.find(gopID));
        se.setNotFlag(nfValues[1].equals(nfID));
        if (value1ID != null) {
            se.setValue1(value1ID);
        }
        se.setOpType2(opTypeID);
        se.setValue2(value2ID);

        tableElements[i] = se;

    }

    public SearchElement[] getColumnElements() {
        return columnElements;
    }

    public String[] getGopValues() {
        return GroupOperator.names;
    }

    public SearchElement[] getTableElements() {
        return tableElements;
    }

    public String[] getTypeButtons() {
        return typeButtons;
    }

    boolean isRowSelected(ISearchTableRow trow, ATableRowSelector[] rselectors, ITableSearchInfo tsi) {
        for (int j = 0; j < rselectors.length; j++) {
            if (!rselectors[j].selectRowInTable(trow, tsi)) {
                return false;
            }
        }

        return true;

    }

    boolean isTableSelected(TableSyntaxNode tsn, ATableSyntaxNodeSelector[] tselectors) {
        for (int j = 0; j < tselectors.length; j++) {
            if (!tselectors[j].selectTable(tsn)) {
                return false;
            }
        }

        return true;

    }

    /**
     * @return
     */
    private ATableSyntaxNodeSelector makePropertyOrHeaderSelectors() {
        return new TableGroupSelector(tableElements);
    }

    TableTypeSelector makeTableTypeSelector() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < selectedType.length; i++) {
            if (selectedType[i]) {
                list.add(types[i]);
            }
        }

        String[] tt = list.toArray(new String[0]);

        return new TableTypeSelector(tt);
    }

    public String[] opTypeValues() {
        return AStringBoolOperator.allNames();
    }

    ATableRowSelector[] getRowSelectors() {
        ArrayList<ATableRowSelector> list = new ArrayList<ATableRowSelector>();

        list.add(new ColumnGroupSelector(columnElements));

        return list.toArray(new ATableRowSelector[0]);

    }

    /*
     * public void fillTableElement(int i, String gopID, String nfID, String
     * typeID, String value1ID, String opTypeID, String value2ID) { boolean
     * isEmpty = typeID == null; int MIN_LEN = 1;
     *
     * if (isEmpty) { if (i < tableElements.length)
     * makeLengthTableElements(Math.max(i, MIN_LEN)); return; }
     * makeLengthTableElements(Math.max(i+1, MIN_LEN));
     *
     *
     *
     * SearchElement se = new SearchElement(typeID);
     *
     * se.setOperator(GroupOperator.find(gopID));
     * se.setNotFlag(nfValues[1].equals(nfID)); se.setValue1(value1ID);
     * se.setOpType(opTypeID); se.setValue2(value2ID);
     *
     * tableElements[i] = se;
     *
     *  }
     *
     */

    /**
     * @param xsn
     * @return
     */
    public Object search(XlsModuleSyntaxNode xsn) {
        ATableSyntaxNodeSelector[] tableSelectors = getTableSelectors();
        ATableRowSelector[] rowSelectors = getRowSelectors();

        // ArrayList tableList = new ArrayList();
        // ArrayList rowList = new ArrayList();

        OpenLAdvancedSearchResult res = new OpenLAdvancedSearchResult(this);

        TableSyntaxNode[] tables = xsn.getXlsTableSyntaxNodesWithoutErrors();
        for (int i = 0; i < tables.length; i++) {
            TableSyntaxNode table = tables[i];

            ISyntaxError[] errors = table.getErrors();
            if (errors != null && errors.length > 0) {
                continue;
            }

            if (!isTableSelected(table, tableSelectors)) {
                continue;
            }

            ITableSearchInfo tablSearchInfo = ATableRowSelector.getTableSearchInfo(table);
            if (tablSearchInfo == null) {
                res.add(table, new ISearchTableRow[0]);
                continue;
            }

            ArrayList<ISearchTableRow> selectedRows = new ArrayList<ISearchTableRow>();
            int numRows = tablSearchInfo.numberOfRows();
            for (int row = 0; row < numRows; row++) {
                ISearchTableRow tablSearchRow = new SearchTableRow(row, tablSearchInfo);
                if (!(tablSearchInfo instanceof TableSearchInfo) && !isRowSelected(tablSearchRow, rowSelectors, tablSearchInfo)) {
                    continue;
                }
                selectedRows.add(tablSearchRow);
            }

            res.add(table, selectedRows.toArray(new ISearchTableRow[0]));
        }

        return res;
    }

    public boolean selectType(int i) {
        return selectedType[i];
    }

    public void selectType(int i, boolean x) {
        selectedType[i] = x;
    }

    public void setColumnElements(SearchElement[] columnElements) {
        this.columnElements = columnElements;
    }

    public void setTableElements(SearchElement[] tableElements) {
        this.tableElements = tableElements;
    }

    public boolean showValue1(String typeValue) {
        for (int i = 0; i < typeValues.length; i++) {
            if (typeValues[i].equals(typeValue)) {
                return typeNeedValue1[i];
            }
        }
        throw new RuntimeException("Unknown type value: " + typeValue);
    }

    ATableSyntaxNodeSelector[] getTableSelectors() {
        ArrayList<ATableSyntaxNodeSelector> sll = new ArrayList<ATableSyntaxNodeSelector>();

        sll.add(makeTableTypeSelector());

        sll.add(makePropertyOrHeaderSelectors());

        return sll.toArray(new ATableSyntaxNodeSelector[0]);
    }

}
