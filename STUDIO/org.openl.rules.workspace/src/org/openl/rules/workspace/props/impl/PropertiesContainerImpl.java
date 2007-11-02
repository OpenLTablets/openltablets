package org.openl.rules.workspace.props.impl;

import org.openl.rules.workspace.props.*;

import java.util.Collection;
import java.util.HashMap;

/**
 * Implementation of Properties Container
 */
public class PropertiesContainerImpl implements PropertiesContainer {
    private HashMap<String, Property> properties;

    public PropertiesContainerImpl() {
        properties = new HashMap<String, Property>();
    }

    /** {@inheritDoc} */
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    /** {@inheritDoc} */
    public Property getProperty(String name) throws PropertyException {
        Property property = properties.get(name);
        if (property == null) {
            throw new PropertyException("No such property ''{0}''", name);
        }

        return property;
    }

    /** {@inheritDoc} */
    public Collection<Property> getProperties() {
        return properties.values();
    }

    /** {@inheritDoc} */
    public void addProperty(Property property) throws PropertyTypeException {
        String name = property.getName();
        Property existing = properties.get(name);

        if (existing == null) {
            // add if there is no prop with such name
            properties.put(name, property);
        } else {
            // smart update
            switch (property.getType()) {
                case DATE:
                    existing.setValue(property.getDate());
                    break;
                case STRING:
                    existing.setValue(property.getString());
                    break;
                default:
                    existing.setValue(property.getString());
            }
        }
    }

    /** {@inheritDoc} */
    public Property removeProperty(String name) throws PropertyException {
        // throws exception if no prop with such name
        Property prop = getProperty(name);

        properties.remove(name);
        return prop;
    }
}
