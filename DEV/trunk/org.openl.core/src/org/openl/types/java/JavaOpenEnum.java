package org.openl.types.java;

import org.openl.domain.IDomain;
import org.openl.types.IOpenSchema;

public class JavaOpenEnum extends JavaOpenClass {

    protected JavaOpenEnum(Class<?> instanceClass, IOpenSchema schema) {
        super(instanceClass, schema);
        domain = new JavaEnumDomain(this);
    }

    
    IDomain<?> domain; 
    
    @Override
    public IDomain<?> getDomain() {
        return domain;
    }

    
    
}
