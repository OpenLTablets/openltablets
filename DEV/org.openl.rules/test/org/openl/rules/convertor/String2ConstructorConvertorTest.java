package org.openl.rules.convertor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class String2ConstructorConvertorTest {

    @Test
    public void testParse() {
        String2ConstructorConvertor<Integer> converter = new String2ConstructorConvertor<Integer>(Integer.class);
        Integer result = converter.parse("123", null);
        assertEquals((Integer)123, result);
    }

    @Test
    public void testFormat() {
        String2ConstructorConvertor<?> converter = new String2ConstructorConvertor<Integer>(Integer.class);
        String result = converter.format(456, null);
        assertEquals("456", result);
    }

    @Test
    public void testParseNull() {
        String2ConstructorConvertor<?> converter = new String2ConstructorConvertor<Integer>(Integer.class);
        assertNull(converter.parse(null, null));
    }

    @Test
    public void testFormatNull() {
        String2ConstructorConvertor<?> converter = new String2ConstructorConvertor<Integer>(Integer.class);
        assertNull(converter.format(null, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNoConstructor() {
        new String2ConstructorConvertor<Object>(Object.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatNoConstructor() {
        new String2ConstructorConvertor<Object>(Object.class);
    }
}
