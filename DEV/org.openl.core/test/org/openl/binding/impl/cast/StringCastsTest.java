package org.openl.binding.impl.cast;

import org.junit.Before;
import org.junit.Test;
import org.openl.binding.impl.Operators;
import org.openl.meta.*;
import org.openl.types.java.JavaOpenClass;

import java.math.BigDecimal;
import java.math.BigInteger;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Created by dl on 5/4/15.
 */
public class StringCastsTest {

    private CastFactory factory;

    @Before
    public void before() {
        factory  = new CastFactory();
        factory.setMethodFactory(JavaOpenClass.getOpenClass(Operators.class));
    }

    @Test
    public void testStringToByte() {
        javaCastTest(String.class, byte.class);
        javaCastTest(String.class, Byte.class);
    }

    @Test
    public void testStringToShort() {
        javaCastTest(String.class, short.class);
        javaCastTest(String.class, Short.class);
    }

    @Test
    public void testStringToInt() {
        javaCastTest(String.class, int.class);
        javaCastTest(String.class, Integer.class);
    }

    @Test
    public void testStringToFloat() {
        javaCastTest(String.class, float.class);
        javaCastTest(String.class, Float.class);
    }

    @Test
    public void testStringToDouble() {
        javaCastTest(String.class, double.class);
        javaCastTest(String.class, Double.class);
    }

    @Test
    public void testStringToBigInt() {
        javaCastTest(String.class, BigInteger.class);
    }

    @Test
    public void testStringToBigDecimal() {
        javaCastTest(String.class, BigDecimal.class);
    }

    @Test
    public void testStringToByteValue() {
        javaCastTest(String.class, ByteValue.class);
    }

    @Test
    public void testStringToShortValue() {
        javaCastTest(String.class, ShortValue.class);
    }

    @Test
    public void testStringToIntValue() {
        javaCastTest(String.class, IntValue.class);
    }

    @Test
    public void testStringToFloatValue() {
        javaCastTest(String.class, FloatValue.class);
    }

    @Test
    public void testStringToDoubleValue() {
        javaCastTest(String.class, DoubleValue.class);
    }

    @Test
    public void testStringToBigIntValue() {
        javaCastTest(String.class, BigIntegerValue.class);
    }

    @Test
    public void testStringToBigDecimalValue() {
        javaCastTest(String.class, BigDecimalValue.class);
    }

    void javaCastTest(Class<?> from, Class<?> to) {
        IOpenCast cast = factory.getCast(JavaOpenClass.getOpenClass(from), JavaOpenClass.getOpenClass(to));
        assertNotNull(cast);
        assertTrue(cast.isImplicit());
    }

}
