package org.openl.rules.ruleservice.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.openl.CompiledOpenClass;
import org.openl.dependency.CompiledDependency;
import org.openl.dependency.IDependencyManager;
import org.openl.dependency.loader.IDependencyLoader;
import org.openl.exception.OpenLCompilationException;
import org.openl.exception.OpenlNotCheckedException;
import org.openl.message.OpenLMessagesUtils;
import org.openl.rules.lang.xls.prebind.IPrebindHandler;
import org.openl.rules.lang.xls.prebind.XlsLazyModuleOpenClass;
import org.openl.rules.method.ITablePropertiesMethod;
import org.openl.rules.project.dependencies.ProjectExternalDependenciesHelper;
import org.openl.rules.project.instantiation.RulesInstantiationStrategy;
import org.openl.rules.project.instantiation.RulesInstantiationStrategyFactory;
import org.openl.rules.project.instantiation.WrapperAdjustingInstantiationStrategy;
import org.openl.rules.project.model.Module;
import org.openl.rules.ruleservice.publish.lazy.CompiledOpenClassCache;
import org.openl.rules.ruleservice.publish.lazy.LazyBinderInvocationHandler;
import org.openl.rules.ruleservice.publish.lazy.LazyCompiledOpenClass;
import org.openl.rules.ruleservice.publish.lazy.LazyCompiledOpenClassCache;
import org.openl.rules.ruleservice.publish.lazy.LazyField;
import org.openl.rules.ruleservice.publish.lazy.LazyInstantiationStrategy;
import org.openl.rules.ruleservice.publish.lazy.LazyMember.EmptyInterface;
import org.openl.rules.ruleservice.publish.lazy.LazyMethod;
import org.openl.rules.ruleservice.publish.lazy.TablePropertiesLazyMethod;
import org.openl.syntax.code.Dependency;
import org.openl.syntax.code.DependencyType;
import org.openl.syntax.impl.IdentifierNode;
import org.openl.types.IOpenField;
import org.openl.types.IOpenMember;
import org.openl.types.IOpenMethod;
import org.openl.vm.IRuntimeEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LazyRuleServiceDependencyLoader implements IDependencyLoader {

    private final Logger log = LoggerFactory.getLogger(LazyRuleServiceDependencyLoader.class);

    private final String name;
    private final DeploymentDescription deployment;
    private final Collection<Module> modules;
    private final boolean realCompileRequred;

    LazyRuleServiceDependencyLoader(DeploymentDescription deployment,
            String dependencyName,
            Collection<Module> modules,
            boolean realCompileRequred) {
        if (deployment == null) {
            throw new IllegalArgumentException("deployment arg can't be null!");
        }
        if (dependencyName == null) {
            throw new IllegalArgumentException("dependencyName arg can't be null!");
        }
        if (modules == null || modules.isEmpty()) {
            throw new IllegalArgumentException("modules arg can't be null or empty!");
        }
        this.name = dependencyName;
        this.deployment = deployment;
        this.modules = modules;
        this.realCompileRequred = realCompileRequred;
    }

    public CompiledOpenClass compile(final String dependencyName,
            final RuleServiceDeploymentRelatedDependencyManager dependencyManager) throws OpenLCompilationException {
        CompiledOpenClass compiledOpenClass = LazyCompiledOpenClassCache.getInstance().get(deployment, dependencyName);
        if (compiledOpenClass != null) {
            log.debug("Lazy CompiledOpenClass for:\n" + " deploymentName=\"{}\",\n" + " deploymentVersion=\"{}\",\n" + " dependencyName=\"{}\"\n" + "was returned from cache.",
                deployment.getName(),
                deployment.getVersion().getVersionName(),
                dependencyName);
            return compiledOpenClass;
        }
        synchronized (LazyCompiledOpenClassCache.getInstance()) {
            compiledOpenClass = LazyCompiledOpenClassCache.getInstance().get(deployment, dependencyName);
            if (compiledOpenClass != null) {
                log.debug("Lazy CompiledOpenClass for:\n" + " deploymentName=\"{}\",\n" + " deploymentVersion=\"{}\",\n" + " dependencyName=\"{}\"\n" + "was returned from cache.",
                    deployment.getName(),
                    deployment.getVersion().getVersionName(),
                    dependencyName);
                return compiledOpenClass;
            }
            IPrebindHandler prebindHandler = LazyBinderInvocationHandler.getPrebindHandler();
            try {
                if (dependencyManager.getCompilationStack().contains(dependencyName)) {
                    OpenLMessagesUtils.addError("Circular dependency detected in module: " + dependencyName);
                    return null;
                }
                RulesInstantiationStrategy rulesInstantiationStrategy = null;
                final ClassLoader classLoader = dependencyManager.getClassLoader(modules.iterator().next().getProject());
                dependencyManager.getCompilationStack().add(dependencyName);
                log.debug("Creating lazy for:\n" + " deploymentName=\"{}\",\n" + " deploymentVersion=\"{}\",\n" + " dependencyName=\"{}\"",
                    deployment.getName(),
                    deployment.getVersion().getVersionName(),
                    dependencyName);
                if (modules.size() > 1) {
                    rulesInstantiationStrategy = new LazyInstantiationStrategy(deployment,
                        modules,
                        dependencyManager,
                        classLoader);
                } else {
                    rulesInstantiationStrategy = RulesInstantiationStrategyFactory.getStrategy(modules.iterator()
                        .next(), true, dependencyManager, classLoader);
                }
                rulesInstantiationStrategy.setServiceClass(LazyRuleServiceDependencyLoaderInterface.class);// Prevent
                // generation
                // interface
                // and
                // Virtual
                // module
                // dublicate
                // (instantiate
                // method).
                // Improve
                // performance.
                final Map<String, Object> parameters = ProjectExternalDependenciesHelper.getExternalParamsWithProjectDependencies(dependencyManager.getExternalParameters(),
                    modules);
                rulesInstantiationStrategy.setExternalParameters(parameters);
                try {
                    if (rulesInstantiationStrategy instanceof WrapperAdjustingInstantiationStrategy) {
                        LazyBinderInvocationHandler.removePrebindHandler();
                    } else {
                        LazyBinderInvocationHandler.setPrebindHandler(new IPrebindHandler() {
                            Module getModuleForMember(IOpenMember member) {
                                String sourceUrl = member.getDeclaringClass().getMetaInfo().getSourceUrl();
                                Module module = getModuleForSourceUrl(sourceUrl, modules);
                                if (module != null) {
                                    return module;
                                }
                                throw new OpenlNotCheckedException("Module not found");
                            }

                            private Module getModuleForSourceUrl(String sourceUrl, Collection<Module> modules) {
                                if (modules.size() == 1) {
                                    return modules.iterator().next();
                                }
                                for (Module module : modules) {
                                    String modulePath = module.getRulesRootPath().getPath();
                                    try {
                                        if (FilenameUtils.normalize(sourceUrl)
                                            .equals(FilenameUtils.normalize(new File(modulePath).getCanonicalFile()
                                                .toURI()
                                                .toURL()
                                                .toExternalForm()))) {
                                            return module;
                                        }
                                    } catch (Exception e) {
                                        log.warn("Failed to build url of module '{}' with path: {}",
                                            module.getName(),
                                            modulePath,
                                            e);
                                    }
                                }
                                return null;
                            }

                            private LazyMethod makeLazyMethod(IOpenMethod method) {
                                final Module declaringModule = getModuleForMember(method);
                                Class<?>[] argTypes = new Class<?>[method.getSignature().getNumberOfParameters()];
                                for (int i = 0; i < argTypes.length; i++) {
                                    argTypes[i] = method.getSignature().getParameterType(i).getInstanceClass();
                                }
                                if (method instanceof ITablePropertiesMethod) {
                                    return new TablePropertiesLazyMethod(method.getName(),
                                        argTypes,
                                        method,
                                        dependencyManager,
                                        classLoader,
                                        true,
                                        parameters) {
                                        @Override
                                        public DeploymentDescription getDeployment(IRuntimeEnv env) {
                                            return deployment;
                                        }

                                        @Override
                                        public Module getModule(IRuntimeEnv env) {
                                            return declaringModule;
                                        }
                                    };
                                } else {
                                    return new LazyMethod(method.getName(),
                                        argTypes,
                                        method,
                                        dependencyManager,
                                        classLoader,
                                        true,
                                        parameters) {
                                        @Override
                                        public DeploymentDescription getDeployment(IRuntimeEnv env) {
                                            return deployment;
                                        }

                                        @Override
                                        public Module getModule(IRuntimeEnv env) {
                                            return declaringModule;
                                        }
                                    };
                                }
                            }

                            private LazyField makeLazyField(IOpenField field) {
                                final Module declaringModule = getModuleForMember(field);
                                return new LazyField(field.getName(),
                                    field,
                                    dependencyManager,
                                    classLoader,
                                    true,
                                    parameters) {
                                    @Override
                                    public DeploymentDescription getDeployment(IRuntimeEnv env) {
                                        return deployment;
                                    }

                                    @Override
                                    public Module getModule(IRuntimeEnv env) {
                                        return declaringModule;
                                    }
                                };
                            }

                            @Override
                            public IOpenMethod processMethodAdded(IOpenMethod method,
                                    XlsLazyModuleOpenClass moduleOpenClass) {
                                return makeLazyMethod(method);
                            }

                            @Override
                            public IOpenField processFieldAdded(IOpenField field, XlsLazyModuleOpenClass moduleOpenClass) {
                                return makeLazyField(field);
                            }
                        });
                    }
                    compiledOpenClass = rulesInstantiationStrategy.compile();// Check
                    // correct
                    // compilation
                    LazyCompiledOpenClassCache.getInstance().putToCache(deployment, dependencyName, compiledOpenClass);
                    log.debug("Lazy for:\n" + " deploymentName=\"{}\",\n" + " deploymentVersion=\"{}\",\n" + " dependencyName=\"{}\"\n" + "was stored in cache.",
                        deployment.getName(),
                        deployment.getVersion().getVersionName(),
                        dependencyName);
                    if (modules.size() == 1 && realCompileRequred) {
                        compileAfterLazyCompile(dependencyName, dependencyManager, classLoader, modules.iterator()
                            .next());
                    }
                    return compiledOpenClass;
                } catch (Exception ex) {
                    throw new OpenLCompilationException("Can't load dependency with name '" + dependencyName + "'.", ex);
                } finally {
                    LazyBinderInvocationHandler.setPrebindHandler(prebindHandler);
                }
            } finally {
                dependencyManager.getCompilationStack().pollLast();
            }
        }
    }

    private void compileAfterLazyCompile(final String dependencyName,
            final RuleServiceDeploymentRelatedDependencyManager dependencyManager,
            final ClassLoader classLoader,
            final Module module) throws OpenLCompilationException {
        synchronized (CompiledOpenClassCache.getInstance()) {
            CompiledOpenClass compiledOpenClass = CompiledOpenClassCache.getInstance().get(deployment, dependencyName);
            if (compiledOpenClass != null) {
                return;
            }
            IPrebindHandler prebindHandler = LazyBinderInvocationHandler.getPrebindHandler();
            try {
                LazyBinderInvocationHandler.removePrebindHandler();
                RulesInstantiationStrategy rulesInstantiationStrategy = RulesInstantiationStrategyFactory.getStrategy(module,
                    true,
                    dependencyManager,
                    classLoader);
                rulesInstantiationStrategy.setServiceClass(EmptyInterface.class);
                Map<String, Object> parameters = ProjectExternalDependenciesHelper.getExternalParamsWithProjectDependencies(dependencyManager.getExternalParameters(),
                    new ArrayList<Module>() {
                        private static final long serialVersionUID = 1L;

                        {
                            add(module);
                        }
                    });
                rulesInstantiationStrategy.setExternalParameters(parameters);
                compiledOpenClass = rulesInstantiationStrategy.compile();
                CompiledOpenClassCache.getInstance().putToCache(deployment, dependencyName, compiledOpenClass);
                log.debug("CompiledOpenClass for deploymentName=\"{}\", deploymentVersion=\"{}\", dependencyName=\"{}\" was stored to cache.",
                    deployment.getName(),
                    deployment.getVersion().getVersionName(),
                    dependencyName);
            } catch (Exception ex) {
                throw new OpenLCompilationException("Can't load dependency with name '" + dependencyName + "'.", ex);
            } finally {
                LazyBinderInvocationHandler.setPrebindHandler(prebindHandler);
            }
        }
    }

    private boolean isCompiledOnce = false;
    private CompiledDependency lazyCompiledDependency = null;

    @Override
    public CompiledDependency load(String dependencyName, IDependencyManager dm) throws OpenLCompilationException {
        if (name.equals(dependencyName)) {
            final RuleServiceDeploymentRelatedDependencyManager dependencyManager;
            if (dm instanceof RuleServiceDeploymentRelatedDependencyManager) {
                dependencyManager = (RuleServiceDeploymentRelatedDependencyManager) dm;
            } else {
                throw new IllegalStateException("This loader works only with RuleServiceDeploymentRelatedDependencyManager!");
            }
            if (!isCompiledOnce) {
                compile(dependencyName, dependencyManager);
                isCompiledOnce = true;
            }
            if (lazyCompiledDependency == null) {
                CompiledOpenClass compiledOpenClass = new LazyCompiledOpenClass(dependencyManager,
                    this,
                    new Dependency(DependencyType.MODULE, new IdentifierNode(null, null, dependencyName, null)));
                lazyCompiledDependency = new CompiledDependency(dependencyName, compiledOpenClass);
            }
            return lazyCompiledDependency;
        }
        return null;
    }

    public static interface LazyRuleServiceDependencyLoaderInterface {
    }
}