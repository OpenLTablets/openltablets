/*
 * Created on May 19, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.FieldNotFoundException;
import org.openl.binding.IBindingContext;
import org.openl.binding.IBoundNode;
import org.openl.syntax.ISyntaxNode;
import org.openl.syntax.impl.ISyntaxConstants;
import org.openl.syntax.impl.IdentifierNode;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;

/**
 * @author snshor
 */

public class IdentifierBinder extends ANodeBinder
{

    public IBoundNode bind(ISyntaxNode node, IBindingContext bindingContext)
	    throws Exception
    {

	boolean strictMatch = isStrictMatch(node);

	String fieldName = ((IdentifierNode) node).getIdentifier();

	IOpenField om = bindingContext.findVar(ISyntaxConstants.THIS_NAMESPACE,
		fieldName, strictMatch);

	if (om != null)
	    return new FieldBoundNode(node, om);

	IOpenClass type = bindingContext.findType(
		ISyntaxConstants.THIS_NAMESPACE, fieldName);

	if (type != null)
	    return new TypeBoundNode(node, type);

	throw new BoundError(node, "Field not found: " + fieldName, null);

    }

    public IBoundNode bindTarget(ISyntaxNode node,
	    IBindingContext bindingContext, IBoundNode target)
    {

	try
	{

	    boolean strictMatch = isStrictMatch(node);

	    String fieldName = ((IdentifierNode) node).getIdentifier();

	    IOpenField of = target.getType().getField(fieldName, strictMatch);

	    if (of == null)
		throw new FieldNotFoundException("Identifier: ", fieldName,
			target.getType());

	    return new FieldBoundNode(node, of, target);

	} catch (Throwable t)
	{
	    bindingContext.addError(new BoundError(node, "Identifier:", t));
	    return new ErrorBoundNode(node);
	}

    }

    static boolean isStrictMatch(ISyntaxNode node)
    {
	return !node.getType().contains(".nostrict");
    }

}
