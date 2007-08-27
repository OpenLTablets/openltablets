/*
 * Created on Jun 24, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.types.impl;

import java.util.HashMap;
import java.util.Map;

import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.IOpenIndex;
import org.openl.types.IOpenSchema;
import org.openl.types.java.JavaOpenClass;

/**
 * @author snshor
 *
 */
public abstract class ArrayOpenClass extends AOpenClass
{

	protected IOpenClass componentClass;
	protected HashMap fieldMap;
	protected IOpenIndex index;

	/**
	 * @param schema
	 */
	public ArrayOpenClass(
		IOpenSchema schema,
		IOpenClass componentClass,
		IOpenField lengthOpenField)
	{
		super(schema);
		this.componentClass = componentClass;
		fieldMap = new HashMap(1);
		fieldMap.put(lengthOpenField.getName(), lengthOpenField);
	}

	/* (non-Javadoc)
	 * @see org.openl.base.INamedThing#getName()
	 */
	public String getName()
	{
		return componentClass.getName() + "[]";
	}

	
	
	
	public String getDisplayName(int mode)
	{
		return componentClass.getDisplayName(mode) + "[]";
	}

	/* (non-Javadoc)
	 * @see org.openl.types.AOpenClass#fieldMap()
	 */
	protected Map fieldMap()
	{
		return fieldMap;
	}

	/* (non-Javadoc)
	 * @see org.openl.types.AOpenClass#methodMap()
	 */
	protected Map methodMap()
	{
		return null;
	}

	

	/**
	 * @return
	 */
	public IOpenClass getComponentClass()
	{
		return componentClass;
	}

	/* (non-Javadoc)
	 * @see org.openl.types.IOpenClass#getInstanceClass()
	 */
	public Class getInstanceClass()
	{
		return JavaOpenClass.makeArrayClass(componentClass.getInstanceClass());
	}

}
