package org.openl.util.math;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import org.openl.util.BooleanUtils;

/**
 * @author DLiauchuk TODO: test all methods.
 */
public class MathUtilsTest {
    @Test
    public void testAvgByte() {
        byte[] byteArray = getTestByteArray();
        assertEquals((byte) 5, MathUtils.avg(byteArray));

        byte[] nullArray = null;
        assertEquals((byte) 0, MathUtils.avg(nullArray));

        byte[] emptyArray = new byte[1];
        assertEquals((byte) 0, MathUtils.avg(emptyArray));
    }

    @Test
    public void testAvgShort() {
        short[] shortArray = new short[] { 2, 3, 4 };
        assertEquals((short) 3, MathUtils.avg(shortArray));

        short[] nullArray = null;
        assertEquals((short) 0, MathUtils.avg(nullArray));

        short[] emptyArray = new short[1];
        assertEquals((short) 0, MathUtils.avg(emptyArray));
    }

    @Test
    public void testAvgBigInteger() {
        BigInteger[] bigIntegerArray = new BigInteger[] { BigInteger.valueOf(2), BigInteger.valueOf(3),
                BigInteger.valueOf(4) };
        assertEquals(BigInteger.valueOf(3), MathUtils.avg(bigIntegerArray));

        BigInteger[] nullArray = null;
        assertEquals(BigInteger.valueOf(0), MathUtils.avg(nullArray));

        BigInteger[] emptyArray = new BigInteger[1];
        assertEquals(BigInteger.valueOf(0), MathUtils.avg(emptyArray));
    }

    @Test
    public void testMedianByte() {
        byte[] byteArray = getTestByteArray();
        assertEquals(5, MathUtils.median(byteArray));
    }

    @Test
    public void testMedianFloat() {
        float[] floatArray = new float[] { (float) 10.5, (float) 7.2 };
        assertEquals((float) 8.85, (float) MathUtils.median(floatArray), 0.01);
    }

    @Test
    public void testProductByte() {
        byte[] byteArray = new byte[] { 3, 4 };
        assertEquals(12, MathUtils.product(byteArray), 0.01);
    }

    @Test
    public void testProductDouble() {
        double[] doubleArray = new double[] { 3, 4, 4.5, -6.78 };
        assertEquals(-366.12, MathUtils.product(doubleArray), 0.01);
    }

    @Test
    public void testQuaotientDouble() {
        assertEquals(1, MathUtils.quotient(3.22, 1.75));
    }

