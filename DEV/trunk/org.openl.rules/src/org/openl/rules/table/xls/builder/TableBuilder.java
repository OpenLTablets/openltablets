package org.openl.rules.table.xls.builder;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.io.IOException;

import org.openl.rules.table.xls.XlsCellStyle;
import org.openl.rules.table.xls.XlsCellStyle2;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.GridRegion;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ui.ICellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.openl.rules.table.ICell;

/**
 * Class that allows creating tables in specified excel sheet.
 *
 * @author Aliaksandr Antonik
 * @author Andrei Astrouski
 */
public class TableBuilder {

    public static final String TABLE_PROPERTIES = "properties";
    public static final String TABLE_PROPERTIES_NAME = "name";

    /** The sheet to write tables to. */
    private final XlsSheetGridModel gridModel;
    /** Current table region in excel sheet. */
    private IGridRegion region;
    /** Table width. */
    private int width;
    /** Table height. */
    private int height;
    /** Current table row to write. */
    private int currentRow;
    /** Default cell style. */
    private CellStyle defaultCellStyle;
    
    /**
     *  Mapping for style to style transformation.
     */
    private Map<CellStyle,CellStyle> style2style;

    /**
     * Creates new instance.
     *
     * @param gridModel represents interface for operations with excel sheets
     */
    public TableBuilder(XlsSheetGridModel gridModel) {
        if (gridModel == null) {
            throw new IllegalArgumentException("gridModel is null");
        }
        this.gridModel = gridModel;
        style2style = new HashMap<CellStyle,CellStyle>();
    }

    /**
     * Begins writing a table.
     *
     * @param width table width in cells
     * @param height table height in cells
     *
     * @throws CreateTableException if unable to create table
     * @throws IllegalStateException if <code>beginTable()</code> has already
     *             been called without subsequent <code>endTable()</code>
     */
    public void beginTable(int width, int height) throws CreateTableException {
        if (region != null) {
            throw new IllegalStateException("beginTable() has already been called");
        }

        this.width = width;
        this.height = height;
        region = gridModel.findEmptyRect(width, height);
        if (region == null) {
            throw new CreateTableException("could not find appropriate region for writing");
        }

        currentRow = 0;
        style2style.clear();
    }

