package org.openl.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.openl.CompiledOpenClass;
import org.openl.OpenL;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.IBoundCode;
import org.openl.classloader.OpenLBundleClassLoader;
import org.openl.dependency.CompiledDependency;
import org.openl.dependency.IDependencyManager;
import org.openl.message.OpenLMessage;
import org.openl.message.OpenLMessages;
import org.openl.message.OpenLMessagesUtils;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.source.SourceType;
import org.openl.syntax.code.IDependency;
import org.openl.syntax.code.IParsedCode;
import org.openl.syntax.code.ProcessedCode;
import org.openl.syntax.exception.CompositeSyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeException;

/**
 * Class that defines OpenL engine manager implementation for source processing
 * operations.
 * 
 */
public class OpenLSourceManager extends OpenLHolder {

    public static final String EXTERNAL_DEPENDENCIES_KEY = "external-dependencies";

    public static final String ADDITIONAL_WARN_MESSAGES_KEY = "additional-warn-messages";
    public static final String ADDITIONAL_ERROR_MESSAGES_KEY = "additional-error-messages";

    private OpenLParseManager parseManager;
    private OpenLBindManager bindManager;

    /**
     * Create new instance of OpenL engine manager.
     * 
     * @param openl {@link OpenL} instance
     */
    public OpenLSourceManager(OpenL openl) {

        super(openl);

        bindManager = new OpenLBindManager(openl);
        parseManager = new OpenLParseManager(openl);

    }

    /**
     * Parses and binds source.
     * 
     * @param source source
     * @param sourceType type of source
     * @return processed code descriptor
     */
    public ProcessedCode processSource(IOpenSourceCodeModule source, SourceType sourceType) {
        return processSource(source, sourceType, null, false, null);
    }

    /**
     * Parses and binds source.
     * 
     * @param source source
     * @param sourceType type of source
     * @return processed code descriptor
     */
    public ProcessedCode processSource(IOpenSourceCodeModule source,
            SourceType sourceType,
            IDependencyManager dependencyManager) {
        return processSource(source, sourceType, null, false, dependencyManager);
    }

    /**
     * Parses and binds source.
     * 
     * @param source source
     * @param sourceType type of source
     * @param bindingContextDelegator binding context
     * @param ignoreErrors define a flag that indicates to suppress errors or
     *            break source processing when an error has occurred
     * @return processed code descriptor
     */
    @SuppressWarnings("unchecked")
    public ProcessedCode processSource(IOpenSourceCodeModule source,
            SourceType sourceType,
            IBindingContextDelegator bindingContextDelegator,
            boolean ignoreErrors,
            IDependencyManager dependencyManager) {

        IParsedCode parsedCode = parseManager.parseSource(source, sourceType);
        SyntaxNodeException[] parsingErrors = parsedCode.getErrors();

        if (!ignoreErrors && parsingErrors.length > 0) {
            throw new CompositeSyntaxNodeException("Parsing Error:", parsingErrors);
        }

        // compile source dependencies
        if (SourceType.MODULE.equals(sourceType)) {

            Set<CompiledOpenClass> compiledDependencies = new LinkedHashSet<CompiledOpenClass>();
            List<IDependency> externalDependencies = getExternalDependencies(source);
            Collection<IDependency> dependencies = CollectionUtils.union(externalDependencies,
                Arrays.asList(parsedCode.getDependencies()));

            List<OpenLMessage> messagesRelatedToDependencies = new ArrayList<OpenLMessage>();

            if (dependencies != null && dependencies.size() > 0) {
                if (dependencyManager != null) {
                    for (IDependency dependency : dependencies) {
                        try {
                            CompiledDependency loadedDependency = dependencyManager.loadDependency(dependency);
                            validateDependency(loadedDependency);
                            OpenLBundleClassLoader currentClassLoader = (OpenLBundleClassLoader) Thread.currentThread()
                                .getContextClassLoader();
                            if (loadedDependency.getClassLoader() != currentClassLoader) {
                                currentClassLoader.addClassLoader(loadedDependency.getClassLoader());
                            }
                            compiledDependencies.add(loadedDependency.getCompiledOpenClass());
                            
                            if (loadedDependency.getCompiledOpenClass().getOpenClassWithErrors() instanceof ExtendableModuleOpenClass){
                                ExtendableModuleOpenClass extendableModuleOpenClass = (ExtendableModuleOpenClass) loadedDependency.getCompiledOpenClass().getOpenClassWithErrors();
                                extendableModuleOpenClass.applyToDependentParsedCode(parsedCode);
                            }
                            
                            OpenLMessages.getCurrentInstance().clear();// clear
                                                                       // all
                                                                       // messages
                                                                       // from
                                                                       // dependency
                        } catch (Exception e) {
                            messagesRelatedToDependencies.addAll(OpenLMessagesUtils.newMessages(e));
                        }
                    }
                    OpenLMessages.getCurrentInstance().addMessages(messagesRelatedToDependencies);

                } else {
                    OpenLMessagesUtils.addError("Can't load dependency. Dependency manager is not defined.");
                }
            }

            parsedCode.setCompiledDependencies(compiledDependencies);
        }

        Map<String, Object> externalParams = source.getParams();

        if (externalParams != null) {
            parsedCode.setExternalParams(externalParams);
            if (externalParams.containsKey(ADDITIONAL_WARN_MESSAGES_KEY)) {
                Set<String> warnMessages = (Set<String>) externalParams.get(ADDITIONAL_WARN_MESSAGES_KEY);
                for (String warnMessage : warnMessages) {
                    OpenLMessagesUtils.addWarn(warnMessage);
                }
            }
            if (externalParams.containsKey(ADDITIONAL_ERROR_MESSAGES_KEY)) {
                Set<String> warnMessages = (Set<String>) externalParams.get(ADDITIONAL_ERROR_MESSAGES_KEY);
                for (String warnMessage : warnMessages) {
                    OpenLMessagesUtils.addError(warnMessage);
                }
            }
        }

        IBoundCode boundCode = bindManager.bindCode(bindingContextDelegator, parsedCode);

        SyntaxNodeException[] bindingErrors = boundCode.getErrors();

        if (!ignoreErrors && bindingErrors.length > 0) {
            throw new CompositeSyntaxNodeException("Binding Error:", bindingErrors);
        }

        ProcessedCode processedCode = new ProcessedCode();
        processedCode.setParsedCode(parsedCode);
        processedCode.setBoundCode(boundCode);

        return processedCode;
    }

    private void validateDependency(CompiledDependency compiledDependency) {
        if (compiledDependency.getCompiledOpenClass().hasErrors()) {
            String message = String.format("Dependency module %s has critical errors",
                compiledDependency.getDependencyName());
            OpenLMessagesUtils.addError(message);
        }
    }

    @SuppressWarnings("unchecked")
    private List<IDependency> getExternalDependencies(IOpenSourceCodeModule source) {

        List<IDependency> dependencies = new ArrayList<IDependency>();
        Map<String, Object> params = source.getParams();

        if (params != null) {
            List<IDependency> externalDependencies = (List<IDependency>) params.get(EXTERNAL_DEPENDENCIES_KEY);

            if (externalDependencies != null) {
                dependencies.addAll(externalDependencies);
            }
        }

        return dependencies;
    }
}
