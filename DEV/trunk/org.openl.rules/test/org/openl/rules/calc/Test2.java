package org.openl.rules.calc;

import static junit.framework.Assert.assertEquals;
import java.io.File;

import org.junit.Test;
import org.openl.rules.TestHelper;
import org.openl.rules.calc.result.SpreadsheetResult;

public class Test2 {
    interface ITestCalc {
        SpreadsheetResult calc2();
    }

    @Test
    public void test1() {
        File xlsFile = new File("test/rules/calc1/calc2-1.xls");
        TestHelper<ITestCalc> testHelper;
        testHelper = new TestHelper<ITestCalc>(xlsFile, ITestCalc.class);

        ITestCalc test = testHelper.getInstance();
        SpreadsheetResult result = test.calc2();

        assertEquals(3, result.height());
        assertEquals(3, result.width());

        assertEquals("Col1", result.getColumnName(0));
        assertEquals(null, result.getColumnName(1));
        assertEquals("Col2", result.getColumnName(2));

        assertEquals("Row1", result.getRowName(0));
        assertEquals(null, result.getRowName(1));
        assertEquals("Row2", result.getRowName(2));

        assertEquals("A", result.getValue(0, 0).toString());
        assertEquals(null, result.getValue(0, 1));
        assertEquals("C", result.getValue(0, 2).toString());

        // TODO consider update implementation
        // nulls if no row/column has no Name is not right
        // at least let them be String(s)
        assertEquals(null, result.getValue(1, 0));
        assertEquals(null, result.getValue(1, 1));
        assertEquals(null, result.getValue(1, 2));
    }
}
