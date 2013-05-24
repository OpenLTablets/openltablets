package org.openl.rules.testmethod;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.openl.engine.OpenLSystemProperties;
import org.openl.rules.TestHelper;
import org.openl.rules.testmethod.TestDataAccessFieldTest.ITestDataAccessField;

/*
 * @author PTarasevich
 */

public class TestDoubleDelta {
    private static final String FILE_NAME = "test/rules/testmethod/DoubleDeltaTest.xlsx";

    public interface ITestDouble {
        TestUnitsResults testSSTestTestAll();
        TestUnitsResults geTestDoubleTestTestAll();
    }

    @Before
    public void before() {
        System.setProperty(OpenLSystemProperties.CUSTOM_SPREADSHEET_TYPE_PROPERTY, "true");
    }

    @Test
    public void testSSTest() {
        File xlsFile = new File(FILE_NAME);
        TestHelper<ITestDouble> testHelper = new TestHelper<ITestDouble>(xlsFile, ITestDouble.class);

        ITestDouble instance = testHelper.getInstance();
        TestUnitsResults result = instance.geTestDoubleTestTestAll();
        assertEquals(2, result.getNumberOfFailures());
    }
}
