package org.openl.types.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openl.binding.exception.AmbiguousMethodException;
import org.openl.domain.IDomain;
import org.openl.domain.IType;
import org.openl.meta.IMetaInfo;
import org.openl.types.IAggregateInfo;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenSchema;
import org.openl.vm.IRuntimeEnv;

public class OpenClassDelegator implements IOpenClass {
    IOpenClass baseClass;
    String name;
    IMetaInfo metaInfo;
    String nameSpace;

    public OpenClassDelegator(String name, IOpenClass baseClass, IMetaInfo metaInfo) {
        this.baseClass = baseClass;
        this.name = name;
        this.metaInfo = metaInfo;
    }

    /**
     * @deprecated use {@link #getFields()} instead.
     */
    public Iterator<IOpenField> fields() {
        return baseClass.fields();
    }

    public IAggregateInfo getAggregateInfo() {
        return baseClass.getAggregateInfo();
    }

    public IOpenClass getBaseClass() {
        return baseClass;
    }

    public String getDisplayName(int mode) {
        return baseClass.getDisplayName(mode);
    }
    
    public IDomain<?> getDomain() {
        return baseClass.getDomain();
    }

    public IOpenField getField(String fname) {
        return baseClass.getField(fname);
    }

    public IOpenField getField(String fname, boolean strictMatch) {
        return baseClass.getField(fname, strictMatch);
    }

    public IOpenField getIndexField() {
        return baseClass.getIndexField();
    }

    public Class<?> getInstanceClass() {
        return baseClass.getInstanceClass();
    }

    public IOpenMethod getMatchingMethod(String mname, IOpenClass[] params) throws AmbiguousMethodException {
        return baseClass.getMatchingMethod(mname, params);
    }

    public IMetaInfo getMetaInfo() {
        return metaInfo;
    }

    public IOpenMethod getMethod(String mname, IOpenClass[] classes) {
        return baseClass.getMethod(mname, classes);
    }

    public String getName() {
        return name;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public IOpenClass getOpenClass() {
        return this;
    }

    public IOpenSchema getSchema() {
        return baseClass.getSchema();
    }

    public IOpenField getVar(String vname, boolean strictMatch) {
        return baseClass.getVar(vname, strictMatch);
    }

    public boolean isAbstract() {
        return baseClass.isAbstract();
    }

    public boolean isAssignableFrom(Class<?> c) {
        return baseClass.isAssignableFrom(c);
    }

    public boolean isAssignableFrom(IOpenClass ioc) {
        return baseClass.isAssignableFrom(ioc);
    }

    public boolean isAssignableFrom(IType type) {
        return baseClass.isAssignableFrom(type);
    }

    public boolean isInstance(Object instance) {
        return baseClass.isInstance(instance);
    }

    public boolean isSimple() {
        return baseClass.isSimple();
    }
    
    public boolean isArray() {
        return baseClass.isArray();
    }
    
    public IOpenClass getComponentClass() {        
        return baseClass.getComponentClass();
    }

    /**
     * @deprecated use {@link #getMethods()} instead.
     */
    public Iterator<IOpenMethod> methods() {
        return baseClass.methods();
    }

    public Object newInstance(IRuntimeEnv env) {
        return baseClass.newInstance(env);
    }

    public Object nullObject() {
        return baseClass.nullObject();
    }

    public void setBaseClass(IOpenClass baseClass) {
        this.baseClass = baseClass;
    }

    public void setMetaInfo(IMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public Iterator<IOpenClass> superClasses() {
        return baseClass.superClasses();
    }

    @Override
    public String toString() {
        return (getNameSpace() == null ? "" : getNameSpace() + ":") + getName();
    }

	public void addType(String namespace, IOpenClass type) throws Exception {
		// Default implementation.
		
	}

	public IOpenClass findType(String namespace, String typeName) {
		// Default implementation.
		return null;
	}

    public Map<String, IOpenClass> getTypes() {
        // Default implementation
        return null;
    }

    public Map<String, IOpenField> getFields() {        
        return baseClass.getFields();
    }

    public Map<String, IOpenField> getDeclaredFields() {
        return baseClass.getDeclaredFields();
    }

    public List<IOpenMethod> getMethods() {        
        return baseClass.getMethods();
    }
    
    public List<IOpenMethod> getDeclaredMethods() {
        return baseClass.getMethods();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IOpenClass)) {
            return false;
        }
        return new EqualsBuilder().append(getName(), ((IOpenClass) obj).getName()).isEquals();
    }

}
