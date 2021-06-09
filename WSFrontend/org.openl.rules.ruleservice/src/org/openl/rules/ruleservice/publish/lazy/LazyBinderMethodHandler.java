package org.openl.rules.ruleservice.publish.lazy;

import org.openl.rules.lang.xls.prebind.IPrebindHandler;

/**
 * IOpenBinder invocation handler that uses XlsPreBinder on prebind step and XlsBinder on compile step (to compile
 * necessary modules on demand). On prebind step use {@link #setPrebindHandler(IPrebindHandler)} , when prebinding is
 * finished, invoke {@link #removePrebindHandler()}
 *
 * @author NSamatov, Marat Kamalov
 */
class LazyBinderMethodHandler {
    private static final ThreadLocal<IPrebindHandler> prebindHandlerHolder = new ThreadLocal<>();

    /**
     * Set a prebind handler for current thread
     *
     * @param prebindHandler prebind handler for current thread
     */
    static void setPrebindHandler(IPrebindHandler prebindHandler) {
        prebindHandlerHolder.set(prebindHandler);
    }

    /**
     * Remove prebind handler for current thread. Necessary modules will be compiled on demand after that.
     */
    static void removePrebindHandler() {
        prebindHandlerHolder.remove();
    }

    static IPrebindHandler getPrebindHandler() {
        return prebindHandlerHolder.get();
    }
}