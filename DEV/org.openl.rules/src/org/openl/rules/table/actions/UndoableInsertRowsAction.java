package org.openl.rules.table.actions;

import org.openl.rules.table.GridRegion;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.IWritableGrid;
import org.openl.rules.table.actions.GridRegionAction.ActionType;

/**
 * @author Andrei Astrouski
 */
public class UndoableInsertRowsAction extends UndoableInsertAction {

    private int nRows;
    private int beforeRow;
    private int col;
  
    public UndoableInsertRowsAction(int nRows, int beforeRow, int col) {
        this.nRows = nRows;
        this.beforeRow = beforeRow;
        this.col = col;
    }

    public static boolean canInsertRows(IGridTable table, int nRows) {
        IGridRegion region = getOriginalRegion(table);
        GridRegion newRegion = new GridRegion(region.getBottom() + 1, region.getLeft() - 1,
                region.getBottom() + 1 + nRows, region.getRight() + 1);
        IGridTable[] allGridTables = table.getGrid().getTables();
        for (int i = 0; i < allGridTables.length; i++) {
            if (!table.getUri().equals(allGridTables[i].getUri())
                    && IGridRegion.Tool.intersects(newRegion, allGridTables[i].getRegion())) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean canPerformAction(IGridTable table) {        
        return UndoableInsertRowsAction.canInsertRows(table, getNumberToInsert(table));
    }

    @Override
    protected GridRegionAction getGridRegionAction(IGridRegion gridRegion, int numberToInsert) {        
        return new GridRegionAction(gridRegion, ROWS, INSERT, ActionType.EXPAND, numberToInsert);
    }

    @Override
    protected int getNumberToInsert(IGridTable table) {
        int cellHeight = getOriginalTable(table).getCell(col, beforeRow).getHeight();
        int rowsToInsert = nRows;
        if (cellHeight > 1) { // merged cell
            rowsToInsert += cellHeight - 1;
        }
        return rowsToInsert;
    }

    @Override
    protected IUndoableGridTableAction performAction(int numberToInsert, IGridRegion fullTableRegion, IGridTable table) {        
        return IWritableGrid.Tool.insertRows(numberToInsert, beforeRow, fullTableRegion, table.getGrid());
    }
}
