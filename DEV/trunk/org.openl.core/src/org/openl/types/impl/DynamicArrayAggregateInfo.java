/*
 * Created on Mar 9, 2004
 *
 * Developed by OpenRules Inc. 2003-2004
 */

package org.openl.types.impl;

import java.lang.reflect.Array;
import java.util.Iterator;

import org.openl.types.IAggregateInfo;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.IOpenIndex;
import org.openl.types.java.JavaOpenClass;
import org.openl.util.AOpenIterator;
import org.openl.util.IntegerValuesUtils;
import org.openl.util.OpenIterator;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 *
 */
public class DynamicArrayAggregateInfo extends AAggregateInfo {

    static class MyArrayLengthOpenField extends ArrayLengthOpenField {

        @Override
        public int getLength(Object target) {
            return Array.getLength(target);
        }
    }

    static class MyArrayOpenClass extends ArrayOpenClass {

        public MyArrayOpenClass(IOpenClass componentClass) {
            super(componentClass.getSchema(), componentClass, new MyArrayLengthOpenField());
        }

        public IAggregateInfo getAggregateInfo() {
            return aggregateInfo;
        }

        public boolean isAssignableFrom(Class<?> c) {
            // TODO Auto-generated method stub
            return false;
        }

        public boolean isAssignableFrom(IOpenClass ioc) {
            // TODO Auto-generated method stub
            return false;
        }

        public boolean isInstance(Object instance) {
            // TODO Auto-generated method stub
            return false;
        }

        public Object newInstance(IRuntimeEnv env) {
            // TODO Auto-generated method stub
            return null;
        }

        public Iterator<IOpenClass> superClasses() {
            return AOpenIterator.empty();
        }
    }

    static public final DynamicArrayAggregateInfo aggregateInfo = new DynamicArrayAggregateInfo();

    public IOpenClass getComponentType(IOpenClass aggregateType) {
        if (aggregateType instanceof ArrayOpenClass) {
            return ((ArrayOpenClass) aggregateType).getComponentClass();
        }

        return null;
    }

    public IOpenIndex getIndex(IOpenClass aggregateType, IOpenClass indexType) {
        if (indexType == JavaOpenClass.INT) {
            // if index type is int we return simple java array index.
            return new ArrayIndex(getComponentType(aggregateType));
        } else {
            // we support to work with Datatype arrays like this: people["John"]
            // also different object types may be used as indexes : vehicleSymbols[vehicle] 
            IOpenClass componentClass = ((ArrayOpenClass) aggregateType).getComponentClass();
            IOpenField indexField = componentClass.getIndexField();

            if (indexField != null) {
                // If the type of the suggested index is the same as the type of indexed field
                // simply create indexed field
                if (indexField.getType() == indexType) {
                    return new ArrayFieldIndex(componentClass, indexField);
                } else if (  IntegerValuesUtils.isIntegerValue(indexField.getType().getInstanceClass()) && String.class.equals(indexType.getInstanceClass())) {
                    // handles the case when index field of Datatype is of type int, and we try to get String index
                    // e.g. person["12"]
                    return new ArrayFieldIndex(componentClass, indexField);
                }
            } 
        }
        return null;        
    };

    @Override
    public IOpenClass getIndexedAggregateType(IOpenClass componentType, int dim) {
        if (dim == 0) {
            return componentType;
        }

        IOpenClass[] arrayTypes = ((ADynamicClass) componentType).getArrayTypes();

        synchronized (arrayTypes) {
            if (arrayTypes[dim - 1] != null) {
                return arrayTypes[dim - 1];
            }

            for (int i = 0; i < dim; i++) {
                componentType = new MyArrayOpenClass(componentType);
            }

            return arrayTypes[dim - 1] = componentType;
        }

    }

    public Iterator<Object> getIterator(Object aggregate) {
        return OpenIterator.fromArrayObj(aggregate);
    }

    public boolean isAggregate(IOpenClass type) {
        return type instanceof ArrayOpenClass;
    }

}
