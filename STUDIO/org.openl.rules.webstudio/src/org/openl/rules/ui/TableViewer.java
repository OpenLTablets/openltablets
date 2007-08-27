package org.openl.rules.ui;

import org.openl.rules.table.ICellInfo;
import org.openl.rules.table.IGrid;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.ui.ICellStyle;
import org.openl.util.Log;

public class TableViewer
{
	IGrid grid;

	IGridRegion reg;

	public TableModel buildModel()
	{

		// IGridTable table = new GridTable(g.getTop(), g.getLeft(), g.getBottom(),
		// g.getRight(), t.getGrid());

		int h = IGridRegion.Tool.height(reg);
		int w = IGridRegion.Tool.width(reg);

		TableModel tm = new TableModel(w, h);

		for (int row = reg.getTop(); row <= reg.getBottom(); row++)
		{
			for (int column = reg.getLeft(); column <= reg.getRight(); column++)
			{
				int c = column - reg.getLeft();
				int r = row - reg.getTop();
				if (tm.hasCell(r, c))
					continue;
				ICellInfo ci = grid.getCellInfo(column, row);

				CellModel cm = buildCell(ci, column, row, new CellModel(r, c));


				tm.addCell(cm, r, c);
				if (cm.getColspan() > 1 || cm.getRowspan() > 1)
				{
					CellModelDelegator cmd = new CellModelDelegator(cm);
					for (int i = 0; i < cm.getRowspan(); i++)
					{
						for (int j = 0; j < cm.getColspan(); j++)
						{
							if (i == 0 && j == 0)
								continue;
							tm.addCell(cmd, r + i, c + j);
						}
					}
				}

			}

		}

		setGrid(tm);
		return tm;
	}

	public String showTable(IGrid grid, IGridRegion region)
	{
		return showTable(grid, region, true);
	}

	public String showTable(IGrid grid, IGridRegion region, boolean showGrid)
	{
		StringBuffer buf = new StringBuffer(1000);
		this.grid = grid;
		this.reg = region;
		TableModel tm = buildModel();
		tm.toHtmlString(buf, showGrid);

		return buf.toString();
	}

	CellModel buildCell(ICellInfo ci, int column, int row, CellModel cm)
	{

		cm.setColspan(getColSpan(ci));
		cm.setRowspan(getRowSpan(ci));

		if (cm.row == 0)
			cm.setWidth(getWidth(ci, column, row));

		String content = grid.getFormattedCellValue(column, row);
		if (content != null && content.trim().length() != 0)
			cm.setContent(content);
		// cm.setContent(content.startsWith(LITERAL) ?
		// content.substring(LITERAL.length()) :
		// StringTool.prepareXMLAttributeValue(content));

		setStyle(ci, cm);
		return cm;
	}

	private static void setStyle(ICellInfo ci, CellModel cm)
	{
		ICellStyle style = ci.getCellStyle();

		if (style == null)
			return;


		switch (style.getHorizontalAlignment())
		{
		case ICellStyle.ALIGN_RIGHT:
			cm.setHalign("right");
			break;
		case ICellStyle.ALIGN_CENTER:
			cm.setHalign("center");
			break;
		case ICellStyle.ALIGN_JUSTIFY:
			cm.setHalign("justify");
			break;
		}

		switch (style.getVerticalAlignment())
		{
		case ICellStyle.VERTICAL_BOTTOM:
			cm.setValign("bottom");
			break;
		case ICellStyle.VERTICAL_CENTER:
			cm.setHalign("center");
			break;
		case ICellStyle.VERTICAL_TOP:
			cm.setValign("top");
			break;
		}

		
		
		if (style.getIdent() > 0)
			cm.setIdent(style.getIdent());

		short[] rgb = style.getFillForegroundColor();
		cm.setRgbBackground(rgb);

		// setCssBorders(cm, style);

		cm.setFont(ci.getFont());

		/*
		 * buf.append(" style=\""); String[] borders = style.cssBorders();
		 * buf.append("border-style:"); for (int i = 0; i < 4; i++) { buf.append('
		 * ').append(borders[ICellStyle.BORDER_TYPE +i]); }
		 * 
		 * buf.append("; border-color:"); for (int i = 0; i < 4; i++) { buf.append('
		 * ').append(borders[ICellStyle.BORDER_COLOR +i]); }
		 * 
		 * buf.append("; border-width:"); for (int i = 0; i < 4; i++) { buf.append('
		 * ').append(borders[ICellStyle.BORDER_WIDTH +i]); }
		 * 
		 * buf.append("\"");
		 */

	}

