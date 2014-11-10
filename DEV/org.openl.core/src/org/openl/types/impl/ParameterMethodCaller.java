/**
 * Created Jul 21, 2007
 */
package org.openl.types.impl;

import org.openl.types.IMethodCaller;
import org.openl.types.IOpenMethod;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 *
 */
public class ParameterMethodCaller implements IMethodCaller {
    int parameterNumber;
    IOpenMethod method;

    public ParameterMethodCaller(IOpenMethod method, int parameterNumber) {
        this.method = method;
        this.parameterNumber = parameterNumber;
    }

    public IOpenMethod getMethod() {
        return method;
    }

    public Object invoke(Object target, Object[] params, IRuntimeEnv env) {
        return params[parameterNumber];
    }
}
