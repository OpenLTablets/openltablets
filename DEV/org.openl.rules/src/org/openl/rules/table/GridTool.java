package org.openl.rules.table;

import org.openl.rules.table.actions.*;
import org.openl.rules.table.ui.CellStyle;
import org.openl.rules.table.ui.ICellStyle;
import org.openl.util.formatters.IFormatter;

import java.util.ArrayList;
import java.util.List;

/**
* Created by ymolchan on 8/13/2014.
*/
public class GridTool {

    private static final String PROPERTIES_SECTION_NAME = "properties";
    static final boolean COLUMNS = true, ROWS = false, INSERT = true, REMOVE = false;

    public static IWritableGrid getWritableGrid(IGridTable table) {
        IGrid grid = table.getGrid();
        if (grid instanceof IWritableGrid) {
            return (IWritableGrid) grid;
        }
        return null;
    }

    /**
     * Searches all merged regions inside the specified region of table for
     * regions that have to be resized.
     *
     * @param grid Current writable grid.
     * @param firstRowOrColumn Index of row or column for
     *            insertion/removing.
     * @param numberOfRowsOrColumns Number of elements to insert/remove.
     * @param isInsert Flag that defines what we have to do(insert/remove).
     * @param isColumns Flag that defines direction of insertion/removing.
     * @param regionOfTable Region of current table.
     * @return All actions to resize merged regions.
     */
    public static List<IUndoableGridTableAction> resizeMergedRegions(IGrid grid, int firstRowOrColumn,
            int numberOfRowsOrColumns, boolean isInsert, boolean isColumns, IGridRegion regionOfTable) {
        ArrayList<IUndoableGridTableAction> resizeActions = new ArrayList<IUndoableGridTableAction>();
        for (int i = 0; i < grid.getNumberOfMergedRegions(); i++) {
            IGridRegion existingMergedRegion = grid.getMergedRegion(i);
            // merged region is contained by region of grid
            if (IGridRegion.Tool.contains(regionOfTable, existingMergedRegion.getLeft(), existingMergedRegion
                    .getTop())) {
                if (isRegionMustBeResized(existingMergedRegion, firstRowOrColumn, numberOfRowsOrColumns, isColumns,
                        regionOfTable)) {
                    ICellStyle oldCellStyle = grid.getCell(existingMergedRegion.getLeft(),existingMergedRegion.getBottom()).getStyle();

                    if (!isColumns && isInsert) {
                        for (int j = 1; j <= numberOfRowsOrColumns; j++) {
                            grid.getCell(existingMergedRegion.getLeft(),existingMergedRegion.getBottom() + 1).getStyle();
                            resizeActions.add(new SetBorderStyleAction(existingMergedRegion.getLeft(), existingMergedRegion.getBottom() + j,
                                    oldCellStyle));
                        }
                    }

                    resizeActions.add(new UndoableResizeMergedRegionAction(existingMergedRegion,
                            numberOfRowsOrColumns, isInsert, isColumns));
                }
            }
        }
        return resizeActions;
    }

