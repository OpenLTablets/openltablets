package org.openl.rules.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

public class BooleansTest {

    @Test
    public void testAnd() {
        // Empty
        assertNull(Booleans.and(new boolean[]{}));
        assertNull(Booleans.and((boolean[]) null));

        // True
        assertTrue(Booleans.and(new boolean[]{true}));
        assertTrue(Booleans.and(new boolean[]{true, true}));
        assertTrue(Booleans.and(new boolean[]{true, true, true}));

        // False
        assertFalse(Booleans.and(new boolean[]{false}));
        assertFalse(Booleans.and(new boolean[]{false, false}));
        assertFalse(Booleans.and(new boolean[]{false, false, false}));

        // True and False
        assertFalse(Booleans.and(new boolean[]{false, true}));
        assertFalse(Booleans.and(new boolean[]{true, false}));
        assertFalse(Booleans.and(new boolean[]{false, false, true}));
        assertFalse(Booleans.and(new boolean[]{false, true, false}));
        assertFalse(Booleans.and(new boolean[]{false, true, true}));
        assertFalse(Booleans.and(new boolean[]{true, false, false}));
        assertFalse(Booleans.and(new boolean[]{true, false, true}));
        assertFalse(Booleans.and(new boolean[]{true, true, false}));
    }

    @Test
    public void testAnd2() {
        // Empty
        assertNull(Booleans.and(new Boolean[]{}));
        assertNull(Booleans.and((Boolean[]) null));
        assertNull(Booleans.and(new Boolean[]{null}));
        assertNull(Booleans.and(new Boolean[]{null, null}));

        // True
        assertTrue(Booleans.and(new Boolean[]{true}));
        assertTrue(Booleans.and(new Boolean[]{true, true}));
        assertTrue(Booleans.and(new Boolean[]{true, true, true}));

        // False
        assertFalse(Booleans.and(new Boolean[]{false}));
        assertFalse(Booleans.and(new Boolean[]{false, false}));
        assertFalse(Booleans.and(new Boolean[]{false, false, false}));

        // True and False
        assertFalse(Booleans.and(new Boolean[]{false, true}));
        assertFalse(Booleans.and(new Boolean[]{true, false}));
        assertFalse(Booleans.and(new Boolean[]{false, false, true}));
        assertFalse(Booleans.and(new Boolean[]{false, true, false}));
        assertFalse(Booleans.and(new Boolean[]{false, true, true}));
        assertFalse(Booleans.and(new Boolean[]{true, false, false}));
        assertFalse(Booleans.and(new Boolean[]{true, false, true}));
        assertFalse(Booleans.and(new Boolean[]{true, true, false}));

        // Null and True
        assertNull(Booleans.and(new Boolean[]{null, true}));
        assertNull(Booleans.and(new Boolean[]{true, null}));
        assertNull(Booleans.and(new Boolean[]{null, null, true}));
        assertNull(Booleans.and(new Boolean[]{null, true, null}));
        assertNull(Booleans.and(new Boolean[]{null, true, true}));
        assertNull(Booleans.and(new Boolean[]{true, null, null}));
        assertNull(Booleans.and(new Boolean[]{true, null, true}));
        assertNull(Booleans.and(new Boolean[]{true, true, null}));

        // Null and False
        assertFalse(Booleans.and(new Boolean[]{null, false}));
        assertFalse(Booleans.and(new Boolean[]{false, null}));
        assertFalse(Booleans.and(new Boolean[]{null, null, false}));
        assertFalse(Booleans.and(new Boolean[]{null, false, null}));
        assertFalse(Booleans.and(new Boolean[]{null, false, false}));
        assertFalse(Booleans.and(new Boolean[]{false, null, null}));
        assertFalse(Booleans.and(new Boolean[]{false, null, false}));
        assertFalse(Booleans.and(new Boolean[]{false, false, null}));

        // Null and True and False
        assertFalse(Booleans.and(new Boolean[]{null, true, false}));
        assertFalse(Booleans.and(new Boolean[]{null, false, true}));
        assertFalse(Booleans.and(new Boolean[]{true, null, false}));
        assertFalse(Booleans.and(new Boolean[]{true, false, null}));
        assertFalse(Booleans.and(new Boolean[]{false, null, true}));
        assertFalse(Booleans.and(new Boolean[]{false, true, null}));
    }

