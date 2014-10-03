/*
 * Created on Jul 24, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl.module;

import org.openl.binding.BindingDependencies;
import org.openl.binding.IBoundNode;
import org.openl.binding.impl.ABoundNode;
import org.openl.exception.OpenLRuntimeException;
import org.openl.syntax.ISyntaxNode;
import org.openl.syntax.impl.IdentifierNode;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IParameterDeclaration;
import org.openl.types.NullOpenClass;
import org.openl.types.impl.ParameterDeclaration;
import org.openl.types.impl.MethodSignature;
import org.openl.util.text.ILocation;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 *
 */
public class MethodParametersNode extends ABoundNode {

    public MethodParametersNode(ISyntaxNode syntaxNode, IBoundNode[] children) {
        super(syntaxNode, children);
    }

    public Object evaluateRuntime(IRuntimeEnv env) throws OpenLRuntimeException {
        throw new UnsupportedOperationException();
    }

    public IMethodSignature getSignature() {
        int len = children.length;

        ParameterDeclaration[] params = new ParameterDeclaration[len];

        for (int i = 0; i < len; i++) {
            params[i] = new ParameterDeclaration(children[i].getType(), ((ParameterNode) children[i])
                    .getName(), IParameterDeclaration.IN);
        }

        return new MethodSignature(params);

    }

    public ILocation getParamTypeLocation(int paramNum) {
        // 0-th child is param type, 1-st child is param name. See ParameterDeclarationNodeBinder
        ISyntaxNode typeNode = children[paramNum].getSyntaxNode().getChild(0);

        while (typeNode.getNumberOfChildren() == 1 && !(typeNode instanceof IdentifierNode)) {
            // Get type node for array
            typeNode = typeNode.getChild(0);
        }
        return typeNode.getSourceLocation();
    }

    public IOpenClass getType() {
        return NullOpenClass.the;
    }

    @Override
    public void updateDependency(BindingDependencies dependencies) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isLiteralExpressionParent() {
        // TODO Auto-generated method stub
        return false;
    }

}
