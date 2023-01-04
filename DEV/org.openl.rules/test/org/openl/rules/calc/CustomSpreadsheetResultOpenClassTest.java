package org.openl.rules.calc;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.openl.types.NullOpenClass;
import org.openl.types.java.JavaOpenClass;

public class CustomSpreadsheetResultOpenClassTest {
    @Test
    public void test() {
        CustomSpreadsheetResultOpenClass openClass = new CustomSpreadsheetResultOpenClass("CSR1",
            null,
            null,
            true,
            true);
        openClass.getField("$f1", true);
        openClass.addField(new CustomSpreadsheetResultField(null, "$f1", JavaOpenClass.OBJECT));
        openClass.addField(new CustomSpreadsheetResultField(null, "$F1", JavaOpenClass.OBJECT));
    }

    @Test
    public void testIsAssignableFromNullOpenClass() {
        CustomSpreadsheetResultOpenClass openClass = new CustomSpreadsheetResultOpenClass("CSR1",
            null,
            null,
            true,
            true);
        assertFalse(openClass.isAssignableFrom(NullOpenClass.the));
    }
}
