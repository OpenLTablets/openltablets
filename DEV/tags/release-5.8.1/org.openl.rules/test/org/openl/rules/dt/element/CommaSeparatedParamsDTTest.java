package org.openl.rules.dt.element;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openl.rules.runtime.RuleEngineFactory;
import org.openl.runtime.EngineFactory;

public class CommaSeparatedParamsDTTest {

    private static String src = "test/rules/Comma_Separated_Params_DT_Test.xls";
    
    private CommaSeparatedTest test;
    
    @Before
    public void initEngine() {
        EngineFactory<CommaSeparatedTest> engineFactory = new EngineFactory<CommaSeparatedTest>(
                RuleEngineFactory.RULE_OPENL_NAME, src, CommaSeparatedTest.class);
        
        test = engineFactory.makeInstance();
    }
    
    @Test
    public void testcommaSeparatedString() {
        testString("firstValue", "comma2", "Good Morning");                
        
        testString("value1", "singleValue", "Good Afternoon");                
    }
    
    @Test
    public void testCommaSeparatedInt() {
        testInt(12, 17, "Rule 10 Fire");
    }
    
    @Test
    public void testArrayParameterBexCodeSnippetsContains() {
        testBexSnippetContains(27, 29, "Rule 20 Fire");
    }
    
    @Test
    public void testArrayParametersContainsAll() {
        testArrayParametersContainsAll(new int[]{12, 14}, new int[]{13, 17}, "Rule 10 Fire");
    }
    
    @Test
    public void testArrayParametersContainsAllString() {
        testArrayParametersContainsAllString(new String[]{"12", "14"}, new String[]{"13", "17"}, "Rule 10 Fire");
    }
    
    private void testArrayParametersContainsAllString(String[] income1, String[] income2, String expectedResult) {
        String result = test.testArrayParametersContainsAllString(income1, income2);
        assertEquals(expectedResult, result);
        
    }

    private void testArrayParametersContainsAll(int[] income1, int[] income2, String expectedResult) {
        String result = test.testArrayParametersContainsAll(income1, income2);
        assertEquals(expectedResult, result);
    }

    private void testInt(int income1, int income2, String expectedResult) {
        String result = test.helloInt(income1, income2);
        assertEquals(expectedResult, result);        
    }

    private void testString(String income1, String income2, String expectedResult) {
        String result = test.hello2(income1, income2);        
        assertEquals(expectedResult, result);
    }
    
    private void testBexSnippetContains(int income1, int income2, String expectedResult) {
        String result = test.testArrayParameterBexCodeSnippetsContains(income1, income2);
        assertEquals(expectedResult, result);
    }

}
