package org.openl.rules.table.actions;

import java.util.ArrayList;
import java.util.List;

import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.IGridTable;

/**
 * Default behaviour for insert operations.
 * 
 * @author DLiauchuk
 *
 */
public abstract class UndoableInsertAction extends UndoableEditTableAction {
    
    private IUndoableGridTableAction action;

    public void doAction(IGridTable table) {
        IUndoableGridTableAction moveTableAction = null;
        if (!canPerformAction(table)) {
            try {
                moveTableAction = moveTable(table);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int numberToInsert = getNumberToInsert(table);
        IGridRegion fullTableRegion = getOriginalRegion(table);
        List<IUndoableGridTableAction> actions = new ArrayList<IUndoableGridTableAction>();
        IUndoableGridTableAction ua = performAction(numberToInsert, fullTableRegion, table);
        actions.add(ua);
        
        GridRegionAction allTable = getGridRegionAction(fullTableRegion, numberToInsert);
        actions.add(allTable);
        
        if (isDecoratorTable(table)) {
            GridRegionAction displayTable = getGridRegionAction(table.getRegion(), numberToInsert);
            actions.add(displayTable);
        }
        action = new UndoableCompositeAction(actions);
        action.doAction(table);
        if (moveTableAction != null) {
            action = new UndoableCompositeAction(moveTableAction, action);
        }
    }

    public void undoAction(IGridTable table) {
        action.undoAction(table);
    }
    
    /**
     * Checks if action can be performed without moving the table.
     * 
     * @param table
     * @return true if action can be performed without moving the table.
     */
    protected abstract boolean canPerformAction(IGridTable table);
    
    /**
     * Get actual number of rows or columns to be inserted.
     * It depends whether the cell is merged or not.
     * 
     * @param table
     * @return actual number to be inserted.
     */
    protected abstract int getNumberToInsert(IGridTable table);
    
    /**
     * Perform action for inserting rows or columns.
     * 
     * @param numberToInsert
     * @param fullTableRegion
     * @param table
     * @return action for inserting rows or columns.
     */
    protected abstract IUndoableGridTableAction performAction(int numberToInsert, IGridRegion fullTableRegion, IGridTable table);
    
    protected abstract GridRegionAction getGridRegionAction(IGridRegion gridRegion, int numberToInsert);
    

}
