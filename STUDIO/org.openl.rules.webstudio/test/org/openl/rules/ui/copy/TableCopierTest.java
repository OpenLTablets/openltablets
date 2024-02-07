package org.openl.rules.ui.copy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TableCopierTest {

    @Test
    public void testIsEmpty() {
        assertTrue(TableCopier.isEmpty(null));
        assertTrue(TableCopier.isEmpty(""));
        assertTrue(TableCopier.isEmpty(new Object[0]));
        assertFalse(TableCopier.isEmpty(12.0));
        assertFalse(TableCopier.isEmpty(new int[]{1, 2}));
    }

}
