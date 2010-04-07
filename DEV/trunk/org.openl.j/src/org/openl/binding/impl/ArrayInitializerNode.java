/*
 * Created on Jul 2, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.IBoundNode;
import org.openl.exception.OpenLRuntimeException;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IAggregateInfo;
import org.openl.types.IOpenCast;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenIndex;
import org.openl.types.java.JavaOpenClass;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 *
 */
public class ArrayInitializerNode extends ABoundNode {

    IOpenClass type;

    IOpenCast[] casts;

    /**
     * @param syntaxNode
     * @param children
     */
    public ArrayInitializerNode(ISyntaxNode syntaxNode, IBoundNode[] children, IOpenClass type, IOpenCast[] casts) {
        super(syntaxNode, children);
        this.type = type;
        this.casts = casts;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundNode#evaluate(java.lang.Object,
     *      java.lang.Object[], org.openl.vm.IRuntimeEnv)
     */
    // public Object evaluate(Object target, Object[] pars, IRuntimeEnv env)
    // {
    // throw new UnsupportedOperationException();
    // }
    //
    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundNode#evaluate(org.openl.vm.IRuntimeEnv)
     */
    public Object evaluateRuntime(IRuntimeEnv env) throws OpenLRuntimeException {
        IAggregateInfo info = type.getAggregateInfo();

        Object ary = info.makeIndexedAggregate(info.getComponentType(type), new int[] { children.length });

        IOpenIndex index = info.getIndex(type, JavaOpenClass.INT);

        for (int i = 0; i < children.length; i++) {
            Object obj = children[i].evaluate(env);
            if (casts[i] != null) {
                obj = casts[i].convert(obj);
            }
            index.setValue(ary, new Integer(i), obj);
        }

        return ary;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundNode#getType()
     */
    public IOpenClass getType() {
        return type;
    }

    @Override
    public boolean isLiteralExpressionParent() {
        return true;
    }

}
