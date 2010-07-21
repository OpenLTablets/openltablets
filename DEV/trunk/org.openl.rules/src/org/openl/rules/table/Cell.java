package org.openl.rules.table;

import java.util.Date;

import org.openl.rules.lang.xls.types.CellMetaInfo;
import org.openl.rules.table.ui.ICellFont;
import org.openl.rules.table.ui.ICellStyle;

public class Cell implements ICell {

    private int row;
    private int column;

    private int width;
    private int height;

    private ICellStyle style;

    private Object objectValue;
    private String stringValue;

    private ICellFont font;

    private IGridRegion region;

    private String formula;

    private int type;

    private String uri;

    private CellMetaInfo metaInfo;

    public Cell() {
    }

    public int getAbsoluteColumn() {
        return getColumn();
    }

    public int getAbsoluteRow() {
        return getRow();
    }

    public IGridRegion getAbsoluteRegion() {
        return getRegion();
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int col) {
        this.column = col;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ICellStyle getStyle() {
        return style;
    }

    public void setStyle(ICellStyle style) {
        this.style = style;
    }

    public Object getObjectValue() {
        return objectValue;
    }

    public void setObjectValue(Object objectValue) {
        this.objectValue = objectValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public ICellFont getFont() {
        return font;
    }

    public void setFont(ICellFont font) {
        this.font = font;
    }

    public IGridRegion getRegion() {
        return region;
    }

    public void setRegion(IGridRegion region) {
        this.region = region;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean getNativeBoolean() {
        throw new UnsupportedOperationException();
    }

    public Date getNativeDate() {
        throw new UnsupportedOperationException();
    }
    public double getNativeNumber() {
        throw new UnsupportedOperationException();
    }

    public int getNativeType() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNativeType() {
        return false;
    }

    public CellMetaInfo getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(CellMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

}
