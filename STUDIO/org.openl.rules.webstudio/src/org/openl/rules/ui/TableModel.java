package org.openl.rules.ui;

import org.openl.rules.table.ui.ICellStyle;

public class TableModel
{
	ICellModel[][] cells;

	String attributes = "cellspacing=0 cellpadding=1";

	public TableModel(int width, int height)
	{
		cells = new ICellModel[height][];
		for (int i = 0; i < cells.length; i++)
		{
			cells[i] = new ICellModel[width];
		}
	}

	public void addCell(ICellModel cm, int row, int column)
	{
		if (row < cells.length && column < cells[row].length)
			cells[row][column] = cm;
	}

	public boolean hasCell(int r, int c)
	{
		return cells[r][c] != null;
	}

	static String EMPTY = "<td width=50 style=\"border-style: dashed;border-width:1; border-color: #C0C0FF\">&nbsp;</td>";

	static String EMPTY_TR = "<td style=\"border-style: dashed dashed none none;border-width:1; border-color: #C0C0FF\">&nbsp;</td>";

	static String EMPTY_TRB = "<td width=50 style=\"border-style: dashed dashed dashed none;border-width:1; border-color: #C0C0FF\">&nbsp;</td>";

	static String EMPTY_BL = "<td width=50 style=\"border-style: none none dashed dashed;border-width:1; border-color: #C0C0FF\">&nbsp;</td>";

	static String EMPTY_RB = "<td width=50 style=\"border-style: none dashed dashed none;border-width:1; border-color: #C0C0FF\">&nbsp;</td>";

	static String EMPTY_RBL = "<td width=50 style=\"border-style: none dashed dashed dashed;border-width:1; border-color: #C0C0FF\">&nbsp;</td>";

	public void toHtmlString(StringBuffer buf, boolean showGrid)
	{
		buf.append("<table ").append(attributes).append(">\n");

		if (showGrid)
		{
			buf.append(EMPTY);

			for (int i = 0; i < cells[0].length; i++)
			{
				buf.append(EMPTY_TR);
			}
			buf.append(EMPTY_TRB);
		}

		for (int row = 0; row < cells.length; ++row)
		{
			buf.append("<tr>\n");

			if (showGrid)
				buf.append(EMPTY_BL);
			ICellModel[] rowCells = cells[row];
			for (int col = 0; col < rowCells.length; ++col)
			{
				ICellModel cm = rowCells[col];
				if (cm != null && cm.isReal())
					cm.toHtmlString(buf, this);
			}
			if (showGrid)
				buf.append(EMPTY_RB);

			buf.append("</tr>\n");
		}

		if (showGrid)
		{
			buf.append("<tr>");
			buf.append(EMPTY_RBL);
			for (int i = 0; i < cells[0].length; i++)
			{
				buf.append(EMPTY_RB);
			}

			buf.append(EMPTY_RB);
			buf.append("</tr>");
		}

		buf.append("</table>\n");
	}

	public ICellModel findOnTop(int row, int col)
	{
		if (row == 0)
			return null;
		return cells[row - 1][col];
	}

	public ICellModel findOnLeft(int row, int column)
	{
		if (column == 0)
			return null;
		return cells[row][column - 1];
	}

	public CellModel findCellModel(int col, int row, int border)
	{
		if (col < 0 || row < 0 || row >= cells.length || col >= cells[0].length)
			return null;

		ICellModel icm = cells[row][col];

		CellModel cm = null;
		switch (border)
		{
		case ICellStyle.TOP:
			if (icm instanceof CellModel)
				return (CellModel) icm;
			cm = ((CellModelDelegator) icm).getModel();
			return cm.row == row ? cm : null;
		case ICellStyle.LEFT:
			if (icm instanceof CellModel)
				return (CellModel) icm;
			cm = ((CellModelDelegator) icm).getModel();
			return cm.column == col ? cm : null;
		case ICellStyle.RIGHT:
			if (icm instanceof CellModel)
			{
				cm = (CellModel) icm;
				return cm.colspan == 1 ? cm : null;
			}
			cm = ((CellModelDelegator) icm).getModel();
			return cm.column + cm.colspan - 1 == col ? cm : null;
		case ICellStyle.BOTTOM:
			if (icm instanceof CellModel)
			{
				cm = (CellModel) icm;
				return cm.rowspan == 1 ? cm : null;
			}
			cm = ((CellModelDelegator) icm).getModel();
			return cm.row + cm.rowspan - 1 == row ? cm : null;
		default:
			throw new RuntimeException();

		}

	}

}
