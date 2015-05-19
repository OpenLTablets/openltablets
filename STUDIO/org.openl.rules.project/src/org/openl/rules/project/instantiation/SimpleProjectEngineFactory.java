package org.openl.rules.project.instantiation;

import org.openl.CompiledOpenClass;
import org.openl.dependency.IDependencyManager;
import org.openl.rules.project.dependencies.ProjectExternalDependenciesHelper;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.model.ProjectDependencyDescriptor;
import org.openl.rules.project.model.ProjectDescriptor;
import org.openl.rules.project.resolving.ProjectResolvingException;
import org.openl.rules.project.resolving.ResolvingStrategy;
import org.openl.rules.project.resolving.RulesProjectResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class SimpleProjectEngineFactory<T> implements ProjectEngineFactory<T> {

    private final Logger log = LoggerFactory.getLogger(SimpleProjectEngineFactory.class);

    private boolean singleModuleMode = false;
    private Map<String, Object> externalParameters = new HashMap<String, Object>();
    private boolean provideRuntimeContext = false;
    private boolean executionMode = true;
    private Class<?> interfaceClass = null;
    private String module;
    private File workspace;
    private File project;
    private ProjectDescriptor projectDescriptor;

    public static class SimpleProjectEngineFactoryBuilder<T> {
        private String project;
        private String workspace;
        private String module;
        private boolean provideRuntimeContext = false;
        private Class<T> interfaceClass = null;
        private boolean executionMode = true;

        public SimpleProjectEngineFactoryBuilder<T> setProject(String project) {
            if (project == null || project.isEmpty()) {
                throw new IllegalArgumentException("project arg can't be null or empty!");
            }
            this.project = project;
            return this;
        }

        public SimpleProjectEngineFactoryBuilder<T> setInterfaceClass(Class<T> interfaceClass) {
            this.interfaceClass = interfaceClass;
            return this;
        }

        public SimpleProjectEngineFactoryBuilder<T> setExecutionMode(boolean executionMode) {
            this.executionMode = executionMode;
            return this;
        }

        public SimpleProjectEngineFactoryBuilder<T> setProvideRuntimeContext(boolean provideRuntimeContext) {
            this.provideRuntimeContext = provideRuntimeContext;
            return this;
        }

        public SimpleProjectEngineFactoryBuilder<T> setModule(String module) {
            if (module == null || module.isEmpty()) {
                throw new IllegalArgumentException("module arg can't be null or empty!");
            }
            this.module = module;
            return this;
        }

        public SimpleProjectEngineFactoryBuilder<T> setWorkspace(String workspace) {
            if (workspace == null || workspace.isEmpty()) {
                throw new IllegalArgumentException("workspace arg can't be null or empty!");
            }
            this.workspace = workspace;
            return this;
        }

        public SimpleProjectEngineFactory<T> build() {
            if (project == null || project.isEmpty()) {
                throw new IllegalArgumentException("project can't be null or empty!");
            }
            File projectFile = new File(project);
            File workspaceFile = workspace == null ? null : new File(workspace);
            if (module == null) {
                return new SimpleProjectEngineFactory<T>(projectFile,
                    workspaceFile,
                    interfaceClass,
                    provideRuntimeContext,
                    executionMode);
            } else {
                return new SimpleProjectEngineFactory<T>(projectFile,
                    workspaceFile,
                    module,
                    interfaceClass,
                    provideRuntimeContext,
                    executionMode);
            }

        }

    }

    private SimpleProjectEngineFactory(File project,
                                       File workspace,
                                       Class<T> interfaceClass,
                                       boolean provideRuntimeContext,
                                       boolean executionMode) {
        if (project == null) {
            throw new IllegalArgumentException("project arg can't be null!");
        }
        if (workspace != null && !workspace.isDirectory()) {
            throw new IllegalArgumentException("workspace should be a directory with projects!");
        }
        this.project = project;
        this.workspace = workspace;
        setInterfaceClass(interfaceClass);
        this.provideRuntimeContext = provideRuntimeContext;
        this.singleModuleMode = false;
        this.executionMode = executionMode;
    }

    private SimpleProjectEngineFactory(File project,
                                       File workspace,
                                       String module,
                                       Class<T> interfaceClass,
                                       boolean provideRuntimeContext,
                                       boolean executionMode) {
        this(project, workspace, interfaceClass, provideRuntimeContext, executionMode);
        if (module == null || module.isEmpty()) {
            throw new IllegalArgumentException("module arg can't be null or empty!");
        }
        this.module = module;
        this.singleModuleMode = true;
    }

    private RulesInstantiationStrategy rulesInstantiationStrategy = null;

    protected RulesInstantiationStrategy getStrategy(Collection<Module> modules, IDependencyManager dependencyManager) {
        if (rulesInstantiationStrategy == null) {
            switch (modules.size()) {
                case 0:
                    throw new IllegalStateException("There are no modules to instantiate.");
                case 1:
                    rulesInstantiationStrategy = RulesInstantiationStrategyFactory.getStrategy(modules.iterator()
                            .next(), isExecutionMode(), dependencyManager);
                default:
                    rulesInstantiationStrategy = new SimpleMultiModuleInstantiationStrategy(modules, dependencyManager, isExecutionMode());
            }
        }
        return rulesInstantiationStrategy;
    }

    private List<ProjectDescriptor> getDependentProjects(ProjectDescriptor project,
                                                         Collection<ProjectDescriptor> projectsInWorkspace) {
        List<ProjectDescriptor> projectDescriptors = new ArrayList<ProjectDescriptor>();
        addDependentProjects(projectDescriptors, project, projectsInWorkspace);
        return projectDescriptors;
    }

    private void addDependentProjects(List<ProjectDescriptor> projectDescriptors,
                                      ProjectDescriptor project,
                                      Collection<ProjectDescriptor> projectsInWorkspace) {
        if (project.getDependencies() != null) {
            for (ProjectDependencyDescriptor dependencyDescriptor : project.getDependencies()) {
                boolean found = false;
                for (ProjectDescriptor projectDescriptor : projectsInWorkspace) {
                    if (dependencyDescriptor.getName().equals(projectDescriptor.getName())) {
                        projectDescriptors.add(projectDescriptor);
                        addDependentProjects(projectDescriptors, projectDescriptor, projectsInWorkspace);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    log.warn("Dependency '{}' for project '{}' not found", dependencyDescriptor.getName(), project.getName());
                }
            }
        }
    }

    protected IDependencyManager buildDependencyManager() throws ProjectResolvingException {
        Collection<ProjectDescriptor> projectDescriptors = new ArrayList<ProjectDescriptor>();
        RulesProjectResolver projectResolver = RulesProjectResolver.loadProjectResolverFromClassPath();
        if (workspace != null) {
            projectResolver.setWorkspace(workspace.getPath());
            projectDescriptors.addAll(getDependentProjects(getProjectDescriptor(), projectResolver.listOpenLProjects()));
        }
        projectDescriptors.add(getProjectDescriptor());
        return new SimpleProjectDependencyManager(projectDescriptors, isSingleModuleMode(), isExecutionMode());
    }

    private IDependencyManager dependencyManager = null;

    protected synchronized final IDependencyManager getDependencyManager() throws ProjectResolvingException {
        if (dependencyManager == null) {
            dependencyManager = buildDependencyManager();
        }
        return dependencyManager;
    }

    public boolean isExecutionMode() {
        return executionMode;
    }

    public boolean isSingleModuleMode() {
        return singleModuleMode;
    }

    public boolean isProvideRuntimeContext() {
        return provideRuntimeContext;
    }

    public void setProvideRuntimeContext(boolean provideRuntimeContext) {
        this.provideRuntimeContext = provideRuntimeContext;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public Map<String, Object> getExternalParameters() {
        return externalParameters;
    }

    public void setExternalParameters(Map<String, Object> externalParameters) {
        this.externalParameters = externalParameters;
    }

    private void resolveInterface(RulesInstantiationStrategy instantiationStrategy) throws RulesInstantiationException,
            ClassNotFoundException {
        if (getInterfaceClass() != null) {
            instantiationStrategy.setServiceClass(getInterfaceClass());
        } else {
            log.info("Class is undefined for factory. Generated interface will be used.");
            this.interfaceClass = instantiationStrategy.getInstanceClass();
        }
    }

    @SuppressWarnings("unchecked")
    public T newInstance() throws RulesInstantiationException, ProjectResolvingException, ClassNotFoundException {
        return (T) getRulesInstantiationStrategy().instantiate();
    }

    protected synchronized final ProjectDescriptor getProjectDescriptor() throws ProjectResolvingException {
        if (this.projectDescriptor == null) {
            RulesProjectResolver projectResolver = RulesProjectResolver.loadProjectResolverFromClassPath();
            ResolvingStrategy resolvingStrategy = projectResolver.isRulesProject(project);
            if (resolvingStrategy == null) {
                throw new ProjectResolvingException("Defined location is not a OpenL project.");
            }
            ProjectDescriptor pd = resolvingStrategy.resolveProject(project);
            this.projectDescriptor = pd;
        }
        return this.projectDescriptor;
    }

    protected RulesInstantiationStrategy instantiationStrategy;

    protected final synchronized RulesInstantiationStrategy getRulesInstantiationStrategy() throws RulesInstantiationException,
            ProjectResolvingException,
            ClassNotFoundException {
        if (rulesInstantiationStrategy == null) {
            RulesInstantiationStrategy instantiationStrategy = null;
            if (!isSingleModuleMode()) {
                instantiationStrategy = getStrategy(getProjectDescriptor().getModules(), getDependencyManager());
            } else {
                for (Module module : getProjectDescriptor().getModules()) {
                    if (module.getName().equals(this.module)) {
                        Collection<Module> modules = new ArrayList<Module>();
                        modules.add(module);
                        instantiationStrategy = getStrategy(modules, getDependencyManager());
                        break;
                    }
                }
                if (instantiationStrategy == null) {
                    throw new RulesInstantiationException("Module isn't found in project!");
                }
            }
            if (isProvideRuntimeContext()) {
                instantiationStrategy = new RuntimeContextInstantiationStrategyEnhancer(instantiationStrategy);
            }

            Map<String, Object> parameters = new HashMap<String, Object>(externalParameters);
            if (!isSingleModuleMode()) {
                parameters = ProjectExternalDependenciesHelper.getExternalParamsWithProjectDependencies(externalParameters,
                        getProjectDescriptor().getModules());
            }
            instantiationStrategy.setExternalParameters(parameters);
            try {
                resolveInterface(instantiationStrategy);
            } catch (Exception ex) {
                throw new RulesInstantiationException(ex);
            }
            rulesInstantiationStrategy = instantiationStrategy;
        }
        return rulesInstantiationStrategy;
    }

    public CompiledOpenClass getCompiledOpenClass() throws RulesInstantiationException,
            ProjectResolvingException,
            ClassNotFoundException {
        return getRulesInstantiationStrategy().compile();
    }

}
