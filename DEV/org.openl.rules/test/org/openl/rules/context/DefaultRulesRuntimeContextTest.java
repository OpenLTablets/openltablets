package org.openl.rules.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Date;

import org.junit.jupiter.api.Test;

public class DefaultRulesRuntimeContextTest {

    @Test
    public void testClone() throws CloneNotSupportedException {
        final Date requestDate = new Date();
        final Date currentDate = new Date();
        final String lob = "UL";

        final DefaultRulesRuntimeContext original = new DefaultRulesRuntimeContext();
        original.setCurrentDate(currentDate);
        original.setRequestDate(requestDate);

        final DefaultRulesRuntimeContext cloned = (DefaultRulesRuntimeContext) original.clone();
        assertNotSame(original, cloned);
        assertSame(currentDate, original.getCurrentDate());
        assertSame(currentDate, original.getValue("currentDate"));
        assertSame(currentDate, cloned.getCurrentDate());
        assertSame(currentDate, cloned.getValue("currentDate"));
        assertSame(requestDate, original.getRequestDate());
        assertSame(requestDate, original.getValue("requestDate"));
        assertSame(requestDate, cloned.getRequestDate());
        assertSame(requestDate, cloned.getValue("requestDate"));
        assertEquals(original.toString(), cloned.toString());

        original.setLob(lob);
        assertSame(lob, original.getLob());
        assertSame(lob, original.getValue("lob"));
        assertNotEquals(original.toString(), cloned.toString());
        assertNull(cloned.getLob());
        assertNull(cloned.getValue("lob"));
    }

}
