/*
 * Created on Jun 17, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openl.binding.AmbiguousMethodException;
import org.openl.binding.ICastFactory;
import org.openl.binding.IMethodFactory;
import org.openl.types.IMethodCaller;
import org.openl.types.IOpenCast;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.impl.CastingMethodCaller;
import org.openl.util.ASelector;
import org.openl.util.OpenIterator;

/**
 * @author snshor
 * 
 */
public class MethodSearch
{

    static final int NO_MATCH = Integer.MAX_VALUE;

    /*
     * (non-Javadoc)
     * 
     * @see org.openl.binding.IMethodFactory#getMethod(java.lang.String,
     *      org.openl.types.IOpenClass[], org.openl.binding.ICastFactory)
     */
    static public IMethodCaller getMethodCaller(String name,
	    IOpenClass[] params, ICastFactory casts, IMethodFactory factory)
	    throws AmbiguousMethodException
    {
	IMethodCaller caller = factory.getMatchingMethod(name, params);
	if (caller != null)
	    return caller;

	if (params.length == 0 || casts == null)
	    return null;

	return getCastingMethodCaller(name, params, casts, factory);

    }

    static protected IMethodCaller getCastingMethodCaller(String name,
	    IOpenClass[] params, ICastFactory casts, IMethodFactory factory)
	    throws AmbiguousMethodException
    {

	List<IOpenMethod> matchingMethods = new ArrayList<IOpenMethod>();
	int bestMatch = NO_MATCH;

	IOpenCast[] bestCastHolder = null;

	for (Iterator<IOpenMethod> iter = methods(name, params.length, factory); iter
		.hasNext();)
	{
	    IOpenMethod method = iter.next();
	    IOpenCast[] castHolder = new IOpenCast[params.length];

	    int match = calcMatch(method.getSignature().getParameterTypes(),
		    params, casts, castHolder);
	    if (match == NO_MATCH)
		continue;
	    if (match < bestMatch)
	    {
		bestMatch = match;
		matchingMethods.clear();
		matchingMethods.add(method);
		bestCastHolder = castHolder;
		continue;
	    }

	    if (match == bestMatch)
	    {
		matchingMethods.add(method);
	    }

	}

	switch (matchingMethods.size())
	{
	case 0:
	    return null;
	case 1:
	    return new CastingMethodCaller(matchingMethods.get(0),
		    bestCastHolder);
	default:
	    throw new AmbiguousMethodException(name, params, matchingMethods);
	}

    }

    protected static int calcMatch(IOpenClass[] methodParam,
	    IOpenClass[] callParam, ICastFactory casts, IOpenCast[] castHolder)
    {
	int maxdiff = 0;
	int ndiff = 0;

	for (int i = 0; i < callParam.length; i++)
	{
	    if (callParam[i] == methodParam[i])
		continue;

	    IOpenCast cast = casts.getCast(callParam[i], methodParam[i]);
	    if (cast == null || !cast.isImplicit())
		return NO_MATCH;

	    castHolder[i] = cast;
	    maxdiff = Math.max(maxdiff, cast.getDistance(callParam[i],
		    methodParam[i]));
	    ndiff++;
	}

	return maxdiff * 100 + ndiff;
    }

    protected static Iterator<IOpenMethod> methods(String name, int nParams,
	    IMethodFactory factory)
    {
	Iterator<IOpenMethod> it = factory.methods();
	if (it == null)
	    return OpenIterator.empty();

	return OpenIterator.select(it, new NameAndParSelector(name, nParams));

    }

    static class NameAndParSelector extends ASelector<IOpenMethod>
    {
	String name;
	int nParams;

	NameAndParSelector(String name, int nParams)
	{
	    this.name = name;
	    this.nParams = nParams;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.util.ISelector#select(java.lang.Object)
	 */
	public boolean select(IOpenMethod method)
	{
	    return method.getName().equals(name)
		    && method.getSignature().getParameterTypes().length == nParams;
	}

	protected boolean equalsSelector(ASelector<?> sel)
	{
	    // TODO Auto-generated method stub
	    return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.util.ASelector#redefinedHashCode()
	 */
	protected int redefinedHashCode()
	{
	    // TODO Auto-generated method stub
	    return 0;
	}
    }

}