    @Test
    public void testOr() {
        // Empty
        assertNull(Booleans.or(new boolean[]{}));
        assertNull(Booleans.or((boolean[]) null));

        // True
        assertTrue(Booleans.or(new boolean[]{true}));
        assertTrue(Booleans.or(new boolean[]{true, true}));
        assertTrue(Booleans.or(new boolean[]{true, true, true}));

        // False
        assertFalse(Booleans.or(new boolean[]{false}));
        assertFalse(Booleans.or(new boolean[]{false, false}));
        assertFalse(Booleans.or(new boolean[]{false, false, false}));

        // True and False
        assertTrue(Booleans.or(new boolean[]{false, true}));
        assertTrue(Booleans.or(new boolean[]{true, false}));
        assertTrue(Booleans.or(new boolean[]{false, false, true}));
        assertTrue(Booleans.or(new boolean[]{false, true, false}));
        assertTrue(Booleans.or(new boolean[]{false, true, true}));
        assertTrue(Booleans.or(new boolean[]{true, false, false}));
        assertTrue(Booleans.or(new boolean[]{true, false, true}));
        assertTrue(Booleans.or(new boolean[]{true, true, false}));
    }

    @Test
    public void testOr2() {
        // Empty
        assertNull(Booleans.or(new Boolean[]{}));
        assertNull(Booleans.or((Boolean[]) null));
        assertNull(Booleans.or(new Boolean[]{null}));
        assertNull(Booleans.or(new Boolean[]{null, null}));

        // True
        assertTrue(Booleans.or(new Boolean[]{true}));
        assertTrue(Booleans.or(new Boolean[]{true, true}));
        assertTrue(Booleans.or(new Boolean[]{true, true, true}));

        // False
        assertFalse(Booleans.or(new Boolean[]{false}));
        assertFalse(Booleans.or(new Boolean[]{false, false}));
        assertFalse(Booleans.or(new Boolean[]{false, false, false}));

        // True and False
        assertTrue(Booleans.or(new Boolean[]{false, true}));
        assertTrue(Booleans.or(new Boolean[]{true, false}));
        assertTrue(Booleans.or(new Boolean[]{false, false, true}));
        assertTrue(Booleans.or(new Boolean[]{false, true, false}));
        assertTrue(Booleans.or(new Boolean[]{false, true, true}));
        assertTrue(Booleans.or(new Boolean[]{true, false, false}));
        assertTrue(Booleans.or(new Boolean[]{true, false, true}));
        assertTrue(Booleans.or(new Boolean[]{true, true, false}));

        // Null and True
        assertTrue(Booleans.or(new Boolean[]{null, true}));
        assertTrue(Booleans.or(new Boolean[]{true, null}));
        assertTrue(Booleans.or(new Boolean[]{null, null, true}));
        assertTrue(Booleans.or(new Boolean[]{null, true, null}));
        assertTrue(Booleans.or(new Boolean[]{null, true, true}));
        assertTrue(Booleans.or(new Boolean[]{true, null, null}));
        assertTrue(Booleans.or(new Boolean[]{true, null, true}));
        assertTrue(Booleans.or(new Boolean[]{true, true, null}));

        // Null and False
        assertNull(Booleans.or(new Boolean[]{null, false}));
        assertNull(Booleans.or(new Boolean[]{false, null}));
        assertNull(Booleans.or(new Boolean[]{null, null, false}));
        assertNull(Booleans.or(new Boolean[]{null, false, null}));
        assertNull(Booleans.or(new Boolean[]{null, false, false}));
        assertNull(Booleans.or(new Boolean[]{false, null, null}));
        assertNull(Booleans.or(new Boolean[]{false, null, false}));
        assertNull(Booleans.or(new Boolean[]{false, false, null}));

        // Null and True and False
        assertTrue(Booleans.or(new Boolean[]{null, true, false}));
        assertTrue(Booleans.or(new Boolean[]{null, false, true}));
        assertTrue(Booleans.or(new Boolean[]{true, null, false}));
        assertTrue(Booleans.or(new Boolean[]{true, false, null}));
        assertTrue(Booleans.or(new Boolean[]{false, null, true}));
        assertTrue(Booleans.or(new Boolean[]{false, true, null}));
    }

