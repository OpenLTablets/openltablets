package org.openl.rules.table;

/**
 * Default implementation for grid tables.
 * 
 * @author snshor
 */
public abstract class AGridTable implements IGridTable {

    public IGridRegion getRegion() {
        int left = getGridColumn(0, 0);
        int top = getGridRow(0, 0);

        int right = -1;
        int bottom = -1;

        if (isNormalOrientation()) {
            right = getGridColumn(getWidth() - 1, 0);
            bottom = getGridRow(0, getHeight() - 1);
        } else {
            right = getGridColumn(0, getHeight() - 1);
            bottom = getGridRow(getWidth() - 1, 0);
        }

        return new GridRegion(top, left, bottom, right);
    }

    public String getUri() {
        int w = getWidth();
        int h = getHeight();
        return getGrid().getRangeUri(getGridColumn(0, 0), getGridRow(0, 0), getGridColumn(w - 1, h - 1),
                getGridRow(w - 1, h - 1));
    }

    public String getUri(int col, int row) {
        int colStart = getGridColumn(col, row);
        int rowStart = getGridRow(col, row);
        return getGrid().getRangeUri(colStart, rowStart, colStart, rowStart);
    }

    public IGridTable transpose() {
        return new TransposedGridTable(this);
    }

    public ICell getCell(int column, int row) {
    	return new GridTableCell(column, row, this);
    }

    public IGridTable getColumn(int column) {
        return getColumns(column, column);
    }

    public IGridTable getColumns(int from) {
        return getColumns(from, getWidth() - 1);
    }

    public IGridTable getColumns(int from, int to) {
        int colsNum = to - from + 1;
        return getSubtable(from, 0, colsNum, getHeight());
    }

    public IGridTable getRow(int row) {
        return getRows(row, row);
    }

    public IGridTable getRows(int from) {
        return getRows(from, getHeight() - 1);
    }

    public IGridTable getRows(int from, int to) {
        int rowsNum = to - from + 1;
        return getSubtable(0, from, getWidth(), rowsNum);
    }

    public IGridTable getSubtable(int column, int row, int width, int height) {
        if (getWidth() == width && getHeight() == height) {
            return this;
        }

        return new SubGridTable(this, column, row, width, height);
    }

    @Override
    public String toString() {
        StringBuffer tableVizualization = new StringBuffer();
        tableVizualization.append(super.toString() + (isNormalOrientation() ? "N" : "T")
                +  getRegion().toString() +"\n");
        for (int i = 0; i < getHeight(); i++) {
            int length = 0;
            for (int j = 0; j < getWidth(); j++) {
                String strValue = getCell(j, i).getStringValue();
                if (strValue == null) {
                    strValue = "EMPTY";
                }
                length += strValue.length();
                tableVizualization.append(strValue);                
                tableVizualization.append("|");
            }
            tableVizualization.append("\n");
            for(int k = 0; k <= length; k++) {
                tableVizualization.append("-");
            }   
            tableVizualization.append("\n");
        }
        
        return  tableVizualization.toString();
    }

}
