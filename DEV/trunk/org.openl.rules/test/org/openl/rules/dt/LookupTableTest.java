package org.openl.rules.dt;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openl.meta.DoubleValue;
import org.openl.rules.runtime.RuleEngineFactory;
import org.openl.runtime.EngineFactory;

public class LookupTableTest {
    
    public interface ILookupTableTest {
        
        DoubleValue getCarPrice(String country, String region, String brand, String model);
        
        DoubleValue getCarPriceMergedHorizontalCond(String country, String region, String brand, String model);
        
        DoubleValue getCarPriceMergedVerticalCondWithRuleCol(String country, String region, String brand, String model);
        
        DoubleValue getCarPriceMergedVerticalCond(String country, String region, String brand, String model);

    }
    
    private static String src = "test/rules/dt/lookup/LookUpTableTest.xls";
    
    private ILookupTableTest test;
    
    @Before
    public void initEngine() {
        EngineFactory<ILookupTableTest> engineFactory = new EngineFactory<ILookupTableTest>(
                RuleEngineFactory.RULE_OPENL_NAME, src, ILookupTableTest.class);
        
        test = engineFactory.makeInstance();
    }
    
    @Test
    public void testNotMergerdLookupTable() {
        DoubleValue result = test.getCarPrice("Belarus,UK", "Minsk", "Porche", "911 Carrera 4S");
        assertEquals(93200, result.intValue());
    }
    
    @Test
    public void testMergedHorizontalCond() {
        DoubleValue result = test.getCarPriceMergedHorizontalCond("Belarus", "Minsk", "Porche", "911 Targa 4");
        assertEquals(90400, result.intValue());
    }
    
    @Test
    public void testMergedVerticalCondWithRuleCol() {
        DoubleValue result = test.getCarPriceMergedVerticalCondWithRuleCol("Belarus", "Minsk", "Porche", "911 Targa 4");
        assertEquals(90400, result.intValue());
    }
    
    @Test
    public void testMergedVerticalCond() {
        DoubleValue result = test.getCarPriceMergedVerticalCond("Belarus", "Minsk", "Porche", "911 Targa 4");
        assertEquals(90400, result.intValue());
    }
    
}
