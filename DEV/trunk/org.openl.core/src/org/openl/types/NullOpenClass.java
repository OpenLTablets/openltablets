/*
 * Created on May 20, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.types;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openl.binding.exception.AmbiguousMethodException;
import org.openl.domain.IDomain;
import org.openl.domain.IType;
import org.openl.meta.IMetaInfo;
import org.openl.syntax.impl.ISyntaxConstants;
import org.openl.util.AOpenIterator;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 *
 */
public class NullOpenClass implements IOpenClass {

    public static final NullOpenClass the = new NullOpenClass();

    private NullOpenClass() {
    }

    @Deprecated
    public Iterator<IOpenField> fields() {
        return null;
    }

    public IAggregateInfo getAggregateInfo() {
        return null;
    }

    public IOpenClass getArrayType(int dim) {
        return null;
    }

    public String getDisplayName(int mode) {
        return getName();
    }

    @SuppressWarnings("unchecked")
    public IDomain getDomain() {
        return null;
    }

    public IOpenFactory getFactory() {
        return null;
    }

    public IOpenField getField(String name) {
        return null;
    }

    public IOpenField getField(String fname, boolean strictMatch) {
        return null;
    }

    public IOpenField getIndexField() {
        return null;
    }

    public Class<?> getInstanceClass() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IMethodFactory#getMatchingMethod(java.lang.String,
     *      org.openl.types.IOpenClass[])
     */
    public IOpenMethod getMatchingMethod(String name, IOpenClass[] params) throws AmbiguousMethodException {
        return null;
    }

    public IMetaInfo getMetaInfo() {
        return null;
    }

    public IOpenMethod getMethod(String name, IOpenClass[] classes) {
        return null;
    }

    public String getName() {
        return "null-Class";
    }

    public String getNameSpace() {
        return ISyntaxConstants.THIS_NAMESPACE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.types.IOpenClassHolder#getOpenClass()
     */
    public IOpenClass getOpenClass() {
        return this;
    }

    public IOpenSchema getSchema() {
        return null;
    }

    public IOpenField getVar(String fname, boolean strictMatch) {
        return null;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isAssignableFrom(Class<?> c) {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.types.IOpenClass#isAssignableFrom(org.openl.types.IOpenClass)
     */
    public boolean isAssignableFrom(IOpenClass ioc) {
        return ioc == this;
    }

    public boolean isAssignableFrom(IType type) {
        return false;
    }

    public boolean isInstance(Object instance) {
        return instance == null;
    }

    public boolean isSimple() {
        return true;
    }

    @Deprecated
    public Iterator<IOpenMethod> methods() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.types.IOpenClass#newInstance()
     */
    public Object newInstance(IRuntimeEnv env) {
        return null;
    }

    public Object nullObject() {
        return null;
    }

    public void setMetaInfo(IMetaInfo info) {
    }

    public Iterator<IOpenClass> superClasses() {
        return AOpenIterator.empty();
    }

	public void addType(String namespace, IOpenClass type) throws Exception {
		// Default implementation
		
	}

	public IOpenClass findType(String namespace, String typeName) {
		// Default implementation
		return null;
	}

    public Map<String, IOpenClass> getTypes() {
        // Default implementation
        return null;
    }

    public Map<String, IOpenField> getFields() {
        // Default implementation
        return null;
    }

    public Map<String, IOpenField> getDeclaredFields() {
        // Default implementation
        return null;
    }


    public List<IOpenMethod> getMethods() {
        // Default implementation
        return null;
    }
    
    public List<IOpenMethod> getDeclaredMethods() {
        // Default implementation
        return null;
    }
}
