package org.openl.rules.dt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.openl.rules.TestUtils;

public class SimpleDTRangeAndArrayTest {
    private static Object instance;

    @BeforeAll
    public static void init() {
        instance = TestUtils.create("test/rules/dt/SimpleDTRangeAndArray.xlsx");
    }

    @Test
    public void testArray1() {
        assertEquals(0, (int) TestUtils.invoke(instance, "TestArray1", "1234"));
        assertEquals(5, (int) TestUtils.invoke(instance, "TestArray1", "-3"));
        assertEquals(0, (int) TestUtils.invoke(instance, "TestArray1", "erty"));
    }

    @Test
    public void testArray2() {
        assertEquals(1, (int) TestUtils.invoke(instance, "TestArray2", "1234"));
        assertEquals(12, (int) TestUtils.invoke(instance, "TestArray2", "werwe"));
        assertEquals(5, (int) TestUtils.invoke(instance, "TestArray2", "asda"));
    }

    @Test
    public void testRangeInt() {
        assertEquals(-10,
                (int) TestUtils.invoke(instance, "TestRangeInt", new Class[]{int.class}, new Object[]{67}));
        assertEquals(89,
                (int) TestUtils.invoke(instance, "TestRangeInt", new Class[]{int.class}, new Object[]{99}));
        assertEquals(78,
                (int) TestUtils.invoke(instance, "TestRangeInt", new Class[]{int.class}, new Object[]{3}));
    }

    @Test
    public void testRangeDouble1() {
        assertEquals(-10,
                (int) TestUtils
                        .invoke(instance, "TestRangeDouble1", new Class[]{double.class}, new Object[]{150.005}));
        assertEquals(85,
                (int) TestUtils.invoke(instance, "TestRangeDouble1", new Class[]{double.class}, new Object[]{0.0}));
        assertEquals(78,
                (int) TestUtils
                        .invoke(instance, "TestRangeDouble1", new Class[]{double.class}, new Object[]{6000000.0}));
    }

    @Test
    public void simpleLookup2Range() {
        assertEquals(Double.valueOf(0.0), TestUtils.invoke(instance, "SimpleLookup2Range", "DE", 150.005));
        assertEquals(Double.valueOf(1.0), TestUtils.invoke(instance, "SimpleLookup2Range", "", 150.005));
        assertEquals(Double.valueOf(1.0), TestUtils.invoke(instance, "SimpleLookup2Range", "DE", 2000.0));
    }

    @Test
    public void simpleLookupRange1() {
        assertEquals(Double.valueOf(0.9), TestUtils.invoke(instance, "SimpleLookupRange1", "DE", 25.0));
        assertEquals(Double.valueOf(1.0), TestUtils.invoke(instance, "SimpleLookupRange1", "", 4.0));
        assertEquals(Double.valueOf(0.0), TestUtils.invoke(instance, "SimpleLookupRange1", "DE", 3.0));
    }

    @Test
    public void simpleLookup3paramRangeArray() {
        assertEquals(Double.valueOf(0.0),
                TestUtils.invoke(instance,
                        "SimpleLookup3paramRangeArray",
                        new Class[]{String.class, Double.class, int.class},
                        new Object[]{"DE", 5d, 5}));
        assertEquals(Double.valueOf(0.9),
                TestUtils.invoke(instance,
                        "SimpleLookup3paramRangeArray",
                        new Class[]{String.class, Double.class, int.class},
                        new Object[]{"DE", 7d, 3}));
        assertEquals(Double.valueOf(1.0),
                TestUtils.invoke(instance,
                        "SimpleLookup3paramRangeArray",
                        new Class[]{String.class, Double.class, int.class},
                        new Object[]{"DE", 10d, 4}));
    }

    @Test
    public void simpleLookup4paramTitleRange() {
        assertEquals(Double.valueOf(0.9),
                TestUtils.invoke(instance,
                        "SimpleLookup4paramTitleRange",
                        new Class[]{String.class, Double.class, int.class, int.class},
                        new Object[]{"DE", 0d, 7, 3}));
        assertEquals(Double.valueOf(1.0),
                TestUtils.invoke(instance,
                        "SimpleLookup4paramTitleRange",
                        new Class[]{String.class, Double.class, int.class, int.class},
                        new Object[]{"DE", 0d, 10, 4}));
        assertEquals(Double.valueOf(56),
                TestUtils.invoke(instance,
                        "SimpleLookup4paramTitleRange",
                        new Class[]{String.class, Double.class, int.class, int.class},
                        new Object[]{"", 1d, 9, 3}));
    }

    @Test
    public void simpleLookup4paramNotEnoughValues() {
        assertEquals(Double.valueOf(0.9),
                TestUtils.invoke(instance,
                        "SimpleLookup4paramNotEnoughValues",
                        new Class[]{String.class, Double.class, int.class, int.class},
                        new Object[]{"DE", 0d, 7, 3}));
        assertEquals(Double.valueOf(1.0),
                TestUtils.invoke(instance,
                        "SimpleLookup4paramNotEnoughValues",
                        new Class[]{String.class, Double.class, int.class, int.class},
                        new Object[]{"DE", 0d, 10, 4}));
        assertEquals(Double.valueOf(56),
                TestUtils.invoke(instance,
                        "SimpleLookup4paramNotEnoughValues",
                        new Class[]{String.class, Double.class, int.class, int.class},
                        new Object[]{"", 1d, 9, 3}));
    }
}
