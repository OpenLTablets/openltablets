package org.openl.excel.parser.sax;

import java.util.Map;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import org.openl.excel.parser.TableStyles;
import org.openl.rules.table.ICellComment;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.ui.ICellFont;
import org.openl.rules.table.ui.ICellStyle;
import org.openl.rules.table.xls.XlsCellFont;
import org.openl.rules.table.xls.XlsCellStyle;
import org.openl.util.StringUtils;

public class SAXTableStyles implements TableStyles {
    private final IGridRegion region;
    private final int[][] cellIndexes;
    private final StylesTable stylesTable;
    private final CommentsTable sheetComments;
    private final Map<CellAddress, String> formulas;

    public SAXTableStyles(IGridRegion region,
                          int[][] cellIndexes,
                          StylesTable stylesTable,
                          CommentsTable sheetComments,
                          Map<CellAddress, String> formulas) {
        this.region = region;
        this.cellIndexes = cellIndexes;
        this.stylesTable = stylesTable;
        this.sheetComments = sheetComments;
        this.formulas = formulas;
    }

    @Override
    public IGridRegion getRegion() {
        return region;
    }

    @Override
    public ICellStyle getStyle(int row, int column) {
        int rowIndex = row - region.getTop();
        int colIndex = column - region.getLeft();
        if (rowIndex < cellIndexes.length && colIndex < cellIndexes[rowIndex].length) {
            int index = cellIndexes[rowIndex][colIndex];
            // For XSSF workbook is not needed
            return new XlsCellStyle(stylesTable.getStyleAt(index), null);
        }
        return null;
    }

    @Override
    public ICellFont getFont(int row, int column) {
        int index = cellIndexes[row - region.getTop()][column - region.getLeft()];
        // For XSSF workbook is not needed
        return new XlsCellFont(stylesTable.getStyleAt(index).getFont(), null);
    }

    @Override
    public ICellComment getComment(int row, int column) {
        if (sheetComments == null) {
            return null;
        }

        XSSFComment comment = sheetComments.findCellComment(new CellAddress(row, column));
        if (comment == null) {
            return null;
        }

        XSSFRichTextString rst = comment.getString();
        String text = rst == null ? null : rst.getString();
        return new SAXCellComment(text, comment.getAuthor());
    }

    @Override
    public String getFormula(int row, int column) {
        return StringUtils.trimToNull(formulas.get(new CellAddress(row, column)));
    }

    private static class SAXCellComment implements ICellComment {
        private final String text;
        private final String author;

        public SAXCellComment(String text, String author) {
            this.text = text;
            this.author = author;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getAuthor() {
            return author;
        }
    }
}
