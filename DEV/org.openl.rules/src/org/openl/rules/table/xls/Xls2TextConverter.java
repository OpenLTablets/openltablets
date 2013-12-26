package org.openl.rules.table.xls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.poi.ss.usermodel.Workbook;
import org.openl.rules.lang.xls.XlsSheetSourceCodeModule;
import org.openl.rules.lang.xls.XlsWorkbookSourceCodeModule;
import org.openl.rules.table.IGrid;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.IGridTable;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.source.impl.FileSourceCodeModule;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.java.JavaOpenClass;

public class Xls2TextConverter {

    boolean printRowStart;

    boolean printRowEnd;

    boolean printEmptyCells;
    
    public static void main(String[] args) throws Exception {

        if (!new File(args[0]).exists()) {
            throw new FileNotFoundException(args[1]);
        }
        
		// FIXME .xls & .xlsx
        if (!args[0].endsWith(".xls")) {
            throw new RuntimeException("The first argument must be an .xls file");
        }
        Xls2TextConverter conv = new Xls2TextConverter();

        for (int i = 2; i < args.length; i++) {
            processArg(args[i], conv);
        }

        conv.convert(args[0], args[1]);
    }
    
    private static void processArg(String string, Xls2TextConverter conv) {
        IOpenClass ioc = JavaOpenClass.getOpenClass(conv.getClass());

        IOpenField f = ioc.getField(string);
        f.set(conv, true, null);
    }

    public void convert(IOpenSourceCodeModule source, PrintWriter out) throws Exception {
        XlsWorkbookSourceCodeModule srcIndex = new XlsWorkbookSourceCodeModule(source);
        Workbook wb = srcIndex.getWorkbook();

        int nsheets = wb.getNumberOfSheets();

        for (int i = 0; i < nsheets; i++) {
            String sheetName = wb.getSheetName(i);

            XlsSheetSourceCodeModule sheetSource = new XlsSheetSourceCodeModule(i, srcIndex);

            XlsSheetGridModel xlsGrid = new XlsSheetGridModel(sheetSource);

            printSheetName(sheetName, out);

            IGridTable[] tables = xlsGrid.getTables();

            for (IGridTable table : tables) {
                printTable(table, out);

            }

        }

    }

    public void convert(String xlsFromFile, String textToFile) throws Exception {
        convert(new FileSourceCodeModule(new File(xlsFromFile), null), new PrintWriter(new FileWriter(textToFile)));
    }

    public boolean isPrintEmptyCells() {
        return printEmptyCells;
    }

    public boolean isPrintRowEnd() {
        return printRowEnd;
    }

    public boolean isPrintRowStart() {
        return printRowStart;
    }

    private void printCell(String stringCellValue, PrintWriter out) {
        out.println(stringCellValue);
    }

    private void printEmptyCell(PrintWriter out) {
        if (printEmptyCells) {
            out.println("---");
        }
    }

    private void printRowEnd(int y, PrintWriter out) {
        if (printRowEnd) {
            out.println("END ROW=" + y);
        }

    }

    private void printRowStart(int y, PrintWriter out) {
        if (printRowStart) {
            out.println("START ROW=" + y);
        }
    }

    private void printSheetName(String sheetName, PrintWriter out) {
        out.println(sheetName);
    }

    private void printTable(IGridTable gridTable, PrintWriter out) {
        IGridRegion reg = gridTable.getRegion();
        IGrid grid = gridTable.getGrid();

        for (int y = reg.getTop(); y <= reg.getBottom(); y++) {
            printRowStart(y, out);
            for (int x = reg.getLeft(); x <= reg.getRight(); x++) {
                if (grid.isEmpty(x, y)) {
                    printEmptyCell(out);
                } else {
                    printCell(grid.getCell(x, y).getStringValue(), out);
                }
            }

            printRowEnd(y, out);

        }
    }

    public void setPrintEmptyCells(boolean printEmptyCells) {
        this.printEmptyCells = printEmptyCells;
    }

    public void setPrintRowEnd(boolean prinRowEnd) {
        printRowEnd = prinRowEnd;
    }

    public void setPrintRowStart(boolean printRowStart) {
        this.printRowStart = printRowStart;
    }

}
