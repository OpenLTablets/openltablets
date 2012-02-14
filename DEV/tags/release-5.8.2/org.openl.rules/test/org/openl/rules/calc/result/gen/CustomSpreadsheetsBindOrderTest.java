package org.openl.rules.calc.result.gen;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openl.engine.OpenLSystemProperties;
import org.openl.rules.BaseOpenlBuilderHelper;

public class CustomSpreadsheetsBindOrderTest extends BaseOpenlBuilderHelper {
    
    private static String __src = "test/rules/calc1/CustomSpreadsheetInSpreadsheet.xls";

    public CustomSpreadsheetsBindOrderTest() {        
    }
    
    @Before
    public void before() {
        System.setProperty(OpenLSystemProperties.CUSTOM_SPREADSHEET_TYPE_PROPERTY, "true");
    }
    
    @After
    public void setDefaultProperty() {
        // set to default 'false' to avoid impact on other tests
        //
        System.setProperty(OpenLSystemProperties.CUSTOM_SPREADSHEET_TYPE_PROPERTY, "false");        
        assertFalse(OpenLSystemProperties.isCustomSpreadsheetType());
    }
    
    @Test
    public void testNoErrors() {
        build(__src);
        // checks that no errors falls on compilation
        // Inside there is a usage of custom spreadsheet result in the other spreadsheet
        //
        assertTrue(OpenLSystemProperties.isCustomSpreadsheetType());
        assertNotNull(getJavaWrapper().getCompiledClass().getOpenClass());
    }
    
    
}
