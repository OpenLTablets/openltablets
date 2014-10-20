package org.openl.rules.ruleservice.core;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openl.dependency.IDependencyManager;
import org.openl.rules.common.CommonVersion;
import org.openl.rules.project.dependencies.ProjectExternalDependenciesHelper;
import org.openl.rules.project.instantiation.RulesInstantiationException;
import org.openl.rules.project.instantiation.RulesInstantiationStrategy;
import org.openl.rules.project.instantiation.RuntimeContextInstantiationStrategyEnhancer;
import org.openl.rules.project.instantiation.variation.VariationInstantiationStrategyEnhancer;
import org.openl.rules.project.model.Module;
import org.openl.rules.ruleservice.core.instantiation.RuleServiceRuntimeContextInstantiationStrategyEnhancer;
import org.openl.rules.ruleservice.core.interceptors.DynamicInterfaceAnnotationEnhancerHelper;
import org.openl.rules.ruleservice.core.interceptors.ServiceInvocationAdvice;
import org.openl.rules.ruleservice.loader.RuleServiceLoader;
import org.openl.rules.ruleservice.management.ServiceDescriptionHolder;
import org.openl.rules.ruleservice.publish.RuleServiceInstantiationStrategyFactory;
import org.openl.rules.ruleservice.publish.RuleServiceInstantiationStrategyFactoryImpl;
import org.openl.rules.ruleservice.publish.lazy.CompiledOpenClassCache;
import org.openl.runtime.IEngineWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;

/**
 * Default implementation of RuleServiceOpenLServiceInstantiationFactory. Depend
 * on RuleLoader.
 *
 * @author Marat Kamalov
 */
public class RuleServiceOpenLServiceInstantiationFactoryImpl implements RuleServiceInstantiationFactory {
    private final Logger log = LoggerFactory.getLogger(RuleServiceOpenLServiceInstantiationFactoryImpl.class);

    private RuleServiceLoader ruleServiceLoader;

    private RuleServiceInstantiationStrategyFactory instantiationStrategyFactory = new RuleServiceInstantiationStrategyFactoryImpl();

    private IDependencyManager externalDependencyManager;

    private Map<String, Object> externalParameters;

    private void initService(IDependencyManager dependencyManager, OpenLService service) throws RulesInstantiationException,
                                                                                        ClassNotFoundException {
        RulesInstantiationStrategy instantiationStrategy = instantiationStrategyFactory.getStrategy(service.getModules(),
            dependencyManager);
        Map<String, Object> parameters = ProjectExternalDependenciesHelper.getExternalParamsWithProjectDependencies(externalParameters,
            service.getModules());
        instantiationStrategy.setExternalParameters(parameters);

        if (service.isProvideVariations()) {
            instantiationStrategy = new VariationInstantiationStrategyEnhancer(instantiationStrategy);
        }
        if (service.isProvideRuntimeContext()) {
            instantiationStrategy = new RuntimeContextInstantiationStrategyEnhancer(instantiationStrategy);
            if (service.isUseRuleServiceRuntimeContext()) {
                instantiationStrategy = new RuleServiceRuntimeContextInstantiationStrategyEnhancer((RuntimeContextInstantiationStrategyEnhancer) instantiationStrategy);
            }
        }
        resolveInterface(service, instantiationStrategy);
        instantiateServiceBean(service, instantiationStrategy);
    }