    @Test
    public void testAllTrue() {
        // Empty
        assertFalse(Booleans.allTrue(new boolean[]{}));
        assertFalse(Booleans.allTrue((boolean[]) null));
        assertFalse(Booleans.allTrue(new Boolean[]{}));
        assertFalse(Booleans.allTrue((Boolean[]) null));
        assertFalse(Booleans.allTrue(new Boolean[]{null}));
        assertFalse(Booleans.allTrue(new Boolean[]{null, null}));

        // primitive
        assertFalse(Booleans.allTrue(new boolean[]{false}));
        assertTrue(Booleans.allTrue(new boolean[]{true}));
        assertFalse(Booleans.allTrue(new boolean[]{false, false}));
        assertFalse(Booleans.allTrue(new boolean[]{false, true}));
        assertFalse(Booleans.allTrue(new boolean[]{true, false}));
        assertTrue(Booleans.allTrue(new boolean[]{true, true}));

        // Object type
        assertFalse(Booleans.allTrue(new Boolean[]{false}));
        assertTrue(Booleans.allTrue(new Boolean[]{true}));
        assertFalse(Booleans.allTrue(new Boolean[]{false, false}));
        assertFalse(Booleans.allTrue(new Boolean[]{false, true}));
        assertFalse(Booleans.allTrue(new Boolean[]{true, false}));
        assertTrue(Booleans.allTrue(new Boolean[]{true, true}));

        // Null
        assertFalse(Booleans.allTrue(new Boolean[]{null, false}));
        assertFalse(Booleans.allTrue(new Boolean[]{null, true}));
        assertFalse(Booleans.allTrue(new Boolean[]{false, null}));
        assertFalse(Booleans.allTrue(new Boolean[]{true, null}));
    }

    @Test
    public void testAllFalse() {
        // Empty
        assertFalse(Booleans.allFalse(new boolean[]{}));
        assertFalse(Booleans.allFalse((boolean[]) null));
        assertFalse(Booleans.allFalse(new Boolean[]{}));
        assertFalse(Booleans.allFalse((Boolean[]) null));
        assertFalse(Booleans.allFalse(new Boolean[]{null}));
        assertFalse(Booleans.allFalse(new Boolean[]{null, null}));

        // primitive
        assertTrue(Booleans.allFalse(new boolean[]{false}));
        assertFalse(Booleans.allFalse(new boolean[]{true}));
        assertTrue(Booleans.allFalse(new boolean[]{false, false}));
        assertFalse(Booleans.allFalse(new boolean[]{false, true}));
        assertFalse(Booleans.allFalse(new boolean[]{true, false}));
        assertFalse(Booleans.allFalse(new boolean[]{true, true}));

        // Object type
        assertTrue(Booleans.allFalse(new Boolean[]{false}));
        assertFalse(Booleans.allFalse(new Boolean[]{true}));
        assertTrue(Booleans.allFalse(new Boolean[]{false, false}));
        assertFalse(Booleans.allFalse(new Boolean[]{false, true}));
        assertFalse(Booleans.allFalse(new Boolean[]{true, false}));
        assertFalse(Booleans.allFalse(new Boolean[]{true, true}));

        // Null
        assertFalse(Booleans.allFalse(new Boolean[]{null, false}));
        assertFalse(Booleans.allFalse(new Boolean[]{null, true}));
        assertFalse(Booleans.allFalse(new Boolean[]{false, null}));
        assertFalse(Booleans.allFalse(new Boolean[]{true, null}));
    }

    @Test
    public void testAnyTrue() {
        // Empty
        assertFalse(Booleans.anyTrue(new boolean[]{}));
        assertFalse(Booleans.anyTrue((boolean[]) null));
        assertFalse(Booleans.anyTrue(new Boolean[]{}));
        assertFalse(Booleans.anyTrue((Boolean[]) null));
        assertFalse(Booleans.anyTrue(new Boolean[]{null}));
        assertFalse(Booleans.anyTrue(new Boolean[]{null, null}));

        // primitive
        assertFalse(Booleans.anyTrue(new boolean[]{false}));
        assertTrue(Booleans.anyTrue(new boolean[]{true}));
        assertFalse(Booleans.anyTrue(new boolean[]{false, false}));
        assertTrue(Booleans.anyTrue(new boolean[]{false, true}));
        assertTrue(Booleans.anyTrue(new boolean[]{true, false}));
        assertTrue(Booleans.anyTrue(new boolean[]{true, true}));

        // Object type
        assertFalse(Booleans.anyTrue(new Boolean[]{false}));
        assertTrue(Booleans.anyTrue(new Boolean[]{true}));
        assertFalse(Booleans.anyTrue(new Boolean[]{false, false}));
        assertTrue(Booleans.anyTrue(new Boolean[]{false, true}));
        assertTrue(Booleans.anyTrue(new Boolean[]{true, false}));
        assertTrue(Booleans.anyTrue(new Boolean[]{true, true}));

        // Null
        assertFalse(Booleans.anyTrue(new Boolean[]{null, false}));
        assertTrue(Booleans.anyTrue(new Boolean[]{null, true}));
        assertFalse(Booleans.anyTrue(new Boolean[]{false, null}));
        assertTrue(Booleans.anyTrue(new Boolean[]{true, null}));
    }

