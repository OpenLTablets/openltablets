/**
 * Created Feb 15, 2007
 */
package org.openl.rules.table;

import java.util.ArrayList;
import java.util.List;

import org.openl.rules.lang.xls.types.CellMetaInfo;
import org.openl.rules.lang.xls.XlsWorkbookSourceCodeModule;
import org.openl.rules.table.actions.GridRegionAction;
import org.openl.rules.table.actions.IUndoableGridAction;
import org.openl.rules.table.actions.MergeCellsAction;
import org.openl.rules.table.actions.UndoableClearAction;
import org.openl.rules.table.actions.UndoableCompositeAction;
import org.openl.rules.table.actions.UndoableCopyValueAction;
import org.openl.rules.table.actions.UndoableResizeMergedRegionAction;
import org.openl.rules.table.actions.UndoableSetStyleAction;
import org.openl.rules.table.actions.UndoableSetValueAction;
import org.openl.rules.table.actions.UnmergeByColumnsAction;
import org.openl.rules.table.actions.GridRegionAction.ActionType;
import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;
import org.openl.rules.table.ui.ICellStyle;
import org.openl.rules.table.xls.XlsSheetGridExporter;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.rules.table.xls.formatters.AXlsFormatter;
import org.openl.rules.table.xls.formatters.XlsFormattersManager;

import static org.openl.rules.table.xls.XlsSheetGridExporter.SHEET_NAME;
import org.openl.util.export.IExporter;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author snshor
 * 
 */
public interface IWritableGrid extends IGrid {
    static public class Tool {

        private static final String PROPERTIES_SECTION_NAME = "properties";
        static final boolean COLUMNS = true, ROWS = false, INSERT = true, REMOVE = false;        
        
        @Deprecated
        public static IExporter createExporter(IWritableGrid wGrid) {
            if (wGrid instanceof XlsSheetGridModel) {
                return new XlsSheetGridExporter((XlsSheetGridModel) wGrid);
            }

            return null;
        }

        public static IExporter createExporter(XlsWorkbookSourceCodeModule workbookModule) {
            Workbook workbook = workbookModule.getWorkbook();
            Sheet sheet;
            synchronized (workbook) {
                sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    sheet = workbook.createSheet(SHEET_NAME);
                }
            }

            return new XlsSheetGridExporter(workbook, new XlsSheetGridModel(sheet));
        }

        public static CellMetaInfo getCellMetaInfo(IGrid grid, int col, int row) {
            IWritableGrid wgrid = getWritableGrid(grid);
            if (wgrid == null) {
                return null;
            }

            return wgrid.getCellMetaInfo(col, row);
        }

        public static CellMetaInfo getCellMetaInfo(IGridTable table, int col, int row) {
            IWritableGrid wgrid = getWritableGrid(table);
            if (wgrid == null) {
                return null;
            }
            int gcol = table.getGridColumn(col, row);
            int grow = table.getGridRow(col, row);

            return wgrid.getCellMetaInfo(gcol, grow);
        }

        public static ICellStyle getCellStyle(IGrid grid, int col, int row) {
            IWritableGrid wgrid = getWritableGrid(grid);
            if (wgrid == null) {
                return null;
            }

            return wgrid.getCell(col, row).getStyle();
        }

