package org.openl.rules.ruleservice.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.openl.dependency.CompiledDependency;
import org.openl.dependency.loader.IDependencyLoader;
import org.openl.exception.OpenLCompilationException;
import org.openl.rules.common.ProjectException;
import org.openl.rules.project.IRulesDeploySerializer;
import org.openl.rules.project.abstraction.AProject;
import org.openl.rules.project.abstraction.AProjectArtefact;
import org.openl.rules.project.abstraction.AProjectResource;
import org.openl.rules.project.abstraction.Deployment;
import org.openl.rules.project.dependencies.ProjectExternalDependenciesHelper;
import org.openl.rules.project.instantiation.AbstractProjectDependencyManager;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.model.ProjectDescriptor;
import org.openl.rules.project.model.RulesDeploy;
import org.openl.rules.project.xml.XmlRulesDeploySerializer;
import org.openl.rules.ruleservice.conf.LastVersionProjectsServiceConfigurer;
import org.openl.rules.ruleservice.loader.RuleServiceLoader;
import org.openl.syntax.code.IDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

public class RuleServiceDeploymentRelatedDependencyManager extends AbstractProjectDependencyManager {

    private final Logger log = LoggerFactory.getLogger(RuleServiceDeploymentRelatedDependencyManager.class);

    private RuleServiceLoader ruleServiceLoader;

    private DeploymentDescription deploymentDescription;

    private Collection<ProjectDescriptor> projectDescriptors = null;
    List<IDependencyLoader> dependencyLoaders = null;
    
    private IRulesDeploySerializer rulesDeploySerializer = new XmlRulesDeploySerializer();

    private boolean lazy;
    
    private PathMatcher wildcardPatternMatcher = new AntPathMatcher();

    public boolean isLazy() {
        return lazy;
    }

    public RuleServiceLoader getRuleServiceLoader() {
        return ruleServiceLoader;
    }

    private static class SemaphoreHolder {
        private static Semaphore limitCompilationThreadsSemaphore = new Semaphore(RuleServiceStaticConfigurationUtil.getMaxThreadsForCompile());
        private static ThreadLocal<Object> threadsMarker = new ThreadLocal<Object>();
    }

    @Override
    public CompiledDependency loadDependency(IDependency dependency) throws OpenLCompilationException {
        try {
            boolean requiredSemophore = SemaphoreHolder.threadsMarker.get() == null;
            try {
                if (requiredSemophore) {
                    SemaphoreHolder.threadsMarker.set(Thread.currentThread());
                    SemaphoreHolder.limitCompilationThreadsSemaphore.acquire();
                }
                return super.loadDependency(dependency);
            } finally {
                if (requiredSemophore) {
                    SemaphoreHolder.threadsMarker.remove();
                    SemaphoreHolder.limitCompilationThreadsSemaphore.release();
                }
            }
        } catch (InterruptedException e) {
            throw new OpenLCompilationException("Interrupter exception!", e);
        }
    }

    public RuleServiceDeploymentRelatedDependencyManager(DeploymentDescription deploymentDescription,
            RuleServiceLoader ruleServiceLoader) {
        this(deploymentDescription, ruleServiceLoader, false);
    }

    public RuleServiceDeploymentRelatedDependencyManager(DeploymentDescription deploymentDescription,
            RuleServiceLoader ruleServiceLoader,
            boolean lazy) {
        if (deploymentDescription == null) {
            throw new IllegalArgumentException("deploymentDescription can't be null!");
        }
        if (ruleServiceLoader == null) {
            throw new IllegalArgumentException("ruleService can't be null!");
        }
        this.deploymentDescription = deploymentDescription;
        this.ruleServiceLoader = ruleServiceLoader;
        this.lazy = lazy;
        super.setExecutionMode(true);
    }

    @Override
    public void setExecutionMode(boolean executionMode) {
        throw new UnsupportedOperationException("This dependency manager supports only executionMode=true");
    }

