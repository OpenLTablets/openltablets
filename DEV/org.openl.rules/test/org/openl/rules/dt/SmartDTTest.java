package org.openl.rules.dt;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openl.rules.TestUtils;

public class SmartDTTest {
    private static Object instance;

    @BeforeClass
    public static void init() {
        instance = TestUtils.create("test/rules/dt/SmartDTTest.xls");
    }

    @Test
    public void test1() {
        assertEquals(Double.valueOf(0.02),
            TestUtils.invoke(instance, "test1", new Class[] { int.class, String.class }, new Object[] { 2, "v2" }));
        assertEquals(Double.valueOf(0.05),
            TestUtils.invoke(instance, "test1", new Class[] { int.class, String.class }, new Object[] { 5, "v5" }));
    }

    @Test
    public void test2() {
        assertEquals(Double.valueOf(0.02),
            TestUtils.invoke(instance, "test2", new Class[] { String.class, int.class }, new Object[] { "v2", 2 }));
        assertEquals(Double.valueOf(0.05),
            TestUtils.invoke(instance, "test2", new Class[] { String.class, int.class }, new Object[] { "v5", 5 }));
    }

    @Test
    public void testLookup2D2params() {
        assertEquals(Double.valueOf(0.01),
            TestUtils.invoke(instance,
                "simple2D2params",
                new Class[] { int.class, String.class },
                new Object[] { 1, "v1" }));
        assertEquals(Double.valueOf(0.09),
            TestUtils.invoke(instance,
                "simple2D2params",
                new Class[] { int.class, String.class },
                new Object[] { 3, "v2" }));
        assertEquals(Double.valueOf(0.17),
            TestUtils.invoke(instance,
                "simple2D2params",
                new Class[] { int.class, String.class },
                new Object[] { 5, "v3" }));
    }

    @Test
    public void testLookup2D3params() {
        assertEquals(Double.valueOf(0.01),
            TestUtils.invoke(instance,
                "simple2D3params",
                new Class[] { int.class, String.class, String.class },
                new Object[] { 1, "v1", "v1" }));
        assertEquals(Double.valueOf(0.08),
            TestUtils.invoke(instance,
                "simple2D3params",
                new Class[] { int.class, String.class, String.class },
                new Object[] { 2, "v1", "v2" }));
        assertEquals(Double.valueOf(0.15),
            TestUtils.invoke(instance,
                "simple2D3params",
                new Class[] { int.class, String.class, String.class },
                new Object[] { 3, "v2", "v1" }));
        assertEquals(Double.valueOf(0.22),
            TestUtils.invoke(instance,
                "simple2D3params",
                new Class[] { int.class, String.class, String.class },
                new Object[] { 4, "v2", "v2" }));
    }
}
