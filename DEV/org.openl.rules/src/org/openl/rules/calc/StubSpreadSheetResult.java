package org.openl.rules.calc;

import java.util.HashMap;
import java.util.Map;

import org.openl.rules.helpers.ITableAdaptor;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.Point;

public class StubSpreadSheetResult extends SpreadsheetResult {
    private static final long serialVersionUID = 1L;

    private Map<String, Object> values = new HashMap<String, Object>();
    private Map<String, Point> fieldCoordinates = new HashMap<String, Point>();

    private int lastRow = 0;

    public StubSpreadSheetResult() {
        super(0, 0);
    }

    @Override
    public int height() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeight(int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[][] getResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResults(Object[][] results) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int width() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getWidth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWidth(int width) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getColumnNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setColumnNames(String[] columnNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getRowNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRowNames(String[] rowNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getValue(int row, int column) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void setValue(int row, int column, Object value) {
        for (java.util.Map.Entry<String, Point> entry : fieldCoordinates.entrySet()) {
            Point p = entry.getValue();
            if (p.getRow() == row && p.getColumn() == column) {
                String name = entry.getKey();
                values.put(name, value);
                return;
            }
        }
    }

    @Override
    public String getColumnName(int column) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRowName(int row) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Point> getFieldsCoordinates() {
        return fieldCoordinates;
    }

    @Override
    protected void addFieldCoordinates(String field, Point coord) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ILogicalTable getLogicalTable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLogicalTable(ILogicalTable logicalTable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getFieldValue(String name) {
        return values.get(name);
    }

    @Override
    public boolean hasField(String name) {
        if (!fieldCoordinates.containsKey(name)) {
            fieldCoordinates.put(name, new Point(0, lastRow));
            lastRow++;
        }
        return true;
    }

    @Override
    public ITableAdaptor makeTableAdaptor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String printAsTable() {
        throw new UnsupportedOperationException();
    }
}
