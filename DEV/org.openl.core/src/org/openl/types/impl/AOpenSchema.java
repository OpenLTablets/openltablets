/*
 * Created on Jun 30, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */
 
package org.openl.types.impl;

import java.util.Iterator;
import java.util.Map;

import org.openl.types.IOpenClass;
import org.openl.types.IOpenClassHolder;
import org.openl.types.IOpenFactory;
import org.openl.types.IOpenSchema;


/**
 * @author snshor
 *
 */
public abstract class AOpenSchema implements IOpenSchema
{
	protected IOpenFactory factory;

	protected Map allClasses;
	
	protected AOpenSchema(IOpenFactory factory)
	{
		this.factory = factory;
	}

	
	
	protected abstract Map buildAllClasses();
	
  /* (non-Javadoc)
   * @see org.openl.types.IOpenSchema#allClasses()
   */
  public synchronized Iterator typeNames()
  {
    return allClasses().keySet().iterator();
  }
  
  
  
  
  protected synchronized Map allClasses()
  {
		if (allClasses == null)
			allClasses = buildAllClasses();
		return allClasses;
  }
  

  /* (non-Javadoc)
   * @see org.openl.types.IOpenSchema#getFactory()
   */
  public IOpenFactory getFactory()
  {
    return factory;
  }

  /* (non-Javadoc)
   * @see org.openl.types.IOpenSchema#getOpenClass(java.lang.String)
   */
  public  synchronized IOpenClass getType(String name)
  {
  	IOpenClassHolder holder = (IOpenClassHolder)allClasses().get(name);
  	
  	return  holder == null ? null : holder.getOpenClass();
  }
}
