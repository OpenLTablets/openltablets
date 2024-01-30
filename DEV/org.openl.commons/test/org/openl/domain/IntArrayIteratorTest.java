package org.openl.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IntArrayIteratorTest {

    @Test
    public void testIterator() {
        int[] arr = {1, 2, 3, 5, 8, 13, 21, 34};
        IntArrayIterator it = new IntArrayIterator(arr);
        assertTrue(it.isResetable());
        assertEquals(arr.length, it.size());
        for (final int expected : arr) {
            assertTrue(it.hasNext());
            final int actual = it.nextInt();
            assertEquals(expected, actual);
        }
        assertFalse(it.hasNext());
        it.reset();
        for (final int expected : arr) {
            assertTrue(it.hasNext());
            final Integer actual = it.next();
            assertEquals((Integer) expected, actual);
        }
        assertFalse(it.hasNext());
    }

}