	BorderStyle getBorderStyle(ICellStyle cs, int side)
	{
		
		int xlsStyle; 
		short[] rgb;
		
		short[] bss = cs.getBorderStyle();
		xlsStyle = bss == null ? ICellStyle.BORDER_NONE : bss[side];
		
		short[][] rgbb = cs.getBorderRGB();
		rgb = rgbb == null ? new short[]{0,0,0} : rgbb[side];
		
		BorderStyle bs = new BorderStyle();
		bs.rgb = rgb;
		switch (xlsStyle)
		{
		case ICellStyle.BORDER_NONE:
			return BorderStyle.NONE;
		case ICellStyle.BORDER_DASH_DOT_DOT:
		case ICellStyle.BORDER_DASH_DOT:
		case ICellStyle.BORDER_DASHED:
			bs.width = 1;
			bs.style = "dashed";
			break;

		case ICellStyle.BORDER_DOTTED:
			bs.width = 1;
			bs.style = "dotted";
			break;
		case ICellStyle.BORDER_DOUBLE:
			bs.width = 1;
			bs.style = "double";
			break;
		case ICellStyle.BORDER_THIN:
			bs.width = 1;
			bs.style = "solid";
			break;
		case ICellStyle.BORDER_THICK:
			bs.width = 2;
			bs.style = "solid";
			break;
		case ICellStyle.BORDER_HAIR:
			bs.width = 1;
			bs.style = "dotted";
			break;
		case ICellStyle.BORDER_MEDIUM:
			bs.width = 2;
			bs.style = "solid";
			break;
		case ICellStyle.BORDER_MEDIUM_DASH_DOT:
		case ICellStyle.BORDER_MEDIUM_DASH_DOT_DOT:
		case ICellStyle.BORDER_MEDIUM_DASHED:
			bs.width = 2;
			bs.style = "dashed";
			break;
		default:
			Log.warn("Unknown border style: " + xlsStyle);
		}
		return bs;
	}

	// private static void setCssBorders(CellModel cm, ICellStyle cs)
	// {
	//
	// short[] xlsb = cs.getBorderStyle();
	// if (xlsb == null)
	// return;
	// int[] borderWidth = new int[4];
	// String[] borderStyle = new String[4];
	//
	// for (int i = 0; i < 4; i++)
	// {
	// switch (xlsb[i])
	// {
	// case ICellStyle.BORDER_NONE:
	// borderWidth[i] = 0;
	// borderStyle[i] = "none";
	// break;
	// case ICellStyle.BORDER_DASH_DOT_DOT:
	// case ICellStyle.BORDER_DASH_DOT:
	// case ICellStyle.BORDER_DASHED:
	// borderWidth[i] = 1;
	// borderStyle[i] = "dashed";
	// break;
	//
	// case ICellStyle.BORDER_DOTTED:
	// borderWidth[i] = 1;
	// borderStyle[i] = "dotted";
	// break;
	// case ICellStyle.BORDER_DOUBLE:
	// borderWidth[i] = 1;
	// borderStyle[i] = "double";
	// break;
	// case ICellStyle.BORDER_THIN:
	// borderWidth[i] = 1;
	// borderStyle[i] = "solid";
	// break;
	// case ICellStyle.BORDER_THICK:
	// borderWidth[i] = 3;
	// borderStyle[i] = "solid";
	// break;
	// case ICellStyle.BORDER_HAIR:
	// borderWidth[i] = 1;
	// borderStyle[i] = "dotted";
	// break;
	// case ICellStyle.BORDER_MEDIUM:
	// borderWidth[i] = 2;
	// borderStyle[i] = "solid";
	// break;
	// case ICellStyle.BORDER_MEDIUM_DASH_DOT:
	// case ICellStyle.BORDER_MEDIUM_DASH_DOT_DOT:
	// case ICellStyle.BORDER_MEDIUM_DASHED:
	// borderWidth[i] = 2;
	// borderStyle[i] = "dashed";
	// break;
	// }
	// }
	// cm.setBorderStyle(borderStyle);
	// cm.setBorderWidth(borderWidth);
	// cm.setBorderColor(cs.getBorderRGB());
	// }

	public int getWidth(ICellInfo ci, int col, int row)
	{
		IGridRegion gr;
		if ((gr = ci.getSurroundingRegion()) == null)
			return grid.getColumnWidth(col);
		int w = 0;

		gr = IGridRegion.Tool.intersect(gr, reg);
		for (int c = gr.getLeft(); c <= gr.getRight(); c++)
		{
			w += grid.getColumnWidth(c);
		}

		return w;
	}

	int getColSpan(ICellInfo ci)
	{
		IGridRegion gr = ci.getSurroundingRegion();
		if (gr == null)
			return 1;
		return IGridRegion.Tool.width(IGridRegion.Tool.intersect(reg, gr));
	}

	int getRowSpan(ICellInfo ci)
	{
		IGridRegion gr = ci.getSurroundingRegion();
		if (gr == null)
			return 1;
		return IGridRegion.Tool.height(IGridRegion.Tool.intersect(reg, gr));
	}

	// static boolean isCellShowable(ICellInfo ci)
	// {
	// if (ci.getSurroundingRegion() == null)
	// return true;
	// // TODO consider when region is not inside the table completely
	// return ci.isTopLeft();
	// }

	int width(BorderStyle bs1, BorderStyle bs2)
	{
		if (bs1 == null && bs2 == null)
			return 0;

		return bs1 == null ? bs2.width : (bs2 == null ? bs1.width : Math.max(
				bs1.width, bs2.width));
	}