    @Test
    public void testAnyFalse() {
        // Empty
        assertFalse(Booleans.anyFalse(new boolean[]{}));
        assertFalse(Booleans.anyFalse((boolean[]) null));
        assertFalse(Booleans.anyFalse(new Boolean[]{}));
        assertFalse(Booleans.anyFalse((Boolean[]) null));
        assertFalse(Booleans.anyFalse(new Boolean[]{null}));
        assertFalse(Booleans.anyFalse(new Boolean[]{null, null}));

        // primitive
        assertTrue(Booleans.anyFalse(new boolean[]{false}));
        assertFalse(Booleans.anyFalse(new boolean[]{true}));
        assertTrue(Booleans.anyFalse(new boolean[]{false, false}));
        assertTrue(Booleans.anyFalse(new boolean[]{false, true}));
        assertTrue(Booleans.anyFalse(new boolean[]{true, false}));
        assertFalse(Booleans.anyFalse(new boolean[]{true, true}));

        // Object type
        assertTrue(Booleans.anyFalse(new Boolean[]{false}));
        assertFalse(Booleans.anyFalse(new Boolean[]{true}));
        assertTrue(Booleans.anyFalse(new Boolean[]{false, false}));
        assertTrue(Booleans.anyFalse(new Boolean[]{false, true}));
        assertTrue(Booleans.anyFalse(new Boolean[]{true, false}));
        assertFalse(Booleans.anyFalse(new Boolean[]{true, true}));

        // Null
        assertTrue(Booleans.anyFalse(new Boolean[]{null, false}));
        assertFalse(Booleans.anyFalse(new Boolean[]{null, true}));
        assertTrue(Booleans.anyFalse(new Boolean[]{false, null}));
        assertFalse(Booleans.anyFalse(new Boolean[]{true, null}));
    }

