/*
 * Created on Jul 1, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.IBindingContext;
import org.openl.binding.IBoundNode;
import org.openl.binding.impl.cast.IOpenCast;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IOpenClass;

/**
 * @author snshor
 *
 */
public class ArrayInitializationBinder extends ANodeBinder {

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.INodeBinder#bind(org.openl.syntax.ISyntaxNode,
     *      org.openl.binding.IBindingContext)
     */
    public IBoundNode bind(ISyntaxNode node, IBindingContext bindingContext) throws Exception {
        
        BindHelper.processError("Array has always to be initialized with a type", node, bindingContext);
        
        return new ErrorBoundNode(node);
//        throw new UnsupportedOperationException("Array has always to be initialized with a type");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.INodeBinder#bindType(org.openl.syntax.ISyntaxNode,
     *      org.openl.binding.IBindingContext, org.openl.types.IOpenClass)
     */
    @Override
    public IBoundNode bindType(ISyntaxNode node, IBindingContext bindingContext, IOpenClass type) throws Exception {

        IOpenClass componentType = type.getAggregateInfo().getComponentType(type);

        IBoundNode[] nodes = bindTypeChildren(node, bindingContext, componentType);
        IOpenCast[] casts = new IOpenCast[nodes.length];

        for (int i = 0; i < nodes.length; i++) {
            casts[i] = getCast(nodes[i], componentType, bindingContext);
        }

        return new ArrayInitializerNode(node, nodes, type, casts);
    }

}
