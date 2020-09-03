package org.openl.rules.ruleservice.publish.jaxrs;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.openl.runtime.AbstractOpenLMethodHandler;

public class JAXRSMethodHandler extends AbstractOpenLMethodHandler<Method, Method> {

    private final Object target;
    private final Map<Method, Method> methodMap;

    @Override
    public Method getTargetMember(Method key) {
        return methodMap.get(key);
    }

    public JAXRSMethodHandler(Object target, Map<Method, Method> methodMap) {
        this.target = Objects.requireNonNull(target, "target cannot be null");
        this.methodMap = Objects.requireNonNull(methodMap, "methodMap cannot be null");
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public Object invoke(Method method, Object[] args) throws Exception {
        Method m = methodMap.get(method);
        if (m == null) {
            throw new IllegalStateException("Method is not found in the map of methods.");
        }
        if (args != null && args.length == 1) {
            int targetParamCount = m.getParameterTypes().length;
            if (targetParamCount > 1) {
                Object requestObject = args[0];
                if (requestObject == null) {
                    args = new Object[targetParamCount];
                } else {
                    args = (Object[]) requestObject.getClass().getMethod("_args").invoke(requestObject);
                }
            }
        }

        Object o = m.invoke(target, args);
        if (o instanceof Response) {
            return o;
        } else {
            return Response.status(Response.Status.OK).entity(o).build();
        }
    }
}