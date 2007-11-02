package org.openl.rules.workspace.props;

import java.util.Collection;

/**
 * Properties Container keeps set of properties.
 * <p>
 * Only one property per name is supported.
 */
public interface PropertiesContainer {
    /**
     * Checks whether property with specified name exists in the container.
     *
     * @param name name of property
     * @return <code>true</code> if such property exists
     */
    boolean hasProperty(String name);

    /**
     * Returns property by name.
     *
     * @param name name of property
     * @return reference on named property
     * @throws PropertyException if no property with specified name
     */
    Property getProperty(String name) throws PropertyException;

    /**
     * Gets list of all properties in the container.
     *
     * @return list of properties
     */
    Collection<Property> getProperties();

    /**
     * Adds property into the container.
     *
     * @param property adding property
     * @throws PropertyTypeException if property with the same name exists already and value cannot be updated.
     */
    void addProperty(Property property) throws PropertyTypeException;

    /**
     * Removes property from the container.
     *
     * @param name name of property
     * @return removed property
     * @throws PropertyException if no property with specified name
     */
    Property removeProperty(String name) throws PropertyException;
}
