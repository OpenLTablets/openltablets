package org.openl.rules.validation.properties.dimentional;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openl.exception.OpenlNotCheckedException;
import org.openl.rules.enumeration.CountriesEnum;
import org.openl.rules.table.properties.TableProperties;
import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.table.properties.inherit.InheritanceLevel;
import org.openl.rules.table.properties.ITableProperties;

public class ArrayParameterColumnTest {
    
    @Test
    public void testGetCodeExpression() {        
        ArrayParameterColumn arrayColumn = new ArrayParameterColumn(getArrayProperty(), 
            getRules());
        
        assertEquals("country == null || countryLocal1 == country || countryLocal2 == country || countryLocal3 == country || countryLocal4 == country", 
            arrayColumn.getCodeExpression());
        
        // test array column with one element
        //
        TableProperties tableProperty = new TableProperties();
        tableProperty.setCountry(new CountriesEnum[]{CountriesEnum.CL});
        List<ITableProperties> properties = new ArrayList<ITableProperties>();
        properties.add(tableProperty);
        ArrayParameterColumn arrayColumn1 = new ArrayParameterColumn(getArrayProperty(), 
            new DispatcherTableRules(properties));
        assertEquals("country == null || countryLocal == country", arrayColumn1.getCodeExpression());
        
    }
    
    @Test
    public void testGetTitle() {
        ArrayParameterColumn arrayColumn = new ArrayParameterColumn(getArrayProperty(), 
            getRules());
        
        assertEquals("Countries", arrayColumn.getTitle());
    }
    
    @Test
    public void testParameterDeclaration() {
        ArrayParameterColumn arrayColumn = new ArrayParameterColumn(getArrayProperty(), 
            getRules());
        
        assertEquals("CountriesEnum countryLocal", arrayColumn.getParameterDeclaration());
    }
    
    @Test
    public void testGetMaxNumberOfValuesForRules() {
        ArrayParameterColumn arrayColumn = new ArrayParameterColumn(getArrayProperty(), 
            getRules());
        
        assertEquals(4, arrayColumn.getNumberOfLocalParameters());
    }
    
    @Test
    public void testGetRuleValue() {
        ArrayParameterColumn arrayColumn = new ArrayParameterColumn(getArrayProperty(), 
            getRules());
        
        assertEquals(2, arrayColumn.getRulesNumber());
        assertEquals(4, arrayColumn.getNumberOfLocalParameters());
        assertEquals("CL", arrayColumn.getRuleValue(0, 0));
        assertEquals("BA", arrayColumn.getRuleValue(0, 1));
        assertEquals("AT", arrayColumn.getRuleValue(0, 2));
        assertEquals("SA", arrayColumn.getRuleValue(0, 3));
        
        assertEquals("CA", arrayColumn.getRuleValue(1, 0));        
        assertEquals("BE", arrayColumn.getRuleValue(1, 1));
        assertEquals("AU", arrayColumn.getRuleValue(1, 2));
    }
    
    @Test
    public void testNotArrayProperty() {
        // create not array property
        //
        TablePropertyDefinition property = new TablePropertyDefinition();        
        property.setType(org.openl.types.java.JavaOpenClass.getOpenClass(String.class));
        
        try {
            new ArrayParameterColumn(property, 
                getRules());
            fail("Exception should be thrown for not array property");
        } catch (OpenlNotCheckedException e) {
            assertTrue("Got the exception, as expected", true);
        }
    }
    
    private DispatcherTableRules getRules() {
        TableProperties tableProperty = new TableProperties();
        tableProperty.setCountry(new CountriesEnum[]{CountriesEnum.CL, CountriesEnum.BA, CountriesEnum.AT, 
                CountriesEnum.SA});
        
        TableProperties tableProperty1 = new TableProperties();
        tableProperty1.setCountry(new CountriesEnum[]{CountriesEnum.CA, CountriesEnum.BE, CountriesEnum.AU});
        List<ITableProperties> properties = new ArrayList<ITableProperties>();
        properties.add(tableProperty);
        properties.add(tableProperty1);
        
        return new DispatcherTableRules(properties);
    }

    private TablePropertyDefinition getArrayProperty() {
        TablePropertyDefinition arrayProperty = new TablePropertyDefinition();
        arrayProperty.setBusinessSearch(false);
        arrayProperty.setConstraints(new org.openl.rules.table.constraints.Constraints("data: countries"));
        arrayProperty.setDescription("Country");
        arrayProperty.setDimensional(true);
        arrayProperty.setDisplayName("Countries");
        arrayProperty.setExpression(
            new org.openl.rules.table.properties.expressions.match.MatchingExpression("contains(country)"));
        arrayProperty.setGroup("Business Dimension");
        arrayProperty.setInheritanceLevel(new InheritanceLevel[] {InheritanceLevel.MODULE, InheritanceLevel.CATEGORY, 
                InheritanceLevel.TABLE});
        arrayProperty.setName("country");
        arrayProperty.setPrimaryKey(false);
        arrayProperty.setSecurityFilter("yes (coma separated filter specification by user role: category/role pairs)");
        arrayProperty.setSystem(false);
        arrayProperty.setType(org.openl.types.java.JavaOpenClass.getOpenClass(
            org.openl.rules.enumeration.CountriesEnum[].class));
        return arrayProperty;
    }
}
