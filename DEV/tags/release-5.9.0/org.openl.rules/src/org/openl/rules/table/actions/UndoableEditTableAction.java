package org.openl.rules.table.actions;

import java.util.ArrayList;
import java.util.List;

import org.openl.rules.table.AGridTableDecorator;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.actions.GridRegionAction.ActionType;

/**
 * @author Andrei Astrouski
 */
public abstract class UndoableEditTableAction implements IUndoableGridTableAction {
    
    public static final boolean COLUMNS = true;
    public static final boolean ROWS = false;
    public static final boolean INSERT = true;
    public static final boolean REMOVE = false;

    public UndoableEditTableAction() {
    }

    /**
     * Extracts original table.
     * 
     * @param table Table which we have.
     * @return Original table that includes our table.
     */
    public static IGridTable getOriginalTable(IGridTable table) {
        while (isDecoratorTable(table)) {
            table = ((AGridTableDecorator) table).getOriginalGridTable();
        }
        return table;
    }

    public static boolean isDecoratorTable(IGridTable table) {
        return table instanceof AGridTableDecorator;
    }

    public static IGridRegion getOriginalRegion(IGridTable table) {
        return getOriginalTable(table).getRegion();
    }

    /**
     * Creates actions that moves the table and executes these actions.
     * 
     * @param table Table to move.
     * @return Actions that moved the table.
     * @throws Exception
     */
    public static synchronized IUndoableGridTableAction moveTable(IGridTable table) throws Exception {
        UndoableMoveTableAction moveTableAction = new UndoableMoveTableAction();
        moveTableAction.doAction(table);
        IUndoableGridTableAction changeRegions = setRegion(moveTableAction.getNewRegion(), table);
        changeRegions.doAction(table);
        return new UndoableCompositeAction(moveTableAction, changeRegions);
    }

    /**
     * After coping or moving the table we need to set new region destination of it
     * 
     * @param newRegion New region of the table
     */
    public static IUndoableGridTableAction setRegion(IGridRegion newRegion, IGridTable table) {
        IGridRegion fullTableRegion = getOriginalRegion(table);
        List<IUndoableGridTableAction> actions = new ArrayList<IUndoableGridTableAction>();
        int topOffset = newRegion.getTop() - fullTableRegion.getTop();
        int leftOffset = newRegion.getLeft() - fullTableRegion.getLeft();
        if (topOffset != 0) {
            actions.add(new GridRegionAction(fullTableRegion, ROWS, INSERT, ActionType.MOVE, topOffset));
            if (isDecoratorTable(table)) {
                actions.add(new GridRegionAction(table.getRegion(), ROWS, INSERT, ActionType.MOVE, topOffset));
            }
        }
        if (leftOffset != 0) {
            actions.add(new GridRegionAction(fullTableRegion, COLUMNS, INSERT, ActionType.MOVE, leftOffset));
            if (isDecoratorTable(table)) {
                actions.add(new GridRegionAction(table.getRegion(), COLUMNS, INSERT, ActionType.MOVE, leftOffset));
            }
        }
        return new UndoableCompositeAction(actions);
    }

}
