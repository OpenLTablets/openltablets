package org.openl.rules.convertor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public class String2EnumConvertorTest {

    @Test
    public void testParse() {
        String2EnumConvertor<?> converter = new String2EnumConvertor<EnumVal>(EnumVal.class);
        Enum<?> result = converter.parse("Val3", null);
        assertEquals(EnumVal.Val3, result);
    }

    @Test
    public void testFormat() {
        String2EnumConvertor<EnumVal> converter = new String2EnumConvertor<EnumVal>(EnumVal.class);
        String result = converter.format(EnumVal.Val3, null);
        assertEquals("Val3", result);
    }

    @Test
    public void testParseCaseInsensetive() {
        String2EnumConvertor<?> converter = new String2EnumConvertor<EnumVal>(EnumVal.class);
        Enum<?> result = converter.parse("vAl3", null);
        assertEquals(EnumVal.Val3, result);
    }

    @Test
    public void testParseOtherEnum() {
        String2EnumConvertor<?> converter = new String2EnumConvertor<EnumRes>(EnumRes.class);
        Enum<?> result = converter.parse("Val3", null);
        assertNotEquals(EnumVal.Val3, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNotPresent() {
        String2EnumConvertor<?> converter = new String2EnumConvertor<EnumRes>(EnumRes.class);
        converter.parse("Val4", null);
    }

    @Test
    public void testParseNull() {
        String2EnumConvertor<?> converter = new String2EnumConvertor<EnumRes>(null);
        assertNull(converter.parse(null, null));
    }

    @Test
    public void testFormatNull() {
        String2EnumConvertor<EnumRes> converter = new String2EnumConvertor<EnumRes>(null);
        assertNull(converter.format(null, null));
    }

    private enum EnumVal { VAL1, val2, Val3 }
    private enum EnumRes { RES1, res2, Val3 }
}
