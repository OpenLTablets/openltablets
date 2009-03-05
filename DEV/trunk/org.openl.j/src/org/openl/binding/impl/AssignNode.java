/*
 * Created on Jun 17, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.BindingDependencies;
import org.openl.binding.IBoundNode;
import org.openl.binding.OpenLRuntimeException;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IMethodCaller;
import org.openl.types.IOpenCast;
import org.openl.types.IOpenClass;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 * 
 */
public class AssignNode extends MethodBoundNode {
    IOpenCast cast;

    /**
     * @param syntaxNode
     * @param child
     * @param method
     */
    public AssignNode(ISyntaxNode syntaxNode, IBoundNode[] child, IMethodCaller method, IOpenCast cast) {
        super(syntaxNode, child, method);
        this.cast = cast;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openl.binding.IBoundNode#evaluate(org.openl.vm.IRuntimeEnv)
     */
    public Object evaluateRuntime(IRuntimeEnv env) throws OpenLRuntimeException {
        Object res = null;
        if (boundMethod != null) {
            Object[] pars = evaluateChildren(env);

            res = BinaryOpNode.evaluateBinaryMethod(env, pars, boundMethod);
        } else {
            res = children[1].evaluate(env);
        }

        res = cast == null ? res : cast.convert(res);
        children[0].assign(res, env);

        return res;
    }

    public IOpenClass getType() {
        if (boundMethod != null)
            return super.getType();
        return children[1].getType();
    }

    public void updateDependency(BindingDependencies dependencies) {
        dependencies.addAssign(children[0], this);
    }

}
