/*
 * Created on Jun 11, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */
 
package org.openl.conf;

import java.util.Iterator;

import org.openl.OpenConfigurationException;
import org.openl.types.IOpenClass;
import org.openl.util.CategorizedMap;

/**
 * @author snshor
 *
 */
public class TypeFactoryConfiguration
  extends AConfigurationElement
  implements  IConfigurationElement
{


	CategorizedMap map = new CategorizedMap();


  /* (non-Javadoc)
   * @see org.openl.newconf.IConfigurationElement#validate(org.openl.newconf.IConfigurationContext)
   */
  public void validate(IConfigurableResourceContext cxt)
    throws OpenConfigurationException
  {
 		for (Iterator<Object> iter = map.values().iterator(); iter.hasNext();)
    {
      NameSpacedTypeConfiguration lib = (NameSpacedTypeConfiguration)iter.next();
      lib.validate(cxt);
    }
  }
  
  
  public void addConfiguredTypeLibrary(NameSpacedTypeConfiguration library)
  {
  	map.put(library.getNamespace(), library);
  }

	public IOpenClass getType(String namespace, String name, IConfigurableResourceContext cxt)
	{
		NameSpacedTypeConfiguration lib = (NameSpacedTypeConfiguration)map.get(namespace);
		return lib == null ? null : lib.getType(name, cxt);
	}



}
