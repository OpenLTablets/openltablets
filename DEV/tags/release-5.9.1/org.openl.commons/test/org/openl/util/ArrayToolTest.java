package org.openl.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class ArrayToolTest {
	
	@SuppressWarnings("deprecation")
	@Test
	public void testRemoveNulls() {
		assertNull(ArrayTool.removeNulls(null));
		Object[] emptyArray = new Object[0];
		
		assertEquals(emptyArray, ArrayTool.removeNulls(emptyArray));
		
		Integer value1 = Integer.valueOf(12);
		Double value2 = Double.valueOf(11);
		Object[] array = new Object[]{value1, null, value2};
		
		Object[] expected = new Object[]{value1, value2};
		
		Object[] actual = ArrayTool.removeNulls(array);
		assertEquals(2, actual.length);
		assertEquals(expected, actual);
		
		
		Object[] allNulls = new Object[]{null, null, null, null};
		Object[] actual1 = ArrayTool.removeNulls(allNulls);
		assertEquals(0, actual1.length);
	}
}
