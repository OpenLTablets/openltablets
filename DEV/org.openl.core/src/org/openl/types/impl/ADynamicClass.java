/*
 * Created on Jul 24, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.types.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openl.binding.DuplicatedMethodException;
import org.openl.binding.DuplicatedVarException;
import org.openl.types.IAggregateInfo;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenSchema;
import org.openl.types.java.JavaNoAggregateInfo;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 * 
 */
public abstract class ADynamicClass extends AOpenClass
{

	String name = "<anonymous class>";;

	Map fieldMap = new HashMap();

	Map methodMap = new HashMap();

	Class instanceClass;

	/**
	 * @param schema
	 */
	public ADynamicClass(IOpenSchema schema, String name, Class instanceClass)
	{
		super(schema);
		this.name = name;
		this.instanceClass = instanceClass;

		// adding defailt constructor

		this.addMethod(new OpenConstructor(this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.impl.AOpenClass#fieldMap()
	 */
	protected Map fieldMap()
	{
		return fieldMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.impl.AOpenClass#methodMap()
	 */
	protected Map methodMap()
	{
		return methodMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenClass#superClasses()
	 */
	public Iterator superClasses()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenClass#getAggregateInfo()
	 */
	public IAggregateInfo getAggregateInfo()
	{
		return JavaNoAggregateInfo.NO_AGGREGATE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenClass#isInstance(java.lang.Object)
	 */
	public boolean isInstance(Object instance)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenClass#isAssignableFrom(org.openl.types.IOpenClass)
	 */
	public boolean isAssignableFrom(IOpenClass ioc)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenClass#isAssignableFrom(java.lang.Class)
	 */
	public boolean isAssignableFrom(Class c)
	{
		return instanceClass.isAssignableFrom(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openl.types.IOpenClass#getInstanceClass()
	 */
	public Class getInstanceClass()
	{
		return instanceClass;
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

	public void addField(IOpenField field)
	{
		Map fields = fieldMap();
		if (fields.containsKey(field.getName()))
			throw new DuplicatedVarException("", field.getName());

		fieldMap().put(field.getName(), field);
	}

	public void addMethod(IOpenMethod method)
	{
		MethodKey key = new MethodKey(method);

		Map methods = methodMap();
		if (methods.containsKey(key))
			throw new DuplicatedMethodException("", method);

		methodMap().put(key, method);
	}

	static public class OpenConstructor implements IOpenMethod
	{

		IOpenClass openClass;

		OpenConstructor(IOpenClass openClass)
		{
			this.openClass = openClass;
		}

		public Object invoke(Object target, Object[] params, IRuntimeEnv env)
		{
			return openClass.newInstance(env);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openl.types.IOpenMember#getDeclaringClass()
		 */
		public IOpenClass getDeclaringClass()
		{
			return openClass;
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
			return openClass;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openl.types.IOpenMember#isStatic()
		 */
		public boolean isStatic()
		{
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openl.types.IMethodCaller#getMethod()
		 */
		public IOpenMethod getMethod()
		{
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openl.base.INamedThing#getName()
		 */
		public String getName()
		{
			return openClass.getName();
		}

		public String getDisplayName(int mode)
		{
			return openClass.getDisplayName(mode);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openl.types.IOpenMethodHeader#getSignature()
		 */
		public IMethodSignature getSignature()
		{
			return IMethodSignature.VOID;
		}

		public String toString()
		{
			return openClass.getName();
		}
		
		

	}

	// TODO fix it
	static final int MAX_DIM = 5;

	IOpenClass[] arrayTypes = new IOpenClass[MAX_DIM];

	public String getDisplayName(int mode)
	{
		return name;
	}
	
	

}
