/*
 * Created on May 28, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.IBindingContext;
import org.openl.binding.IBoundNode;
import org.openl.syntax.ISyntaxNode;

/**
 * @author snshor
 * 
 */
public class BlockBinder extends ANodeBinder {

    /*
     * (non-Javadoc)
     * 
     * @see org.openl.binding.INodeBinder#bind(org.openl.parser.ISyntaxNode,
     *      org.openl.env.IOpenEnv, org.openl.binding.IBindingContext)
     */
    public IBoundNode bind(ISyntaxNode node, IBindingContext bindingContext) {
        IBoundNode[] children = IBoundNode.EMPTY;
        try {
            bindingContext.pushLocalVarContext();
            children = bindChildren(node, bindingContext);
        } catch (BoundError e) {
            // TODO think about it;
        } finally {
            bindingContext.popLocalVarContext();
        }

        return new BlockNode(node, children, bindingContext.getLocalVarFrameSize());
    }

}
