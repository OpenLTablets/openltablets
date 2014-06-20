package org.openl.rules.convertor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class String2ByteConvertorTest {

    @Test
    public void testConvertPositive() {
        String2ByteConvertor converter = new String2ByteConvertor();
        Number result = converter.parse("127", null);
        assertEquals(Byte.MAX_VALUE, result);
    }

    @Test
    public void testConvertNegative() {
        String2ByteConvertor converter = new String2ByteConvertor();
        Number result = converter.parse("-128", null);
        assertEquals(Byte.MIN_VALUE, result);
    }

    @Test(expected = NumberFormatException.class)
    public void testConvertPositiveOverflow() {
        String2ByteConvertor converter = new String2ByteConvertor();
        converter.parse("128", null);
    }

    @Test(expected = NumberFormatException.class)
    public void testConvertNegativeOverflow() {
        String2ByteConvertor converter = new String2ByteConvertor();
        converter.parse("-129", null);
    }

    @Test(expected = NumberFormatException.class)
    public void testConvertNonInteger() {
        String2ByteConvertor converter = new String2ByteConvertor();
        converter.parse("1.3", null);
    }

    @Test
    public void testFormat() {
        String2ByteConvertor converter = new String2ByteConvertor();
        String result = converter.format(Byte.MIN_VALUE, null);
        assertEquals("-128", result);
    }
}
