/*
 * Created on Jun 23, 2004
 *
 * Developed by OpenRules Inc 2003-2004
 */
package org.openl.types.impl;

import org.openl.binding.MethodUtil;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenMethodHeader;

/**
 * @author snshor
 */
public abstract class AMethod implements IOpenMethod {

    protected IOpenMethodHeader header;

    public AMethod(IOpenMethodHeader header) {
        this.header = header;
    }

    public String getDisplayName(int mode) {
        return MethodUtil.printMethod(this, mode, false);
    }

    /**
     * @return
     */
    public IOpenMethodHeader getHeader() {
        return header;
    }

    /**
     * @return
     */
    public IOpenClass getDeclaringClass() {
        return header.getDeclaringClass();
    }

    /**
     * @return
     */
    public IMemberMetaInfo getInfo() {
        return header.getInfo();
    }

    /**
     * @return
     */
    public String getName() {
        return header.getName();
    }

    /**
     * @return
     */
    public IMethodSignature getSignature() {
        return header.getSignature();
    }

    /**
     * @return
     */
    public IOpenClass getType() {
        return header.getType();
    }

    /**
     * @return
     */
    public boolean isStatic() {
        return header.isStatic();
    }

    public IOpenMethod getMethod() {
        return this;
    }

}
