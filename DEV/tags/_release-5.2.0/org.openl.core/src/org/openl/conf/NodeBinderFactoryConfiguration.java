/*
 * Created on Jun 11, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */
 
package org.openl.conf;

import java.util.Iterator;

import org.openl.OpenConfigurationException;
import org.openl.binding.INodeBinder;
import org.openl.syntax.ISyntaxNode;
import org.openl.util.CategorizedMap;

/**
 * @author snshor
 *
 */
public class NodeBinderFactoryConfiguration extends AConfigurationElement
{

  /* (non-Javadoc)
   * @see org.openl.binding.INodeBinderFactory#getNodeBinder(org.openl.syntax.ISyntaxNode)
   */
  public INodeBinder getNodeBinder(ISyntaxNode node, IConfigurableResourceContext cxt)
  {
  	SingleBinderFactory factory = (SingleBinderFactory)map.get(node.getType());
  	
  	
    return factory == null ? null : (INodeBinder)factory.getResource(cxt);
  }
  
  
  public void validate(IConfigurableResourceContext cxt)
    throws OpenConfigurationException
  {
  	for (Iterator<Object> iter = map.values().iterator(); iter.hasNext();)
    {
      SingleBinderFactory factory = (SingleBinderFactory)iter.next();
      factory.validate(cxt);
    }
  }
  
  public void addConfiguredBinder(SingleBinderFactory factory)
  {
  	map.put(factory.getNode(), factory);
  }
  
  
  CategorizedMap map = new CategorizedMap();
  
  
  static public class SingleBinderFactory extends ClassFactory
  {
  	String node;
  	
		public SingleBinderFactory()
		{
			singleton = true;
		}
  	
      /* (non-Javadoc)
     * @see org.openl.newconf.ClassFactory#getExtendsClassName()
     */
    public String getExtendsClassName()
    {
      return INodeBinder.class.getName();
    }


    /**
     * @return
     */
    public String getNode()
    {
      return node;
    }

    /**
     * @param string
     */
    public void setNode(String string)
    {
      node = string;
    }

}

}
