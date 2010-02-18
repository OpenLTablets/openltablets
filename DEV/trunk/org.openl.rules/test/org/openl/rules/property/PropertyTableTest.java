package org.openl.rules.property;

import static org.junit.Assert.*;
import java.util.Map;

import org.junit.Test;
import org.openl.rules.BaseOpenlBuilder;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.properties.ITableProperties;
import org.openl.rules.table.properties.inherit.InheritanceLevel;

public class PropertyTableTest extends BaseOpenlBuilder{    

    private String __src = "test/rules/PropertyTableTest.xls";
    
    @Test
    public void testPropertyTableLoading() {
        String tableName = "Rules void hello1(int hour)";        
        TableSyntaxNode[] tsns = getTableSyntaxNodes(__src);
        TableSyntaxNode resultTsn = findTable(tableName, tsns);
        if (resultTsn != null) {
            ITableProperties tableProperties  = resultTsn.getTableProperties();
            assertNotNull(tableProperties);
                
            Map<String, Object> moduleProperties = tableProperties.getPropertiesAppliedForModule();                
            assertTrue(moduleProperties.size() == 3);
            assertEquals(InheritanceLevel.MODULE.getDisplayName(),(String) moduleProperties.get("scope"));
            assertEquals("Any phase",(String) moduleProperties.get("buildPhase"));                
            assertEquals("on",(String) moduleProperties.get("validateDT"));
                
            Map<String, Object> categoryProperties = tableProperties.getPropertiesAppliedForCategory();                
            assertTrue(categoryProperties.size() == 4);
            assertEquals(InheritanceLevel.CATEGORY.getDisplayName(),(String) categoryProperties.get("scope"));
            assertEquals("newLob",(String) categoryProperties.get("lob"));
            assertEquals("alaska",(String) categoryProperties.get("usregion"));                
            assertEquals("east",(String) categoryProperties.get("region"));
                
             Map<String, Object> defaultProperties = tableProperties.getPropertiesAppliedByDefault();
            // assertTrue(defaultProperties.size() == 5);
            // assertEquals("US",(String) defaultProperties.get("country"));
            assertEquals(org.openl.rules.enumeration.CurrenciesEnum.USD,
                    (org.openl.rules.enumeration.CurrenciesEnum) defaultProperties.get("currency"));
            assertEquals("en",(String) defaultProperties.get("lang"));
            assertTrue((Boolean) defaultProperties.get("active"));
            assertTrue((Boolean) defaultProperties.get("failOnMiss"));
            } else {
                fail();
        }
    }
}
