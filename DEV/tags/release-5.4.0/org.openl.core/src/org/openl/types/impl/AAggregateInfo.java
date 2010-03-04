/*
 * Created on Jul 10, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.types.impl;

import java.lang.reflect.Array;

import org.openl.types.IAggregateInfo;
import org.openl.types.IOpenClass;
import org.openl.types.java.JavaOpenClass;

/**
 * @author snshor
 *
 * This class provides standard Java metaphor of array implementation
 */
public abstract class AAggregateInfo implements IAggregateInfo {

    public IOpenClass getIndexedAggregateType(IOpenClass componentType, int dim) {
        int[] dims = new int[dim];

        Object ary = Array.newInstance(componentType.getInstanceClass(), dims);

        return JavaOpenClass.getOpenClass(ary.getClass());
    }

    public Object makeIndexedAggregate(IOpenClass componentClass, int[] dimValues) {
        return Array.newInstance(componentClass.getInstanceClass(), dimValues);
    }

}
