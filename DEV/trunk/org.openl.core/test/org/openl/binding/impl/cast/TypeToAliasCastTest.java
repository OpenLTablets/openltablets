package org.openl.binding.impl.cast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;
import org.openl.domain.IDomain;
import org.openl.domain.StringDomain;
import org.openl.types.IOpenClass;
import org.openl.types.impl.DomainOpenClass;
import org.openl.types.java.JavaOpenClass;

public class TypeToAliasCastTest {
    @Test
    public void testSingle() {
        IDomain<String> strDomain = new StringDomain(new String[] { "Val1", "Val2" });
        DomainOpenClass domain = new DomainOpenClass("TestDomain", JavaOpenClass.STRING, strDomain, null);
        TypeToAliasCast cast = new TypeToAliasCast(JavaOpenClass.STRING, domain);

        Object value = cast.convert("Val1");
        assertNotNull(value);
        assertEquals("Val1", value);

        assertNull(cast.convert(null));

        try {
            cast.convert("Not Existing");
            fail("Should be exception");
        } catch (RuntimeException e) {
            assertEquals(e.getMessage(), "Object Not Existing is outside of a valid domain");
        }
    }

    @Test
    public void testArray() {
        String[] strArray = new String[] { "Val1", "Val2" };

        IDomain<String> strDomain = new StringDomain(strArray);
        DomainOpenClass domain = new DomainOpenClass("TestDomain", JavaOpenClass.STRING, strDomain, null);
        IOpenClass arrayDomain = domain.getAggregateInfo().getIndexedAggregateType(domain, 1);
        TypeToAliasCast cast = new TypeToAliasCast(JavaOpenClass.STRING, arrayDomain);

        Object[] value = (Object[]) cast.convert(strArray);
        assertNotNull(value);
        assertEquals(strArray.length, value.length);
        assertTrue(Arrays.deepEquals(strArray, value));

        assertNull(cast.convert(null));
    }

}
