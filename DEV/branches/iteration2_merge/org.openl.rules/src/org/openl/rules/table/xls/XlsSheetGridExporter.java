package org.openl.rules.table.xls;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.openl.rules.table.GridRegion;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.ui.ICellStyle;
import org.openl.rules.lang.xls.IXlsTableNames;
import org.openl.util.export.ExportException;
import org.openl.util.export.IExportRow;
import org.openl.util.export.IExportSection;
import org.openl.util.export.IExportable;
import org.openl.util.export.IExporter;

/**
 * An {@link org.openl.util.export.IExporter} implementation that persists instances into an excel sheet.
 */
public class XlsSheetGridExporter implements IExporter {
    private final XlsSheetGridModel gridModel;
    private HSSFCellStyle headerStyle;
    private HSSFCellStyle style;

    public XlsSheetGridExporter(XlsSheetGridModel gridModel) {
        if (gridModel == null) {
            throw new NullPointerException("gridModel is null");
        }
        this.gridModel = gridModel;
    }

    public void persist(IExportable exportable) throws ExportException {
        IExportSection exportSection = exportable.mainSection();
        int width = width(exportSection), height = height(exportSection) + 1;

        IGridRegion region = gridModel.findEmptyRect(width, height);
        if (region == null) {
            throw new ExportException("could not find appropriate region for writing");
        }

        gridModel.addMergedRegion(new GridRegion(region.getTop(), region.getLeft(), region.getTop(), region.getRight()));

        persistHeader(region, IXlsTableNames.PERSISTENCE_TABLE + " " + exportable.getClass().getName());
        persist(region.getLeft(), region.getTop() + 1, exportSection, width);
    }

    private void persistHeader(IGridRegion region, String text) {
        gridModel.setCellValue(region.getLeft(), region.getTop(), text);
        HSSFCellStyle hstyle = getHeaderStyle();

        for (int col = region.getLeft(); col <= region.getRight(); ++col) {
            gridModel.createNewCell(col, region.getTop()).setCellStyle(hstyle);
        }
    }

    HSSFCellStyle getHeaderStyle() {
        if (headerStyle == null) {
            HSSFWorkbook workbook = gridModel.getSheetSource().getWorkbookSource().getWorkbook();
            HSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(ICellStyle.BORDER_THIN);
            cellStyle.setBorderTop(ICellStyle.BORDER_THIN);
            cellStyle.setBorderLeft(ICellStyle.BORDER_THIN);
            cellStyle.setBorderRight(ICellStyle.BORDER_THIN);
            
            cellStyle.setFillForegroundColor(HSSFColor.BLACK.index);
            cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            HSSFFont font = workbook.createFont();
            font.setColor(HSSFColor.WHITE.index);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            cellStyle.setFont(font);

            headerStyle = cellStyle;
        }

        return headerStyle;
    }

    private HSSFCellStyle getStyle() {
        if (style == null) {
            HSSFWorkbook workbook = gridModel.getSheetSource().getWorkbookSource().getWorkbook();
            HSSFCellStyle cellStyle = workbook.createCellStyle();

            cellStyle.setBorderBottom(ICellStyle.BORDER_THIN);
            cellStyle.setBorderTop(ICellStyle.BORDER_THIN);
            cellStyle.setBorderLeft(ICellStyle.BORDER_THIN);
            cellStyle.setBorderRight(ICellStyle.BORDER_THIN);

            style = cellStyle;
        }

        return style;
    }

    /**
     * Persists a section recursively. Writes data to specified position as top-left corner on grid.
     *
     * @param col column to write to
     * @param row row to write to
     * @param section section to persist
     * @param widthToFill number of columns not filled yet in current row
     * @return height of the section
     */
    private int persist(int col, int row, IExportSection section, int widthToFill) {
        int height = 0;
        ++col;
        --widthToFill;

        // saving subsections
        IExportSection[] subSections = section.getSubSections();
        if (subSections != null) {
            for (IExportSection subSection : subSections) {
                height += persist(col, row + height, subSection, widthToFill);
            }
        }

        // saving rows
        IExportRow[] rows = section.getRows();
        if (rows != null) {
            for (IExportRow expRrow : rows) {
                persistRow(col, row + height++, expRrow, widthToFill);
            }
        }

        // saving the section name itself
        --col;
        if (height > 1)
            gridModel.addMergedRegion(new GridRegion(row, col, row + height - 1, col));
        fillCell(col, row, height, section.getName());

        return height == 0 ? 1 : height;
    }

    private void fillCell(int col, int row, int height, String value) {
        gridModel.setCellValue(col, row, value);
        HSSFCellStyle style = getStyle();
        
        for (int r = 0; r < height; ++r) 
            gridModel.createNewCell(col, row + r).setCellStyle(style);
    }

    private void fillCellStretchHorizontally(int col, int row, int width, String value) {
        if (width == 1)
            fillCell(col, row, 1, value);
        else {
            gridModel.setCellValue(col, row, value);
            HSSFCellStyle style = getStyle();

            for (int c = 0; c < width; ++c)
                gridModel.createNewCell(col + c, row).setCellStyle(style);
        }
    }

    private void persistRow(int col, int row, IExportRow expRrow, int widthToFill) {
        if (expRrow.size() >= widthToFill)
            for (String cell : expRrow.record()) {
                fillCell(col++, row, 1, cell);
            }
        else {
            String[] cells = expRrow.record();
            for (int i = 0; i < cells.length - 1; ++i)
                fillCell(col++, row, 1, cells[i]);

            int width = widthToFill - cells.length + 1;
            gridModel.addMergedRegion(new GridRegion(row, col, row, col + width - 1));
            fillCellStretchHorizontally(col, row, width, cells[cells.length-1]);
        }
    }

    /**
     * Computes width of the section when saved to excel.
     *
     * @param section exported section
     * @return column number required for the section.
     */
    private static int width(IExportSection section) {
        int width = 0;
        IExportSection[] subSections = section.getSubSections();
        if (subSections != null)
            for (IExportSection subSection : subSections) {
                width = Math.max(width, width(subSection));
            }

        IExportRow[] rows = section.getRows();
        if (rows != null)
            for (IExportRow row : rows) {
                width = Math.max(width, row.size());
            }

        return width + 1;
    }

    /**
     * Computes height of the section when saved to excel.
     *
     * @param section exported section
     * @return row number required for the section.
     */
    private static int height(IExportSection section) {
        int height = 0;
        IExportSection[] subSections = section.getSubSections();
        if (subSections != null)
            for (IExportSection subSection : subSections) {
                height += height(subSection);
            }

        IExportRow[] rows = section.getRows();
        if (rows != null)
            height += rows.length;

        return height == 0 ? 1 : height;
    }
}
