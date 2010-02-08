package org.openl.rules.table.properties.def;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.openl.rules.table.properties.def.TablePropertyDefinition.InheritanceLevel;


public class TablePropertyDefinitionUtils {
    
    /**
     * Gets the array of properties names that are dimensional. 
     * 
     * @return names of properties that are dimensional.
     */
    public static String[] getDimensionalTableProperties() {        
        List<String> names = new ArrayList<String>();         
        TablePropertyDefinition[] definitions = DefaultPropertyDefinitions.getDefaultDefinitions();
        
        for (TablePropertyDefinition definition : definitions) {
            if (definition.isDimensional()) {
                names.add(definition.getName());
            }
        }
        
        return names.toArray(new String[names.size()]);
    }

    /**
     * Gets the name of the property by the given display name
     * @param displayName
     * @return name
     */
    public static String getPropertyName(String displayName) {
        String result = null;
        for(TablePropertyDefinition propDefinition : DefaultPropertyDefinitions.getDefaultDefinitions()){
            if(propDefinition.getDisplayName().equals(displayName)){
                result = propDefinition.getName();
            }
        }
        return result;
    }

    /**
     * Gets the display name of the property by the given name
     * @param name
     * @return diplayName
     */
    public static String getPropertyDisplayName(String name) {
        String result = null;
        for(TablePropertyDefinition propDefinition : DefaultPropertyDefinitions.getDefaultDefinitions()){
            if(propDefinition.getName().equals(name)){
                result = propDefinition.getDisplayName();
            }
        }
        return result;
    }

    /**
     * Gets the property by its given name
     * @param name
     * @return property definition
     */
    public static TablePropertyDefinition getPropertyByName(String name) {
        TablePropertyDefinition result = null;
        for(TablePropertyDefinition propDefinition : DefaultPropertyDefinitions.getDefaultDefinitions()){
            if(propDefinition.getName().equals(name)){
                result = propDefinition;
            }
        }
        return result;
    }

    /**
     * Gets list of properties that must me set for every table by default.
     *
     * @return list of properties.
     */
    public static List<TablePropertyDefinition> getPropertiesToBeSetByDefault() {
        List<TablePropertyDefinition> result = new ArrayList<TablePropertyDefinition>();
        for(TablePropertyDefinition propDefinition : DefaultPropertyDefinitions.getDefaultDefinitions()){
            if(propDefinition.getDefaultValue() != null){
                result.add(propDefinition);
            }
        }
        return result;
    }

    /**
     * Gets list of properties that are marked as system.
     *  
     * @return list of properties.
     */
    public static List<TablePropertyDefinition> getSystemProperties() {	    
        List<TablePropertyDefinition> result = new ArrayList<TablePropertyDefinition>();
            for(TablePropertyDefinition propDefinition : DefaultPropertyDefinitions.getDefaultDefinitions()){
                if(propDefinition.isSystem()){	                
                    result.add(propDefinition);
                }
            }
        return result;
    }

    public static TablePropertyDefinition[] getDefaultDefinitionsByInheritanceLevel(
            InheritanceLevel inheritanceLevel) {
        List<TablePropertyDefinition> resultDefinitions = new ArrayList<TablePropertyDefinition>();
        for (TablePropertyDefinition propertyDefinition : DefaultPropertyDefinitions.getDefaultDefinitions()) {
            if (ArrayUtils.contains(propertyDefinition.getInheritanceLevel(), inheritanceLevel)) {
                resultDefinitions.add(propertyDefinition);
            }
        }
        return resultDefinitions.toArray(new TablePropertyDefinition[0]);
    }
}
