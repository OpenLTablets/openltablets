package org.openl.rules.workspace.props.impl;

import java.util.Date;

import org.openl.rules.workspace.props.Property;
import org.openl.rules.workspace.props.PropertyException;
import org.openl.rules.workspace.props.ValueType;

/**
 * Implementation of Property
 */
public class PropertyImpl implements Property {
    private static final long serialVersionUID = 3446381998422819894L;

    private String name;
    private ValueType type;
    private Object value;

    public PropertyImpl(String name, ValueType type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public PropertyImpl(String name, Date value) {
        this(name, ValueType.DATE, value);
    }

    public PropertyImpl(String name, String value) {
        this(name, ValueType.STRING, value);
    }

    /** {@inheritDoc} */
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    public ValueType getType() {
        return type;
    }

    /** {@inheritDoc} */
    public Object getValue() {
        return value;
    }

    /** {@inheritDoc} */
    public String getString() {
        return value.toString();
    }

    /** {@inheritDoc} */
    public Date getDate() throws PropertyException {
        checkType(ValueType.DATE);
        return (Date)value;
    }

    /** {@inheritDoc} */
    public void setValue(String value) throws PropertyException {
        checkType(ValueType.STRING);
        this.value = value;
    }

    /** {@inheritDoc} */
    public void setValue(Date value) throws PropertyException {
        checkType(ValueType.DATE);
        this.value = value;
    }

    // --- protected

    protected void checkType(ValueType type) throws PropertyException {
        if (this.type != type) {
            throw new PropertyException("Property has {0} type", null, this.type);
        }
    }
}