    /**
     * Checks if the specified region must be resized.
     *
     * If we delete all we remove all rows/columns in region then region
     * must be deleted(not resized).
     */
    private static boolean isRegionMustBeResized(IGridRegion region, int firstRowOrColumn,
            int numberOfRowsOrColumns, boolean isColumns, IGridRegion regionOfTable) {
        if (isColumns) {
            // merged region contains column which we copy/remove
            if (IGridRegion.Tool.width(region) > numberOfRowsOrColumns
                    && IGridRegion.Tool.contains(region, regionOfTable.getLeft() + firstRowOrColumn, region
                            .getTop())) {
                return true;
            } else {
                return false;
            }
        } else {
            // merged region contains row which we copy/remove
            if (IGridRegion.Tool.height(region) > numberOfRowsOrColumns
                    && IGridRegion.Tool.contains(region, region.getLeft(), regionOfTable.getTop()
                            + firstRowOrColumn)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static IUndoableGridTableAction insertColumns(int nCols, int beforeColumns, IGridRegion region,
            IGrid grid) {
        int h = IGridRegion.Tool.height(region);
        int w = IGridRegion.Tool.width(region);
        int columnsToMove = w - beforeColumns;

        ArrayList<IUndoableGridTableAction> actions = new ArrayList<IUndoableGridTableAction>(h * columnsToMove);

        int firstToMove = region.getLeft() + beforeColumns;
        int colTo = firstToMove + nCols;
        int top = region.getTop();
        // shift cells by column, copy cells of inserted column and resize merged regions after
        actions.addAll(shiftColumns(colTo, nCols, INSERT, region, grid));
        actions.addAll(copyCells(firstToMove, top, colTo, top, nCols, h, grid));
        actions.addAll(resizeMergedRegions(grid, beforeColumns, nCols, INSERT, COLUMNS, region));
        actions.addAll(emptyCells(firstToMove, top, nCols, h, grid));

        return new UndoableCompositeAction(actions);
    }

    public static IUndoableGridTableAction insertRows(int nRows, int beforeRow,
            IGridRegion region, IGrid grid) {
        int h = IGridRegion.Tool.height(region);
        int w = IGridRegion.Tool.width(region);
        int rowsToMove = h - beforeRow;

        ArrayList<IUndoableGridTableAction> actions = new ArrayList<IUndoableGridTableAction>(w * rowsToMove);

        int firstToMove = region.getTop() + beforeRow;
        int rowTo = firstToMove + nRows;
        int left = region.getLeft();
        // Shift cells by row, copy cells of inserted row and resize merged regions after
        actions.addAll(shiftRows(rowTo, nRows, INSERT, region, grid));
        actions.addAll(copyCells(left, firstToMove, left, rowTo, w, nRows, grid));
        actions.addAll(resizeMergedRegions(grid, beforeRow, nRows, INSERT, ROWS, region));
        actions.addAll(emptyCells(left, rowTo, w, nRows, grid));

        return new UndoableCompositeAction(actions);
    }

    private static List<IUndoableGridTableAction> copyCells(int colFrom, int rowFrom, int colTo, int rowTo, int nCols, int nRows, IGrid grid) {
        List<IUndoableGridTableAction> actions = new ArrayList<IUndoableGridTableAction>();
        for (int i = nCols - 1; i >= 0; i--) {
            for (int j = nRows - 1; j >= 0; j--) {
                int cFrom = colFrom + i;
                int rFrom = rowFrom + j;
                int cTo = colTo + i;
                int rTo = rowTo + j;
                if (!grid.isInOneMergedRegion(cFrom, rFrom, cTo, rTo)) {
                    actions.add(new UndoableCopyValueAction(cFrom, rFrom, cTo, rTo));
                }
            }
        }
        return actions;
    }

    private static List<IUndoableGridTableAction> emptyCells(int colFrom, int rowFrom, int nCols, int nRows, IGrid grid) {
        List<IUndoableGridTableAction> actions = new ArrayList<IUndoableGridTableAction>();
        for (int i = nCols - 1; i >= 0; i--) {
            for (int j = nRows - 1; j >= 0; j--) {
                int cFrom = colFrom + i;
                int rFrom = rowFrom + j;
                if (grid.isTopLeftCellInMergedRegion(cFrom, rFrom)) {
                    ICell cell = grid.getCell(cFrom, rFrom);
                    if (cell.getHeight() > nRows || cell.getWidth() > nCols) {
                        // Don't clear merged cells which are bigger than the cleaned region.
                        continue;
                    }
                } else if (grid.isPartOfTheMergedRegion(cFrom, rFrom)) {
                    // Don't clear middle of the merged cells.
                    continue;
                }
                actions.add(new UndoableSetValueAction(cFrom, rFrom, null));
            }
        }
        return actions;
    }

    /**
     * Checks if the table specified by its region contains property.
     */
    public static CellKey getPropertyCoordinates(IGridRegion region, IGrid grid, String propName) {
        int left = region.getLeft();
        int top = region.getTop();

        ICell propsHeaderCell = grid.getCell(left, top + 1);
        String propsHeader = propsHeaderCell.getStringValue();
        if (propsHeader == null || !propsHeader.equals(PROPERTIES_SECTION_NAME)) {
            // There is no properties
            return null;
        }
        int propsCount = propsHeaderCell.getHeight();

        for (int i = 0; i < propsCount; i++) {
            ICell propNameCell = grid.getCell(left + propsHeaderCell.getWidth(), top + 1 + i);
            String pName = propNameCell.getStringValue();

            if (pName != null && pName.equals(propName)) {
                return new CellKey(1, 1 + i);
            }
        }

        return null;
    }

    /**
     * @return null if set new property with empty or same value
     */
    public static IUndoableGridTableAction insertProp(IGridRegion tableRegion, IGrid grid,
            String newPropName, Object newPropValue) {
        if (newPropValue == null) {
            return null;
        }

        int propertyRowIndex = getPropertyRowIndex(tableRegion, grid, newPropName);
        if (propertyRowIndex > 0) {
            return setExistingPropertyValue(tableRegion, grid, newPropValue, propertyRowIndex);
        } else {
            return insertNewProperty(tableRegion, grid, newPropName, newPropValue);
        }
    }

    private static int getPropertyRowIndex(IGridRegion tableRegion, IGrid grid, String newPropName) {
        int leftCell = tableRegion.getLeft();
        int topCell = tableRegion.getTop();
        int firstPropertyRow = IGridRegion.Tool.height(grid.getCell(leftCell, topCell).getAbsoluteRegion());
        String propsHeader = grid.getCell(leftCell, topCell + firstPropertyRow).getStringValue();
        if (!tableContainsPropertySection(propsHeader)) {
            return -1;
        }
        int propsCount = grid.getCell(leftCell, topCell + 1).getHeight();
        int propNameCellOffset = grid.getCell(leftCell, topCell + 1).getWidth();
        for (int i = 0; i < propsCount; i++) {
            String propNameFromTable = grid.getCell(leftCell + propNameCellOffset, topCell + 1 + i)
                    .getStringValue();
            if (propNameFromTable != null && propNameFromTable.equals(newPropName)) {
                return topCell + 1 + i;
            }
        }
        return -1;
    }

    private static IUndoableGridTableAction setExistingPropertyValue(IGridRegion tableRegion, IGrid grid, Object newPropValue, int propertyRowIndex) {
        int leftCell = tableRegion.getLeft();
        int topCell = tableRegion.getTop();
        int propNameCellOffset = grid.getCell(leftCell, topCell + 1).getWidth();
        int propValueCellOffset = propNameCellOffset
                + grid.getCell(leftCell + propNameCellOffset, topCell + 1).getWidth();

        Object propValueFromTable = grid.getCell(leftCell + propValueCellOffset, propertyRowIndex)
                .getObjectValue();
        if (propValueFromTable != null && newPropValue != null
                && propValueFromTable.equals(newPropValue)) {
            // Property with such name and value already exists
            return null;
        }
        return new UndoableSetValueAction(leftCell + propValueCellOffset, propertyRowIndex, newPropValue);
    }

    private static IUndoableGridTableAction insertNewProperty(IGridRegion tableRegion,
            IGrid grid, String newPropName, Object newPropValue) {
        int leftCell = tableRegion.getLeft();
        int topCell = tableRegion.getTop();
        int firstPropertyRow = IGridRegion.Tool.height(grid.getCell(leftCell, topCell).getAbsoluteRegion());

        int rowsToMove = IGridRegion.Tool.height(tableRegion) - firstPropertyRow;
        ArrayList<IUndoableGridTableAction> actions = new ArrayList<IUndoableGridTableAction>(IGridRegion.Tool
                .width(tableRegion)* rowsToMove);

        String propsHeader = grid.getCell(leftCell, topCell + firstPropertyRow).getStringValue();
        int propNameCellOffset;
        int propValueCellOffset;

        if (!tableContainsPropertySection(propsHeader)) {
            actions.addAll(shiftRows(tableRegion.getTop() + firstPropertyRow, 1, INSERT, tableRegion, grid));
            actions.add(createPropertiesSection(tableRegion, grid));
            propNameCellOffset = 1;
            propValueCellOffset = 2;
        } else {
            actions.add(insertRows(1, firstPropertyRow, tableRegion, grid));
            actions.add(resizePropertiesHeader(tableRegion, grid));
            propNameCellOffset = grid.getCell(leftCell, topCell + firstPropertyRow).getWidth();
            propValueCellOffset = propNameCellOffset
                    + grid.getCell(leftCell + propNameCellOffset, topCell + firstPropertyRow).getWidth();
        }

        actions.add(new UndoableSetValueAction(leftCell + propNameCellOffset, topCell + firstPropertyRow,
                newPropName));

        actions.add(new UndoableSetValueAction(leftCell + propValueCellOffset, topCell + firstPropertyRow,
                newPropValue));
        return new UndoableCompositeAction(actions);
    }

    public static Object parseStringValue(IFormatter formatter, String value) {
        Object result = null;
        if (formatter != null) {
            result = formatter.parse(value);
        } else {
            result = value;
        }
        return result;
    }

    private static IUndoableGridTableAction createPropertiesSection(IGridRegion tableRegion, IGrid grid) {
        int regionWidth = IGridRegion.Tool.width(tableRegion);
        int leftCell = tableRegion.getLeft();
        int topCell = tableRegion.getTop();
        IGridRegion headerRegion = grid.getCell(leftCell, topCell).getAbsoluteRegion();

        ArrayList<IUndoableGridTableAction> actions = new ArrayList<IUndoableGridTableAction>();

        actions.add(new SetBorderStyleAction(leftCell, headerRegion.getBottom() + 1,
                makeNewPropStyle(grid, leftCell, headerRegion.getBottom() + 1, leftCell, regionWidth) ));
        actions.add(new UnmergeByColumnsAction(new GridRegion(headerRegion.getBottom() + 1, leftCell, headerRegion
                .getBottom() + 1, tableRegion.getRight())));
        actions.add(new UndoableSetValueAction(leftCell, headerRegion.getBottom() + 1, PROPERTIES_SECTION_NAME));

        // clear cells for properties
        for (int prpCell = leftCell + 1; prpCell < leftCell + regionWidth; prpCell++) {
            actions.add(new UndoableClearAction(prpCell, headerRegion.getBottom() + 1));
            /*actions.add(new SetBorderStyleAction(prpCell, headerRegion.getBottom() + 1,
                    makeNewPropStyle(grid, prpCell, headerRegion.getBottom() + 1, prpCell, regionWidth, null) ));*/
        }

        if (regionWidth >= 3) {
            // set cell style
            //leftCell + 2 - this is index of last property column
            for (int j = leftCell + 2; j < leftCell + regionWidth; j++) {
                actions.add(new SetBorderStyleAction(j, headerRegion.getBottom() + 1, makeNewPropStyle(grid, j, headerRegion.getBottom() + 1, leftCell, regionWidth)));
            }
        } else if (regionWidth < 3) {
            // expand table by including neighboring cell in merged
            // regions, width will equal 3
            int propSize = 3;

            actions.add(new MergeCellsAction(new GridRegion(topCell, leftCell, headerRegion.getBottom(),
                    leftCell + 2)));

            //add style for expanded header's and properties's  cells
            for(int row = topCell; row < tableRegion.getBottom(); row++) {
                for(int j = leftCell + regionWidth; j < leftCell + 3; j++) {
                    actions.add(new SetBorderStyleAction(j, row, grid.getCell(leftCell + regionWidth - 1, row).getStyle()));
                }
            }

          //add style for expanded others cells
            for(int row = topCell + 1; row < tableRegion.getBottom(); row++) {
                for(int j = leftCell + regionWidth; j < leftCell + 3; j++) {
                    actions.add(new SetBorderStyleAction(j, row + 1, grid.getCell(leftCell + regionWidth - 1, row).getStyle()));
                }
            }

            // merge right cells in each row
            IGridRegion cellToExpandRegion;
            for (int row = headerRegion.getBottom() + 1; row < tableRegion.getBottom(); row = cellToExpandRegion
                    .getBottom() + 1) {
                cellToExpandRegion = grid.getCell(leftCell + regionWidth - 1, row).getAbsoluteRegion();

                actions.add(new MergeCellsAction(new GridRegion(row + 1, cellToExpandRegion.getLeft(),
                        cellToExpandRegion.getBottom() + 1, leftCell + 2)));

                actions.add(new SetBorderStyleAction(leftCell + 2, topCell, grid.getCell(leftCell + regionWidth - 1, topCell).getStyle()));
            }

            actions.add(new GridRegionAction(tableRegion, COLUMNS, INSERT, GridRegionAction.ActionType.EXPAND, propSize - regionWidth));
        }

        return new UndoableCompositeAction(actions);
    }

    private static CellStyle makeNewPropStyle(IGrid grid, int col, int row, int regionLeftCell, int regionWidth) {
        ICell cell = grid.getCell(col, row);
        CellStyle newCellStyle = new CellStyle(cell.getStyle());

        ICellStyle cellStyle =  cell.getStyle();
        short[] borderStyle = cellStyle != null ? cellStyle.getBorderStyle() : null;

        /*Create new cell style*/

        if (borderStyle != null && col == regionLeftCell) {
            // Only left border will be set
            if (borderStyle.length == 4) {
                borderStyle = new short[] { CellStyle.BORDER_NONE, CellStyle.BORDER_NONE, CellStyle.BORDER_NONE, borderStyle[3] };
            }
        } else if (borderStyle != null && (col - regionLeftCell == regionWidth - 1)) {
            // Only right border will be set
            if (borderStyle.length == 4) {
                borderStyle = new short[]{CellStyle.BORDER_NONE, borderStyle[1], CellStyle.BORDER_NONE, CellStyle.BORDER_NONE};
                /*FIXME add bottom border for expender row (only for last)
                if (actionType != null && actionType == ActionType.EXPAND) {
                    borderStyle = new short[]{CellStyle.BORDER_NONE, borderStyle[1], borderStyle[2], CellStyle.BORDER_NONE};
                } else {
                    borderStyle = new short[]{CellStyle.BORDER_NONE, borderStyle[1], CellStyle.BORDER_NONE, CellStyle.BORDER_NONE};
                }
                */
           }
        } else {
            borderStyle = new short[] { CellStyle.BORDER_NONE, CellStyle.BORDER_NONE, CellStyle.BORDER_NONE, CellStyle.BORDER_NONE };
        }

        newCellStyle.setBorderStyle(borderStyle);

        return newCellStyle;
    }

    private static IUndoableGridTableAction resizePropertiesHeader(IGridRegion tableRegion, IGrid grid) {
        int leftCell = tableRegion.getLeft();
        int topCell = tableRegion.getTop();
        int firstPropertyRow = IGridRegion.Tool.height(grid.getCell(leftCell, topCell).getAbsoluteRegion());

        int propsCount = grid.getCell(leftCell, topCell + firstPropertyRow).getHeight();
        if (propsCount == 1) {
            IGridRegion propHeaderRegion = grid.getRegionContaining(leftCell, topCell + firstPropertyRow);
            if (propHeaderRegion == null) {
                propHeaderRegion = new GridRegion(topCell + firstPropertyRow, leftCell, topCell + firstPropertyRow, leftCell);
            }
            return new UndoableResizeMergedRegionAction(propHeaderRegion, 1, INSERT, ROWS);
        } else {
            return new UndoableCompositeAction(resizeMergedRegions(grid, firstPropertyRow, 1, INSERT, ROWS, tableRegion));
        }

    }

    private static boolean tableContainsPropertySection(String propsHeader) {
        boolean containsPropSection = false;
        if (propsHeader != null && propsHeader.equals(PROPERTIES_SECTION_NAME)) {
            containsPropSection = true;
        }
        return containsPropSection;
    }

    private static List<IUndoableGridTableAction> clearCells(int startColumn, int nCols, int startRow, int nRows, IGrid grid) {
        List<IUndoableGridTableAction> clearActions = new ArrayList<IUndoableGridTableAction>();
        for (int i = startColumn; i < startColumn + nCols; i++) {
            for (int j = startRow; j < startRow + nRows; j++) {
                if (!grid.isPartOfTheMergedRegion(i, j)
                        || (grid.isTopLeftCellInMergedRegion(i, j))){
                    clearActions.add(new UndoableClearAction(i, j));
                }
            }
        }
        return clearActions;
    }

    private static AUndoableCellAction shiftCell(int colFrom, int rowFrom, int colTo, int rowTo, IGrid grid) {

        if (!grid.isPartOfTheMergedRegion(colFrom, rowFrom) || grid.isTopLeftCellInMergedRegion(colFrom, rowFrom)) {
            // non top left cell of merged region have to be skipped
            return new UndoableShiftValueAction(colFrom, rowFrom, colTo, rowTo);
        }

        return new SetBorderStyleAction(colTo, rowTo, grid.getCell(colFrom, rowFrom).getStyle(), false);
    }

    private static List<IUndoableGridTableAction> shiftColumns(int startColumn, int nCols, boolean isInsert,
            IGridRegion region, IGrid grid) {
        ArrayList<IUndoableGridTableAction> shiftActions = new ArrayList<IUndoableGridTableAction>();

        // The first step: clear cells that will be lost after shifting
        // columns(just because we need to restore this cells after UNDO)
        if (isInsert) {
            shiftActions.addAll(clearCells(region.getRight() + 1, nCols, region.getTop(),
                    IGridRegion.Tool.height(region), grid));
        } else {
            for (int column = startColumn - nCols; column < startColumn; column++) {
                for (int row = region.getTop(); row <= region.getBottom(); row++) {
                    if (!grid.isPartOfTheMergedRegion(column, row)
                            || (grid.isTopLeftCellInMergedRegion(column, row) && IGridRegion.Tool
                                    .width(grid.getRegionStartingAt(column, row)) <= nCols)) {
                        // Sense of the second check: if it was a merged
                        // cell then it can be removed or resized depending
                        // on count of columns deleted
                        shiftActions.add(new UndoableClearAction(column, row));
                    }
                }
            }
        }

        //The second step: shift cells
        int direction, colFromCopy, colToCopy;
        if (isInsert) {// shift columns left
            direction = -1;
            colFromCopy = region.getRight();
        } else {// shift columns right
            direction = 1;
            colFromCopy = startColumn;
        }
        int numColumnsToBeShifted = region.getRight() - startColumn;
        for (int i = 0; i <= numColumnsToBeShifted; i++) {
            colToCopy = colFromCopy - direction * nCols;
            // from bottom to top, it is made for copying non_top_left cells
            // of merged before the topleft cell of merged region
            for (int row = region.getBottom(); row >= region.getTop(); row--) {
                AUndoableCellAction action =  shiftCell(colFromCopy, row, colToCopy, row, grid);
                if (action != null) {
                    shiftActions.add(action);
                }
            }
            colFromCopy += direction;
        }
        return shiftActions;
    }

    /**
     *
     * @param startRow number of the row in region to start some manipulations (shifting down or up)
     * @param nRows number of rows to be moved
     * @param isInsert do we need to insert rows or to shift it up.
     * @param region region to work with.
     * @return
     */
    private static List<IUndoableGridTableAction> shiftRows(int startRow, int nRows, boolean isInsert,
            IGridRegion region, IGrid grid) {
        ArrayList<IUndoableGridTableAction> shiftActions = new ArrayList<IUndoableGridTableAction>();

        // The first step: clear cells that will be lost after shifting
        // rows(just because we need to restore this cells after UNDO)
        if (isInsert) {
            shiftActions.addAll(clearCells(region.getLeft(), IGridRegion.Tool.width(region),
                    region.getBottom() + 1, nRows, grid));
        } else {
            for (int row = startRow - nRows; row < startRow; row++) {
                for (int column = region.getLeft(); column <= region.getRight(); column++) {
                    if (!grid.isPartOfTheMergedRegion(column, row)
                            || (grid.isTopLeftCellInMergedRegion(column, row) && IGridRegion.Tool
                                    .height(grid.getRegionStartingAt(column, row)) <= nRows)) {
                        // Sense of the second check: if it was a merged
                        // cell then it can be removed or resized depending
                        // on count of rows deleted
                        shiftActions.add(new UndoableClearAction(column, row));
                    }
                }
            }
        }

        //The second step: shift cells
        int direction, rowFromCopy;
        if (isInsert) {// shift rows down
            direction = -1;
            rowFromCopy = region.getBottom(); // we gets the bottom row from the region, and are
            // going to shift it down.
        } else {// shift rows up
            direction = 1;
            rowFromCopy = startRow; // we gets the startRow and are
            // going to shift it up.
        }
        int numRowsToBeShifted = region.getBottom() - startRow;
        for (int i = 0; i <= numRowsToBeShifted; i++) {
            int rowToCopy = rowFromCopy - direction * nRows; // compute to which row we need to shift.
            // from right to left, it is made for copying non_top_left cells
            // of merged before the topleft cell of merged region
            for (int column = region.getRight(); column >= region.getLeft(); column--) {
                AUndoableCellAction action = shiftCell(column, rowFromCopy, column, rowToCopy, grid);
                if (action != null) {
                    shiftActions.add(action);
                }
            }
            rowFromCopy += direction;
        }
        return shiftActions;
    }

    public static IUndoableGridTableAction removeColumns(int nCols, int startColumn, IGridRegion region, IGrid grid) {
        int firstToMove = region.getLeft() + startColumn + nCols;
        int w = IGridRegion.Tool.width(region);
        int h = IGridRegion.Tool.height(region);

        ArrayList<IUndoableGridTableAction> actions = new ArrayList<IUndoableGridTableAction>(h * (w - startColumn));

        // resize merged regions -> shift cells by column -> clear cells
        actions.addAll(resizeMergedRegions(grid, startColumn, nCols, REMOVE, COLUMNS, region));
        actions.addAll(shiftColumns(firstToMove, nCols, REMOVE, region, grid));
        actions.addAll(clearCells(region.getRight() + 1 - nCols, nCols, region.getTop(), h, grid));

        return new UndoableCompositeAction(actions);
    }

    public static IUndoableGridTableAction removeRows(int nRows, int startRow, IGridRegion region, IGrid grid) {
        int w = IGridRegion.Tool.width(region);
        int h = IGridRegion.Tool.height(region);
        int firstToMove = region.getTop() + startRow + nRows;

        ArrayList<IUndoableGridTableAction> actions = new ArrayList<IUndoableGridTableAction>(w * (h - startRow));

        // resize merged regions -> shift cells by row -> clear cells
        actions.addAll(resizeMergedRegions(grid, startRow, nRows, REMOVE, ROWS, region));
        actions.addAll(shiftRows(firstToMove, nRows, REMOVE, region, grid));
        actions.addAll(clearCells(region.getLeft(), w, region.getBottom() + 1 - nRows, nRows, grid));

        return new UndoableCompositeAction(actions);
    }

    public static IUndoableGridTableAction setStringValue(int col, int row, IGridRegion region, String value,
            IFormatter formatter) {
        int gcol = region.getLeft() + col;
        int grow = region.getTop() + row;

        Object objectValue = parseStringValue(formatter, value);

        return new UndoableSetValueAction(gcol, grow, objectValue);
    }

    public static IUndoableGridTableAction setStringValue(int col, int row, IGridTable table, String value,
            IFormatter formatter) {
        // IWritableGrid wgrid = getWritableGrid(table);
        int gcol = table.getGridColumn(col, row);
        int grow = table.getGridRow(col, row);

        Object objectValue = parseStringValue(formatter, value);

        return new UndoableSetValueAction(gcol, grow, objectValue);
    }

}
