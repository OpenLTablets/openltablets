package org.openl.rules.tableeditor.model.ui;

import java.util.HashMap;
import java.util.Map;

import org.openl.rules.table.ui.ICellFont;
import org.openl.rules.tableeditor.model.ui.util.HTMLHelper;

public class CellModel implements ICellModel {

    private int row;
    private int column;

    private int colspan = 1;
    private int rowspan = 1;

    private int ident = 0;
    private String halign;
    private String valign;
    private short[] rgbBackground;
    private BorderStyle[] borderStyle;

    private static final Map<String, Object> DEFAULT_CELL_STYLES = new HashMap<String, Object>();
    static {
        // tableeditor.all.css
        DEFAULT_CELL_STYLES.put("padding", 1);
        DEFAULT_CELL_STYLES.put("font-family", "Franklin Gothic Book");
        DEFAULT_CELL_STYLES.put("font-size", 12);
        DEFAULT_CELL_STYLES.put("color", "#000");
        DEFAULT_CELL_STYLES.put("border-style", "solid");
        DEFAULT_CELL_STYLES.put("border-width", "1px");
        DEFAULT_CELL_STYLES.put("border-color", "#bbd");
        DEFAULT_CELL_STYLES.put("background", "#fff");
    };

    private boolean hasFormula;
    private String formula;

    private String content = "&nbsp;";

    private String comment;

    private ICellFont font;
    private int width;

    static int calcMaxLineLength(String content) {
        int max = 0;
        int from = 0;

        while (true) {
            int idx1 = content.indexOf('\n', from);
            if (idx1 <= 0) {
                return max;
            }
            max = Math.max(max, idx1 - from);
            from = idx1 + 1;
        }
    }

    public CellModel(int row, int column) {
        this.row = row;
        this.column = column;
        hasFormula = false;
    }

    public void atttributesToHtml(StringBuilder buf, TableModel table, boolean selectErrorCell) {
        if (colspan != 1) {
            buf.append(" colspan=\"").append(colspan).append("\"");
        }
        if (rowspan != 1) {
            buf.append(" rowspan=\"").append(rowspan).append("\"");
        }

        String style = getHtmlStyle(table, selectErrorCell);

        buf.append(" style=\"" + style + "\"");
    }

    public void atttributesToHtml(StringBuilder buf, TableModel table) {
        atttributesToHtml(buf, table, false);
    }

    private void borderToHtml(StringBuilder buf, TableModel table) {
        if (borderStyle == null) {
            return;
        }

        String[] bwidth = new String[4];
        for (int i = 0; i < borderStyle.length; i++) {
            int width = (borderStyle[i] == null) ? 0 : borderStyle[i].getWidth();
            bwidth[i] = width + (width != 0 ? "px" : "");
        }
        String widthStr = HTMLHelper.boxCSStoString(bwidth);
        if (!widthStr.equals(DEFAULT_CELL_STYLES.get("border-width"))) {
            buf.append("border-width:").append(widthStr).append(';');
        }

        String[] styles = new String[4];
        for (int i = 0; i < borderStyle.length; i++) {
            String style = null;
            if ((borderStyle[i] == null || borderStyle[i].getWidth() == 0) && i != 1) {
                style = (borderStyle[1] == null) ? "none" : borderStyle[1].getStyle();
            } else {
                style = borderStyle[i].getStyle();
            }
            styles[i] = style;
        }
        String styleStr = HTMLHelper.boxCSStoString(styles);
        if (!styleStr.equals(DEFAULT_CELL_STYLES.get("border-style"))) {
            buf.append("border-style:").append(styleStr).append(';');
        }

        String[] colors = new String[4];
        for (int i = 0; i < borderStyle.length; i++) {
            String color = null;
            if ((borderStyle[i] == null || borderStyle[i].getWidth() == 0) && i != 1) {
                color = (borderStyle[1] == null) ? "#000" : HTMLHelper.toHexColor(borderStyle[1].getRgb());
            } else {
                color = HTMLHelper.toHexColor(borderStyle[i].getRgb());
            }
            colors[i] = color;
        }
        String colorStr = HTMLHelper.boxCSStoString(colors);
        if (!colorStr.equals(DEFAULT_CELL_STYLES.get("border-color"))) {
            buf.append("border-color:").append(colorStr).append(";");
        }
    }

    String convertContent(String content) {
        StringBuilder buf = new StringBuilder(content.length() + 100);

        boolean startLine = true;

        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);

