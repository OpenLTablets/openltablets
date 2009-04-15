package org.openl.rules.table.xls;

import java.util.Hashtable;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openl.rules.table.ui.ICellStyle;

public class XlsCellStyle2 implements ICellStyle {
	private static Hashtable<Integer, HSSFColor> oldIndexedColors = HSSFColor.getIndexHash();
	
	XSSFCellStyle xlsStyle;
	XSSFWorkbook workbook;

	public XlsCellStyle2(XSSFCellStyle xlsStyle, XSSFWorkbook workbook)
	{
		this.xlsStyle = xlsStyle;
		this.workbook = workbook;
	}

	@Override
	public short[][] getBorderRGB() {
		short[][] ccRgb = new short[4][];
		ccRgb[0] = colorToArray(xlsStyle.getTopBorderXSSFColor());
		ccRgb[1] = colorToArray(xlsStyle.getRightBorderXSSFColor());
		ccRgb[2] = colorToArray(xlsStyle.getBottomBorderXSSFColor());
		ccRgb[3] = colorToArray(xlsStyle.getLeftBorderXSSFColor());

		return ccRgb;
	}

	private short[] colorToArray(XSSFColor color) {
		if (color == null) return null;

		byte[] rgb = color.getRgb();
		if (rgb == null) {
			Integer key = new Integer(color.getIndexed());
			HSSFColor c = oldIndexedColors.get(key);
			if (c == null) {
				return null;
			} else {
				return c.getTriplet();
			}
		}

		short[] result = new short[3];
		for (int i = 1; i < 4; i++) {
			// TODO FIXME (byte: -128 .. 127), short-color: 0..255 or 0..65xxx ?
			result[i - 1] = rgb[i];
		}

		return result;
	}
	
	@Override
	public short[] getBorderStyle() {
		short[] bb = new short[4];
		bb[0] = xlsStyle.getBorderTop();
		bb[1] = xlsStyle.getBorderRight();
		bb[2] = xlsStyle.getBorderBottom();
		bb[3] = xlsStyle.getBorderLeft();
		return bb;		
	}

	public boolean hasNoFill() {
	    return (xlsStyle.getFillPattern() == CellStyle.NO_FILL);
	}

	@Override
	public short[] getFillBackgroundColor() {
	    if (hasNoFill()) return null;
		return colorToArray(xlsStyle.getFillBackgroundXSSFColor());
	}

	@Override
	public short[] getFillForegroundColor() {
	    if (hasNoFill()) return null;
		return colorToArray(xlsStyle.getFillForegroundXSSFColor());
	}

	@Override
	public int getHorizontalAlignment() {
		return xlsStyle == null ? ALIGN_GENERAL : xlsStyle.getAlignment();
	}

	@Override
	public int getIdent() {
		return xlsStyle.getIndention();
	}

	@Override
	public int getRotation() {
		return xlsStyle.getRotation();
	}

	@Override
	public String getTextFormat() {
		return workbook.createDataFormat().getFormat(xlsStyle.getDataFormat());
	}

	@Override
	public int getVerticalAlignment() {
		return xlsStyle == null ? ALIGN_GENERAL : xlsStyle.getVerticalAlignment();
	}

	@Override
	public boolean isWrappedText() {
		return xlsStyle.getWrapText();
	}

}
