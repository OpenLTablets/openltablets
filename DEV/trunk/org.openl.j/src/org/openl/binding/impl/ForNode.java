/*
 * Created on Jun 17, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.IBoundNode;
import org.openl.binding.OpenLRuntimeException;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IOpenClass;
import org.openl.types.NullOpenClass;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 *
 */
public class ForNode extends ABoundNode {

    /**
     * @param syntaxNode
     * @param children
     */
    public ForNode(ISyntaxNode syntaxNode, IBoundNode[] children) {
        super(syntaxNode, children);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundNode#evaluate(org.openl.vm.IRuntimeEnv)
     */
    public Object evaluateRuntime(IRuntimeEnv env) throws OpenLRuntimeException {
        if (children[0] != null) {
            children[0].evaluate(env);
        }

        while (true) {
            // check condition
            if (children[1] != null) {
                Boolean b = (Boolean) children[1].evaluate(env);
                if (!b.booleanValue()) {
                    break;
                }
            }

            // do action
            if (children[3] != null) {
                children[3].evaluate(env);
            }

            if (children[2] != null) {
                children[2].evaluate(env);
            }

        }

        return null;
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
    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundNode#getType()
     */
    public IOpenClass getType() {
        return NullOpenClass.the;
    }

}
