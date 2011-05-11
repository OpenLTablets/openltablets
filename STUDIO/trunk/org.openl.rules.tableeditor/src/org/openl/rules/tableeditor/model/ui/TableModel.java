package org.openl.rules.tableeditor.model.ui;

import org.apache.commons.lang.ArrayUtils;
import org.openl.rules.table.ui.FilteredGrid;
import org.openl.rules.table.ui.ICellStyle;
import org.openl.rules.table.ui.filters.IGridFilter;
import org.openl.rules.table.GridRegion;
import org.openl.rules.table.IGrid;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.IGridTable;

public class TableModel {

    private ICellModel[][] cells;

    private IGridTable gridTable;

    private int numRowsToDisplay = -1;

    public static TableModel initializeTableModel(IGridTable table) {
        return initializeTableModel(table, null);
    }

    public static TableModel initializeTableModel(IGridTable table, int numRows) {
        return initializeTableModel(table, null, numRows, null);
    }

    public static TableModel initializeTableModel(IGridTable table, IGridFilter[] filters) {
        return initializeTableModel(table, filters, -1, null);
    }

    public static TableModel initializeTableModel(IGridTable table, IGridFilter[] filters, int numRows, String showLinksBase) {
        if (table == null) {
            return null;
        }

        IGrid grid = null;

        if (!(grid instanceof FilteredGrid)) {
            if (ArrayUtils.isNotEmpty(filters)) {
                grid = new FilteredGrid(table.getGrid(), filters);
            } else {
                grid = table.getGrid();
            }
        }

        IGridRegion region = table.getRegion();
        if (numRows > -1 && region.getTop() + numRows < region.getBottom()) {
            region = new GridRegion(region);
            ((GridRegion) region).setBottom(region.getTop() + numRows - 1);
        }

        return new TableViewer(grid, region, showLinksBase).buildModel(table, numRows);
    }

    public TableModel(int width, int height, IGridTable gridTable) {
        cells = new ICellModel[height][];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new ICellModel[width];
        }

        this.gridTable = gridTable;
    }

    public int getNumRowsToDisplay() {
        return numRowsToDisplay;
    }

    public void setNumRowsToDisplay(int numRowsToDisplay) {
        this.numRowsToDisplay = numRowsToDisplay;
    }

    public void addCell(ICellModel cm, int row, int column) {
        if (row < cells.length && column < cells[row].length) {
            cells[row][column] = cm;
        }
    }

    public CellModel findCellModel(int col, int row, int border) {
        if (col < 0 || row < 0 || row >= cells.length || col >= cells[0].length) {
            return null;
        }

        ICellModel icm = cells[row][col];

        CellModel cm = null;
        switch (border) {
            case ICellStyle.TOP:
                if (icm instanceof CellModel) {
                    return (CellModel) icm;
                }
                cm = ((CellModelDelegator) icm).getModel();
                return cm.getRow() == row ? cm : null;
            case ICellStyle.LEFT:
                if (icm instanceof CellModel) {
                    return (CellModel) icm;
                }
                cm = ((CellModelDelegator) icm).getModel();
                return cm.getColumn() == col ? cm : null;
            case ICellStyle.RIGHT:
                if (icm instanceof CellModel) {
                    cm = (CellModel) icm;
                    return cm.getColspan() == 1 ? cm : null;
                }
                cm = ((CellModelDelegator) icm).getModel();
                return cm.getColumn() + cm.getColspan() - 1 == col ? cm : null;
            case ICellStyle.BOTTOM:
                if (icm instanceof CellModel) {
                    cm = (CellModel) icm;
                    return cm.getRowspan() == 1 ? cm : null;
                }
                cm = ((CellModelDelegator) icm).getModel();
                return cm.getRow() + cm.getRowspan() - 1 == row ? cm : null;
            default:
                throw new RuntimeException();

        }

    }

    public ICellModel findOnLeft(int row, int column) {
        if (column == 0) {
            return null;
        }
        return cells[row][column - 1];
    }

    public ICellModel findOnTop(int row, int col) {
        if (row == 0) {
            return null;
        }
        return cells[row - 1][col];
    }

    /**
     * Cells property getter
     *
     * @return cells
     */
    public ICellModel[][] getCells() {
        return cells;
    }

    public IGridTable getGridTable() {
        return gridTable;
    }

    public boolean hasCell(int r, int c) {
        return cells[r][c] != null;
    }

}
