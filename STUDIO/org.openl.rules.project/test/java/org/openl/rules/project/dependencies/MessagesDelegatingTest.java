package org.openl.rules.project.dependencies;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openl.CompiledOpenClass;
import org.openl.binding.exception.DuplicatedMethodException;
import org.openl.dependency.loader.IDependencyLoader;
import org.openl.message.OpenLErrorMessage;
import org.openl.message.OpenLMessage;
import org.openl.message.OpenLMessagesUtils;
import org.openl.message.Severity;
import org.openl.rules.lang.xls.IXlsTableNames;
import org.openl.rules.project.instantiation.SimpleMultiModuleInstantiationStrategy;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.resolving.ResolvingStrategy;
import org.openl.rules.project.resolving.RulesProjectResolver;
import org.openl.syntax.code.Dependency;
import org.openl.syntax.code.DependencyType;
import org.openl.syntax.code.IDependency;
import org.openl.syntax.impl.IdentifierNode;

public class MessagesDelegatingTest {
    private List<Module> modules;
    private RulesProjectDependencyManager dependencyManager;

    @Before
    public void init() throws Exception {
        File rulesFolder = new File("test/resources/modules_with_errors/");
        ResolvingStrategy resolvingStrategy = RulesProjectResolver.loadProjectResolverFromClassPath()
            .isRulesProject(rulesFolder);
        modules = resolvingStrategy.resolveProject(rulesFolder).getModules();
        dependencyManager = new RulesProjectDependencyManager();
        List<IDependencyLoader> dependencyLoaders = new ArrayList<IDependencyLoader>(1);
        dependencyLoaders.add(new RulesModuleDependencyLoader(modules));
        dependencyManager.setDependencyLoaders(dependencyLoaders);
    }

    private Module findModuleByName(String moduleName) {
        for (Module module : modules) {
            if (module.getName().equals(moduleName)) {
                return module;
            }
        }
        return null;
    }

    private static IDependency getDependencyForModule(String moduleName) {
        return new Dependency(DependencyType.MODULE,
            new IdentifierNode(IXlsTableNames.DEPENDENCY, null, moduleName, null));
    }

    private CompiledOpenClass getCompiledOpenClassForModule(String moduleName) throws Exception {
        // it is passed through the dependency manager to receive the same
        // instances of OpenLMessages
        IDependency dependency = getDependencyForModule(moduleName);
        return dependencyManager.loadDependency(dependency).getCompiledOpenClass();
    }

    @Test
    public void testMessagesDelegatingFromDependencies() throws Exception {
        CompiledOpenClass compiledRules = getCompiledOpenClassForModule("Rules");
        assertTrue(compiledRules.getMessages().size() > 0);
        CompiledOpenClass compiledRules2 = getCompiledOpenClassForModule("Rules2");
        assertTrue(compiledRules2.getMessages().size() > compiledRules.getMessages().size());
        assertTrue(compiledRules2.getMessages().containsAll(compiledRules.getMessages()));
        CompiledOpenClass compiledRules3 = getCompiledOpenClassForModule("Rules3");
        assertTrue(compiledRules3.getMessages().size() > compiledRules.getMessages().size());
        assertTrue(compiledRules3.getMessages().containsAll(compiledRules.getMessages()));
        assertTrue(compiledRules3.getMessages().size() > compiledRules2.getMessages().size());
        assertTrue(compiledRules3.getMessages().containsAll(compiledRules2.getMessages()));
    }

    @Test
    public void testMessagesGatheringInMultimodule() throws Exception {
        List<Module> forGrouping = new ArrayList<Module>();
        forGrouping.add(findModuleByName("Rules3"));
        forGrouping.add(findModuleByName("Rules4"));
        forGrouping.add(findModuleByName("Rules5"));
        SimpleMultiModuleInstantiationStrategy strategy = new SimpleMultiModuleInstantiationStrategy(forGrouping, true);
        CompiledOpenClass compiledMultiModule = strategy.compile();
        for (Module module : modules) {
            CompiledOpenClass compiledModule = getCompiledOpenClassForModule(module.getName());
            compiledMultiModule.getMessages().containsAll(compiledModule.getMessages());
        }

        assertFalse("During compilation DuplicatedMethodException must not be thrown", hasDuplicatedMethodException(compiledMultiModule));
    }

    @Test
    public void testDublicateTableDefenitionInMultimodule() throws Exception {
        List<Module> forGrouping = new ArrayList<Module>();
        forGrouping.add(findModuleByName("Rules"));
        forGrouping.add(findModuleByName("Rules2"));
        forGrouping.add(findModuleByName("Rules3"));
        forGrouping.add(findModuleByName("Rules6"));
        SimpleMultiModuleInstantiationStrategy strategy = new SimpleMultiModuleInstantiationStrategy(forGrouping, true);
        CompiledOpenClass compiledMultiModule = strategy.compile();
        for (Module module : modules) {
            CompiledOpenClass compiledModule = getCompiledOpenClassForModule(module.getName());
            compiledMultiModule.getMessages().containsAll(compiledModule.getMessages());
        }

        assertTrue("During compilation DuplicatedMethodException must be thrown", hasDuplicatedMethodException(compiledMultiModule));
    }

    private boolean hasDuplicatedMethodException(CompiledOpenClass compiledMultiModule) {
        boolean hasDuplicatedMethodException = false;
        for (OpenLMessage error : OpenLMessagesUtils.filterMessagesBySeverity(compiledMultiModule.getMessages(), Severity.ERROR)) {
            if (error instanceof OpenLErrorMessage){
                Throwable cause = ((OpenLErrorMessage) error).getError().getCause();
                if (cause instanceof DuplicatedMethodException) {
                    hasDuplicatedMethodException = true;
                    break;
                }
            }
        }
        return hasDuplicatedMethodException;
    }

}
