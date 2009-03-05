package org.openl.types.impl;

import org.openl.types.IMethodCaller;
import org.openl.types.IOpenMethod;
import org.openl.vm.IRuntimeEnv;

public class MethodCallerDelegator implements IMethodCaller {
    IMethodCaller delegate;

    public MethodCallerDelegator(IMethodCaller delegate) {
        super();
        this.delegate = delegate;
    }

    public IOpenMethod getMethod() {
        return delegate.getMethod();
    }

    public Object invoke(Object target, Object[] params, IRuntimeEnv env) {
        return delegate.invoke(target, params, env);
    }

    public IMethodCaller getDelegate() {
        return delegate;
    }

}
