package org.openl.engine;

import java.util.List;

import org.openl.CompiledOpenClass;
import org.openl.OpenL;
import org.openl.binding.IBindingContext;
import org.openl.binding.IBoundCode;
import org.openl.binding.IBoundMethodNode;
import org.openl.binding.impl.ExecutionModeBindingContextDelegator;
import org.openl.binding.impl.module.MethodBindingContext;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.classloader.SimpleBundleClassLoader;
import org.openl.dependency.IDependencyManager;
import org.openl.message.OpenLMessage;
import org.openl.message.OpenLMessages;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.source.SourceType;
import org.openl.syntax.code.ProcessedCode;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.types.IOpenClass;
import org.openl.types.impl.CompositeMethod;
import org.openl.validation.ValidationResult;
import org.openl.validation.ValidationUtils;

/**
 * Class that defines OpenL engine manager implementation for compilation operations.
 * 
 */
public class OpenLCompileManager extends OpenLHolder {

    private OpenLSourceManager sourceManager;
    private OpenLBindManager bindManager;
    private OpenLValidationManager validationManager;

    /**
     * Construct new instance of manager.
     * 
     * @param openl {@link OpenL} instance
     */
    public OpenLCompileManager(OpenL openl) {
        super(openl);
        sourceManager = new OpenLSourceManager(openl);
        bindManager = new OpenLBindManager(openl);
        validationManager = new OpenLValidationManager(openl);
    }

    /**
     * Compiles module. As a result a module open class will be returned by engine.
     * 
     * @param source source
     * @param executionMode <code>true</code> if module should be compiled in
     *            memory optimized mode for only execution
     * @return {@link IOpenClass} instance
     */
    public IOpenClass compileModule(IOpenSourceCodeModule source, boolean executionMode, IDependencyManager dependencyManager) {
    	/** clear {@link OpenLMessages} as they are stored ThreadLocal. It is not right because in one thread 
    	 * there can be a lot of compiled modules.
    	 * @author DLiauchuk
    	 */
    	OpenLMessages.getCurrentInstance().clear();
        ProcessedCode processedCode;
        if(executionMode){
            processedCode = sourceManager.processSource(source, SourceType.MODULE, new ExecutionModeBindingContextDelegator(null), false, dependencyManager);
        }else{
            processedCode = sourceManager.processSource(source, SourceType.MODULE, dependencyManager);
        }

        IOpenClass openClass = processedCode.getBoundCode().getTopNode().getType();
        if (executionMode) {
            ((ModuleOpenClass)openClass).clearOddDataForExecutionMode();
        }

        return openClass;
    }

    /**
     * Compiles module. As a result a module open class will be returned by engine. All errors that occurred during
     * compilation are suppressed.
     * 
     * @param source source
     * @param executionMode <code>true</code> if module should be compiled in
     *            memory optimized mode for only execution
     * @return {@link CompiledOpenClass} instance
     */
    public CompiledOpenClass compileModuleWithErrors(IOpenSourceCodeModule source, boolean executionMode,
        IDependencyManager dependencyManager) {
    	
    	/** clear {@link OpenLMessages} as they are stored ThreadLocal. It is not right because in one thread 
    	 * there can be a lot of compiled modules.
    	 * @author DLiauchuk
    	 */
    	OpenLMessages.getCurrentInstance().clear();
        ProcessedCode processedCode;
        if (executionMode) {
            processedCode = sourceManager.processSource(source, SourceType.MODULE,
                new ExecutionModeBindingContextDelegator(null), true, dependencyManager);
        } else {
            processedCode = sourceManager.processSource(source, SourceType.MODULE, null, true, dependencyManager);
        }
        IOpenClass openClass = processedCode.getBoundCode().getTopNode().getType();
        SyntaxNodeException[] parsingErrors = processedCode.getParsingErrors();
        SyntaxNodeException[] bindingErrors = processedCode.getBindingErrors();
        if (!executionMode) {
            List<ValidationResult> validationResults = validationManager.validate(openClass);
            List<OpenLMessage> validationMessages = ValidationUtils.getValidationMessages(validationResults);
            OpenLMessages.getCurrentInstance().addMessages(validationMessages);
        }
        OpenLMessages messages = OpenLMessages.getCurrentInstance();
        messages.addMessages(processedCode.getMessagesFromDependencies());
        if (executionMode) {
            ((ModuleOpenClass) openClass).clearOddDataForExecutionMode();
        }
        return new CompiledOpenClass(openClass, messages.getMessages(), parsingErrors, bindingErrors);
    }
   
    /**
     * Compiles a method.
     * 
     * @param source method source
     * @param compositeMethod {@link CompositeMethod} instance
     * @param bindingContext binding context
     */
    public void compileMethod(IOpenSourceCodeModule source,
                              CompositeMethod compositeMethod,
                              IBindingContext bindingContext) {

        try {

            bindingContext.pushErrors();

            MethodBindingContext methodBindingContext = new MethodBindingContext(compositeMethod.getHeader(),
                bindingContext);

            ProcessedCode processedCode = sourceManager.processSource(source,
                SourceType.METHOD_BODY,
                methodBindingContext,
                false, null);

            IBoundCode boundCode = processedCode.getBoundCode();

            IBoundMethodNode boundMethodNode = bindManager.bindMethod(boundCode,
                compositeMethod.getHeader(),
                bindingContext);

            compositeMethod.setMethodBodyBoundNode(boundMethodNode);
        } finally {
            bindingContext.popErrors();
        }
    }
}
