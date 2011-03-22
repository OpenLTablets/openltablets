/*
 * Created on May 19, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.BindingDependencies;
import org.openl.binding.IBoundNode;
import org.openl.binding.impl.ATargetBoundNode;
import org.openl.exception.OpenLRuntimeException;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IMethodCaller;
import org.openl.types.IOpenClass;
import org.openl.types.java.JavaOpenClass;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 *
 */
public class MethodBoundNode extends ATargetBoundNode {

    protected IMethodCaller boundMethod;

    public MethodBoundNode(ISyntaxNode syntaxNode, IBoundNode[] child, IMethodCaller method) {
        this(syntaxNode, child, method, null);        
    }

    public MethodBoundNode(ISyntaxNode syntaxNode, IBoundNode[] child, IMethodCaller method, IBoundNode targetNode) {
        super(syntaxNode, child, targetNode);
        boundMethod = method;
    }

    public Object evaluateRuntime(IRuntimeEnv env) throws OpenLRuntimeException {

        try {
            Object target = getTargetNode() == null ? env.getThis() : getTargetNode().evaluate(env);
            Object[] pars = evaluateChildren(env);
            
            return boundMethod.invoke(target, pars, env);
        } catch (ControlSignalReturn signal) {
            return signal.getReturnValue();
        } catch (OpenLRuntimeException opex) {
            opex.pushMethodNode(this);
            throw opex;
        }

    }

    // /* (non-Javadoc)
    // * @see org.openl.binding.IBoundNode#invoke(java.lang.Object[])
    // */
    // public Object evaluate(Object target, Object[] pars, IRuntimeEnv env)
    // {
    // return boundMethod.invoke(target, pars);
    // }

    public IOpenClass getType() {
        return boundMethod.getMethod().getType();
    }

    @Override
    public void updateDependency(BindingDependencies dependencies) {
        dependencies.addMethodDependency(boundMethod.getMethod(), this);
    }

    @Override
    public boolean isLiteralExpressionParent() {
        return boundMethod.getMethod().isStatic() && hasLiteralReturnType(boundMethod.getMethod().getType());
    }
    
    protected IMethodCaller getMethodCaller() {
        return boundMethod;
    }

    private boolean hasLiteralReturnType(IOpenClass type) {
        return type != JavaOpenClass.VOID;
    }

}
