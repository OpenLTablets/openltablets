package org.openl.rules.datatype;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;
import org.openl.rules.BaseOpenlBuilderHelper;
import org.openl.rules.testmethod.TestUnitsResults;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;

public class DatatypeArrayTest extends BaseOpenlBuilderHelper {
    
    private static final String src = "test/rules/datatype/DatatypeArray.xls";
    
    public DatatypeArrayTest() {
        super(src);
    }
    
    /**
     * In the rule we defined a datatype HomeownerArray, than defined a datatype SasriaData, that contains as a 
     * field an array of HomeownerArray. Than using Data table mechanism we initialized a number of test data.
     * Decision table "Rules String testArrays(SasriaData sasriaData)" gets an SasriaData object as parameter, and works with 
     * HomeownerArray field. Also a test method was written for this decision table(it was done to prevent using generated
     * java classes beans of datatypes, as it is some sort of hardcode).
     * Using Openl API we invokes this test method, to test that Openl correctly works with arrays of user defined 
     * datatypes.
     * 
     */
    @Test
    public void testCase1() {
        Object result = invokeMethod("testArraysTestTestAll");
        
        TestUnitsResults testUnitsResult = (TestUnitsResults) result;        
        assertEquals(0, testUnitsResult.getNumberOfFailures());
    }
    
    /**
     * Test accessing datatype array via user defined string index (e.g. people["David"])
     */
    @Test
    public void testStringUserIndex() {
        Object result = invokeMethod("foo");
                
        assertEquals("passed", result.toString());
    }
    
    @Test
    public void testObjectIndexCall() {
        Object result = invokeMethod("myMethod");
        assertEquals("1 passed", result.toString());        
    }
    
    @Test
    public void testObjectIndex() {
        Object result = invokeMethod("vehicleIndex");
        assertTrue("2".equals(result.toString()));        
    }
        
    @Test
    public void testShortIndex() {
        Object result = invokeMethod("shortIndex");
        assertNotNull(result);        
    }
    
    @Test
    public void testIntIndex() {
        Object result = invokeMethod("intIndex");
        assertNotNull(result);        
    }

    private Object invokeMethod(String methodName) {
        IOpenClass __class = getJavaWrapper().getOpenClassWithErrors(); 
        
        IOpenMethod testMethod = __class.getMatchingMethod(methodName, new IOpenClass[] {});
        
        Assert.assertNotNull(String.format("Method with name %s exists", methodName), testMethod);
        
        Object[] __params = new Object[0];
        org.openl.vm.IRuntimeEnv environment = new org.openl.vm.SimpleVM().getRuntimeEnv();
        Object __myInstance = __class.newInstance(environment);
        
        Object result = testMethod.invoke(__myInstance, __params, environment);        
        
        return result;
    }

}
