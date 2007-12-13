package org.openl.rules.ui;

import org.openl.rules.table.ui.ICellFont;


/**
 *
 * @author Stanislav Shor
 */
public interface ICellModel {
    public BorderStyle[] getBorderStyle();

    public void setBorderStyle(BorderStyle[] borderStyle);

    public int getColspan();

    public void setColspan(int colspan);

    public String getContent();

    public void setContent(String content);

    public ICellFont getFont();

    public void setFont(ICellFont font);

    public short[] getRgbBackground();

    public void setRgbBackground(short[] rgbBackground);

    public int getRowspan();

    public void setRowspan(int rowspan);

    public void toHtmlString(StringBuffer buf, TableModel model);

    public boolean isReal();

//!!!	public void setColorFilter(IColorFilter[] filter);
}
