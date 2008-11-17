package org.openl.binding.impl;

import org.openl.binding.IBindingContext;
import org.openl.binding.IBoundNode;
import org.openl.syntax.ISyntaxNode;

/**
 * @author snshor
 * 
 */
public class WhereExpressionNodeBinder extends ANodeBinder
{

    public IBoundNode bind(ISyntaxNode node, IBindingContext bindingContext)
	    throws Exception
    {

	IBoundNode[] boundNodes = makeLocalVarsFromWhere(node.getChild(1),
		bindingContext);

	IBoundNode exprNode = bindChildNode(node.getChild(0), bindingContext);

	boundNodes[boundNodes.length - 1] = exprNode;

	return new BlockNode(node, boundNodes, 0);
    }

    private static IBoundNode[] makeLocalVarsFromWhere(ISyntaxNode whereNode,
	    IBindingContext bindingContext) throws Exception
    {
	int n = whereNode.getNumberOfChildren();

	IBoundNode[] boundNodes = new IBoundNode[n + 1];

	for (int i = 0; i < n; i++)
	{
	    boundNodes[i] = makeLocalVar(whereNode.getChild(i), bindingContext);
	}

	return boundNodes;
    }

    public static IBoundNode makeLocalVar(ISyntaxNode node,
	    IBindingContext bindingContext) throws Exception
    {

	return bindChildNode(node, bindingContext);


    }

}
