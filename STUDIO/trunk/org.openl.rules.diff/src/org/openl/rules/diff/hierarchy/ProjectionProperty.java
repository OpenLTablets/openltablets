package org.openl.rules.diff.hierarchy;

public interface ProjectionProperty {
    /**
     * Name of the property.
     * 
     * @return name of property
     */
    String getName();

    /**
     * Type of the property. Corresponding java Class.
     * 
     * @return type of property.
     */
    Class<?> getType();

    /**
     * Raw value of the property.
     * <p>
     * It can be any object, i.e. String, Long, array of bytes and so on.
     * 
     * @return raw value or property.
     */
    Object getRawValue();
    
    boolean isComparable();
}