        public static IWritableGrid getWritableGrid(IGrid grid) {
            if (grid instanceof IWritableGrid) {
                return (IWritableGrid) grid;
            }
            return null;
        }

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
         * @param wgrid Current writable grid.
         * @param firstRowOrColumn Index of row or column for
         *            insertion/removing.
         * @param numberOfRowsOrColumns Number of elements to insert/remove.
         * @param isInsert Flag that defines what we have to do(insert/remove).
         * @param isColumns Flag that defines direction of insertion/removing.
         * @param regionOfTable Region of current table.
         * @return All actions to resize merged regions.
         */
        public static List<IUndoableGridAction> resizeMergedRegions(IWritableGrid wgrid, int firstRowOrColumn,
                int numberOfRowsOrColumns, boolean isInsert, boolean isColumns, IGridRegion regionOfTable) {
            ArrayList<IUndoableGridAction> resizeActions = new ArrayList<IUndoableGridAction>();
            for (int i = 0; i < wgrid.getNumberOfMergedRegions(); i++) {
                IGridRegion existingMergedRegion = wgrid.getMergedRegion(i);
                // merged region is contained by region of grid
                if (IGridRegion.Tool.contains(regionOfTable, existingMergedRegion.getLeft(), existingMergedRegion
                        .getTop())) {
                    if (isRegionMustBeResized(existingMergedRegion, firstRowOrColumn, numberOfRowsOrColumns, isColumns,
                            regionOfTable)) {
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

        public static IUndoableGridAction insertColumns(int nColumns, int beforeColumns, IGridRegion region,
                IWritableGrid wgrid) {
            int h = IGridRegion.Tool.height(region);
            int w = IGridRegion.Tool.width(region);
            int columnsToMove = w - beforeColumns;

            ArrayList<IUndoableGridAction> actions = new ArrayList<IUndoableGridAction>(h * columnsToMove);

            int firstToMove = region.getLeft() + beforeColumns;
            actions.addAll(shiftColumns(firstToMove, nColumns, INSERT, region));
            actions.addAll(resizeMergedRegions(wgrid, beforeColumns, nColumns, INSERT, COLUMNS, region));

            return new UndoableCompositeAction(actions);
        }

        public static IUndoableGridAction insertRows(int nRows, int beforeRow, IGridRegion region, IWritableGrid wgrid) {
            int h = IGridRegion.Tool.height(region);
            int w = IGridRegion.Tool.width(region);
            int rowsToMove = h - beforeRow;

            ArrayList<IUndoableGridAction> actions = new ArrayList<IUndoableGridAction>(w * rowsToMove);

            int firstToMove = region.getTop() + beforeRow;
            actions.addAll(shiftRows(firstToMove, nRows, INSERT, region));
            actions.addAll(resizeMergedRegions(wgrid, beforeRow, nRows, INSERT, ROWS, region));

            return new UndoableCompositeAction(actions);
        }

        /**
         * Checks if the table specified by its region contains property.
         */
        public static CellKey getPropertyCoordinates(IGridRegion region, IWritableGrid wgrid, String propName) {
            int left = region.getLeft();
            int top = region.getTop();

            String propsHeader = wgrid.getCell(left, top + 1).getStringValue();
            if (propsHeader == null || !propsHeader.equals(PROPERTIES_SECTION_NAME)) {
                // there is no properties
                return null;
            }
            int propsCount = wgrid.getCell(left, top + 1).getHeight();

            for (int i = 0; i < propsCount; i++) {

                String pName = wgrid.getCell(left + 1, top + 1 + i).getStringValue();

                if (pName != null && pName.equals(propName)) {
                    return new CellKey(1, 1 + i);
                }
            }

            return null;
        }

        /**
         * TODO To refactor
         * 
         * @return null if set new property with empty or same value
         * */

        public static IUndoableGridAction insertProp(IGridRegion tableRegion, IGridRegion diplayedTableRegion,
                IWritableGrid wgrid, String newPropName, String newPropValue) {
            AXlsFormatter format = getFormat(newPropName);
            int regionHeight = IGridRegion.Tool.height(tableRegion);
            int regionWidth = IGridRegion.Tool.width(tableRegion);
            int nRows = 1;
            int beforeRow = 1;

            int leftCell = tableRegion.getLeft();
            int topCell = tableRegion.getTop();
            
            String propsHeader = wgrid.getCell(leftCell, topCell + 1).getStringValue();
            boolean containsPropSection = tableContainsPropertySection(propsHeader);
            int propsCount = 0;
            if (containsPropSection) {
                propsCount = wgrid.getCell(leftCell, topCell + 1).getHeight();
                for (int i = 0; i < propsCount; i++) {

                    String propNameFromTable = wgrid.getCell(leftCell + 1, topCell + 1 + i).getStringValue();
                    // if such name already exists in the table, we need to change its value.
                    if (propNameFromTable != null && propNameFromTable.equals(newPropName)) {
                        String propValueFromTable = wgrid.getCell(leftCell + 2, topCell + 1 + i).getStringValue();
                        if (propValueFromTable!= null && newPropValue!= null 
                                && propValueFromTable.trim().equals(newPropValue.trim())) {
                            // property with such name and value already exists.
                            return null;
                        }
                        return new UndoableSetValueAction(leftCell + 2, topCell + 1 + i, newPropValue, format);
                    }
                }
            }

            if (StringUtils.isBlank(newPropValue)) {
                return null;
            }

            int rowsToMove = regionHeight - beforeRow;

            ArrayList<IUndoableGridAction> actions = new ArrayList<IUndoableGridAction>(regionWidth * rowsToMove);

            int firstToMove = tableRegion.getTop() + beforeRow;
            actions.addAll(shiftRows(firstToMove, nRows, INSERT, tableRegion));

            if (!containsPropSection) {
                actions.add(new UnmergeByColumnsAction(
                        new GridRegion(topCell + beforeRow, leftCell, topCell + beforeRow, tableRegion.getRight())));
                actions.add(new UndoableSetValueAction(leftCell, topCell + beforeRow, PROPERTIES_SECTION_NAME, null));
                if (regionWidth > 3) {
                    // clear cells
                    for (int j = leftCell + 3; j < leftCell + regionWidth; j++) {
                        actions.add(new UndoableClearAction(j, topCell + beforeRow));
                    }
                } else if (regionWidth < 3) {
                    // expand table by including neighboring cell in merged
                    // regions, width will equal 3
                    actions.add(new MergeCellsAction(new GridRegion(topCell, leftCell, topCell, leftCell + 2)));
                    for (int row = topCell + 2; row < tableRegion.getBottom() + nRows; row++) {
                        actions.add(new MergeCellsAction(new GridRegion(row, leftCell + regionWidth - 1, row,
                                leftCell + 2)));
                    }
                    actions.add(new GridRegionAction(
                            tableRegion, COLUMNS, INSERT, ActionType.EXPAND, 3 - regionWidth));
                    actions.add(new GridRegionAction(
                            diplayedTableRegion, COLUMNS, INSERT, ActionType.EXPAND, 3 - regionWidth));
                }

            } else {
                actions.add(new UndoableSetValueAction(leftCell, topCell + beforeRow + 1, StringUtils.EMPTY, null));
            }

            actions.add(new UndoableSetValueAction(leftCell + 1, topCell + beforeRow, newPropName, null));
            actions.add(new UndoableSetValueAction(leftCell + 2, topCell + beforeRow, newPropValue, format));

            if (propsCount == 1) {
                // resize 'properties' cell
                actions.add(new UndoableResizeMergedRegionAction(new GridRegion(topCell + 1, leftCell,
                        topCell + 1, leftCell), nRows, INSERT, ROWS));
            } else {
                actions.addAll(resizeMergedRegions(wgrid, beforeRow, nRows, INSERT, ROWS, tableRegion));
            }

            return new UndoableCompositeAction(actions);
        }

        private static boolean tableContainsPropertySection(String propsHeader) {
            boolean containsPropSection = false;
            if (propsHeader != null && propsHeader.equals(PROPERTIES_SECTION_NAME)) {
                containsPropSection = true;
            }
            return containsPropSection;
        }

        private static AXlsFormatter getFormat(String propertyName) {

            AXlsFormatter result = null;
            TablePropertyDefinition tablePropeprtyDefinition = TablePropertyDefinitionUtils
                    .getPropertyByName(propertyName);

            if (tablePropeprtyDefinition != null) {

                Class<?> type = tablePropeprtyDefinition.getType().getInstanceClass();
                result = XlsFormattersManager.getFormatter(type, tablePropeprtyDefinition.getFormat());
            }

            return result;
        }

        public static void putCellMetaInfo(IGridTable table, int col, int row, CellMetaInfo meta) {
            IWritableGrid wgrid = getWritableGrid(table);
            if (wgrid == null) {
                return;
            }
            int gcol = table.getGridColumn(col, row);
            int grow = table.getGridRow(col, row);

            wgrid.setCellMetaInfo(gcol, grow, meta);
        }

        private static List<IUndoableGridAction> clearCells(int startColumn, int nCols, int startRow, int nRows) {
            ArrayList<IUndoableGridAction> clearActions = new ArrayList<IUndoableGridAction>();
            for (int i = startColumn; i < startColumn + nCols; i++) {
                for (int j = startRow; j < startRow + nRows; j++) {
                    clearActions.add(new UndoableClearAction(i, j));
                }
            }
            return clearActions;
        }

        private static List<IUndoableGridAction> shiftColumns(int startColumn, int nCols, boolean isInsert,
                IGridRegion region) {
            ArrayList<IUndoableGridAction> shiftActions = new ArrayList<IUndoableGridAction>();
            int direction, colFromCopy, colToCopy;
            if (isInsert) {// shift columns left
                direction = -1;
                colFromCopy = region.getRight();
            } else {// shift columns right
                direction = 1;
                colFromCopy = startColumn;
            }
            for (int i = 0; i <= region.getRight() - startColumn; i++) {
                colToCopy = colFromCopy - direction * nCols;
                for (int row = region.getTop(); row <= region.getBottom(); row++) {
                    shiftActions.add(new UndoableCopyValueAction(colFromCopy, row, colToCopy, row));
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
        private static List<IUndoableGridAction> shiftRows(int startRow, int nRows, boolean isInsert, IGridRegion region) {
            ArrayList<IUndoableGridAction> shiftActions = new ArrayList<IUndoableGridAction>();
            int direction, rowFromCopy, rowToCopy;
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
                rowToCopy = rowFromCopy - direction * nRows; // compute to which row we need to shift.
                // each column we shift from one row to another
                for (int column = region.getLeft(); column <= region.getRight(); column++) {
                    shiftActions.add(new UndoableCopyValueAction(column, rowFromCopy, column, rowToCopy));
                }
                rowFromCopy += direction;
            }
            return shiftActions;
        }

        public static IUndoableGridAction removeColumns(int nCols, int startColumn, IGridRegion region,
                IWritableGrid wgrid) {
            int firstToMove = region.getLeft() + startColumn + nCols;
            int w = IGridRegion.Tool.width(region);
            int h = IGridRegion.Tool.height(region);

            ArrayList<IUndoableGridAction> actions = new ArrayList<IUndoableGridAction>(h * (w - startColumn));

            actions.addAll(shiftColumns(firstToMove, nCols, REMOVE, region));
            actions.addAll(clearCells(region.getRight() + 1 - nCols, nCols, region.getTop(), h));
            actions.addAll(resizeMergedRegions(wgrid, startColumn, nCols, REMOVE, COLUMNS, region));

            return new UndoableCompositeAction(actions);
        }

        public static IUndoableGridAction removeRows(int nRows, int startRow, IGridRegion region, IWritableGrid wgrid) {
            int w = IGridRegion.Tool.width(region);
            int h = IGridRegion.Tool.height(region);
            int firstToMove = region.getTop() + startRow + nRows;

            ArrayList<IUndoableGridAction> actions = new ArrayList<IUndoableGridAction>(w * (h - startRow));

            actions.addAll(shiftRows(firstToMove, nRows, REMOVE, region));
            actions.addAll(clearCells(region.getLeft(), w, region.getBottom() + 1 - nRows, nRows));
            actions.addAll(resizeMergedRegions(wgrid, startRow, nRows, REMOVE, ROWS, region));

            return new UndoableCompositeAction(actions);
        }

        public static IUndoableGridAction setStringValue(int col, int row, IGridRegion region, String value,
                AXlsFormatter format) {

            int gcol = region.getLeft() + col;
            int grow = region.getTop() + row;

            // wgrid.setCellStringValue(gcol, grow, value);
            return new UndoableSetValueAction(gcol, grow, value, format);
        }

        public static IUndoableGridAction setStringValue(int col, int row, IGridTable table, String value,
                AXlsFormatter format) {
            // IWritableGrid wgrid = getWritableGrid(table);
            int gcol = table.getGridColumn(col, row);
            int grow = table.getGridRow(col, row);

            // wgrid.setCellStringValue(gcol, grow, value);
            return new UndoableSetValueAction(gcol, grow, value, format);

        }

        public static IUndoableGridAction setStyle(int col, int row, IGridRegion region, ICellStyle style) {
            int gcol = region.getLeft() + col;
            int grow = region.getTop() + row;
            return new UndoableSetStyleAction(gcol, grow, style);
        }

        public static IUndoableGridAction setStyle(int col, int row, IGridTable table, ICellStyle style) {
            int gcol = table.getGridColumn(col, row);
            int grow = table.getGridRow(col, row);
            return new UndoableSetStyleAction(gcol, grow, style);
        }
    }

    int addMergedRegion(IGridRegion reg);

    void clearCell(int col, int row);

    void copyCell(int colFrom, int rowFrom, int colTo, int RowTo);

    /**
     * Finds a rectangular area of given width and height on the grid that can
     * be used for writing. The returned region should not intersect with or
     * touch existing not empty cells.
     * 
     * @param width rectangle width
     * @param height rectangle height
     * @return region representing required rectangle or <code>null</code> if
     *         not found
     */
    IGridRegion findEmptyRect(int width, int height);

    CellMetaInfo getCellMetaInfo(int col, int row);

    void removeMergedRegion(IGridRegion to);

    void setCellMetaInfo(int col, int row, CellMetaInfo meta);

    void setCellStyle(int col, int row, ICellStyle style);

    void setCellValue(int col, int row, Object value);

    boolean isTopLeftCellInMergedRegion(int column, int row);
}
