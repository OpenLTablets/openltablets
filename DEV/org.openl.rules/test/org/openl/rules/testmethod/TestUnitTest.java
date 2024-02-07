package org.openl.rules.testmethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.openl.types.impl.ThisField;
import org.openl.types.java.JavaOpenClass;

public class TestUnitTest {

    @Test
    public void testResultValueModification() {
        TestDescription test = mock(TestDescription.class);
        when(test.isExpectedResultDefined()).thenReturn(true);
        when(test.getExpectedResult()).thenReturn(0.93);
        when(test.getFields()).thenReturn(Collections.singletonList(new ThisField(JavaOpenClass.DOUBLE)));

        ITestUnit unit = new TestUnit(test, 0.93, null, 100);

        assertEquals(0.93, unit.getActualResult());
        assertEquals(TestStatus.TR_OK, unit.getResultStatus());
    }

}
