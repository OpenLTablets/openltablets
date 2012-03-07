package org.openl.rules.ruleservice.publish.cache;

import static junit.framework.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openl.dependency.loader.IDependencyLoader;
import org.openl.rules.project.dependencies.RulesModuleDependencyLoader;
import org.openl.rules.project.dependencies.RulesProjectDependencyManager;
import org.openl.rules.project.instantiation.ApiBasedEngineFactoryInstantiationStrategy;
import org.openl.rules.project.instantiation.ReloadType;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.model.PathEntry;
import org.openl.rules.project.model.ProjectDescriptor;
import org.openl.rules.project.resolving.SimpleXlsResolvingStrategy;
import org.openl.types.IOpenMethod;

public class LazyMultiModuleEngineFactoryTest {
    private static RulesProjectDependencyManager dependencyManager;

    @BeforeClass
    public static void init() {
        dependencyManager = new RulesProjectDependencyManager();
        List<IDependencyLoader> loaders = new ArrayList<IDependencyLoader>();
        loaders.add(new RulesModuleDependencyLoader(new SimpleXlsResolvingStrategy().resolveProject(
                new File("./test-resources/multi-module_overloaded/project3")).getModules()));
        dependencyManager.setDependencyLoaders(loaders);
    }

    /**
     * Test is concluded in module paths.
     */
    public static Collection<Module> resolveAllModules(String root) {
        Collection<Module> modules = new ArrayList<Module>();
        ProjectDescriptor projectDescriptor = new ProjectDescriptor();
        Module module1_1 = new Module();
        module1_1.setName("Module1_1");
        module1_1.setRulesRootPath(new PathEntry("./test-resources/multi-module_overloaded/project1/Module1_1.xlsx"));
        module1_1.setProject(projectDescriptor);
        modules.add(module1_1);
        Module module2_1 = new Module();
        module2_1.setName("Module2_1");
        module2_1.setRulesRootPath(new PathEntry("/test-resources/multi-module_overloaded/project2/Module3_1.xlsx"));
        module2_1.setProject(projectDescriptor);
        modules.add(module1_1);
        Module module3_1 = new Module();
        module3_1.setName("Module3_1");
        module3_1.setRulesRootPath(new PathEntry(new File(".").getAbsolutePath()
                + "/test-resources/../test-resources/multi-module_overloaded/project3/Module3_1.xlsx"));
        module3_1.setProject(projectDescriptor);
        modules.add(module3_1);
        return modules;
    }

    @Test
    public void testModulesMatching() throws Exception {
        Collection<Module> modules = resolveAllModules("./test-resources/multi-module_overloaded");
        LazyMultiModuleEngineFactory factory = new LazyMultiModuleEngineFactory(modules);
        factory.setDependencyManager(dependencyManager);
        checkModules(factory, modules);
    }

    private void checkModules(LazyMultiModuleEngineFactory factory, Collection<Module> modules) throws Exception {
        for (Module module : modules) {
            for (IOpenMethod method : getMethodsFromModule(module)) {
                assertEquals(factory.getModuleForMember(method), module);
            }
        }
    }

    private List<IOpenMethod> getMethodsFromModule(Module module) throws Exception {
        ApiBasedEngineFactoryInstantiationStrategy instantiationStrategy = new ApiBasedEngineFactoryInstantiationStrategy(
                module, false, dependencyManager);
        return instantiationStrategy.compile(ReloadType.NO).getOpenClass().getMethods();
    }
}
