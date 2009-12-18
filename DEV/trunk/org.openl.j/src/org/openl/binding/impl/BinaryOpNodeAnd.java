/*
 * Created on May 19, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.IBoundNode;
import org.openl.binding.OpenLRuntimeException;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IOpenClass;
import org.openl.types.java.JavaOpenClass;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 *
 */
public class BinaryOpNodeAnd extends ATargetBoundNode {
    /**
     * @param syntaxNode
     * @param child
     * @param method
     */
    public BinaryOpNodeAnd(ISyntaxNode syntaxNode, IBoundNode[] child) {
        super(syntaxNode, child);
    }

    public Object evaluateRuntime(IRuntimeEnv env) throws OpenLRuntimeException {
        IBoundNode[] children = getChildren();
        Boolean b1 = (Boolean) children[0].evaluate(env);
        if (b1.booleanValue()) {
            return children[1].evaluate(env);
        }
        return Boolean.FALSE;

    }

    public IOpenClass getType() {
        return JavaOpenClass.BOOLEAN;
    }

}
