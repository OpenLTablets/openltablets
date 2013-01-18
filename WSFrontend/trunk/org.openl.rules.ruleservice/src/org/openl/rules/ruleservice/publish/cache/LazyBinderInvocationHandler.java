package org.openl.rules.ruleservice.publish.cache;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.openl.IOpenBinder;
import org.openl.conf.IUserContext;
import org.openl.rules.lang.xls.prebind.IPrebindHandler;
import org.openl.rules.lang.xls.prebind.XlsPreBinder;

/**
 * IOpenBinder invocation handler that uses XlsPreBinder on prebind step and
 * XlsBinder on compile step (to compile necessary modules on demand). On
 * prebind step use {@link #setPrebindHandler(IPrebindHandler)} , when
 * prebinding is finished, invoke {@link #removePrebindHandler()}
 * 
 * @author NSamatov
 */
public class LazyBinderInvocationHandler implements InvocationHandler {
    private static final ThreadLocal<IPrebindHandler> prebindHandlerHolder = new ThreadLocal<IPrebindHandler>();

    private final IOpenBinder originalBinder;
    private final IUserContext ucxt;

    private IOpenBinder binder;

    /**
     * Set a prebind handler for current thread
     * 
     * @param prebindHandler prebind handler for current thread
     */
    public static void setPrebindHandler(IPrebindHandler prebindHandler) {
        prebindHandlerHolder.set(prebindHandler);
    }

    /**
     * Remove prebind handler for current thread. Necessary modules will be
     * compiled on demand after that.
     */
    public static void removePrebindHandler() {
        prebindHandlerHolder.remove();
    }

    /**
     * Create an IOpenBinder invocation handler.
     * 
     * @param originalBinder original binder that will be used to compile
     *            necessary modules on demand
     * @param ucxt user context for module
     */
    public LazyBinderInvocationHandler(IOpenBinder originalBinder, IUserContext ucxt) {
        this.originalBinder = originalBinder;
        this.ucxt = ucxt;

        this.binder = originalBinder;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        IPrebindHandler prebindHandler = prebindHandlerHolder.get();
        if (prebindHandler != null) {
            if (binder == originalBinder) {
                binder = new XlsPreBinder(ucxt, prebindHandler);
            }
        } else {
            binder = originalBinder;
        }

        return method.invoke(binder, args);
    }
}