    /**
     * Finishes writing a table. Saves the changes to excel sheet.
     *
     * @throws IllegalStateException if method is called without prior
     *             <code>beginTable()</code> call
     * @throws CreateTableException if an exception occurred when saving
     */
    public void endTable() throws CreateTableException {
        if (region == null) {
            throw new IllegalStateException("endTable() call without prior beginTable() call");
        }
        for (int y = currentRow; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                writeCell(x, y, 1, 1, "");
            }
        }
        region = null;
        style2style.clear();
    }

    public void save() throws CreateTableException {
        try {
            gridModel.getSheetSource().getWorkbookSource().save();
        } catch (IOException e) {
            throw new CreateTableException("could not save table");
        }
    }

    protected int getCurrentRow() {
        return currentRow;
    }

    /**
     * Initializes default cell style.
     *
     * @return cell style
     */
    protected CellStyle getDefaultCellStyle() {
        if (defaultCellStyle == null) {
            Workbook workbook = gridModel.getSheetSource()
                    .getWorkbookSource().getWorkbook();
            CellStyle cellStyle = workbook.createCellStyle();

            cellStyle.setBorderBottom(ICellStyle.BORDER_THIN);
            cellStyle.setBorderTop(ICellStyle.BORDER_THIN);
            cellStyle.setBorderLeft(ICellStyle.BORDER_THIN);
            cellStyle.setBorderRight(ICellStyle.BORDER_THIN);

            defaultCellStyle = cellStyle;
        }
        return defaultCellStyle;
    }

    protected XlsSheetGridModel getGridModel() {
        return gridModel;
    }

    protected int getHeight() {
        return height;
    }

    public IGridRegion getTableRegion() {
        return region;
    }

    protected int getWidth() {
        return width;
    }

    protected void incCurrentRow() {
        incCurrentRow(1);
    }

    protected void incCurrentRow(int increment) {
        currentRow += increment;
    }

    /**
     * Writes cell.
     *
     * @param x cell x coordinate
     * @param y cell y coordinate
     * @param width cell width
     * @param height cell height
     * @param value cell value
     */
    protected void writeCell(int x, int y, int width, int height, Object value) {
        writeCell(x, y, width, height, value, null);
    }

    /**
     * Writes cell.
     *
     * @param x cell x coordinate
     * @param y cell y coordinate
     * @param width cell width
     * @param height cell height
     * @param value cell value
     * @param style cell style
     */
    protected void writeCell(int x, int y, int width, int height, Object value, ICellStyle style) {
       
        CellStyle cellStyle = null;
        if (style != null) {
            if (style instanceof XlsCellStyle) {
                cellStyle = ((XlsCellStyle) style).getXlsStyle();
            } else if (style instanceof XlsCellStyle2) {
                cellStyle = ((XlsCellStyle2) style).getXlsStyle();
            }           
        } else {
            cellStyle = getDefaultCellStyle();               
        }
        x += region.getLeft();
        y += region.getTop();
        if (width == 1 && height == 1) {
            Cell cell = gridModel.getOrCreateXlsCell(x, y);
            gridModel.setCellValue(x, y, value);
            // we need to add data format in style for dates
            if (value instanceof Date) {
                modifyCellStyle(cell, cellStyle);
            }
            setCellStyle(cell, cellStyle);
        } else {
            int x2 = x + width - 1;
            int y2 = y + height - 1;
            gridModel.addMergedRegion(new GridRegion(y, x, y2, x2));
            for (int col = x; col <= x2; col++) {
                for (int row = y; row <= y2; row++) {
                    Cell newCell = gridModel.getOrCreateXlsCell(col, row);
                    gridModel.setCellValue(x, y, value);
                    setCellStyle(newCell, cellStyle);
                }
            }
        }
    }
    
    /**
     * If the value was set to the cell of type date, we need to modify our 
     * default style with data format for dates.
     * @param cell Cell with value in it.
     * @param cellStyle Cell style.
     */
    private void modifyCellStyle(Cell cell, CellStyle cellStyle) {
        cellStyle.setDataFormat(cell.getCellStyle().getDataFormat());        
    }

    private void setCellStyle(Cell cell, CellStyle cellStyle) {
        CellStyle newStyle = style2style.get(cellStyle);
        if (newStyle != null) {
            cellStyle = newStyle;
        }
        try {
            cell.setCellStyle(cellStyle);
        } catch (Exception e) {
            CellStyle style = findWorkbookCellStyle(cellStyle);
            if (style != null) {
                style2style.put(cellStyle, style);
            } else {
                Workbook workbook = gridModel.getSheetSource().getWorkbookSource().getWorkbook();
                style = workbook.createCellStyle();
                try {
                    style.cloneStyleFrom(cellStyle);
                } catch (IllegalArgumentException ex) {
                    // FIXME: remove try.. catch
                }
                style2style.put(cellStyle, style);
            }
            cell.setCellStyle(style);
        }
    }

    private CellStyle findWorkbookCellStyle(CellStyle cellStyle) {
        Workbook workbook = gridModel.getSheetSource().getWorkbookSource().getWorkbook();
        short numCellStyles = workbook.getNumCellStyles();
        for (int i = 0; i < numCellStyles; i++) {
            CellStyle cellStyleAt = workbook.getCellStyleAt((short) i);
            if (equalsStyle(cellStyleAt, cellStyle)) {
                return cellStyleAt;
            }
        }
        return null;
    }

    private boolean equalsStyle(CellStyle cs1, CellStyle cs2) {
        return (cs1.getAlignment() == cs2.getAlignment() && cs1.getAlignment() == cs2.getAlignment()
                && cs1.getHidden() == cs2.getHidden() && cs1.getLocked() == cs2.getLocked()
                && cs1.getWrapText() == cs2.getWrapText() && cs1.getBorderBottom() == cs2.getBorderBottom()
                && cs1.getBorderLeft() == cs2.getBorderLeft() && cs1.getBorderRight() == cs2.getBorderRight()
                && cs1.getBorderTop() == cs2.getBorderTop() && cs1.getBottomBorderColor() == cs2.getBottomBorderColor()
                && cs1.getFillBackgroundColor() == cs2.getFillBackgroundColor()
                && cs1.getFillForegroundColor() == cs2.getFillForegroundColor()
                && cs1.getFillPattern() == cs2.getFillPattern() && cs1.getIndention() == cs2.getIndention()
                && cs1.getLeftBorderColor() == cs2.getLeftBorderColor()
                && cs1.getRightBorderColor() == cs2.getRightBorderColor() && cs1.getRotation() == cs2.getRotation()
                && cs1.getTopBorderColor() == cs2.getTopBorderColor() && cs1.getVerticalAlignment() == cs2
                .getVerticalAlignment());
    }
    
    

    /**
     * Writes cell.
     *
     * @param x cell x coordinate
     * @param y cell y coordinate
     * @param value cell value
     */
    protected void writeCell(int x, int y, Object value) {
        writeCell(x, y, 1, 1, value, null);
    }

    /**
     * Writes table grid.
     *
     * @param table table grid
     *
     * @throws IllegalArgumentException if table is null
     * @throws IllegalStateException if method is called without prior
     *             <code>beginTable()</code> call
     */
    public void writeGridTable(IGridTable table) {
        if (table == null) {
            throw new IllegalArgumentException("table must be not null");
        }
        if (region == null) {
            throw new IllegalStateException("beginTable() has to be called");
        }
        for (int i = 0; i < table.getGridWidth(); i++) {
            for (int j = 0; j < table.getGridHeight(); j++) {
            	ICell cell = table.getCell(i, j);
                int cellWidth = cell.getWidth();
                int cellHeight = cell.getHeight();
                Object cellValue = cell.getObjectValue();
                ICellStyle style = cell.getStyle();
                writeCell(i, currentRow + j, cellWidth, cellHeight, cellValue, style);
            }
        }
        currentRow += table.getGridHeight();
    }

    /**
     * Writes table header.
     *
     * @param header header text for the table
     * @param style header style
     *
     * @throws IllegalStateException if method is called without prior
     *             <code>beginTable()</code> call
     */
    public void writeHeader(String header, ICellStyle style) {
        if (region == null) {
            throw new IllegalStateException("beginTable() has to be called");
        }
        writeCell(0, currentRow++, width, 1, header, style);
    }

    /**
     * Writes table properties.
     *
     * @param properties table properties
     * @param style properties style
     *
     * @throws IllegalArgumentException if properties is null
     * @throws IllegalStateException if method is called without prior
     *             <code>beginTable()</code> call
     */
    public void writeProperties(Map<String, Object> properties, ICellStyle style) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must be not null");
        }
        if (region == null) {
            throw new IllegalStateException("beginTable() has to be called");
        }
        writeCell(0, currentRow, 1, properties.size(), TABLE_PROPERTIES, style);
        Set<String> keys = properties.keySet();
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
            String key = iterator.next();
            writeCell(1, currentRow, 1, 1, key, style);
            Object value = properties.get(key);
            writeCell(2, currentRow, 1, 1, value, style);
            currentRow++;
        }
    }

}
