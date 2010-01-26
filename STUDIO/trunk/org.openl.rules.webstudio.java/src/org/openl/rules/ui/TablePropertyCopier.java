package org.openl.rules.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ajax4jsf.component.UIRepeat;
import org.apache.commons.lang.StringUtils;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.constraints.Constraint;
import org.openl.rules.table.constraints.Constraints;
import org.openl.rules.table.constraints.LessThanConstraint;
import org.openl.rules.table.constraints.MoreThanConstraint;
import org.openl.rules.table.properties.ITableProperties;
import org.openl.rules.table.properties.def.DefaultPropertyDefinitions;
import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.tableeditor.renderkit.TableProperty;

public class TablePropertyCopier extends TableCopier {
    
    private List<TableProperty> propsToCopy = new ArrayList<TableProperty>();
    
    private List<TableProperty> defaultProps = new ArrayList<TableProperty>();

    private UIRepeat propsTable;

    public List<TableProperty> getPropsToCopy() {
        return propsToCopy;
    }

    public void setPropsToCopy(List<TableProperty> propsToCopy) {
        this.propsToCopy = propsToCopy;
    }
    
    public void setDefaultProps(List<TableProperty> defaultProps) {
        this.defaultProps = defaultProps;
    }

    public List<TableProperty> getDefaultProps() {
        return defaultProps;
    }
    
    public TablePropertyCopier(String elementUri) {        
        start();
        setElementUri(elementUri);        
        initTableNames();  
        initProperties(false);
    }    
    
    public TablePropertyCopier(String elementUri, boolean editAsNewVersion) {        
        start();
        setElementUri(elementUri);        
        initTableNames();
        setEdit(editAsNewVersion);
        initProperties(editAsNewVersion == true);
    }    
    
    /**
     * When doing the copy of the table, just properties that where physically defined in original table
     * get to the properties copying page.
     */
    private void initProperties(boolean excludeDimensionalProperties) {
        TablePropertyDefinition[] propDefinitions = DefaultPropertyDefinitions
                .getDefaultDefinitions();
        TableSyntaxNode node = getCopyingTable();
        
        for (TablePropertyDefinition propDefinition : propDefinitions) {
            if (!propDefinition.isSystem() && !(excludeDimensionalProperties && propDefinition.isDimensional())) {
                ITableProperties tableProperties = node.getTableProperties();
                String name = propDefinition.getName();
                Object propertyValue = tableProperties.getPropertyValue(name) != null ?
                        tableProperties.getPropertyValue(name) : null;
                if (!tableProperties.getPropertiesDefinedInTable().containsKey(name)) {
                    propertyValue = StringUtils.EMPTY;
                }
                Class<?> propertyType = null;
                if (propDefinition.getType() != null) {
                    propertyType = propDefinition.getType().getInstanceClass();
                }
                String displayName = propDefinition.getDisplayName();                
                String format = propDefinition.getFormat();
                
                propsToCopy.add(new TableProperty.TablePropertyBuilder(name, displayName)
                        .value(propertyValue).type(propertyType).format(format)
                        .build());
            }
        }
    }

    public String getValidationJS() {
        StringBuilder validation = new StringBuilder();
        TableProperty prop = getCurrentProp();
        Constraints constraints = prop.getConstraints();
        if (constraints != null) {
            String inputId = getInputIdJS(prop.getName());
            for (Constraint constraint : constraints.getAll()) {
                if (constraint instanceof LessThanConstraint || constraint instanceof MoreThanConstraint) {
                    String validator = constraint instanceof LessThanConstraint ? "lessThan" : "moreThan";
                    String compareToField = (String) constraint.getParams()[0];
                    String compareToFieldId = getInputIdJS(compareToField);
                    TableProperty compareToProperty = getProperty(prop.getName());
                    String compareToPropertyDisplayName = compareToProperty == null ? ""
                            : compareToProperty.getDisplayName();
                    validation.append("new Validation(" + inputId + ", '"
                            + validator + "', '', {compareToFieldId:" + compareToFieldId
                            + ",messageParams:'" + compareToPropertyDisplayName + "'})");
                }
            }
        }
        return validation.toString();
    }

    private String getInputIdJS(String propName) {
        return "$('" + propsTable.getParent().getId() + "').down('input[type=hidden][name=id][value="
            + propName + "]').up().down('input').id";
    }

    private TableProperty getCurrentProp() {
        return (TableProperty) propsTable.getRowData();
    }

    private TableProperty getProperty(String name) {
        for (TableProperty property : propsToCopy) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "changeProperties";
    }
    
    @Override
    protected void reset() {
        super.reset();
        propsToCopy.clear();
    }
    
    @Override
    protected Map<String, Object> buildProperties() {
        Map<String, Object> newProperties = new LinkedHashMap<String, Object>();
        newProperties.putAll(buildSystemProperties());
        //TO DO:
        // validateIfNecessaryPropertiesWereChanged(tableProperties);
        
        for (int i = 0; i < propsToCopy.size(); i++) {
            String key = (propsToCopy.get(i)).getName();
            Object value = (propsToCopy.get(i)).getValue();
            if (value == null || (value instanceof String && StringUtils.isEmpty((String)value))) {
                continue;
            } else {
                newProperties.put(key.trim(), value);
            }
        }        
        newProperties.putAll(processDefaultValues());
        return newProperties;        
    }
    
    /**
     * Base table may have properties set by default. These properties exists only in {@link TableSyntaxNode} 
     * representation of the table and not in source of the table. So when copying we must add to new table just 
     * properties and values that were revalued during the copy.
     * 
     * @return Map of properties that in base table were as default and were revalued during copy for new table.
     */
    private Map<String, Object> processDefaultValues() {
        Map<String, Object> revaluedDefaultProperties = new HashMap<String, Object>();
        TableSyntaxNode node = getCopyingTable();
        ITableProperties baseTableProperties = node.getTableProperties();        
        if (baseTableProperties != null) {
            for (TableProperty defaultCopyingProp : defaultProps) {
                String propName = defaultCopyingProp.getName();
                Object basePropValue = baseTableProperties.getPropertyValue(propName);
                Object newPropValue = defaultCopyingProp.getValue();
                if (newPropValue != null) {
                    boolean emptyString = false;
                    if (newPropValue instanceof String) {
                         emptyString = StringUtils.isEmpty((String) newPropValue);
                    }
                    if (!basePropValue.equals(newPropValue) && !emptyString) {
                        revaluedDefaultProperties.put(propName, newPropValue);
                    }
                }
            }
        }
        return revaluedDefaultProperties;
    }

    public void setPropsTable(UIRepeat propsTable) {
        this.propsTable = propsTable;
    }

    public UIRepeat getPropsTable() {
        return propsTable;
    }

    /**
     * It is only necessary for version editor.
     * 
     * @return "Version" TableProperty.
     */
    public TableProperty getVersion(){
        return getProperty("version");
    }
}
