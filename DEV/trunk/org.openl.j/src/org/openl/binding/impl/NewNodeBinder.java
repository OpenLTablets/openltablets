/*
 * Created on May 19, 2003 Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.IBindingContext;
import org.openl.binding.IBoundNode;
import org.openl.binding.MethodUtil;
import org.openl.syntax.ISyntaxNode;
import org.openl.syntax.impl.ISyntaxConstants;
import org.openl.syntax.impl.IdentifierNode;
import org.openl.types.IMethodCaller;
import org.openl.types.IOpenClass;

/**
 * @author snshor
 */
public class NewNodeBinder extends ANodeBinder {

    /*
     * (non-Javadoc)
     * @see org.openl.binding.INodeBinder#bind(org.openl.parser.ISyntaxNode, org.openl.env.IOpenEnv,
     * org.openl.binding.IBindingContext)
     */
    public IBoundNode bind(ISyntaxNode node, IBindingContext bindingContext) throws Exception {

        int childrenCount = node.getNumberOfChildren();

        if (childrenCount < 1) {

            BindHelper.processError("New node must have at least one subnode", node, bindingContext);

            return new ErrorBoundNode(node);
        }

        ISyntaxNode typeNode = node.getChild(0);
        String typeName = ((IdentifierNode) typeNode).getIdentifier();
        IOpenClass type = bindingContext.findType(ISyntaxConstants.THIS_NAMESPACE, typeName);

        if (type == null) {

            String message = String.format("Type '%s' not found", typeName);
            BindHelper.processError(message, typeNode, bindingContext);

            return new ErrorBoundNode(node);
        }

        IBoundNode[] children = bindChildren(node, bindingContext, 1, childrenCount);
        IOpenClass[] types = getTypes(children);

        IMethodCaller methodCaller = MethodSearch.getMethodCaller(type.getName(), types, bindingContext, type);

        if (methodCaller == null) {

            String errMsg = "Constructor not found: " + MethodUtil.printMethod(type.getName(), types);
            BindHelper.processError(errMsg, typeNode, bindingContext);

            return new ErrorBoundNode(node);
        }

        return new MethodBoundNode(node, children, methodCaller);
    }

}
