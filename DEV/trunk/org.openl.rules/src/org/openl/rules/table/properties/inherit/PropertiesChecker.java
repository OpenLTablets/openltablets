package org.openl.rules.table.properties.inherit;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.rules.lang.xls.XlsNodeTypes;
import org.openl.rules.table.properties.def.DefaultPropertyDefinitions;
import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;

/**
 * Class to check properties according to some situations.
 * 
 * @author DLiauchuk
 *
 */
public class PropertiesChecker {
    
    /**
     * Checks if property with given name is suitable for given level. Checks according to the property 
     * definitions in {@link DefaultPropertyDefinitions}. 
     * 
     * @param currentLevel current level of current property.
     * @param propertyName name of the property to check.
     * @return true if property with income name can be defined in income level.
     */
    public static boolean isPropertySuitableForLevel(InheritanceLevel currentLevel, String propertyName) {
    	final Log log = LogFactory.getLog(PropertiesChecker.class);
        boolean result = false;
        TablePropertyDefinition propertyDefinition = TablePropertyDefinitionUtils.getPropertyByName(propertyName);
        if (propertyDefinition != null) {
            InheritanceLevel[] inheritanceLevels = propertyDefinition.getInheritanceLevel();
            if (inheritanceLevels != null && inheritanceLevels.length > 0) {
                if (Arrays.asList(inheritanceLevels).contains(currentLevel)) {
                    result = true;
                } 
            } else {
                log.debug(String.format("Inheritance levels were not defined for property with name [%s].", propertyName));
            }
        } else {
            log.debug(String.format("There is no such property in Definitions with name [%s].", propertyName));
        }
        return result;
    }
    
    /**
     * Checks if properties can be defined for given type of table.
     * 
     * @param propertyName
     * @param tableType     
     * @return TRUE if given property can be set for given type of table. 
     */
    public static boolean isPropertySuitableForTableType(String propertyName, String tableType) {        
        XlsNodeTypes[] definitionTableTypes = TablePropertyDefinitionUtils.getSuitableTableTypes(propertyName);
        if (definitionTableTypes != null) {
            for (XlsNodeTypes nodeType : definitionTableTypes) {
                if (nodeType.toString().equals(tableType)) {
                    // If type from property definition and current table type are equals. It means property is suitable 
                    // for this kind of table.
                    return true;
                } 
            }
        } else {
            // If definitionTableTypes is empty, it means that property is suitable for all kinds of tables.
            return true;
        }
        return false;
    }
}