    @Test
    public void testQuaotientBigDecimal() {
        assertEquals(1, MathUtils.quotient(BigDecimal.valueOf(3.22), BigDecimal.valueOf(1.75)));

        BigDecimal nullObj = null;
        assertEquals(0, MathUtils.quotient(nullObj, nullObj));

        try {
            assertEquals(0, MathUtils.quotient(BigDecimal.valueOf(3.22), BigDecimal.valueOf(0)));
            fail();
        } catch (ArithmeticException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testModByte() {
        assertEquals(6, MathUtils.mod((byte) 19, (byte) 13));
        assertEquals(-7, MathUtils.mod((byte) 19, (byte) -13));
        assertEquals(7, MathUtils.mod((byte) -19, (byte) 13));
        assertEquals(-6, MathUtils.mod((byte) -19, (byte) -13));
        assertEquals(0, MathUtils.mod((byte) 19, (byte) 19));
        assertEquals(0, MathUtils.mod((byte) 0, (byte) 19));
    }

    @Test
    public void testModFloat() {
        assertEquals(1.47, MathUtils.mod((float) 3.22, (float) 1.75), 0.01);
    }

    @Test
    public void testModDouble() {
        assertEquals(1.47, MathUtils.mod(3.22, 1.75), 0.01);
    }

    @Test
    public void testModBigDecimal() {
        assertEquals(BigDecimal.valueOf(1.47), MathUtils.mod(BigDecimal.valueOf(3.22), BigDecimal.valueOf(1.75)));
        assertEquals(BigDecimal.valueOf(0), MathUtils.mod(null, BigDecimal.valueOf(1.75)));
    }

    @Test
    public void testSmallInt() {
        int[] intMas = new int[] { 10, 17, 13, 44, 1 };
        assertEquals(1, MathUtils.small(intMas, 1));
        assertEquals(10, MathUtils.small(intMas, 2));
        assertEquals(13, MathUtils.small(intMas, 3));
        assertEquals(44, MathUtils.small(intMas, 5));
        try {
            assertEquals(0, MathUtils.small(intMas, 6));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("There is no position '6' in the given array", e.getMessage());
        }

        intMas = null;
        try {
            assertEquals(0, MathUtils.small(intMas, 5));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("The array cannot be null", e.getMessage());
        }

        intMas = new int[1];
        try {
            assertEquals(0, MathUtils.small(intMas, 5));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("There is no position '5' in the given array", e.getMessage());
        }
    }

    @Test
    public void testBigForInt() {
        int[] mas = new int[] { 10, 45, 4, 44, 22 };
        assertEquals(45, MathUtils.big(mas, 1));
        assertEquals(44, MathUtils.big(mas, 2));
        assertEquals(22, MathUtils.big(mas, 3));
        assertEquals(10, MathUtils.big(mas, 4));
        assertEquals(4, MathUtils.big(mas, 5));

        try {
            assertEquals(0, MathUtils.big(mas, 6));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("There is no position '6' in the given array", e.getMessage());
        }

        mas = null;
        try {
            assertEquals(0, MathUtils.big(mas, 5));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("The array cannot be null", e.getMessage());
        }

        mas = new int[1];
        try {
            assertEquals(0, MathUtils.big(mas, 5));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("There is no position '5' in the given array", e.getMessage());
        }
    }

    @Test
    public void testBigForLong() {
        Long[] mas = new Long[] { Long.valueOf(10), Long.valueOf(45), Long.valueOf(4), Long.valueOf(44),
                Long.valueOf(22) };
        assertEquals(Long.valueOf(45), MathUtils.big(mas, 1));
        assertEquals(Long.valueOf(44), MathUtils.big(mas, 2));
        assertEquals(Long.valueOf(22), MathUtils.big(mas, 3));
        assertEquals(Long.valueOf(10), MathUtils.big(mas, 4));
        assertEquals(Long.valueOf(4), MathUtils.big(mas, 5));

        try {
            assertEquals(Long.valueOf(0), MathUtils.big(mas, 6));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("There is no position '6' in the given array", e.getMessage());
        }

        mas = null;
        try {
            assertEquals(Long.valueOf(0), MathUtils.big(mas, 5));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("The array cannot be null", e.getMessage());
        }

        mas = new Long[1];
        try {
            assertEquals(Long.valueOf(0), MathUtils.big(mas, 5));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("There is no position '5' in the given array", e.getMessage());
        }
    }

    @Test
    public void testAnd() {
        boolean[] boolAr = null;
        assertFalse(BooleanUtils.and(boolAr));

        boolAr = new boolean[] { true, false, true };
        assertFalse(BooleanUtils.and(boolAr));

        boolAr = new boolean[] { true, true, true };
        assertTrue(BooleanUtils.and(boolAr));
    }

    @Test
    public void testEqNaN() {
        assertTrue(MathUtils.eq(Float.NaN, Float.NaN));
        assertTrue(MathUtils.eq(Double.NaN, Double.NaN));

        assertFalse(MathUtils.eq(Float.NaN, 1.1f));
        assertFalse(MathUtils.eq(Double.NaN, 1.1d));
        assertFalse(MathUtils.eq(1.1f, Float.NaN));
        assertFalse(MathUtils.eq(1.1d, Double.NaN));

        assertFalse(MathUtils.eq(Float.NaN, Float.NEGATIVE_INFINITY));
        assertFalse(MathUtils.eq(Double.NaN, Double.NEGATIVE_INFINITY));
        assertFalse(MathUtils.eq(Float.NaN, Float.POSITIVE_INFINITY));
        assertFalse(MathUtils.eq(Double.NaN, Double.POSITIVE_INFINITY));
    }

    @Test
    public void testEqInfinity() {
        assertTrue(MathUtils.eq(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
        assertTrue(MathUtils.eq(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));

        assertFalse(MathUtils.eq(Float.NEGATIVE_INFINITY, 1.1f));
        assertFalse(MathUtils.eq(Double.NEGATIVE_INFINITY, 1.1d));
        assertFalse(MathUtils.eq(1.1f, Float.NEGATIVE_INFINITY));
        assertFalse(MathUtils.eq(1.1d, Double.NEGATIVE_INFINITY));

        assertTrue(MathUtils.eq(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
        assertTrue(MathUtils.eq(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));

        assertFalse(MathUtils.eq(Float.POSITIVE_INFINITY, 1.1f));
        assertFalse(MathUtils.eq(Double.POSITIVE_INFINITY, 1.1d));
        assertFalse(MathUtils.eq(1.1f, Float.POSITIVE_INFINITY));
        assertFalse(MathUtils.eq(1.1d, Double.POSITIVE_INFINITY));

        // positive & negative
        assertFalse(MathUtils.eq(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
        assertFalse(MathUtils.eq(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
    }

    @Test
    public void testGtNaN() {
        assertFalse(MathUtils.gt(Float.NaN, Float.NaN));
        assertFalse(MathUtils.gt(Double.NaN, Double.NaN));

        assertFalse(MathUtils.gt(Float.NaN, 1.1f));
        assertFalse(MathUtils.gt(Double.NaN, 1.1d));
        assertFalse(MathUtils.gt(1.1f, Float.NaN));
        assertFalse(MathUtils.gt(1.1d, Double.NaN));
    }

    @Test
    public void testGtInfinity() {
        assertFalse(MathUtils.gt(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
        assertFalse(MathUtils.gt(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));

        assertFalse(MathUtils.gt(Float.NEGATIVE_INFINITY, 1.1f));
        assertFalse(MathUtils.gt(Double.NEGATIVE_INFINITY, 1.1d));
        assertTrue(MathUtils.gt(1.1f, Float.NEGATIVE_INFINITY));
        assertTrue(MathUtils.gt(1.1d, Double.NEGATIVE_INFINITY));

        assertFalse(MathUtils.gt(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
        assertFalse(MathUtils.gt(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));

        assertTrue(MathUtils.gt(Float.POSITIVE_INFINITY, 1.1f));
        assertTrue(MathUtils.gt(Double.POSITIVE_INFINITY, 1.1d));
        assertFalse(MathUtils.gt(1.1f, Float.POSITIVE_INFINITY));
        assertFalse(MathUtils.gt(1.1d, Double.POSITIVE_INFINITY));

        // positive & negative
        assertFalse(MathUtils.gt(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
        assertFalse(MathUtils.gt(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertTrue(MathUtils.gt(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY));
        assertTrue(MathUtils.gt(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));

        // NaN
        assertFalse(MathUtils.gt(Float.NaN, Float.NEGATIVE_INFINITY));
        assertFalse(MathUtils.gt(Double.NaN, Double.NEGATIVE_INFINITY));
        assertFalse(MathUtils.gt(Float.NaN, Float.POSITIVE_INFINITY));
        assertFalse(MathUtils.gt(Double.NaN, Double.POSITIVE_INFINITY));
    }

    @Test
    public void testLtInfinity() {
        assertFalse(MathUtils.lt(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
        assertFalse(MathUtils.lt(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));

        assertTrue(MathUtils.lt(Float.NEGATIVE_INFINITY, 1.1f));
        assertTrue(MathUtils.lt(Double.NEGATIVE_INFINITY, 1.1d));
        assertFalse(MathUtils.lt(1.1f, Float.NEGATIVE_INFINITY));
        assertFalse(MathUtils.lt(1.1d, Double.NEGATIVE_INFINITY));

        assertFalse(MathUtils.lt(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
        assertFalse(MathUtils.lt(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));

        assertFalse(MathUtils.lt(Float.POSITIVE_INFINITY, 1.1f));
        assertFalse(MathUtils.lt(Double.POSITIVE_INFINITY, 1.1d));
        assertTrue(MathUtils.lt(1.1f, Float.POSITIVE_INFINITY));
        assertTrue(MathUtils.lt(1.1d, Double.POSITIVE_INFINITY));

        // positive & negative
        assertTrue(MathUtils.lt(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
        assertTrue(MathUtils.lt(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertFalse(MathUtils.lt(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY));
        assertFalse(MathUtils.lt(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));

        // NaN
        assertFalse(MathUtils.lt(Float.NaN, Float.NEGATIVE_INFINITY));
        assertFalse(MathUtils.lt(Double.NaN, Double.NEGATIVE_INFINITY));
        assertFalse(MathUtils.lt(Float.NaN, Float.POSITIVE_INFINITY));
        assertFalse(MathUtils.lt(Double.NaN, Double.POSITIVE_INFINITY));
    }

    private byte[] getTestByteArray() {
        return new byte[] { 3, 5, 8 };
    }
}
