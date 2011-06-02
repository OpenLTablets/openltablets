package org.openl.rules.calc;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openl.rules.BaseOpenlBuilderHelper;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;

public class SpreadsheetErrorsTest extends BaseOpenlBuilderHelper {
    
    public SpreadsheetErrorsTest() {
        super(__src);        
    }

    private static String __src = "test/rules/calc1/SpreadsheetErrorsTest.xlsx";
    
    @Test
    public void testProcessingSpreadsheetCellsAfterError() {
        TableSyntaxNode table = findTable("Spreadsheet SpreadsheetResult testSPRErrors ()");
        if (table != null) {
            Spreadsheet spr = (Spreadsheet)table.getMember();
            assertEquals(3, spr.getCells().length);
            assertEquals(2, spr.getCells()[0].length);
            assertEquals(2, spr.getCells()[1].length);
            assertEquals(2, spr.getCells()[2].length);
            
            // check that spreadsheet cells contains data, in the next row after
            // the row which contains errors.
            //
            assertNotNull(spr.getCells()[2][0].getValue());
            assertNotNull(spr.getCells()[2][1].getMethod());
        } else {
            fail();
        }
    }
    
}
