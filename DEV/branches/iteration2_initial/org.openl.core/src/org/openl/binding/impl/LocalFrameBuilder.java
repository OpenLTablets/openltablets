/*
 * Created on Jul 25, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import org.openl.binding.DuplicatedVarException;
import org.openl.binding.ILocalVar;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IOpenClass;
import org.openl.types.NullOpenClass;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 * 
 */
public class LocalFrameBuilder
{
    Stack<LocalVarFrameElement> localFrames = new Stack<LocalVarFrameElement>();

    int localVarFrameSize = 0;

    /**
     * 
     */
    public LocalFrameBuilder()
    {
	super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openl.binding.IBindingContext#pushLocalVarContext(org.openl.binding.ILocalVarContext)
     */
    public void pushLocalVarContext()
    {
	localFrames.push(new LocalVarFrameElement());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openl.binding.IBindingContext#popLocalVarcontext()
     */
    public void popLocalVarcontext()
    {
	localVarFrameSize = Math.max(localVarFrameSize, currentFrameSize());
	localFrames.pop();
    }

    public int getLocalVarFrameSize()
    {
	return localVarFrameSize;
    }

    public int currentFrameSize()
    {
	int sum = 0;
	for (Iterator<LocalVarFrameElement> iter = localFrames.iterator(); iter
		.hasNext();)
	{
	    LocalVarFrameElement element = iter.next();
	    sum += element.size();
	}
	return sum;
    }

    public ILocalVar findLocalVar(String namespace, String varname)
    {
	for (Iterator<LocalVarFrameElement> iter = localFrames.iterator(); iter
		.hasNext();)
	{
	    LocalVarFrameElement frame = iter.next();
	    ILocalVar var = frame.findVar(namespace, varname);
	    if (var != null)
		return var;
	}

	return null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openl.binding.IBindingContext#addVar(java.lang.String,
     *      java.lang.String)
     */
    public ILocalVar addVar(String namespace, String name, IOpenClass type)
	    throws DuplicatedVarException
    {
	ILocalVar var = findLocalVar(namespace, name);
	if (var != null)
	{
	    throw new DuplicatedVarException(null, name);
	}

	var = new LocalVar(namespace, name, currentFrameSize(), type);
	localFrames.peek().add(var);
	return var;
    }

    static public class LocalVarFrameElement extends ArrayList<ILocalVar>
    {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9004729180675641226L;

	static boolean compareStrings(String s1, String s2)
	{
	    return s1 != null ? s1.equals(s2) : s1 == s2;
	}

	ILocalVar findVar(String namespace, String varname)
	{
	    for (Iterator<ILocalVar> iter = iterator(); iter.hasNext();)
	    {
		ILocalVar var = iter.next();
		if (var.getName().equals(varname)
			&& compareStrings(var.getNamespace(), namespace))
		    return var;
	    }
	    return null;
	}

	public void addVar(ILocalVar var)
	{
	    add(var);
	}
    }

    static class LocalVar implements ILocalVar
    {
	String namespace;
	String name;
	int indexInLocalFrame;
	IOpenClass type;

	LocalVar(String namespace, String name, int indexInLocalFrame,
		IOpenClass type)
	{
	    this.namespace = namespace;
	    this.name = name;
	    this.indexInLocalFrame = indexInLocalFrame;
	    this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenField#get(java.lang.Object)
	 */
	public Object get(Object target, IRuntimeEnv env)
	{
	    Object res = env.getLocalFrame()[indexInLocalFrame];

	    return res != null ? res : getType().nullObject();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenField#isConst()
	 */
	public boolean isConst()
	{
	    return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenField#isReadable()
	 */
	public boolean isReadable()
	{
	    return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenField#isWritable()
	 */
	public boolean isWritable()
	{
	    return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenField#set(java.lang.Object,
	 *      java.lang.Object)
	 */
	public void set(Object target, Object value, IRuntimeEnv env)
	{
	    env.getLocalFrame()[indexInLocalFrame] = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenMember#getDeclaringClass()
	 */
	public IOpenClass getDeclaringClass()
	{
	    return NullOpenClass.the;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenMember#getInfo()
	 */
	public IMemberMetaInfo getInfo()
	{
	    return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenMember#getType()
	 */
	public IOpenClass getType()
	{
	    return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenMember#isStatic()
	 */
	public boolean isStatic()
	{
	    return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.base.INamedThing#getName()
	 */
	public String getName()
	{
	    return name;
	}

	public String toString()
	{
	    return "~" + name;
	}

	/**
	 * @return
	 */
	public int getIndexInLocalFrame()
	{
	    return indexInLocalFrame;
	}

	/**
	 * @return
	 */
	public String getNamespace()
	{
	    return namespace;
	}

	public String getDisplayName(int mode)
	{
	    return name;
	}

    }

    public LocalVarFrameElement getTopFrame()
    {
	return localFrames.peek();
    }

}
