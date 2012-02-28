/*
 * Created on May 28, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.IBoundMethodNode;
import org.openl.binding.IBoundNode;
import org.openl.exception.OpenLRuntimeException;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IOpenClass;
import org.openl.types.NullOpenClass;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 *
 */
public class BlockNode extends ABoundNode implements IBoundMethodNode {

    private int localFrameSize = 0;

    public BlockNode(ISyntaxNode node, IBoundNode[] children, int localFrameSize) {
        super(node, children);
        this.localFrameSize = localFrameSize;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundNode#evaluate(org.openl.vm.IRuntimeEnv)
     */
    public Object evaluateRuntime(IRuntimeEnv env) throws OpenLRuntimeException {
        Object[] res = evaluateChildren(env);
        return res == null ? null : (res.length == 0 ? null : res[res.length - 1]);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundMethodNode#getLocalFrameSize()
     */
    public int getLocalFrameSize() {
        return localFrameSize;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundMethodNode#getParametersSize()
     */
    public int getParametersSize() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundNode#getType()
     */
    public IOpenClass getType() {
        return (children == null || children.length == 0) ? NullOpenClass.the : children[children.length - 1].getType();
    }

    @Override
    public boolean isLiteralExpressionParent() {
        return true;
    }

}
