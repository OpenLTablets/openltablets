package org.openl.engine;

import org.openl.CompiledOpenClass;
import org.openl.IOpenSourceCodeModule;
import org.openl.OpenL;
import org.openl.SourceType;
import org.openl.binding.IBindingContext;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.MethodNotFoundException;
import org.openl.binding.OpenLRuntimeException;
import org.openl.syntax.SyntaxErrorException;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethodHeader;
import org.openl.types.impl.CompositeMethod;

/**
 * Helper class that encapsulates several OpenL engine methods.
 * 
 */
public class OpenLManager {

    /**
     * Makes open class that describes a type.
     * 
     * @param opel OpenL engine context
     * @param source source
     * @param bindingContextDelegator binding context
     * @return {@link IOpenClass} instance
     */
    public static IOpenClass makeType(OpenL openl, IOpenSourceCodeModule source,
            IBindingContextDelegator bindingContextDelegator) {

        OpenLCodeManager codeManager = new OpenLCodeManager(openl);

        return codeManager.makeType(source, bindingContextDelegator);

    }

    /**
     * Makes a method from source using method header descriptor.
     * 
     * @param opel OpenL engine context
     * @param source source
     * @param methodHeader method header descriptor
     * @param bindingContext binding context
     * @return {@link CompositeMethod} instance
     */
    public static CompositeMethod makeMethod(OpenL openl, IOpenSourceCodeModule source, IOpenMethodHeader methodHeader,
            IBindingContext bindingContext) {

        OpenLCodeManager codeManager = new OpenLCodeManager(openl);

        return codeManager.makeMethod(source, methodHeader, bindingContext);
    }

    /**
     * Makes a method header from source.
     * 
     * @param opel OpenL engine context
     * @param source source
     * @param bindingContextDelegator binding context
     * @return {@link IOpenMethodHeader} instance
     */
    public static IOpenMethodHeader makeMethodHeader(OpenL openl, IOpenSourceCodeModule source,
            IBindingContextDelegator bindingContextDelegator) {

        OpenLCodeManager codeManager = new OpenLCodeManager(openl);

        return codeManager.makeMethodHeader(source, bindingContextDelegator);
    }

    /**
     * Makes method with unknown return type from source using method name and
     * method signature. This method used to create open class that hasn't
     * information of return type at compile time. Return type can be recognized
     * at runtime time.
     * 
     * @param opel OpenL engine context
     * @param source source
     * @param methodName method name
     * @param signature method signature
     * @param declaringClass open class that declare method
     * @param bindingContext binding context
     * @return {@link IOpenMethodHeader} instance
     */
    public static CompositeMethod makeMethodWithUnknownType(OpenL openl, IOpenSourceCodeModule source,
            String methodName, IMethodSignature signature, IOpenClass declaringClass, IBindingContext bindingContext) {

        OpenLCodeManager codeManager = new OpenLCodeManager(openl);

        return codeManager.makeMethodWithUnknownType(source, methodName, signature, declaringClass, bindingContext);

    }

    /**
     * Compiles a method.
     * 
     * @param opel OpenL engine context
     * @param source method source
     * @param compositeMethod {@link CompositeMethod} instance
     * @param bindingContext binding context
     */
    public static void compileMethod(OpenL openl, IOpenSourceCodeModule source, CompositeMethod compositeMethod,
            IBindingContext bindingContext) {

        OpenLCompileManager compileManager = new OpenLCompileManager(openl);

        compileManager.compileMethod(source, compositeMethod, bindingContext);
    }

    /**
     * Compiles module. As a result a module open class will be returned by
     * engine.
     * 
     * @param opel OpenL engine context
     * @param source source
     * @return {@link IOpenClass} instance
     */
    public static IOpenClass compileModule(OpenL openl, IOpenSourceCodeModule source) {

        OpenLCompileManager compileManager = new OpenLCompileManager(openl);

        return compileManager.compileModule(source);
    }

    /**
     * Compiles module. As a result a module open class will be returned by
     * engine. All errors that occurred during compilation are suppressed.
     * 
     * @param opel OpenL engine context
     * @param source source
     * @return {@link CompiledOpenClass} instance
     */
    public static CompiledOpenClass compileModuleWithErrors(OpenL openl, IOpenSourceCodeModule source) {

        OpenLCompileManager compileManager = new OpenLCompileManager(openl);

        return compileManager.compileModuleWithErrors(source);

    }

    /**
     * Compiles and runs OpenL script.
     * 
     * @param opel OpenL engine context
     * @param source source
     * @return result of script execution
     * @throws OpenLRuntimeException
     */
    public static Object runScript(OpenL openl, IOpenSourceCodeModule source) throws OpenLRuntimeException {

        OpenLRunManager runManager = new OpenLRunManager(openl);

        return runManager.runScript(source);
    }

    /**
     * Compiles and runs specified method.
     * 
     * @param opel OpenL engine context
     * @param source source
     * @param methodName method name
     * @param paramTypes parameters types
     * @param params parameters values
     * @return result of method execution
     * @throws OpenLRuntimeException
     * @throws MethodNotFoundException
     * @throws SyntaxErrorException
     */
    public static Object runMethod(OpenL openl, IOpenSourceCodeModule source, String methodName,
            IOpenClass[] paramTypes, Object[] params) throws OpenLRuntimeException, MethodNotFoundException,
            SyntaxErrorException {

        OpenLRunManager runManager = new OpenLRunManager(openl);

        return runManager.runMethod(source, methodName, paramTypes, params);

    }

    /**
     * Compiles source and runs code.
     * 
     * @param opel OpenL engine context
     * @param source source
     * @param sourceType type of source
     * @return result of execution
     * @throws OpenLRuntimeException
     */
    public static Object run(OpenL openl, IOpenSourceCodeModule source, SourceType sourceType)
            throws OpenLRuntimeException {

        OpenLRunManager runManager = new OpenLRunManager(openl);

        return runManager.run(source, sourceType);

    }

}