	String style(BorderStyle bs1, BorderStyle bs2)
	{
		if (bs1 == null && bs2 == null)
			return "none";

		return bs1 == null ? bs2.style : (bs2 == null ? bs1.style : (bs1 == BorderStyle.NONE ? bs2.style : bs1.style));
	}

	short[] rgb(BorderStyle bs1, BorderStyle bs2)
	{
		if (bs1 == null && bs2 == null)
			return new short[] { 0, 0, 0 };

		return bs1 == null ? bs2.rgb : (bs2 == null ? bs1.rgb : (bs1 == BorderStyle.NONE ? bs2.rgb : bs1.rgb));
	}

	void setGrid(TableModel tm)
	{
		int width = IGridRegion.Tool.width(reg);

		for (int i = 0; i <= width; i++)
		{
			setVerticalBorder(i, tm);
		}

		int height = IGridRegion.Tool.height(reg);

		for (int i = 0; i <= height; i++)
		{
			setHorizontalBorder(i, tm);
		}

	}

	void setVerticalBorder(int column, TableModel tm)
	{
		int height = IGridRegion.Tool.height(reg);
		int left = reg.getLeft();
		int top = reg.getTop();

		for (int i = 0; i < height; i++)
		{
			ICellStyle ls = column + left - 1 < 0 ? null : grid.getCellInfo(column
					+ left - 1, i + top).getCellStyle();
			ICellStyle rs = column + left - 1 < 0 ? null : grid.getCellInfo(column
					+ left, i + top).getCellStyle();

			CellModel cmLeft = ls == null ? null : tm.findCellModel(column - 1, i,
					ICellStyle.RIGHT);
			CellModel cmRight = rs == null ? null : tm.findCellModel(column, i,
					ICellStyle.LEFT);

			if (cmLeft == null && cmRight == null)
				continue;

			BorderStyle lStyle = ls != null ? getBorderStyle(ls, ICellStyle.RIGHT) : null;
			BorderStyle rStyle = rs != null ? getBorderStyle(rs, ICellStyle.LEFT) : null;

			int W = width(lStyle, rStyle);
			String style = style(lStyle, rStyle);
			short[] rgb = rgb(lStyle, rStyle);

			BorderStyle bstyle = new BorderStyle(W, style, rgb);

			switch (W)
			{
			case 0:
				break;
			case 1:
				if (cmLeft == null)
				{
					cmRight.setBorderStyle(bstyle, ICellStyle.LEFT);

				} else
				{
					cmLeft.setBorderStyle(bstyle, ICellStyle.RIGHT);
				}
				break;
			case 2:
				if (cmLeft == null)
				{
					cmRight.setBorderStyle(bstyle, ICellStyle.LEFT);
				} else if (cmRight == null)
				{
					cmLeft.setBorderStyle(bstyle, ICellStyle.RIGHT);
				} else
				{
					bstyle.width = 1;
					cmRight.setBorderStyle(bstyle, ICellStyle.LEFT);
					cmLeft.setBorderStyle(bstyle, ICellStyle.RIGHT);
				}

			}
		}

	}

	void setHorizontalBorder(int row, TableModel tm)
	{
		int width = IGridRegion.Tool.width(reg);
		int left = reg.getLeft();
		int top = reg.getTop();

		for (int i = 0; i < width; i++)
		{
			ICellStyle ts = row + top - 1 < 0 ? null : grid.getCellInfo(i + left,
					row + top - 1).getCellStyle();
			ICellStyle bs = grid.getCellInfo(i + left, row + top).getCellStyle();

			CellModel cmTop = ts == null ? null : tm.findCellModel(i, row - 1,
					ICellStyle.BOTTOM);
			CellModel cmBottom = bs == null ? null : tm.findCellModel(i, row,
					ICellStyle.TOP);

			if (cmTop == null && cmBottom == null)
				continue;

			BorderStyle tStyle = ts != null ? getBorderStyle(ts, ICellStyle.BOTTOM) : null;
			BorderStyle bStyle = bs != null ? getBorderStyle(bs, ICellStyle.TOP) : null;

			int W = width(tStyle, bStyle);
			String style = style(tStyle, bStyle);
			short[] rgb = rgb(tStyle, bStyle);

			BorderStyle bstyle = new BorderStyle(W, style, rgb);

			switch (W)
			{
			case 0:
				break;
			case 1:
				if (cmTop == null)
				{
					cmBottom.setBorderStyle(bstyle, ICellStyle.TOP);

				} else
				{
					cmTop.setBorderStyle(bstyle, ICellStyle.BOTTOM);
				}
				break;
			case 2:
				if (cmTop == null)
				{
					cmBottom.setBorderStyle(bstyle, ICellStyle.TOP);
				} else if (cmBottom == null)
				{
					cmTop.setBorderStyle(bstyle, ICellStyle.BOTTOM);
				} else
				{
					bstyle.width = 1;
					cmBottom.setBorderStyle(bstyle, ICellStyle.TOP);
					cmTop.setBorderStyle(bstyle, ICellStyle.BOTTOM);
				}

			}
		}

	}

}
