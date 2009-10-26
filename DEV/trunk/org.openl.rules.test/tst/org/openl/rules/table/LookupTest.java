package org.openl.rules.table;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openl.rules.lang.xls.XlsSheetSourceCodeModule;
import org.openl.rules.lang.xls.XlsWorkbookSourceCodeModule;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.syntax.impl.FileSourceCodeModule;

public class LookupTest extends TestCase
{
	public void testMergeBounds() throws Exception
	{
		String testXls = "tst/xls/TestLookup.xls";

		FileSourceCodeModule source = new FileSourceCodeModule(testXls, null);

		
		XlsWorkbookSourceCodeModule wbsrc = new XlsWorkbookSourceCodeModule(source);

		Workbook wb = wbsrc.getWorkbook();
		
		int nsheets = wb.getNumberOfSheets();

		for (int i = 0; i < nsheets; i++)
		{
			Sheet sheet = wb.getSheetAt(i);
			String name = wb.getSheetName(i);
			
			XlsSheetSourceCodeModule sheetSrc = new XlsSheetSourceCodeModule(sheet, name, wbsrc);
			XlsSheetGridModel xsGrid =
				new XlsSheetGridModel(sheetSrc);

			IGridTable[] tables = new GridSplitter(xsGrid).split();

			if (name.equals("Sheet1"))
			{
				_testSheet1(tables);
			}

		}

	}
	
	
	void _testSheet1(IGridTable[] tables)
	{
		Assert.assertEquals(2, tables.length);

		ILogicalTable lookupTable = LogicalTable.logicalTable(tables[0]);

		ILogicalTable t1 = lookupTable.rows(1);
		
		ILogicalTable lookupRow1 = t1.getLogicalRow(0);
		
		ILogicalTable t2 = t1.rows(1);
		
		ILogicalTable lookupColumn1 = t2.getLogicalColumn(0);

		
		ILogicalTable body = LogicalTable.mergeBounds(lookupColumn1, lookupRow1);
		
		Assert.assertEquals(5, body.getLogicalHeight());
		Assert.assertEquals(3, body.getLogicalWidth());
		
		Assert.assertEquals("1", body.getLogicalRegion(0, 0, 1, 1).getGridTable().getCell(0, 0).getStringValue());
		Assert.assertEquals("7", body.getLogicalRegion(0, 2, 1, 1).getGridTable().getCell(0, 0).getStringValue());
		Assert.assertEquals("15", body.getLogicalRegion(2, 4, 1, 1).getGridTable().getCell(0, 0).getStringValue());
	}
	
	
	

	public static void main(String[] args)
	{
	}

}
