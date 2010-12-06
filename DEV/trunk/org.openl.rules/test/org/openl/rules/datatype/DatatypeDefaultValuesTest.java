package org.openl.rules.datatype;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openl.meta.DoubleValue;
import org.openl.rules.BaseOpenlBuilderHelper;

public class DatatypeDefaultValuesTest extends BaseOpenlBuilderHelper {
    
    private static final String src = "test/rules/datatype/DatatypeDefaultValues.xlsx";    

    public DatatypeDefaultValuesTest() {
        super(src);
    }
    
    @Before
    public void testBefore() {
        testNoErrors();
    }
    
    @SuppressWarnings("deprecation")
    private void testNoErrors() {
        Assert.assertTrue("No binding errors", getJavaWrapper().getCompiledClass().getBindingErrors().length == 0);
        Assert.assertTrue("No parsing errors", getJavaWrapper().getCompiledClass().getParsingErrors().length == 0);
        Assert.assertTrue("No warnings", getJavaWrapper().getCompiledClass().getMessages().size() == 0);        
    }
    
    @Test    
    public void testDefaultValues() {
        Class<?> clazz = null;
        
        try {
            clazz = Class.forName("org.openl.generated.beans.TestType", true, Thread.currentThread().getContextClassLoader());
            checkTestTypeClass(clazz);
            
            clazz = Class.forName("org.openl.generated.beans.TypeWithLong", true, Thread.currentThread().getContextClassLoader());
            checkTypeWithLong(clazz);

            clazz = Class.forName("org.openl.generated.beans.TestType2", true, Thread.currentThread().getContextClassLoader());
            checkTestType2(clazz);
            
            clazz = Class.forName("org.openl.generated.beans.TestType3", true, Thread.currentThread().getContextClassLoader());
            checkTestType3(clazz);
            
            clazz = Class.forName("org.openl.generated.beans.TestBigTypes", true, Thread.currentThread().getContextClassLoader());
            checkTestBigTypes(clazz);
           
        } catch (Throwable e) {            
            fail();        
        }  
    }

    private void checkTestBigTypes(Class<?> clazz) throws InstantiationException,
                                                    IllegalAccessException,
                                                    NoSuchMethodException,
                                                    InvocationTargetException {
        Object instance = getInstance(clazz);
        
        String methodName = "getBigIntVal";
        testValue(clazz, instance, methodName, BigInteger.valueOf(2000000000));
        
        methodName = "getBigDecVal";
        testValue(clazz, instance, methodName, BigDecimal.valueOf(1115.37));
        
        methodName = "getBigIntVal2";
        testValue(clazz, instance, methodName, null);
        
        
    }

    private void checkTestType3(Class<?> clazz) throws InstantiationException,
                                                IllegalAccessException,
                                                NoSuchMethodException,
                                                InvocationTargetException {
        Object instance = getInstance(clazz);
        
        String methodName = "getDoubleValue";
        testValue(clazz, instance, methodName, new DoubleValue("12.44"));
        
        methodName = "getIntegerValue";
        testValue(clazz, instance, methodName, new Integer("45"));
        
        methodName = "getStr";
        testValue(clazz, instance, methodName, "fgfs");
        
    }    

    private void checkTestType2(Class<?> clazz) throws InstantiationException,
                                                IllegalAccessException,
                                                NoSuchMethodException,
                                                InvocationTargetException {
                
        Object instance = getInstance(clazz);
        
        String methodName = "getOpenlDouble";
        testValue(clazz, instance, methodName, new DoubleValue("12.23"));
    }

    private void checkTypeWithLong(Class<?> clazz) throws InstantiationException,
                                                    IllegalAccessException,
                                                    NoSuchMethodException,
                                                    InvocationTargetException {
        Object instance = getInstance(clazz);
        
        String methodName = "getStrVal";
        testValue(clazz, instance, methodName, null);            
        
        methodName = "getBoolVal";
        testValue(clazz, instance, methodName, true);
        
        methodName = "getDoubleVal";
        testValue(clazz, instance, methodName, new Double("45.47"));
        
        methodName = "getLongVal";
        testValue(clazz, instance, methodName, new Long("45678"));
    }

    private void checkTestTypeClass(Class<?> clazz) throws InstantiationException,
                                                   IllegalAccessException,
                                                   NoSuchMethodException,
                                                   InvocationTargetException {
        Object instance = getInstance(clazz);
        
        String methodName = "getName";
        testValue(clazz, instance, methodName, "Denis");            
        
        methodName = "getByteVal";
        testValue(clazz, instance, methodName, new Byte((byte) 12));
        
        methodName = "getShortVal";
        testValue(clazz, instance, methodName, new Short((short)13));
        
        methodName = "getIntVal";
        testValue(clazz, instance, methodName, 14);
        
        methodName = "getCharVal";
        testValue(clazz, instance, methodName, 'c');
        
        methodName = "getFloatVal";
        testValue(clazz, instance, methodName, new Float("12.23"));
    }

    private void testValue(Class<?> clazz, Object instance, String methodName, Object expectedResult) throws NoSuchMethodException,
                                                           IllegalAccessException,
                                                           InvocationTargetException {
        Method method = clazz.getMethod(methodName, new Class<?>[0]);
        assertNotNull(method);
        Object result = method.invoke(instance, new Object[0]);
        assertEquals(expectedResult, result);
    }
    
    private Object getInstance(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        assertNotNull(clazz);
        
        Object instance = clazz.newInstance();
        return instance;
    }

}
