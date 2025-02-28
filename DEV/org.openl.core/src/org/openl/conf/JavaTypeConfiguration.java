/*
 * Created on Jun 11, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.conf;

import org.openl.types.ITypeLibrary;

/**
 * @author snshor
 */
public class JavaTypeConfiguration extends AConfigurationElement implements ITypeFactoryConfigurationElement {

    private String className;
    private ITypeLibrary library;

    public String getClassName() {
        return className;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.newconf.IMethodFactoryConfigurationElement#getFactory()
     */
    @Override
    public synchronized ITypeLibrary getLibrary(IConfigurableResourceContext cxt) {
        if (library == null) {
            library = (ITypeLibrary) ClassFactory.newInstance(className, cxt, getUri());
        }
        return library;
    }

    public void setClassName(String string) {
        className = string;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.newconf.IConfigurationElement#validate(org.openl.newconf.IConfigurationContext)
     */
    @Override
    public void validate(IConfigurableResourceContext cxt) {
        Class<?> c = ClassFactory.validateClassExistsAndPublic(className, cxt.getClassLoader(), getUri());
        ClassFactory.validateSuper(c, ITypeLibrary.class, getUri());
        ClassFactory.validateHaveNewInstance(c, getUri());
    }

}
