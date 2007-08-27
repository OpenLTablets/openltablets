/*
 * Created on Jun 2, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.IOpenSourceCodeModule;
import org.openl.binding.IBoundError;
import org.openl.syntax.ISyntaxNode;
import org.openl.syntax.impl.SyntaxError;
import org.openl.util.text.ILocation;

/**
 * @author snshor
 *
 */
public class BoundError extends SyntaxError implements IBoundError
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5982280103016729377L;

	public BoundError(
			Throwable t,
			IOpenSourceCodeModule module)
		{
			super(null, null, t, module);
		}

	
	/**
	 * @param location
	 * @param msg
	 * @param t
	 */
	public BoundError(
		ILocation location,
		String msg,
		Throwable t,
		IOpenSourceCodeModule module)
	{
		super(location, msg, t, module);
	}

	/**
	 * @param node
	 * @param msg
	 * @param t
	 */
	public BoundError(ISyntaxNode node, String msg, Throwable t)
	{
		super(node, msg, t);
	}

	public BoundError(ISyntaxNode node, String msg)
	{
		super(node, msg, null);
	}

}
