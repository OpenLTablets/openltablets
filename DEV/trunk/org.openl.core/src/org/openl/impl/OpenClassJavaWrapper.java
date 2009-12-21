/*
 * Created on Oct 26, 2005
 *
 */

package org.openl.impl;

import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;

import org.openl.CompiledOpenClass;
import org.openl.IOpenSourceCodeModule;
import org.openl.OpenL;
import org.openl.conf.IUserEnvironmentContext;
import org.openl.engine.OpenLManager;
import org.openl.syntax.impl.FileSourceCodeModule;
import org.openl.syntax.impl.URLSourceCodeModule;
import org.openl.types.IOpenClass;
import org.openl.util.PropertiesLocator;
import org.openl.util.RuntimeExceptionWrapper;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 */
public class OpenClassJavaWrapper {
    CompiledOpenClass __compiledClass;
    IRuntimeEnv __env;

    static public OpenClassJavaWrapper createWrapper(String openlName, IUserEnvironmentContext ucxt, IOpenSourceCodeModule src) {
        OpenL openl = OpenL.getInstance(openlName, ucxt);

        CompiledOpenClass openClass = OpenLManager.compileModuleWithErrors(openl, src);

        return new OpenClassJavaWrapper(openClass, openl.getVm().getRuntimeEnv());
    }

    static public OpenClassJavaWrapper createWrapper(String openlName, IUserEnvironmentContext ucxt, String srcFile) {
        OpenL openl = OpenL.getInstance(openlName, ucxt);

        IOpenSourceCodeModule src = null;

        String fileOrURL = PropertiesLocator.locateFileOrURL(srcFile, ucxt.getUserClassLoader(), new String[] { ucxt
                .getUserHome() });

        if (fileOrURL == null) {
            throw new RuntimeException("File " + srcFile + " is not found");
        }

        try {
            if (fileOrURL.indexOf(':') < 2) {
                src = new FileSourceCodeModule(fileOrURL, null);
            } else {
                src = new URLSourceCodeModule(new URL(fileOrURL));
            }
        } catch (MalformedURLException e) {
            throw RuntimeExceptionWrapper.wrap(e);
        }

        CompiledOpenClass openClass = OpenLManager.compileModuleWithErrors(openl, src);

        return new OpenClassJavaWrapper(openClass, openl.getVm().getRuntimeEnv());

    }

    @SuppressWarnings("unchecked")
    public static OpenClassJavaWrapper createWrapper(String name, IUserEnvironmentContext ucxt, String __src, String srcClass) {
        if (srcClass == null) {
            return createWrapper(name, ucxt, __src);
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName(srcClass);
            Class<IOpenSourceCodeModule> src = (Class<IOpenSourceCodeModule>) clazz;
            Constructor<IOpenSourceCodeModule> ctr = src.getConstructor(String.class, IUserEnvironmentContext.class);
            IOpenSourceCodeModule module = ctr.newInstance(__src, ucxt);
            return createWrapper(name, ucxt, module);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Can not instantiate source code module class(String source, IUserContext cxt):", e);
        }

    }

    public OpenClassJavaWrapper(CompiledOpenClass compiledClass, IRuntimeEnv env) {
        __compiledClass = compiledClass;
        __env = env;
    }

    // /factory methods

    /**
     * @return
     */
    public CompiledOpenClass getCompiledClass() {
        return __compiledClass;
    }

    public IRuntimeEnv getEnv() {
        return __env;
    }

    public IOpenClass getOpenClass() {
        return __compiledClass.getOpenClass();
    }

    /**
     * @return
     */
    public IOpenClass getOpenClassWithErrors() {
        return __compiledClass.getOpenClassWithErrors();
    }

    public Object newInstance() {
        return getOpenClass().newInstance(__env);
    }

}
