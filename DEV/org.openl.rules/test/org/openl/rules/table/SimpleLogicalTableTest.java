package org.openl.rules.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.openl.rules.lang.xls.XlsSheetSourceCodeModule;
import org.openl.rules.lang.xls.XlsWorkbookSourceCodeModule;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.source.impl.URLSourceCodeModule;

public class SimpleLogicalTableTest {

    private XlsSheetSourceCodeModule getXlsGrid() {
        URLSourceCodeModule source = new URLSourceCodeModule("./test/rules/SimpleLogicalTableTest.xls");
        XlsWorkbookSourceCodeModule wbSrc = new XlsWorkbookSourceCodeModule(source);

        return new XlsSheetSourceCodeModule(0, wbSrc);
    }

    @Test
    public void testSimpleLogicalTable() throws Exception {

        XlsSheetSourceCodeModule sheetSrc = getXlsGrid();

        XlsSheetGridModel xsGrid = new XlsSheetGridModel(sheetSrc);

        IGridTable[] tables = xsGrid.getTables();

        assertEquals(2, xsGrid.getNumberOfMergedRegions());
        assertEquals(1, tables.length);

        ILogicalTable logicalTable = LogicalTableHelper.logicalTable(tables[0]);

        assertTrue(logicalTable instanceof SimpleLogicalTable);

        assertEquals(5, logicalTable.getWidth());

        ILogicalTable subTable = logicalTable.getSubtable(0, 1, logicalTable.getWidth(), logicalTable.getHeight() - 1);

        assertTrue(subTable instanceof SimpleLogicalTable);

        // this is not correct behaviour for ILogicalTable see SimpleLogicalTable docs for info
        assertEquals(5, subTable.getWidth());
    }

}
