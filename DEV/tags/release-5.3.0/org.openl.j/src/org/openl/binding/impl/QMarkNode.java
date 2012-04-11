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
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 *
 */
public class QMarkNode extends ABoundNode {

    /**
     * @param syntaxNode
     * @param children
     */
    public QMarkNode(ISyntaxNode syntaxNode, IBoundNode[] children) {
        super(syntaxNode, children);
    }

    public Object evaluateRuntime(IRuntimeEnv env) throws OpenLRuntimeException {
        Boolean res = (Boolean) children[0].evaluate(env);

        return res.booleanValue() ? children[1].evaluate(env) : children[2].evaluate(env);

    }

    public IOpenClass getType() {
        // return NullOpenClass.the;
        // TODO use both branches
        return children[1].getType();
    }

}
