package org.openl.rules.lang.xls.bindibg;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openl.rules.lang.xls.binding.TableVersionComparator;
import org.openl.rules.table.properties.TableProperties;

public class TableVersionComparatorTest {

    private TableVersionComparator comparator;
    private TableProperties first;
    private TableProperties second;

    @Before
    public void setUp() {
        comparator = TableVersionComparator.getInstance();
        first = new TableProperties();
        second = new TableProperties();
    }

    @Test
    public void testCompareEqualProperties() {
        // two different instances
        first.setActive(new Boolean(true));
        second.setActive(new Boolean(true));
        assertEquals(0, comparator.compare(first, second));

        first.setActive(Boolean.TRUE);
        second.setActive(Boolean.TRUE);
        assertEquals(0, comparator.compare(first, second));

        first.setActive(Boolean.FALSE);
        second.setActive(new Boolean(false));
        assertEquals(0, comparator.compare(first, second));
    }

    @Test
    public void testCompareNullProperties() {
        first.setActive(null);
        second.setActive(Boolean.TRUE);
        assertEquals(0, comparator.compare(first, second));

        first.setActive(Boolean.TRUE);
        second.setActive(null);
        assertEquals(0, comparator.compare(first, second));

        first.setActive(null);
        second.setActive(null);
        assertEquals(0, comparator.compare(first, second));

        first.setActive(null);
        second.setActive(Boolean.FALSE);
        assertEquals(-1, comparator.compare(first, second));

        first.setActive(Boolean.FALSE);
        second.setActive(null);
        assertEquals(1, comparator.compare(first, second));
    }
}
