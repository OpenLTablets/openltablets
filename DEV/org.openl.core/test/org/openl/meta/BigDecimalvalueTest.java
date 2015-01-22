package org.openl.meta;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

public class BigDecimalvalueTest {

    @Test
    public void testAutocastByte() {
        BigDecimalValue expectedResult = new BigDecimalValue("12");

        BigDecimalValue result = BigDecimalValue.autocast((byte) 12, null);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testAutocastShort() {
        BigDecimalValue expectedResult = new BigDecimalValue("11");

        BigDecimalValue result = BigDecimalValue.autocast((short) 11, null);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testAutocastChar() {
        char charVal = 'c';
        BigDecimalValue expectedResult = new BigDecimalValue(String.valueOf((int) charVal));

        BigDecimalValue result = BigDecimalValue.autocast(charVal, null);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testAutocastInt() {
        BigDecimalValue expectedResult = new BigDecimalValue("12");

        BigDecimalValue result = BigDecimalValue.autocast((int) 12, null);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testAutocastLong() {
        BigDecimalValue expectedResult = new BigDecimalValue("2000000000");

        Long value = Long.valueOf("2000000000");
        BigDecimalValue result = BigDecimalValue.autocast(value.longValue(), null);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testAutocastFloat() {
        BigDecimalValue expectedResult = new BigDecimalValue("12.23");

        BigDecimalValue result = BigDecimalValue.autocast((float) 12.23, null);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testAutocastDouble() {
        BigDecimalValue expectedResult = new BigDecimalValue("12.23");

        BigDecimalValue result = BigDecimalValue.autocast((double) 12.23, null);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testDivide() {
        BigDecimalValue expectedResult = new BigDecimalValue("12.24552");

        BigDecimalValue result = BigDecimalValue.divide(new BigDecimalValue("96.4947"), new BigDecimalValue("7.88"));

        assertEquals(expectedResult, result);
    }

    @Test
    public void testMin() {
        BigDecimalValue[] la = getTestArray();
        assertEquals(new BigDecimalValue("5.23"), BigDecimalValue.min(la));

        BigDecimalValue[] nullArray = null;
        try {
            assertEquals(null, BigDecimalValue.min(nullArray));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        BigDecimalValue[] emptyArray = new BigDecimalValue[0];
        try {
            assertEquals(null, BigDecimalValue.min(emptyArray));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    private BigDecimalValue[] getTestArray() {
        return new BigDecimalValue[] { new BigDecimalValue("10.24"),
                new BigDecimalValue("100.56"),
                new BigDecimalValue("5.23") };
    }

    @Test
    public void testMax() {
        BigDecimalValue[] la = getTestArray();
        assertEquals(new BigDecimalValue("100.56"), BigDecimalValue.max(la));

        BigDecimalValue[] nullArray = null;
        try {
            assertEquals(null, BigDecimalValue.max(nullArray));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        BigDecimalValue[] emptyArray = new BigDecimalValue[0];
        try {
            assertEquals(null, BigDecimalValue.max(emptyArray));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testAvg() {
        BigDecimalValue[] la = getTestArray();
        assertEquals(new BigDecimalValue("38.67667"), BigDecimalValue.avg(la));

        BigDecimalValue[] nullArray = null;
        assertEquals(null, BigDecimalValue.avg(nullArray));

        BigDecimalValue[] emptyArray = new BigDecimalValue[0];
        assertEquals(null, BigDecimalValue.avg(emptyArray));
    }

    @Test
    public void testSum() {
        BigDecimalValue[] la = getTestArray();
        assertEquals(new BigDecimalValue("116.03"), BigDecimalValue.sum(la));

        BigDecimalValue[] nullArray = null;
        assertEquals(null, BigDecimalValue.sum(nullArray));

        BigDecimalValue[] emptyArray = new BigDecimalValue[0];
        assertEquals(null, BigDecimalValue.sum(emptyArray));
    }

    @Test
    public void testProduct() {
        BigDecimalValue[] la = getTestArray();
        assertEquals(new BigDecimalValue("5385.510912"), BigDecimalValue.product(la));

        BigDecimalValue[] nullArray = null;
        assertEquals(null, BigDecimalValue.product(nullArray));

        BigDecimalValue[] emptyArray = new BigDecimalValue[0];
        assertEquals(null, BigDecimalValue.product(emptyArray));
    }

    @Test
    public void testQuaotient() {
        assertEquals(new LongValue(5),
            BigDecimalValue.quotient(new BigDecimalValue("26.77"), new BigDecimalValue("5.13")));

        BigDecimalValue nullObj = null;
        assertEquals(null, BigDecimalValue.quotient(nullObj, new BigDecimalValue("5")));

        assertEquals(null, BigDecimalValue.quotient(new BigDecimalValue("5"), nullObj));

        try {
            assertEquals(null, BigDecimalValue.quotient(new BigDecimalValue("5"), new BigDecimalValue("0")));
            fail();
        } catch (ArithmeticException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testMod() {
        assertEquals(new BigDecimalValue("2.54"),
            BigDecimalValue.mod(new BigDecimalValue("55.24"), new BigDecimalValue("3.1")));

        BigDecimalValue nullObj = null;
        assertEquals(null, BigDecimalValue.mod(nullObj, new BigDecimalValue("5")));

        assertEquals(null, BigDecimalValue.mod(new BigDecimalValue("5"), nullObj));

        try {
            assertEquals(new BigDecimalValue("0"),
                BigDecimalValue.mod(new BigDecimalValue("5"), new BigDecimalValue("0")));
            fail();
        } catch (ArithmeticException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testRounding() {

        BigDecimalValue value1 = BigDecimalValue.multiply(new BigDecimalValue("0.7"), new BigDecimalValue("0.75"));
        assertEquals("0.53", BigDecimalValue.round(value1, 2).toString());

        assertNull(BigDecimalValue.round(null));
        assertNull(BigDecimalValue.round(null, 2));
        assertNull(BigDecimalValue.round(null, 2, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    public void testAdd() {
        assertEquals("0", BigDecimalValue.add((BigDecimalValue) null, new BigDecimalValue("0")).toString());
        assertEquals("0", BigDecimalValue.add(new BigDecimalValue("0"), (BigDecimalValue) null).toString());
    }
}
