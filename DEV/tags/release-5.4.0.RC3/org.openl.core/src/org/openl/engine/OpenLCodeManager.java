package org.openl.engine;

import org.openl.IOpenSourceCodeModule;
import org.openl.OpenL;
import org.openl.ProcessedCode;
import org.openl.SourceType;
import org.openl.binding.IBindingContext;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.IBoundCode;
import org.openl.binding.IBoundMethodHeader;
import org.openl.binding.IBoundMethodNode;
import org.openl.binding.impl.Binder;
import org.openl.binding.impl.BindingContext;
import org.openl.binding.impl.BindingContextDelegator;
import org.openl.binding.impl.TypeBoundNode;
import org.openl.binding.impl.module.MethodBindingContext;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethodHeader;
import org.openl.types.NullOpenClass;
import org.openl.types.impl.CompositeMethod;
import org.openl.types.impl.OpenMethodHeader;
import org.openl.types.java.JavaOpenClass;

/**
 * Class that defines OpenL engine manager implementation for operations with
 * code such as make type, make method and etc.
 * 
 */
public class OpenLCodeManager extends OpenLHolder {

    private OpenLSourceManager sourceManager;
    private OpenLCompileManager compileManager;
    private OpenLBindManager bindManager;

    /**
     * Default constructor.
     * 
     * @param openl {@link OpenL} instance
     */
    public OpenLCodeManager(OpenL openl) {
        super(openl);
        sourceManager = new OpenLSourceManager(openl);
        compileManager = new OpenLCompileManager(openl);
        bindManager = new OpenLBindManager(openl);
    }

    /**
     * Makes open class that describes a type.
     * 
     * @param source source
     * @param bindingContextDelegator binding context
     * @return {@link IOpenClass} instance
     */
    public IOpenClass makeType(IOpenSourceCodeModule source, IBindingContextDelegator bindingContextDelegator) {

        try {

            if (bindingContextDelegator == null) {
                bindingContextDelegator = new BindingContextDelegator(getOpenL().getBinder().makeBindingContext());
            }

            bindingContextDelegator.pushErrors();

            ProcessedCode processedCode = sourceManager.processSource(source, SourceType.TYPE, bindingContextDelegator,
                    false);

            IBoundCode boundCode = processedCode.getBoundCode();

            return ((TypeBoundNode) boundCode.getTopNode()).getType();

        } finally {
            bindingContextDelegator.popErrors();
        }
    }

    /**
     * Makes a method from source using method header descriptor.
     * 
     * @param source source
     * @param methodHeader method header descriptor
     * @param bindingContext binding context
     * @return {@link CompositeMethod} instance
     */
    public CompositeMethod makeMethod(IOpenSourceCodeModule source, IOpenMethodHeader methodHeader,
            IBindingContext bindingContext) {

        CompositeMethod compositeMethod = new CompositeMethod(methodHeader, null);

        compileManager.compileMethod(source, compositeMethod, bindingContext);

        return compositeMethod;
    }

    /**
     * Makes a method header from source.
     * 
     * @param source source
     * @param bindingContextDelegator binding context
     * @return {@link IOpenMethodHeader} instance
     */
    public IOpenMethodHeader makeMethodHeader(IOpenSourceCodeModule source,
            IBindingContextDelegator bindingContextDelegator) {

        if (bindingContextDelegator == null) {
            bindingContextDelegator = new BindingContextDelegator(new BindingContext((Binder) getOpenL().getBinder(),
                    JavaOpenClass.VOID, getOpenL()));
        }

        try {
            bindingContextDelegator.pushErrors();

            ProcessedCode processedCode = sourceManager.processSource(source, SourceType.METHOD_HEADER,
                    bindingContextDelegator, false);

            IBoundCode boundCode = processedCode.getBoundCode();

            return ((IBoundMethodHeader) boundCode.getTopNode()).getMethodHeader();

        } finally {
            bindingContextDelegator.popErrors();
        }
    }

    /**
     * Makes method with unknown return type from source using method name and
     * method signature. This method used to create open class that hasn't
     * information of return type at compile time. Return type can be recognized
     * at runtime time.
     * 
     * @param source source
     * @param methodName method name
     * @param signature method signature
     * @param declaringClass open class that declare method
     * @param bindingContext binding context
     * @return {@link IOpenMethodHeader} instance
     */
    public CompositeMethod makeMethodWithUnknownType(IOpenSourceCodeModule source, String methodName,
            IMethodSignature signature, IOpenClass declaringClass, IBindingContext bindingContext) {

        OpenMethodHeader header = new OpenMethodHeader(methodName, NullOpenClass.the, signature, declaringClass);

        try {
            bindingContext.pushErrors();

            MethodBindingContext methodBindingContext = new MethodBindingContext(header, bindingContext);

            ProcessedCode processedCode = sourceManager.processSource(source, SourceType.METHOD_BODY,
                    methodBindingContext, false);

            IBoundCode boundCode = processedCode.getBoundCode();

            IOpenClass retType = methodBindingContext.getReturnType();

            if (retType == NullOpenClass.the) {
                retType = boundCode.getTopNode().getType();
            }

            header.setTypeClass(retType);

            IBoundMethodNode boundMethodNode = bindManager.bindMethod(boundCode, header, bindingContext);

            return new CompositeMethod(header, boundMethodNode);
        } finally {
            bindingContext.popErrors();
        }
    }

}