    @Override
    public void reset(IDependency dependency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<ProjectDescriptor> getProjectDescriptors() {
        if (dependencyLoaders == null){
            initDependencyLoaders();
        }
        return projectDescriptors;
    }

    @Override
    public List<IDependencyLoader> getDependencyLoaders() {
        if (dependencyLoaders == null){
            initDependencyLoaders();
        }
        return dependencyLoaders;
    }

    private boolean compilationAfterLazyCompilationRequred(Set<String> wildcardPatterns, String moduleName){
        for (String pattern : wildcardPatterns){
            if (wildcardPatternMatcher.match(pattern, moduleName)){
                return true;
            }
        }
        return false;
    }
    
    public final IRulesDeploySerializer getRulesDeploySerializer() {
        return rulesDeploySerializer;
    }
    
    private synchronized void initDependencyLoaders() {
        if (projectDescriptors == null && dependencyLoaders == null) {
            dependencyLoaders = new ArrayList<IDependencyLoader>();
            projectDescriptors = new ArrayList<ProjectDescriptor>();
            Collection<Deployment> deployments = ruleServiceLoader.getDeployments();
            for (Deployment deployment : deployments) {
                String deploymentName = deployment.getDeploymentName();
                if (deploymentDescription.getName().equals(deploymentName) && deploymentDescription.getVersion()
                    .equals(deployment.getCommonVersion())) {
                    for (AProject project : deployment.getProjects()) {
                        try {
                            Collection<Module> modulesOfProject = ruleServiceLoader.resolveModulesForProject(deployment.getDeploymentName(),
                                deployment.getCommonVersion(),
                                project.getName());
                            ProjectDescriptor projectDescriptor = null;
                            Set<String> wildcardPatterns = new HashSet<String>(); 
                            if (!modulesOfProject.isEmpty()) {
                                Module firstModule = modulesOfProject.iterator().next();
                                projectDescriptor = firstModule.getProject();
                                
                                InputStream content = null;
                                RulesDeploy rulesDeploy = null;
                                try {
                                    AProjectArtefact artifact = project.getArtefact(LastVersionProjectsServiceConfigurer.RULES_DEPLOY_XML);
                                    if (artifact instanceof AProjectResource) {
                                        AProjectResource resource = (AProjectResource) artifact;
                                        content = resource.getContent();
                                        rulesDeploy = getRulesDeploySerializer().deserialize(content);
                                        if (rulesDeploy.getLazyModulesForCompilationPatterns() != null){
                                            for (RulesDeploy.WildcardPattern wp : rulesDeploy.getLazyModulesForCompilationPatterns()){
                                                wildcardPatterns.add(wp.getValue());
                                            }
                                        }
                                    }
                                } catch (ProjectException e) {
                                } finally {
                                    if (content != null) {
                                        try {
                                            content.close();
                                        } catch (IOException e) {
                                            log.error(e.getMessage(), e);
                                        }
                                    }
                                }

                                for (Module m : modulesOfProject) {
                                    IDependencyLoader moduleLoader;
                                    String moduleName = m.getName();
                                    List<Module> module = Arrays.asList(m);
                                    if (isLazy()) {
                                        boolean compileAfterLazyCompilation = compilationAfterLazyCompilationRequred(wildcardPatterns, moduleName);
                                        moduleLoader = new LazyRuleServiceDependencyLoader(deploymentDescription,
                                            moduleName,
                                            module,
                                            compileAfterLazyCompilation);
                                    } else {
                                        moduleLoader = new RuleServiceDependencyLoader(moduleName, module);
                                    }
                                    dependencyLoaders.add(moduleLoader);
                                }
                            }
                            if (projectDescriptor != null) {
                                IDependencyLoader projectLoader;
                                if (isLazy()) {
                                    projectLoader = new LazyRuleServiceDependencyLoader(deploymentDescription,
                                        ProjectExternalDependenciesHelper.buildDependencyNameForProjectName(projectDescriptor.getName()),
                                        projectDescriptor.getModules(), false);
                                } else {
                                    projectLoader = new RuleServiceDependencyLoader(ProjectExternalDependenciesHelper.buildDependencyNameForProjectName(projectDescriptor.getName()),
                                        projectDescriptor.getModules());
                                }
                                projectDescriptors.add(projectDescriptor);
                                dependencyLoaders.add(projectLoader);
                            }
                        } catch (Exception e) {
                            log.error("Build dependency manager loaders for project \"{}\" from deployment \"{}\" was failed!",
                                project.getName(),
                                deploymentName,
                                e);
                        }
                    }
                }
            }
        }
    }
}
