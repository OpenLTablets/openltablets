/**
 * Created Feb 15, 2007
 */
package test.writable;

import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openl.rules.lang.xls.XlsSheetSourceCodeModule;
import org.openl.rules.lang.xls.XlsWorkbookSourceCodeModule;
import org.openl.rules.table.GridSplitter;
import org.openl.rules.table.GridTable;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.actions.IUndoableGridAction;
import org.openl.rules.table.IWritableGrid;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.rules.table.xls.XlsUndoGrid;
import org.openl.syntax.impl.FileSourceCodeModule;

/**
 * @author snshor
 * 
 */
public class TestUndoable extends TestCase {

    public void testColumns() throws Exception {
        String testXls = "tst/test/writable/TestUndo.xls";

        FileSourceCodeModule source = new FileSourceCodeModule(testXls, null);

        XlsWorkbookSourceCodeModule wbSrc = new XlsWorkbookSourceCodeModule(source);

        Workbook wb = wbSrc.getWorkbook();

        Sheet sheet = wb.getSheetAt(0);
        String name = wb.getSheetName(0);
        XlsSheetSourceCodeModule sheetSrc = new XlsSheetSourceCodeModule(sheet, name, wbSrc);

        XlsSheetGridModel xsGrid = new XlsSheetGridModel(sheetSrc);

        IGridTable[] tables = new GridSplitter(xsGrid).split();
        XlsUndoGrid undoGrid = new XlsUndoGrid(xsGrid);

        IUndoableGridAction[] uaa0 = new IUndoableGridAction[tables.length];
        for (int j = 0; j < tables.length; j++) {
            uaa0[j] = IWritableGrid.Tool
                    .insertColumns(1, 2, tables[j].getRegion(), (IWritableGrid) tables[j].getGrid());
            uaa0[j].doAction(xsGrid, undoGrid);
            tables[j] = new GridTable(tables[j].getRegion().getTop(), tables[j].getRegion().getLeft(), tables[j]
                    .getRegion().getBottom(), tables[j].getRegion().getRight() + 1, tables[j].getGrid());
        }

        saveWb(wb, "wb1.xls");

        IUndoableGridAction[] uaa1 = new IUndoableGridAction[tables.length];
        for (int j = 0; j < tables.length; j++) {
            uaa1[j] = IWritableGrid.Tool.removeColumns(1, 3, tables[j].getRegion(), xsGrid);
            uaa1[j].doAction(xsGrid, undoGrid);
            tables[j] = new GridTable(tables[j].getRegion().getTop(), tables[j].getRegion().getLeft(), tables[j]
                    .getRegion().getBottom(), tables[j].getRegion().getRight() + 1, tables[j].getGrid());
        }
        saveWb(wb, "wb2.xls");
    }

    public void testRemove() throws Exception {
        String testXls = "tst/test/writable/TestUndo.xls";

        FileSourceCodeModule source = new FileSourceCodeModule(testXls, null);

        XlsWorkbookSourceCodeModule wbSrc = new XlsWorkbookSourceCodeModule(source);

        Workbook wb = wbSrc.getWorkbook();

        Sheet sheet = wb.getSheetAt(0);
        String name = wb.getSheetName(0);
        XlsSheetSourceCodeModule sheetSrc = new XlsSheetSourceCodeModule(sheet, name, wbSrc);

        XlsSheetGridModel xsGrid = new XlsSheetGridModel(sheetSrc);

        IGridTable[] tables = new GridSplitter(xsGrid).split();
        XlsUndoGrid undoGrid = new XlsUndoGrid(xsGrid);

        IUndoableGridAction[] uaa1 = new IUndoableGridAction[tables.length];
        for (int j = 0; j < tables.length; j++) {
            uaa1[j] = IWritableGrid.Tool.removeRows(1, 0, tables[j].getRegion(), xsGrid);
            uaa1[j].doAction(xsGrid, undoGrid);
            tables[j] = new GridTable(tables[j].getRegion().getTop(), tables[j].getRegion().getLeft(), tables[j]
                    .getRegion().getBottom(), tables[j].getRegion().getRight() + 1, tables[j].getGrid());
        }
        saveWb(wb, "wb1.xls");
    }

    public void testInsert() throws Exception {
        String testXls = "tst/test/writable/TestCopy.xls";

        FileSourceCodeModule source = new FileSourceCodeModule(testXls, null);

        XlsWorkbookSourceCodeModule wbSrc = new XlsWorkbookSourceCodeModule(source);

        Workbook wb = wbSrc.getWorkbook();

        Sheet sheet = wb.getSheetAt(0);
        String name = wb.getSheetName(0);
        XlsSheetSourceCodeModule sheetSrc = new XlsSheetSourceCodeModule(sheet, name, wbSrc);

        XlsSheetGridModel xsGrid = new XlsSheetGridModel(sheetSrc);

        IGridTable[] tables = new GridSplitter(xsGrid).split();
        XlsUndoGrid undoGrid = new XlsUndoGrid(xsGrid);

        IUndoableGridAction[] uaa1 = new IUndoableGridAction[tables.length];
        for (int j = 0; j < tables.length; j++) {
            uaa1[j] = IWritableGrid.Tool
                    .insertColumns(1, 1, tables[j].getRegion(), (IWritableGrid) tables[j].getGrid());
            uaa1[j].doAction(xsGrid, undoGrid);
            tables[j] = new GridTable(tables[j].getRegion().getTop(), tables[j].getRegion().getLeft(), tables[j]
                    .getRegion().getBottom(), tables[j].getRegion().getRight() + 1, tables[j].getGrid());
        }
        saveWb(wb, "wb1.xls");

        IUndoableGridAction[] uaa2 = new IUndoableGridAction[tables.length];

        for (int j = 0; j < tables.length; j++) {
            uaa2[j] = IWritableGrid.Tool.insertRows(3, 1, tables[j].getRegion(), (IWritableGrid) tables[j].getGrid());
            uaa2[j].doAction(xsGrid, undoGrid);
        }

        saveWb(wb, "wb11.xls");

        IUndoableGridAction[] uaa3 = new IUndoableGridAction[tables.length];

        for (int j = 0; j < tables.length; j++) {
            uaa3[j] = IWritableGrid.Tool.setStringValue(1, 1, tables[j], "12345", null);
            uaa3[j].doAction(xsGrid, undoGrid);
        }

        saveWb(wb, "wb12.xls");

        for (int j = 0; j < uaa3.length; j++) {
            uaa3[j].undoAction(xsGrid, undoGrid);
        }

        saveWb(wb, "wb21.xls");

        for (int j = 0; j < uaa2.length; j++) {
            uaa2[j].undoAction(xsGrid, undoGrid);
        }

        saveWb(wb, "wb22.xls");

        for (int j = 0; j < uaa1.length; j++) {
            uaa1[j].undoAction(xsGrid, undoGrid);
        }

        saveWb(wb, "wb23.xls");
        // Write the output to a file

    }

    void saveWb(Workbook wb, String name) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(name);
        wb.write(fileOut);
        fileOut.close();
    }

    public static void main(String[] args) throws Exception {
        new TestUndoable().testInsert();
    }

}
