package org.openl.rules.ruleservice.publish.lazy;

import java.util.List;
import java.util.Map;

import org.openl.CompiledOpenClass;
import org.openl.exception.OpenLCompilationException;
import org.openl.exception.OpenlNotCheckedException;
import org.openl.message.OpenLMessage;
import org.openl.rules.ruleservice.core.LazyRuleServiceDependencyLoader;
import org.openl.rules.ruleservice.core.RuleServiceDeploymentRelatedDependencyManager;
import org.openl.syntax.code.IDependency;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.types.IOpenClass;

public class LazyCompiledOpenClass extends CompiledOpenClass {

    private LazyRuleServiceDependencyLoader lazyRuleServiceDependencyLoader;
    private RuleServiceDeploymentRelatedDependencyManager dependencyManager;
    private IDependency dependency;
    
    public LazyCompiledOpenClass(RuleServiceDeploymentRelatedDependencyManager dependencyManager, LazyRuleServiceDependencyLoader lazyRuleServiceDependencyLoader, IDependency dependency) {
        super(null, null, null, null);
        if (lazyRuleServiceDependencyLoader == null) {
            throw new IllegalArgumentException("lazyRuleServiceDependencyLoader can't be null!");
        }
        if (dependency == null) {
            throw new IllegalArgumentException("dependency can't be null!");
        }
        if (dependencyManager == null) {
            throw new IllegalArgumentException("dependencyManager can't be null!");
        }
        this.dependencyManager = dependencyManager;
        this.lazyRuleServiceDependencyLoader = lazyRuleServiceDependencyLoader;
        this.dependency = dependency;
    }

    protected CompiledOpenClass getCompiledOpenClass() {
        try {
            CompiledOpenClass compiledOpenClass = lazyRuleServiceDependencyLoader.compile(dependency.getNode().getIdentifier(), dependencyManager);
            return compiledOpenClass;
        } catch (OpenLCompilationException e) {
            throw new OpenlNotCheckedException("Shouldn't happen! Compilation validated before!!");
        }

    }

    @Override
    public SyntaxNodeException[] getBindingErrors() {
        return getCompiledOpenClass().getBindingErrors();
    }

    @Override
    public IOpenClass getOpenClass() {
        return getCompiledOpenClass().getOpenClass();
    }

    @Override
    public IOpenClass getOpenClassWithErrors() {
        return getCompiledOpenClass().getOpenClassWithErrors();
    }

    @Override
    public int hashCode() {
        return getCompiledOpenClass().hashCode();
    }

    @Override
    public SyntaxNodeException[] getParsingErrors() {
        return getCompiledOpenClass().getParsingErrors();
    }

    @Override
    public boolean hasErrors() {
        return getCompiledOpenClass().hasErrors();
    }

    @Override
    public void throwErrorExceptionsIfAny() {
        getCompiledOpenClass().throwErrorExceptionsIfAny();
    }

    @Override
    public List<OpenLMessage> getMessages() {
        return getCompiledOpenClass().getMessages();
    }

    @Override
    public Map<String, IOpenClass> getTypes() {
        return getCompiledOpenClass().getTypes();
    }

    @Override
    public ClassLoader getClassLoader() {
        return getCompiledOpenClass().getClassLoader();
    }

    @Override
    public boolean equals(Object obj) {
        return getCompiledOpenClass().equals(obj);
    }

    @Override
    public String toString() {
        return getCompiledOpenClass().toString();
    }
}
