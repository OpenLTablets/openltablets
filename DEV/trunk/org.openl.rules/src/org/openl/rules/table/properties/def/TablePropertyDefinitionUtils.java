package org.openl.rules.table.properties.def;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.openl.rules.lang.xls.XlsNodeTypes;
import org.openl.rules.table.properties.inherit.InheritanceLevel;
import org.openl.rules.table.properties.inherit.PropertiesChecker;


/**
 * Helper methods, for working with properties.<br> 
 * See also {@link PropertiesChecker} for more methods.
 * 
 * @author DLiauchuk
 *
 */
public class TablePropertyDefinitionUtils {
    
    /**
     * Gets the array of properties names that are dimensional. 
     * 
     * @return names of properties that are dimensional.
     */
    public static String[] getDimensionalTablePropertiesNames() {        
        List<String> names = new ArrayList<String>();         
        List<TablePropertyDefinition> dimensionalProperties = getDimensionalTableProperties();
        
        for (TablePropertyDefinition definition : dimensionalProperties) {
                names.add(definition.getName());
        }
        
        return names.toArray(new String[names.size()]);
    }
    
    /**
     * Gets the array of properties names that are dimensional. 
     * 
     * @return names of properties that are dimensional.
     */
    public static List<TablePropertyDefinition> getDimensionalTableProperties() {        
        List<TablePropertyDefinition> dimensionalProperties = new ArrayList<TablePropertyDefinition>();         
        TablePropertyDefinition[] definitions = DefaultPropertyDefinitions.getDefaultDefinitions();
        
        for (TablePropertyDefinition definition : definitions) {
            if (definition.isDimensional()) {
                dimensionalProperties.add(definition);
            }
        }
        
        return dimensionalProperties;
    }

    /**
     * Gets the name of the property by the given display name
     * 
     * @param displayName
     * @return name
     */
    public static String getPropertyName(String displayName) {        
        for(TablePropertyDefinition propDefinition : DefaultPropertyDefinitions.getDefaultDefinitions()){
            if(propDefinition.getDisplayName().equals(displayName)){
                return propDefinition.getName();
            }
        }
        return null;
    }

    /**
     * Gets the display name of the property by the given name
     * 
     * @param name
     * @return diplayName
     */
    public static String getPropertyDisplayName(String name) {        
        for(TablePropertyDefinition propDefinition : DefaultPropertyDefinitions.getDefaultDefinitions()){
            if(propDefinition.getName().equals(name)){
                return propDefinition.getDisplayName();
            }
        }
        return null;
    }

    /**
     * Gets the property by its given name
     * 
     * @param name
     * @return property definition
     */
    public static TablePropertyDefinition getPropertyByName(String name) {        
        for(TablePropertyDefinition propDefinition : DefaultPropertyDefinitions.getDefaultDefinitions()){
            if(propDefinition.getName().equals(name)){
                return propDefinition;
            }
        }
        return null;
    }

    public static boolean doesPropertyExist(String name) {
        return getPropertyByName(name) != null;
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
     * Gets list of properties that must me set for particular table type by default. 
     * 
     * @param tableType type of the table. see {@link XlsNodeTypes}. 
     * @return list of properties that must me set for particular table type by default. If <b>tableType</b> is <code>null</code>,
     * returns {@link #getPropertiesToBeSetByDefault()}
     */
    public static List<TablePropertyDefinition> getPropertiesToBeSetByDefault(String tableType) {
        if (tableType != null) {
            List<TablePropertyDefinition> result = new ArrayList<TablePropertyDefinition>();
            for(TablePropertyDefinition propDefinition : DefaultPropertyDefinitions.getDefaultDefinitions()){
                if(propDefinition.getDefaultValue() != null && tableType.equals(propDefinition.getTableType())){
                    result.add(propDefinition);
                }
            }
            return result;
        }
        return getPropertiesToBeSetByDefault();
        
    }

    /**
     * Gets list of properties that are marked as system.
     *  
     * @return list of properties.
     */
    public static List<TablePropertyDefinition> getSystemProperties() {	    
        List<TablePropertyDefinition> result = new ArrayList<TablePropertyDefinition>();
            for (TablePropertyDefinition propDefinition : DefaultPropertyDefinitions.getDefaultDefinitions()){
                if (propDefinition.isSystem()){	                
                    result.add(propDefinition);
                }
            }
        return result;
    }

    public static TablePropertyDefinition[] getDefaultDefinitionsByInheritanceLevel(
            InheritanceLevel inheritanceLevel) {
        List<TablePropertyDefinition> resultDefinitions = new ArrayList<TablePropertyDefinition>();
        for(TablePropertyDefinition propertyDefinition : DefaultPropertyDefinitions.getDefaultDefinitions()) {
            if (ArrayUtils.contains(propertyDefinition.getInheritanceLevel(), inheritanceLevel)) {
                resultDefinitions.add(propertyDefinition);
            }
        }
        return resultDefinitions.toArray(new TablePropertyDefinition[0]);
    }

    public static TablePropertyDefinition[] getDefaultDefinitionsForTable(String tableType) {
        return getDefaultDefinitionsForTable(tableType, null, false);
    }

    public static TablePropertyDefinition[] getDefaultDefinitionsForTable(
            String tableType, InheritanceLevel inheritanceLevel, boolean ignoreSystem) {
        List<TablePropertyDefinition> resultDefinitions = new ArrayList<TablePropertyDefinition>();

        for (TablePropertyDefinition propertyDefinition : DefaultPropertyDefinitions.getDefaultDefinitions()) {
            String name = propertyDefinition.getName();
            if (PropertiesChecker.isPropertySuitableForTableType(name, tableType)
                    && (inheritanceLevel == null // any level
                    ||  ArrayUtils.contains(propertyDefinition.getInheritanceLevel(), inheritanceLevel))
                    && (!ignoreSystem || (ignoreSystem && !propertyDefinition.isSystem()))) {
                    resultDefinitions.add(propertyDefinition);
            }
        }

        return resultDefinitions.toArray(new TablePropertyDefinition[resultDefinitions.size()]);
    }

    public static Map<String, Set<TablePropertyDefinition>> groupProperties(TablePropertyDefinition[] properties) {
        Map<String, Set<TablePropertyDefinition>> groups = new LinkedHashMap<String, Set<TablePropertyDefinition>>();

        for (TablePropertyDefinition property : properties) {
            String groupName = property.getGroup();

            Set<TablePropertyDefinition> group = groups.get(groupName);
            if (group == null) {
                group = new TreeSet<TablePropertyDefinition>();
                groups.put(groupName, group);
            }
            group.add(property);
        }

        return groups;
    }

    /**
     * Gets the table types in which this property can be defined.
     * 
     * @param propertyName property name.
     * @return the table type in which this property can be defined. <code>NULL</code> if property can be defined for 
     * each type of tables.
     */
    public static XlsNodeTypes[] getSuitableTableTypes(String propertyName) {        
        TablePropertyDefinition propDefinition = getPropertyByName(propertyName);
        if (propDefinition != null) {
            return propDefinition.getTableType();
        }
        return null;
    }
    
    public static Class<?> getPropertyTypeByPropertyName(String name) {        
        TablePropertyDefinition propDefinition = getPropertyByName(name);
        if (propDefinition != null) {
            return propDefinition.getType().getInstanceClass();
        }
        return null;
    }
}
