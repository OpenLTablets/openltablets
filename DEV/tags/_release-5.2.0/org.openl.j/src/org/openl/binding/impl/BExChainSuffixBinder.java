/*
 * Created on May 19, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.IBindingContext;
import org.openl.binding.IBoundNode;
import org.openl.syntax.ISyntaxNode;

/**
 * @author snshor
 */

public class BExChainSuffixBinder extends ANodeBinder
{

    public IBoundNode bind(ISyntaxNode node, IBindingContext bindingContext)
	    throws Exception
    {
	String nodeType = node.getType();
	
	int targetInd = 0;
	int chainInd = 1;
	
	if (nodeType.startsWith("chain.suffix.dot"))
	{
	}
	else if (nodeType.startsWith("chain.suffix.of.the"))
	{
	    targetInd = 1;
	    chainInd = 0;
	}  
	
	IBoundNode target = bindChildNode(node.getChild(targetInd), bindingContext);
	
	IBoundNode res = bindTargetNode(node.getChild(chainInd), bindingContext, target);
	
	return res;
	
	
    }
}
