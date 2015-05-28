package org.openl.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openl.exception.OpenlNotCheckedException;

public class IntValueTest {
    
    @Test
    public void testEquals() {
        IntValue value1 = new IntValue(10000);
        IntValue value2 = new IntValue(10000);
        assertEquals(value1, value2);
        value2.setMetaInfo(value1.getMetaInfo());
        assertEquals(value1, value2);
    }
    
    @Test
    public void testSubstract() {
    	IntValue v1 = new IntValue(12);
    	IntValue v2 = null;    	
    	assertEquals(12, IntValue.subtract(v1, v2).intValue());
    	
    	v1 = null;
    	v2 = new IntValue(15);
    	assertEquals(-15, IntValue.subtract(v1, v2).intValue());
    	
    	v1 = null;
    	v2 = null;
    	assertNull(IntValue.subtract(v1, v2));
    }
    
    @Test
    public void testAdd() {
    	IntValue v1 = new IntValue(123);
    	IntValue v2 = null;    	
    	assertEquals(123, IntValue.add(v1, v2).intValue());

    	v1 = null;
    	v2 = new IntValue(1578);
    	assertEquals(1578, IntValue.add(v1, v2).intValue());
    	
    	v1 = null;
    	v2 = null;
    	assertNull(IntValue.add(v1, v2));

        assertEquals("0", IntValue.add((IntValue) null, new IntValue(0)).toString());
        assertEquals("0", IntValue.add(new IntValue(0), (IntValue) null).toString());
    }
    
    @Test
    public void testMultiply() {
    	IntValue v1 = new IntValue(123);
    	IntValue v2 = null; 
    	assertEquals(123, IntValue.multiply(v1, v2).intValue());
    	
    	v1 = null;
    	v2 = new IntValue(12);
    	assertEquals(12, IntValue.multiply(v1, v2).intValue());
    	
    	v1 = null;
    	v2 = null;
    	assertNull(IntValue.multiply(v1, v2));
    }
    
    @Test
    public void testDivide() {
        IntValue value1 = new IntValue(10000);
        IntValue value2 = new IntValue(10000);
        assertEquals(1, IntValue.divide(value1, value2).getValue());
        
        value2 = new IntValue(0);
        try {
            assertEquals(1, IntValue.divide(value1, value2));
            fail();
        } catch (OpenlNotCheckedException e) {
            assertTrue(true);
        }
        
        IntValue val1 = new IntValue(10);
        IntValue val2 = null;
        assertEquals(10, IntValue.divide(val1, val2).intValue());
        
        val1 = null;
        val2 = new IntValue(10);
        assertEquals(0, IntValue.divide(val1, val2).intValue());
        
        val1 = null;
        val2 = null;
        assertEquals(null, IntValue.divide(val1, val2));
    }
    
    @Test
    public void testBig() {
        IntValue[] mas = new IntValue[]{new IntValue(5), new IntValue(47), new IntValue(34), new IntValue(44), 
                new IntValue(11)};
        assertEquals(new IntValue(47), IntValue.big(mas, 1));
        assertEquals(new IntValue(44), IntValue.big(mas, 2));
        assertEquals(new IntValue(34), IntValue.big(mas, 3));
        assertEquals(new IntValue(11), IntValue.big(mas, 4));
        assertEquals(new IntValue(5), IntValue.big(mas, 5));
        
        try {
            assertEquals(0, IntValue.big(mas, 6));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("There is no position '6' in the given array", e.getMessage());
        }
        
        mas = null;
        assertEquals(null, IntValue.big(mas, 5));
        
        mas = new IntValue[1];
        try {
            assertEquals(0, IntValue.big(mas, 5));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("There is no position '5' in the given array", e.getMessage());
        }
    }

    @Test
    public void testEq () {
        IntValue v = new IntValue(1);
        IntValue v2 = new IntValue(1);
        assertEquals(v, v2);
    }

    @Test
    public void testCopy () {
        IntValue v = new IntValue(1);
        IntValue copied = IntValue.copy(v, "v2");

        assertEquals("v2", copied.getName());
        assertEquals(v.getValue(), copied.getValue());

    }

    @Test
    public void testRem () {
        IntValue v = new IntValue (1);
        IntValue v2 = new IntValue (2);
        IntValue rem = IntValue.rem(v, v2);

        assertEquals(1, rem.getValue());
    }

}
