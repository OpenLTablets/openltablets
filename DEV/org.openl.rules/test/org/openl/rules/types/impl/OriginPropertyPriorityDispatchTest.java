package org.openl.rules.types.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openl.rules.TestUtils;

/**
 * Created by dl on 10/3/14.
 */
public class OriginPropertyPriorityDispatchTest {
    private static final String RULES_SOURCE_FILE = "test/rules/dispatching/OriginPropertyPriorityDispatch.xls";

    private Rules instance;

    @BeforeEach
    public void setUp() {
        instance = TestUtils.create(RULES_SOURCE_FILE, Rules.class);
    }

    @Test
    public void testOriginProperty() {
        assertEquals("Deviation", instance.testOriginProperty());
    }

    public interface Rules {
        String testOriginProperty();
    }
}
