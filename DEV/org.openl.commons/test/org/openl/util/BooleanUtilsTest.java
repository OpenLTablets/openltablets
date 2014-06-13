package org.openl.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class BooleanUtilsTest {

    @Test
    public void testObjectToTrue() {
        assertTrue(BooleanUtils.toBooleanObject("y"));
        assertTrue(BooleanUtils.toBooleanObject("Y"));
        assertTrue(BooleanUtils.toBooleanObject("t"));
        assertTrue(BooleanUtils.toBooleanObject("T"));
        assertTrue(BooleanUtils.toBooleanObject("On"));
        assertTrue(BooleanUtils.toBooleanObject("on"));
        assertTrue(BooleanUtils.toBooleanObject("Yes"));
        assertTrue(BooleanUtils.toBooleanObject("yEs"));
        assertTrue(BooleanUtils.toBooleanObject("tRUe"));
        assertTrue(BooleanUtils.toBooleanObject("TruE"));
        assertTrue(BooleanUtils.toBooleanObject(1));
        assertTrue(BooleanUtils.toBooleanObject(987));
        assertTrue(BooleanUtils.toBooleanObject(-1));
        assertTrue(BooleanUtils.toBooleanObject(true));
    }

    @Test
    public void testObjectToFalse() {
        assertFalse(BooleanUtils.toBooleanObject("n"));
        assertFalse(BooleanUtils.toBooleanObject("N"));
        assertFalse(BooleanUtils.toBooleanObject("f"));
        assertFalse(BooleanUtils.toBooleanObject("F"));
        assertFalse(BooleanUtils.toBooleanObject("no"));
        assertFalse(BooleanUtils.toBooleanObject("nO"));
        assertFalse(BooleanUtils.toBooleanObject("ofF"));
        assertFalse(BooleanUtils.toBooleanObject("Off"));
        assertFalse(BooleanUtils.toBooleanObject("FalSE"));
        assertFalse(BooleanUtils.toBooleanObject("falSe"));
        assertFalse(BooleanUtils.toBooleanObject(0));
        assertFalse(BooleanUtils.toBooleanObject(false));
    }

    @Test
    public void testObjectToNull() {
        assertNull(BooleanUtils.toBooleanObject(null));
        assertNull(BooleanUtils.toBooleanObject(""));
        assertNull(BooleanUtils.toBooleanObject("Not"));
        assertNull(BooleanUtils.toBooleanObject("yet"));
        assertNull(BooleanUtils.toBooleanObject("fake"));
        assertNull(BooleanUtils.toBooleanObject(1.1));
        assertNull(BooleanUtils.toBooleanObject(1L));
    }

    @Test
    public void testNullToFalse() {
        assertFalse(BooleanUtils.toBoolean(null));
        assertFalse(BooleanUtils.toBoolean(""));
        assertFalse(BooleanUtils.toBoolean("Not"));
        assertFalse(BooleanUtils.toBoolean("yet"));
        assertFalse(BooleanUtils.toBoolean("fake"));
        assertFalse(BooleanUtils.toBoolean(1.1));
        assertFalse(BooleanUtils.toBoolean(1L));
    }

    @Test
    public void testAnd() {
        //True
        assertTrue(BooleanUtils.and(new boolean[]{true}));
        assertTrue(BooleanUtils.and(new boolean[]{true, true}));    //3
        assertTrue(BooleanUtils.and(new boolean[]{true, true, true}));     //7
        //False
        assertFalse(BooleanUtils.and(new boolean[]{false}));
        assertFalse(BooleanUtils.and(new boolean[]{false, false})); //0
        assertFalse(BooleanUtils.and(new boolean[]{false, true}));  //1
        assertFalse(BooleanUtils.and(new boolean[]{true, false}));  //2
        assertFalse(BooleanUtils.and(new boolean[]{false, false, false})); //0
        assertFalse(BooleanUtils.and(new boolean[]{false, false, true}));  //1
        assertFalse(BooleanUtils.and(new boolean[]{false, true, false}));  //2
        assertFalse(BooleanUtils.and(new boolean[]{false, true, true}));   //3
        assertFalse(BooleanUtils.and(new boolean[]{true, false, false}));  //4
        assertFalse(BooleanUtils.and(new boolean[]{true, false, true}));   //5
        assertFalse(BooleanUtils.and(new boolean[]{true, true, false}));   //6
        //Empty
        assertTrue(BooleanUtils.and(new boolean[]{}));
        //Null
        assertFalse(BooleanUtils.and((boolean[])null));
    }

    @Test
    public void testAndObjectBoolean() {
        //True
        assertTrue(BooleanUtils.and(new Boolean[]{true}));
        assertTrue(BooleanUtils.and(new Boolean[]{true, true}));    //3
        assertTrue(BooleanUtils.and(new Boolean[]{true, true, true}));     //7
        //False
        assertFalse(BooleanUtils.and(new Boolean[]{false}));
        assertFalse(BooleanUtils.and(new Boolean[]{false, false})); //0
        assertFalse(BooleanUtils.and(new Boolean[]{false, true}));  //1
        assertFalse(BooleanUtils.and(new Boolean[]{true, false}));  //2
        assertFalse(BooleanUtils.and(new Boolean[]{false, false, false})); //0
        assertFalse(BooleanUtils.and(new Boolean[]{false, false, true}));  //1
        assertFalse(BooleanUtils.and(new Boolean[]{false, true, false}));  //2
        assertFalse(BooleanUtils.and(new Boolean[]{false, true, true}));   //3
        assertFalse(BooleanUtils.and(new Boolean[]{true, false, false}));  //4
        assertFalse(BooleanUtils.and(new Boolean[]{true, false, true}));   //5
        assertFalse(BooleanUtils.and(new Boolean[]{true, true, false}));   //6
        //Empty
        assertTrue(BooleanUtils.and(new Boolean[]{}));
        //Null
        assertFalse(BooleanUtils.and((Boolean[])null));
        //null like false
        assertFalse(BooleanUtils.and(new Boolean[]{null}));
        assertFalse(BooleanUtils.and(new Boolean[]{true, null}));
        assertFalse(BooleanUtils.and(new Boolean[]{null, true}));
        assertFalse(BooleanUtils.and(new Boolean[]{null, null}));
        assertFalse(BooleanUtils.and(new Boolean[]{true, true, null}));
        assertFalse(BooleanUtils.and(new Boolean[]{null, false, true}));
    }

    @Test
    public void testOr() {
        //True
        assertTrue(BooleanUtils.or(new boolean[]{true}));
        assertTrue(BooleanUtils.or(new boolean[]{false, true}));   //1
        assertTrue(BooleanUtils.or(new boolean[]{true, false}));   //2
        assertTrue(BooleanUtils.or(new boolean[]{true, true}));    //3
        assertTrue(BooleanUtils.or(new boolean[]{false, false, true}));   //1
        assertTrue(BooleanUtils.or(new boolean[]{false, true, false}));   //2
        assertTrue(BooleanUtils.or(new boolean[]{false, true, true}));    //3
        assertTrue(BooleanUtils.or(new boolean[]{true, false, false}));   //4
        assertTrue(BooleanUtils.or(new boolean[]{true, false, true}));    //5
        assertTrue(BooleanUtils.or(new boolean[]{true, true, false}));    //6
        assertTrue(BooleanUtils.or(new boolean[]{true, true, true}));     //7
        //False
        assertFalse(BooleanUtils.or(new boolean[]{false}));
        assertFalse(BooleanUtils.or(new boolean[]{false, false})); //0
        assertFalse(BooleanUtils.or(new boolean[]{false, false, false})); //0
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOrEmpty() {
        BooleanUtils.or(new boolean[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOrNull() {
        BooleanUtils.or((boolean[])null);
    }

    @Test
    public void testOrObjectBoolean() {
        //True
        assertTrue(BooleanUtils.or(new Boolean[]{true}));
        assertTrue(BooleanUtils.or(new Boolean[]{false, true}));   //1
        assertTrue(BooleanUtils.or(new Boolean[]{true, false}));   //2
        assertTrue(BooleanUtils.or(new Boolean[]{true, true}));    //3
        assertTrue(BooleanUtils.or(new Boolean[]{false, false, true}));   //1
        assertTrue(BooleanUtils.or(new Boolean[]{false, true, false}));   //2
        assertTrue(BooleanUtils.or(new Boolean[]{false, true, true}));    //3
        assertTrue(BooleanUtils.or(new Boolean[]{true, false, false}));   //4
        assertTrue(BooleanUtils.or(new Boolean[]{true, false, true}));    //5
        assertTrue(BooleanUtils.or(new Boolean[]{true, true, false}));    //6
        assertTrue(BooleanUtils.or(new Boolean[]{true, true, true}));     //7
        //False
        assertFalse(BooleanUtils.or(new Boolean[]{false}));
        assertFalse(BooleanUtils.or(new Boolean[]{false, false})); //0
        assertFalse(BooleanUtils.or(new Boolean[]{false, false, false})); //0
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOrObjectBooleanEmpty() {
        BooleanUtils.or(new Boolean[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOrObjectBooleanNull() {
        BooleanUtils.or((Boolean[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOrObjectBooleanNullValue() {
        BooleanUtils.or(new Boolean[]{true, true, null});
    }

    @Test
    public void testXor() {
        //True
        assertTrue(BooleanUtils.xor(new boolean[]{true}));
        assertTrue(BooleanUtils.xor(new boolean[]{false, true}));   //1
        assertTrue(BooleanUtils.xor(new boolean[]{true, false}));   //2
        assertTrue(BooleanUtils.xor(new boolean[]{false, false, true}));   //1
        assertTrue(BooleanUtils.xor(new boolean[]{false, true, false}));   //2
        assertTrue(BooleanUtils.xor(new boolean[]{true, false, false}));   //4
        assertTrue(BooleanUtils.xor(new boolean[]{true, true, true}));     //7
        //False
        assertFalse(BooleanUtils.xor(new boolean[]{false}));
        assertFalse(BooleanUtils.xor(new boolean[]{false, false})); //0
        assertFalse(BooleanUtils.xor(new boolean[]{true, true}));   //3
        assertFalse(BooleanUtils.xor(new boolean[]{false, false, false})); //0
        assertFalse(BooleanUtils.xor(new boolean[]{false, true, true}));   //3
        assertFalse(BooleanUtils.xor(new boolean[]{true, false, true}));   //5
        assertFalse(BooleanUtils.xor(new boolean[]{true, true, false}));   //6
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXorEmpty() {
        BooleanUtils.xor(new boolean[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXorNull() {
        BooleanUtils.xor((boolean[])null);
    }

    @Test
    public void testXorObjectBoolean() {
        //True
        assertTrue(BooleanUtils.xor(new Boolean[]{true}));
        assertTrue(BooleanUtils.xor(new Boolean[]{false, true}));   //1
        assertTrue(BooleanUtils.xor(new Boolean[]{true, false}));   //2
        assertTrue(BooleanUtils.xor(new Boolean[]{false, false, true}));   //1
        assertTrue(BooleanUtils.xor(new Boolean[]{false, true, false}));   //2
        assertTrue(BooleanUtils.xor(new Boolean[]{true, false, false}));   //4
        assertTrue(BooleanUtils.xor(new Boolean[]{true, true, true}));     //7
        //False
        assertFalse(BooleanUtils.xor(new Boolean[]{false}));
        assertFalse(BooleanUtils.xor(new Boolean[]{false, false})); //0
        assertFalse(BooleanUtils.xor(new Boolean[]{true, true}));   //3
        assertFalse(BooleanUtils.xor(new Boolean[]{false, false, false})); //0
        assertFalse(BooleanUtils.xor(new Boolean[]{false, true, true}));   //3
        assertFalse(BooleanUtils.xor(new Boolean[]{true, false, true}));   //5
        assertFalse(BooleanUtils.xor(new Boolean[]{true, true, false}));   //6
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXorObjectBooleanEmpty() {
        BooleanUtils.xor(new Boolean[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXorObjectBooleanNull() {
        BooleanUtils.xor((Boolean[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXorObjectBooleanNullValue() {
        BooleanUtils.xor(new Boolean[]{true, true, null});
    }
}