    @Test
    public void toBooleanTest() {
        //null
        assertNull(Booleans.toBoolean((Byte) null));
        assertNull(Booleans.toBoolean((Short) null));
        assertNull(Booleans.toBoolean((Integer) null));
        assertNull(Booleans.toBoolean((Long) null));
        assertNull(Booleans.toBoolean((Character) null));
        assertNull(Booleans.toBoolean((String) null));
        assertNull(Booleans.toBoolean((BigInteger) null));
        assertNull(Booleans.toBoolean(""));
        assertNull(Booleans.toBoolean("foo"));
        assertNull(Booleans.toBoolean("y "));
        assertNull(Booleans.toBoolean("false "));
        assertNull(Booleans.toBoolean((Character) 'b'));
        assertNull(Booleans.toBoolean('b'));
        assertNull(Booleans.toBoolean((byte) 10));
        assertNull(Booleans.toBoolean((short) 10));
        assertNull(Booleans.toBoolean(10));
        assertNull(Booleans.toBoolean(10L));
        assertNull(Booleans.toBoolean((Byte) (byte) 10));
        assertNull(Booleans.toBoolean((Short) (short) 10));
        assertNull(Booleans.toBoolean((Integer) 10));
        assertNull(Booleans.toBoolean((Long) 10L));
        assertNull(Booleans.toBoolean(new BigInteger("10")));
        assertNull(Booleans.toBoolean((byte) -10));
        assertNull(Booleans.toBoolean((short) -10));
        assertNull(Booleans.toBoolean(-10));
        assertNull(Booleans.toBoolean(-10L));
        assertNull(Booleans.toBoolean((Byte) (byte) -10));
        assertNull(Booleans.toBoolean((Short) (short) -10));
        assertNull(Booleans.toBoolean((Integer) (-10)));
        assertNull(Booleans.toBoolean((Long) (-10L)));
        assertNull(Booleans.toBoolean(new BigInteger("-10")));

        //TRUE
        assertEquals(Boolean.TRUE, Booleans.toBoolean("y"));
        assertEquals(Boolean.TRUE, Booleans.toBoolean("Y"));
        assertEquals(Boolean.TRUE, Booleans.toBoolean("1"));
        assertEquals(Boolean.TRUE, Booleans.toBoolean("yes"));
        assertEquals(Boolean.TRUE, Booleans.toBoolean("YES"));
        assertEquals(Boolean.TRUE, Booleans.toBoolean("yEs"));
        assertEquals(Boolean.TRUE, Booleans.toBoolean("true"));
        assertEquals(Boolean.TRUE, Booleans.toBoolean("TRUE"));
        assertEquals(Boolean.TRUE, Booleans.toBoolean("TrUe"));
        assertEquals(Boolean.TRUE, Booleans.toBoolean("on"));
        assertEquals(Boolean.TRUE, Booleans.toBoolean("ON"));
        assertEquals(Boolean.TRUE, Booleans.toBoolean("On"));
        assertEquals(Boolean.TRUE, Booleans.toBoolean((byte) 1));
        assertEquals(Boolean.TRUE, Booleans.toBoolean((short) 1));
        assertEquals(Boolean.TRUE, Booleans.toBoolean(1));
        assertEquals(Boolean.TRUE, Booleans.toBoolean(1L));
        assertEquals(Boolean.TRUE, Booleans.toBoolean('1'));
        assertEquals(Boolean.TRUE, Booleans.toBoolean((Byte) (byte) 1));
        assertEquals(Boolean.TRUE, Booleans.toBoolean((Short) (short) 1));
        assertEquals(Boolean.TRUE, Booleans.toBoolean((Integer) 1));
        assertEquals(Boolean.TRUE, Booleans.toBoolean((Long) 1L));
        assertEquals(Boolean.TRUE, Booleans.toBoolean(new BigInteger("1")));
        assertEquals(Boolean.TRUE, Booleans.toBoolean('1'));
        assertEquals(Boolean.TRUE, Booleans.toBoolean('Y'));
        assertEquals(Boolean.TRUE, Booleans.toBoolean('y'));
        assertEquals(Boolean.TRUE, Booleans.toBoolean((Character) '1'));
        assertEquals(Boolean.TRUE, Booleans.toBoolean((Character) 'y'));
        assertEquals(Boolean.TRUE, Booleans.toBoolean((Character) 'Y'));

        //FALSE
        assertEquals(Boolean.FALSE, Booleans.toBoolean("n"));
        assertEquals(Boolean.FALSE, Booleans.toBoolean("N"));
        assertEquals(Boolean.FALSE, Booleans.toBoolean("0"));
        assertEquals(Boolean.FALSE, Booleans.toBoolean("no"));
        assertEquals(Boolean.FALSE, Booleans.toBoolean("NO"));
        assertEquals(Boolean.FALSE, Booleans.toBoolean("nO"));
        assertEquals(Boolean.FALSE, Booleans.toBoolean("false"));
        assertEquals(Boolean.FALSE, Booleans.toBoolean("FALSE"));
        assertEquals(Boolean.FALSE, Booleans.toBoolean("fAlSe"));
        assertEquals(Boolean.FALSE, Booleans.toBoolean("off"));
        assertEquals(Boolean.FALSE, Booleans.toBoolean("OFF"));
        assertEquals(Boolean.FALSE, Booleans.toBoolean("oFf"));
        assertEquals(Boolean.FALSE, Booleans.toBoolean((byte) 0));
        assertEquals(Boolean.FALSE, Booleans.toBoolean((short) 0));
        assertEquals(Boolean.FALSE, Booleans.toBoolean(0));
        assertEquals(Boolean.FALSE, Booleans.toBoolean(0L));
        assertEquals(Boolean.FALSE, Booleans.toBoolean('0'));
        assertEquals(Boolean.FALSE, Booleans.toBoolean((Byte) (byte) 0));
        assertEquals(Boolean.FALSE, Booleans.toBoolean((Short) (short) 0));
        assertEquals(Boolean.FALSE, Booleans.toBoolean((Integer) 0));
        assertEquals(Boolean.FALSE, Booleans.toBoolean((Long) 0L));
        assertEquals(Boolean.FALSE, Booleans.toBoolean(new BigInteger("0")));
        assertEquals(Boolean.FALSE, Booleans.toBoolean('0'));
        assertEquals(Boolean.FALSE, Booleans.toBoolean('n'));
        assertEquals(Boolean.FALSE, Booleans.toBoolean('N'));
        assertEquals(Boolean.FALSE, Booleans.toBoolean((Character) '0'));
        assertEquals(Boolean.FALSE, Booleans.toBoolean((Character) 'n'));
        assertEquals(Boolean.FALSE, Booleans.toBoolean((Character) 'N'));
    }
}
