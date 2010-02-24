/*
 * Created on Jun 11, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.conf;

import java.util.Iterator;

import org.openl.OpenConfigurationException;
import org.openl.binding.AmbiguousMethodException;
import org.openl.binding.ICastFactory;
import org.openl.types.IMethodCaller;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.util.CategorizedMap;

/**
 * @author snshor
 *
 */
public class LibraryFactoryConfiguration extends AConfigurationElement implements IConfigurationElement {

    CategorizedMap map = new CategorizedMap();

    public void addConfiguredLibrary(NameSpacedLibraryConfiguration library) {
        map.put(library.getNamespace(), library);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.INameSpacedMethodFactory#getMethodCaller(java.lang.String,
     *      java.lang.String, org.openl.types.IOpenClass[],
     *      org.openl.binding.ICastFactory)
     */
    public IMethodCaller getMethodCaller(String namespace, String name, IOpenClass[] params, ICastFactory casts,
            IConfigurableResourceContext cxt) throws AmbiguousMethodException {
        NameSpacedLibraryConfiguration lib = (NameSpacedLibraryConfiguration) map.get(namespace);
        return lib == null ? null : lib.getMethodCaller(name, params, casts, cxt);
    }

    public IOpenField getVar(String namespace, String name, IConfigurableResourceContext cxt, boolean strictMatch) {
        NameSpacedLibraryConfiguration lib = (NameSpacedLibraryConfiguration) map.get(namespace);
        return lib == null ? null : lib.getField(name, cxt, strictMatch);
    }

    public void validate(IConfigurableResourceContext cxt) throws OpenConfigurationException {
        for (Iterator<Object> iter = map.values().iterator(); iter.hasNext();) {
            NameSpacedLibraryConfiguration lib = (NameSpacedLibraryConfiguration) iter.next();
            lib.validate(cxt);
        }
    }

}