            if ((ch == ' ') && startLine) {
                buf.append("&nbsp;");
                continue;
            }

            if (ch == '\n') {
                startLine = true;
                buf.append("<br>");
                continue;
            }

            buf.append(ch);
            startLine = false;
        }

        return buf.toString();
    }

    public BorderStyle[] getBorderStyle() {
        return borderStyle;
    }

    public int getColspan() {
        return colspan;
    }

    public String getContent(boolean showFormulas) {
        if (showFormulas && hasFormula) {
            return convertContent(formula);
        } else {
            return convertContent(content);
        }
    }

    public ICellFont getFont() {
        return font;
    }

    public String getHalign() {
        return halign;
    }

    /**
     * Returns style string for cell.
     *
     * @param tm
     *
     * @return style string for cell
     */
    public String getHtmlStyle(TableModel tm, boolean selectErrorCell) {
        StringBuilder sb = new StringBuilder();
        if (halign != null) {
            sb.append("text-align:" + halign + ";");
        }

        if (valign != null) {
            sb.append("vertical-align:" + valign + ";");
        }

        if (width != 0) {
            sb.append("width:" + width + "px" + ";");
        }

        if (rgbBackground != null) {
            String rgb = HTMLHelper.toHexColor(rgbBackground);
            if (!rgb.equals(DEFAULT_CELL_STYLES.get("background"))) {
                sb.append("background:" + rgb + ";");
            }
        }

        if (selectErrorCell) {
            sb.append("border: 2px solid red;");
        } else if (borderStyle != null) {
            borderToHtml(sb, tm);
        }

        if (font != null) {
            fontToHtml(font, sb);
        }

        if (ident > 0) {
            sb.append("padding-left:" + ((Integer) DEFAULT_CELL_STYLES.get("padding") * 0.063 + ident) + "em" + ";");
        }

        return sb.toString();
    }

    public static StringBuilder fontToHtml(ICellFont font, StringBuilder buf) {
        if (font == null) {
            return buf;
        }

        if (font.isUnderlined() || font.isStrikeout()) {
            buf.append("text-decoration:");
            if (font.isUnderlined()) {
                buf.append("underline");
            }
            if (font.isStrikeout()) {
                buf.append("line-through");
            }
            buf.append(";");
        }

        String fontName = font.getName();
        if (!fontName.equals(DEFAULT_CELL_STYLES.get("font-family"))) {
            buf.append("font-family:").append(fontName).append(";");
        }
        int fontSize = font.getSize() + 2;
        if (fontSize != (Integer) DEFAULT_CELL_STYLES.get("font-size")) {
            buf.append("font-size:").append(fontSize).append(";");
        }
        if (font.isItalic()) {
            buf.append("font-style:italic").append(";");
        }
        if (font.isBold()) {
            buf.append("font-weight:bold").append(";");
        }

        short[] color = font.getFontColor();
        if (color != null) {
            String colorStr = HTMLHelper.toHexColor(color);
            if (!colorStr.equals(DEFAULT_CELL_STYLES.get("color"))) {
                buf.append("color:" + colorStr + ";");
            }
        }

        return buf;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public int getIdent() {
        return ident;
    }

    public short[] getRgbBackground() {
        return rgbBackground;
    }

    public int getRowspan() {
        return rowspan;
    }

    public String getValign() {
        return valign;
    }

    public boolean isReal() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param bStyle
     * @param dir
     */
    public void setBorderStyle(BorderStyle bStyle, int dir) {
        if (borderStyle == null) {
            borderStyle = new BorderStyle[4];
        }
        borderStyle[dir] = bStyle;
    }

    public void setBorderStyle(BorderStyle[] borderStyle) {
        this.borderStyle = borderStyle;
    }

    public void setColspan(int colspan) {
        this.colspan = colspan;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFont(ICellFont font) {
        this.font = font;
    }

    public void setHalign(String halign) {
        this.halign = halign;
    }

    public void setIdent(int ident) {
        this.ident = ident;
    }

    public void setRgbBackground(short[] rgbBackground) {
        this.rgbBackground = rgbBackground;
    }

    public void setRowspan(int rowspan) {
        this.rowspan = rowspan;
    }

    public void setValign(String valign) {
        this.valign = valign;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean hasFormula() {
        return hasFormula;
    }

    public void setFormula(String formula) {
        this.formula = "=" + formula;
        hasFormula = true;
    }

    public String getFormula() {
        return formula;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
