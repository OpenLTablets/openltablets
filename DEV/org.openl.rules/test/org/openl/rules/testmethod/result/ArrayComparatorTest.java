package org.openl.rules.testmethod.result;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ArrayComparatorTest {
    @Test
    public void test() {
        ArrayComparator comparator = new ArrayComparator(Integer.class, null);

        assertTrue(comparator.isEqual(null, null));
        Integer[] intArray = new Integer[]{1, 2};
        assertFalse(comparator.isEqual(intArray, null));

        assertFalse(comparator.isEqual(null, intArray));

        assertTrue(comparator.isEqual(intArray, intArray));
    }
}