    private void instantiateServiceBean(OpenLService service, RulesInstantiationStrategy instantiationStrategy) throws RulesInstantiationException,
                                                                                                               ClassNotFoundException {
        Class<?> serviceClass = service.getServiceClass();
        Object serviceBean = instantiationStrategy.instantiate();
        ProxyFactory factory = new ProxyFactory();
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(instantiationStrategy.getClassLoader());
            factory.addAdvice(new ServiceInvocationAdvice(serviceBean, serviceClass));
            if (serviceClass.isInterface()) {
                factory.addInterface(serviceClass);
                if (!service.isProvideRuntimeContext()) {
                    factory.addInterface(IEngineWrapper.class);
                }
            } else {
                // deprecated approach with wrapper: service class is not
                // interface
                factory.setTarget(serviceBean);
                if (!Proxy.isProxyClass(serviceBean.getClass())) {
                    factory.setProxyTargetClass(true);
                } else {
                    factory.setProxyTargetClass(false);
                }
            }

            Object proxyServiceBean = factory.getProxy();
            service.setServiceBean(proxyServiceBean);
        } catch (Throwable t) {
            throw new RuleServiceRuntimeException("Can't create a proxy of service bean object", t);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    private void resolveInterface(OpenLService service, RulesInstantiationStrategy instantiationStrategy) throws RulesInstantiationException,
                                                                                                         ClassNotFoundException {
        String serviceClassName = service.getServiceClassName();
        Class<?> serviceClass;
        ClassLoader serviceClassLoader = instantiationStrategy.getClassLoader();
        if (serviceClassName != null) {
            try {
                serviceClass = serviceClassLoader.loadClass(serviceClassName);
                instantiationStrategy.setServiceClass(RuleServiceInstantiationFactoryHelper.getInterfaceForInstantiationStrategy(instantiationStrategy,
                    serviceClass));
            } catch (ClassNotFoundException e) {
                log.error("Failed to load service class with name \"{}\"", serviceClassName, e);
                log.info("Service class is undefined of service '{}'. Generated interface will be used.",
                    service.getName());
                serviceClass = processGeneratedServiceClass(service,
                    instantiationStrategy.getInstanceClass(),
                    serviceClassLoader);
            }
        } else {
            log.info("Service class is undefined of service '{}'. Generated interface will be used.", service.getName());
            serviceClass = processGeneratedServiceClass(service,
                instantiationStrategy.getInstanceClass(),
                serviceClassLoader);
        }
        service.setServiceClass(serviceClass);
    }

    private Class<?> processGeneratedServiceClass(OpenLService service, Class<?> serviceClass, ClassLoader classLoader) {
        Class<?> resultClass = processInterceptingTemplateClassConfiguration(service, serviceClass, classLoader);
        return processCustomSpreadSheetResults(service, resultClass, classLoader);
    }

    private Class<?> processCustomSpreadSheetResults(OpenLService service,
            Class<?> serviceClass,
            ClassLoader classLoader) {
        if (serviceClass == null) {
            throw new IllegalStateException("It shouldn't happen!");
        }
        try {
            Class<?> decoratedClass = CustomSpreadsheetResultInterfaceEnhancerHelper.decorate(serviceClass, classLoader);
            return decoratedClass;
        } catch (Exception e) {
            log.error("Failed to applying custom spreadsheet result convertor for class with name \"{}\"",
                serviceClass.getCanonicalName(),
                e);
        }
        return serviceClass;
    }

    private Class<?> processInterceptingTemplateClassConfiguration(OpenLService service,
            Class<?> serviceClass,
            ClassLoader classLoader) {
        if (serviceClass == null) {
            throw new IllegalStateException("It shouldn't happen!");
        }
        ServiceDescription serviceDescription = ServiceDescriptionHolder.getInstance().getServiceDescription();
        if (serviceDescription == null) {
            log.error("Service description didn't find! Something wrong!\n" + "Interceptor template configuration was ignored for service '{}!",
                service.getName());
            return serviceClass;
        } else {
            String clazzName = serviceDescription.getInterceptorTemplateClassName();
            if (clazzName != null) {
                try {
                    Class<?> interceptingTemplateClass = classLoader.loadClass(clazzName);
                    Class<?> decoratedClass = DynamicInterfaceAnnotationEnhancerHelper.decorate(serviceClass,
                        interceptingTemplateClass,
                        classLoader);
                    log.info("Interceptor template class \"{}\" was used for service: {}",
                        clazzName,
                        serviceDescription.getName());
                    return decoratedClass;
                } catch (Exception e) {
                    log.error("Intercepting template class wasn't used! Failed to load or applying intercepting template class with name \"{}\"",
                        clazzName,
                        e);
                }
            }
        }
        return serviceClass;
    }

    /**
     * {@inheritDoc}
     */
    public OpenLService createService(ServiceDescription serviceDescription) throws RuleServiceInstantiationException {
        try {
            log.debug("Resoliving modules for service with name={}", serviceDescription.getName());
            DeploymentDescription deployment = serviceDescription.getDeployment();
            String deploymentName = deployment.getName();
            CommonVersion deploymentVersion = deployment.getVersion();
            Collection<ModuleDescription> modulesToLoad = serviceDescription.getModules();
            Collection<Module> modules = getModulesByServiceDescription(deploymentName,
                deploymentVersion,
                modulesToLoad);

            OpenLService.OpenLServiceBuilder builder = new OpenLService.OpenLServiceBuilder();
            builder.setName(serviceDescription.getName())
                .setUrl(serviceDescription.getUrl())
                .setServiceClassName(serviceDescription.getServiceClassName())
                .setProvideRuntimeContext(serviceDescription.isProvideRuntimeContext())
                .setProvideVariations(serviceDescription.isProvideVariations())
                .setUseRuleServiceRuntimeContext(serviceDescription.isUseRuleServiceRuntimeContext())
                .addModules(modules);

            if (serviceDescription.getPublishers() != null) {
                for (String key : serviceDescription.getPublishers()) {
                    builder.addPublisher(key);
                }
            }

            OpenLService openLService = builder.build();

            initService(getDependencyManager(deployment), openLService);
            return openLService;
        } catch (Exception e) {
            throw new RuleServiceInstantiationException(String.format("Failed to initialiaze OpenL service \"%s\"",
                serviceDescription.getName()), e);
        }
    }

    private Collection<Module> getModulesByServiceDescription(String deploymentName,
            CommonVersion deploymentVersion,
            Collection<ModuleDescription> modulesToLoad) {

        Map<String, Collection<ModuleDescription>> projectModules = new HashMap<String, Collection<ModuleDescription>>();

        for (ModuleDescription moduleDescription : modulesToLoad) {
            String projectName = moduleDescription.getProjectName();
            if (projectModules.containsKey(projectName)) {
                Collection<ModuleDescription> modules = projectModules.get(projectName);
                modules.add(moduleDescription);
            } else {
                Collection<ModuleDescription> modules = new ArrayList<ModuleDescription>();
                modules.add(moduleDescription);
                projectModules.put(projectName, modules);
            }
        }

        Collection<Module> ret = new ArrayList<Module>();
        for (String projectName : projectModules.keySet()) {

            Collection<Module> modules = ruleServiceLoader.resolveModulesForProject(deploymentName,
                deploymentVersion,
                projectName);
            for (ModuleDescription moduleDescription : projectModules.get(projectName)) {
                for (Module module : modules) {
                    if (moduleDescription.getModuleName().equals(module.getName())) {
                        ret.add(module);
                    }
                }
            }
        }
        return Collections.unmodifiableCollection(ret);
    }

    public RuleServiceLoader getRuleServiceLoader() {
        return ruleServiceLoader;
    }

    public void setRuleServiceLoader(RuleServiceLoader ruleServiceLoader) {
        if (ruleServiceLoader == null) {
            throw new IllegalArgumentException("rulesLoader arg can't be null");
        }
        this.ruleServiceLoader = ruleServiceLoader;
    }

    public IDependencyManager getExternalDependencyManager() {
        return externalDependencyManager;
    }

    public void setExternalDependencyManager(IDependencyManager externalDependencyManager) {
        this.externalDependencyManager = externalDependencyManager;
    }

    public RuleServiceInstantiationStrategyFactory getInstantiationStrategyFactory() {
        return instantiationStrategyFactory;
    }

    public void setInstantiationStrategyFactory(RuleServiceInstantiationStrategyFactory instantiationStrategyFactory) {
        if (instantiationStrategyFactory == null) {
            throw new IllegalArgumentException("instantiationStrategyFactory arg can't be null");
        }
        this.instantiationStrategyFactory = instantiationStrategyFactory;
    }

    public Map<String, Object> getExternalParameters() {
        return externalParameters;
    }

    public void setExternalParameters(Map<String, Object> externalParameters) {
        this.externalParameters = externalParameters;
    }

    private Map<DeploymentDescription, RuleServiceDeploymentRelatedDependencyManager> dependencyManagerMap = new HashMap<DeploymentDescription, RuleServiceDeploymentRelatedDependencyManager>();

    public void clear(DeploymentDescription deploymentDescription) {
        dependencyManagerMap.remove(deploymentDescription);
        CompiledOpenClassCache.getInstance().removeAll(deploymentDescription);
    }

    private IDependencyManager getDependencyManager(DeploymentDescription deployment) {
        if (externalDependencyManager != null) {
            return externalDependencyManager;
        }

        RuleServiceDeploymentRelatedDependencyManager dependencyManager;
        if (dependencyManagerMap.containsKey(deployment)) {
            dependencyManager = dependencyManagerMap.get(deployment);
        } else {
            boolean isLazy = false;
            if (instantiationStrategyFactory instanceof RuleServiceInstantiationStrategyFactoryImpl) {
                isLazy = ((RuleServiceInstantiationStrategyFactoryImpl) instantiationStrategyFactory).isLazy();
            }
            dependencyManager = new RuleServiceDeploymentRelatedDependencyManager(deployment, ruleServiceLoader, isLazy);
            dependencyManager.setExternalParameters(externalParameters);
            dependencyManagerMap.put(deployment, dependencyManager);
        }
        return dependencyManager;
    }
}
