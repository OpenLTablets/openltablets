package org.openl.rules.project.dependencies;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openl.CompiledOpenClass;
import org.openl.classloader.OpenLClassLoader;
import org.openl.classloader.OpenLClassLoaderHelper;
import org.openl.classloader.SimpleBundleClassLoader;
import org.openl.dependency.CompiledDependency;
import org.openl.dependency.IDependencyManager;
import org.openl.dependency.loader.IDependencyLoader;
import org.openl.exception.OpenlNotCheckedException;
import org.openl.rules.project.instantiation.RulesInstantiationStrategyFactory;
import org.openl.rules.project.instantiation.SingleModuleInstantiationStrategy;
import org.openl.rules.project.model.Module;

public class RulesModuleDependencyLoader implements IDependencyLoader {

    private Map<String, Module> modulesMap = new HashMap<String, Module>();
    
    public RulesModuleDependencyLoader(Collection<Module> modules) {
        init(modules);
    }
    
    public CompiledDependency load(String dependencyName, IDependencyManager dependencyManager) {

        Module dependencyModule = findDependencyModule(dependencyName);
        
        if(dependencyModule != null) {
     
            try {
                URL[] urls = dependencyModule.getProject().getClassPathUrls();
                ClassLoader oldClassLoader = OpenLClassLoaderHelper.getContextClassLoader();
                
                // create classloader for the dependency. With the parent for current module.
                //
                OpenLClassLoader moduleClassLoader = new SimpleBundleClassLoader(oldClassLoader);
                OpenLClassLoaderHelper.extendClasspath(moduleClassLoader, urls);
                
                SingleModuleInstantiationStrategy strategy = RulesInstantiationStrategyFactory.getStrategy(dependencyModule, 
                    dependencyManager.isExecutionMode(), dependencyManager, moduleClassLoader);
                strategy.setExternalParameters(dependencyManager.getExternalParameters());
                CompiledOpenClass compiledOpenClass = strategy.compile();
                
                return new CompiledDependency(dependencyName, compiledOpenClass);
            } catch (Exception e) {
                throw new OpenlNotCheckedException(String.format("Cannot load dependency '%s'", dependencyName) , e);
            }
        }

        return null;
    }
    
    protected Module findDependencyModule(String moduleName){
        return modulesMap.get(moduleName);
    }
    
    private void init(Collection<Module> modules) {
        for (Module module : modules) {
            String key = module.getName();
            modulesMap.put(key, module);
        }
    }
    
}
