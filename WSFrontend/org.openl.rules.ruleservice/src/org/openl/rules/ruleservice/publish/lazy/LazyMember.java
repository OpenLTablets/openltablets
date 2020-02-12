package org.openl.rules.ruleservice.publish.lazy;

import java.util.ArrayList;
import java.util.Map;

import org.openl.CompiledOpenClass;
import org.openl.dependency.IDependencyManager;
import org.openl.exception.OpenLCompilationException;
import org.openl.rules.lang.xls.prebind.ILazyMember;
import org.openl.rules.lang.xls.prebind.IPrebindHandler;
import org.openl.rules.lang.xls.prebind.XlsLazyModuleOpenClass;
import org.openl.rules.project.dependencies.ProjectExternalDependenciesHelper;
import org.openl.rules.project.instantiation.RulesInstantiationStrategy;
import org.openl.rules.project.instantiation.RulesInstantiationStrategyFactory;
import org.openl.rules.project.model.Module;
import org.openl.rules.ruleservice.core.DeploymentDescription;
import org.openl.rules.ruleservice.core.MaxThreadsForCompileSemaphore;
import org.openl.rules.ruleservice.core.MaxThreadsForCompileSemaphore.Callable;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lazy IOpenMember that contains info about module where it was declared. When we try to do some operations with lazy
 * member it will compile module and wrap the compiled member.
 *
 * @author Marat Kamalov
 */
public abstract class LazyMember<T extends IOpenMember> implements ILazyMember<T>, IOpenMember {
    private final Logger log = LoggerFactory.getLogger(LazyMember.class);

    private IDependencyManager dependencyManager;
    private boolean executionMode;
    private T original;
    private Map<String, Object> externalParameters;

    /**
     * ClassLoader used in "lazy" compilation. It should be reused because it contains generated classes for
     * datatypes.(If we use different ClassLoaders we can get ClassCastException because generated classes for datatypes
     * have been loaded by different ClassLoaders).
     */
    private ClassLoader classLoader;
    private volatile T cachedMember;

    public LazyMember(IDependencyManager dependencyManager,
            boolean executionMode,
            ClassLoader classLoader,
            T original,
            Map<String, Object> externalParameters) {
        this.dependencyManager = dependencyManager;
        this.executionMode = executionMode;
        this.classLoader = classLoader;
        this.original = original;
        this.externalParameters = externalParameters;
    }

    protected abstract T getMember();

    protected T getCachedMember() {
        return cachedMember;
    }

    protected void setCachedMember(T member) {
        cachedMember = member;
    }

    public void clearCachedMember() {
        cachedMember = null;
    }

    protected CompiledOpenClass getCompiledOpenClassWithThrowErrorExceptionsIfAny() throws Exception {
        CompiledOpenClass compiledOpenClass = getCompiledOpenClass();
        if (compiledOpenClass.hasErrors()) {
            compiledOpenClass.throwErrorExceptionsIfAny();
        }
        return compiledOpenClass;
    }

    protected CompiledOpenClass getCompiledOpenClass() throws Exception {
        CompiledOpenClass compiledOpenClass = CompiledOpenClassCache.getInstance()
            .get(getDeployment(), getModule().getName());
        if (compiledOpenClass != null) {
            return compiledOpenClass;
        }

        synchronized (getXlsLazyModuleOpenClass()) {
            compiledOpenClass = CompiledOpenClassCache.getInstance().get(getDeployment(), getModule().getName());
            if (compiledOpenClass != null) {
                return compiledOpenClass;
            }
            try {
                return MaxThreadsForCompileSemaphore.getInstance().run(new Callable<CompiledOpenClass>() {
                    @Override
                    public CompiledOpenClass call() throws Exception {
                        CompiledOpenClass compiledOpenClass = null;
                        IPrebindHandler prebindHandler = LazyBinderMethodHandler.getPrebindHandler();
                        try {
                            LazyBinderMethodHandler.removePrebindHandler();
                            RulesInstantiationStrategy rulesInstantiationStrategy = RulesInstantiationStrategyFactory
                                .getStrategy(getModule(), true, getDependencyManager(), getClassLoader());
                            rulesInstantiationStrategy.setServiceClass(EmptyInterface.class);// Prevent
                            Map<String, Object> parameters = ProjectExternalDependenciesHelper
                                .getExternalParamsWithProjectDependencies(dependencyManager.getExternalParameters(),
                                    new ArrayList<Module>() {
                                        private static final long serialVersionUID = 1L;

                                        {
                                            add(getModule());
                                        }
                                    });
                            rulesInstantiationStrategy.setExternalParameters(parameters);
                            compiledOpenClass = rulesInstantiationStrategy.compile();
                            CompiledOpenClassCache.getInstance()
                                .putToCache(getDeployment(), getModule().getName(), compiledOpenClass);
                            if (log.isDebugEnabled()) {
                                log.debug(
                                    "CompiledOpenClass for deploymentName='{}', deploymentVersion='{}', dependencyName='{}' was stored to cache.",
                                    getDeployment().getName(),
                                    getDeployment().getVersion().getVersionName(),
                                    getModule().getName());
                            }
                            return compiledOpenClass;
                        } catch (Exception ex) {
                            log.error("Failed to load dependency '{}'.", getModule().getName(), ex);
                            return compiledOpenClass;
                        } finally {
                            LazyBinderMethodHandler.setPrebindHandler(prebindHandler);
                        }
                    }
                });
            } catch (OpenLCompilationException e) {
                throw e;
            } catch (InterruptedException e) {
                throw new OpenLCompilationException("Interrupted exception.", e);
            } catch (Exception e) {
                throw new OpenLCompilationException("Failed to compile.", e);
            }
        }
    }

    public abstract XlsLazyModuleOpenClass getXlsLazyModuleOpenClass();

    /**
     * @return Module containing current member.
     */
    public abstract Module getModule();

    /**
     * @return Deployment containing current module.
     */
    public abstract DeploymentDescription getDeployment();

    /**
     * @return DependencyManager used for lazy compiling.
     */
    protected IDependencyManager getDependencyManager() {
        return dependencyManager;
    }

    protected boolean isExecutionMode() {
        return executionMode;
    }

    /**
     * @return ClassLoader used for lazy compiling.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public T getOriginal() {
        return original;
    }

    @Override
    public String getDisplayName(int mode) {
        return getOriginal().getDisplayName(mode);
    }

    @Override
    public String getName() {
        return getOriginal().getName();
    }

    @Override
    public IOpenClass getType() {
        return getOriginal().getType();
    }

    @Override
    public boolean isStatic() {
        return getOriginal().isStatic();
    }

    @Override
    public IMemberMetaInfo getInfo() {
        return getOriginal().getInfo();
    }

    @Override
    public IOpenClass getDeclaringClass() {
        return getOriginal().getDeclaringClass();
    }

    public Map<String, Object> getExternalParameters() {
        return externalParameters;
    }

    public interface EmptyInterface {
    }
}
