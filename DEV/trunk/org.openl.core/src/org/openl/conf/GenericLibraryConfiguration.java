/*
 * Created on Jun 19, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.conf;


import org.openl.binding.IOpenLibrary;

/**
 * @author snshor
 *
 */
public class GenericLibraryConfiguration
  extends AGenericConfiguration
  implements IMethodFactoryConfigurationElement
{

  IOpenLibrary library;

  /* (non-Javadoc)
   * @see org.openl.conf.IMethodFactoryConfigurationElement#getLibrary(org.openl.conf.IConfigurableResourceContext)
   */
  public synchronized IOpenLibrary getLibrary(IConfigurableResourceContext cxt)
  {

    if (library == null)
      library = (IOpenLibrary)createResource(cxt);
    return library;

  }

  public Class getImplementingClass()
  {
    return IOpenLibrary.class;
  }

}
