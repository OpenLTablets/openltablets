/*
 * Created on Mar 9, 2004
 *
 * Developed by OpenRules Inc. 2003-2004
 */

package org.openl.types.impl;

import java.lang.reflect.Array;

import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.IOpenIndex;

public class ArrayFieldIndex implements IOpenIndex {
    private IOpenClass elementType;
    private IOpenField indexField;

    public ArrayFieldIndex(IOpenClass elementType, IOpenField indexField) {
        this.elementType = elementType;
        this.indexField = indexField;
    }

    public IOpenClass getElementType() {
        return elementType;
    }

    public IOpenClass getIndexType() {
        return indexField.getType();
    }

    public Object getValue(Object container, Object index) {
        if (index != null) {
            int len = Array.getLength(container);

            for (int i = 0; i < len; i++) {
                Object obj = Array.get(container, i);

                Object fieldValue = indexField.get(obj, null);

                if (index.equals(fieldValue)) {
                    return obj;
                }
            }

        }

        return null;
    }

    public boolean isWritable() {
        return false;
    }

    public void setValue(Object container, Object index, Object value) {
        throw new UnsupportedOperationException();
    }

}