/*
 * Created on Jul 9, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.BindingDependencies;
import org.openl.binding.IBoundNode;
import org.openl.binding.OpenLRuntimeException;
import org.openl.binding.impl.ABoundNode;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IOpenCast;
import org.openl.types.IOpenClass;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 *
 */
public class CastNode extends ABoundNode {

    IOpenCast cast;
    IOpenClass castedType;

    // protected IBoundNode bnode;

    /**
     * @param syntaxNode
     * @param children
     */
    public CastNode(ISyntaxNode castSyntaxNode, IBoundNode bnode, IOpenCast cast, IOpenClass castedType) {
        super(castSyntaxNode == null ? bnode.getSyntaxNode() : castSyntaxNode, new IBoundNode[] { bnode });
        this.cast = cast;
        // this.bnode = bnode;
        this.castedType = castedType;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundNode#assign(java.lang.Object,
     *      org.openl.vm.IRuntimeEnv)
     */
    @Override
    public void assign(Object value, IRuntimeEnv env) throws OpenLRuntimeException {
        getChildren()[0].assign(value, env);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundNode#evaluate(org.openl.vm.IRuntimeEnv)
     */
    public Object evaluateRuntime(IRuntimeEnv env) throws OpenLRuntimeException {
        Object res = getChildren()[0].evaluate(env);
        return cast.convert(res);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundNode#getTargetNode()
     */
    @Override
    public IBoundNode getTargetNode() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundNode#getType()
     */
    public IOpenClass getType() {
        return castedType;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundNode#isLvalue()
     */
    @Override
    public boolean isLvalue() {
        return getChildren()[0].isLvalue();
    }

    @Override
    public void updateDependency(BindingDependencies dependencies) {
        dependencies.addTypeDependency(castedType, this);
    }

}
