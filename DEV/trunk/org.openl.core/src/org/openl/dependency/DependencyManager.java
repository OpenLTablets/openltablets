package org.openl.dependency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openl.dependency.loader.IDependencyLoader;
import org.openl.exception.OpenLCompilationException;
import org.openl.syntax.code.IDependency;

public abstract class DependencyManager implements IDependencyManager {
    
    private boolean executionMode;
    private Map<String, Object> externalParameters;
    
    private Map<String, CompiledDependency> compiledDependencies = new HashMap<String, CompiledDependency>();  
    
    public CompiledDependency loadDependency(IDependency dependency) throws OpenLCompilationException {

        String dependencyName = dependency.getNode().getIdentifier();

        if (compiledDependencies.containsKey(dependencyName)) {
            return compiledDependencies.get(dependencyName);
        }

        CompiledDependency compiledDependency = handleLoadDependency(dependency);

        if (compiledDependency == null) {
            throw new OpenLCompilationException(String.format("Dependency with name '%s' is not found", dependencyName), 
                null, dependency.getNode().getSourceLocation());
        }   
        
        compiledDependencies.put(dependencyName, compiledDependency);

        return compiledDependency;
    }

    public void reset(IDependency dependency) {
        String dependencyName = dependency.getNode().getIdentifier();
        if (compiledDependencies.containsKey(dependencyName)) {
            compiledDependencies.remove(dependencyName);
        }
    }

    public void resetAll() {
    	compiledDependencies.clear();
    }
    
    /**
     * In execution mode all meta info that is not used in rules running is being cleaned.
     * 
     * @param executionMode flag indicating is it execution mode or not. 
     * 
     */
    public void setExecutionMode(boolean executionMode) {
        this.executionMode = executionMode;
    }

    public boolean isExecutionMode() {
        return executionMode;
    }

    public abstract List<IDependencyLoader> getDependencyLoaders();

    /**
     * Handles loading dependent modules. This method should not cache
     * dependencies (method {@link #loadDependency(IDependency)} already uses
     * caching) Default implementation uses dependency loaders to load the
     * dependency. Can be overriden to redefine behavior.
     * 
     * @param dependency dependency to load
     * @return loaded and compiled dependency
     * @throws OpenLCompilationException if exception during compilation is
     *             occured
     */
    protected CompiledDependency handleLoadDependency(IDependency dependency) throws OpenLCompilationException {
        List<IDependencyLoader> dependencyLoaders = getDependencyLoaders();
        return loadDependency(dependency.getNode().getIdentifier(), dependencyLoaders);
    }
    
    private CompiledDependency loadDependency(String dependencyName, List<IDependencyLoader> loaders) {
        
        for (IDependencyLoader loader : loaders) {
            CompiledDependency dependency = loader.load(dependencyName, this);
            
            if (dependency != null) {
                return dependency;
            }
        }
        
        return null;
    }
    
    @Override
    public Map<String, Object> getExternalParameters() {
        return externalParameters;
    }
    
    public void setExternalParameters(Map<String, Object> parameters) {
        this.externalParameters = parameters;
    }
}
