package org.openl.rules.ui.tablewizard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;
import org.openl.rules.tableeditor.renderkit.TableProperty;
import org.openl.rules.ui.EnumValuesUIHelper;

/**
 * Bean that handles property selection (in "copy table",
 * "create new property table").
 * 
 * @author PUdalau
 */
public class PropertiesBean {
    private List<TableProperty> properties = new ArrayList<TableProperty>();
    private String propNameToAdd;
    private TableProperty propToRemove;
    private Set<String> possibleToAddProps;
    private EnumValuesUIHelper enumHelper = new EnumValuesUIHelper();

    /**
     * Creates PropertiesBean with specified pack of possible properties.
     * 
     * @param possibleProperties List of property names that can be contained by
     *            this bean.
     */
    public PropertiesBean(List<String> possibleProperties) {
        possibleToAddProps = new HashSet<String>(possibleProperties);
    }

    public EnumValuesUIHelper getEnumHelper() {
        return enumHelper;
    }

    public String getPropNameToAdd() {
        return propNameToAdd;
    }

    public void setPropNameToAdd(String propNameToAdd) {
        this.propNameToAdd = propNameToAdd;
    }

    public TableProperty getPropToRemove() {
        return propToRemove;
    }

    public void setPropToRemove(TableProperty propToRemove) {
        this.propToRemove = propToRemove;
    }

    /**
     * @return List of table properties that has bean already added to bean.
     */
    public List<TableProperty> getProperties() {
        return properties;
    }

    /**
     * Set predefined properties into bean.
     * 
     * @param properties List of properties.
     */
    public void setProperties(List<TableProperty> properties) {
        this.properties = properties;
        for (TableProperty property : properties) {
            possibleToAddProps.remove(property.getName());
        }
    }

    private TablePropertyDefinition getPropByName(String name) {
        return TablePropertyDefinitionUtils.getPropertyByName(name);
    }

    /**
     * @return List of {@link SelectItem} that represents all properties that
     *         can be added to bean.
     */
    public List<SelectItem> getPropertiesThatCanBeAdded() {
        List<SelectItem> propertyNames = new ArrayList<SelectItem>();
        for (String propName : possibleToAddProps) {
            propertyNames.add(new SelectItem(propName, getPropByName(propName).getDisplayName()));
        }
        return propertyNames;
    }

    /**
     * Action that can be use on form. "propNameToAdd" must be specified.
     */
    public void addProperty() {
        TablePropertyDefinition propDefinition = getPropByName(propNameToAdd);
        Class<?> propType = propDefinition.getType() == null ? String.class : propDefinition.getType()
                .getInstanceClass();
        properties
                .add(new TableProperty.TablePropertyBuilder(propDefinition.getName(), propDefinition.getDisplayName())
                        .type(propType).format(propDefinition.getFormat()).build());
        possibleToAddProps.remove(propNameToAdd);
    }

    /**
     * Action that can be use on form. "propToRemove" must be specified.
     */
    public void removeProperty() {
        properties.remove(propToRemove);
        possibleToAddProps.add(propToRemove.getName());
    }
}
