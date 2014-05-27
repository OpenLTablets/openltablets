package org.openl.rules.ruleservice.core.interceptors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openl.meta.DoubleValue;
import org.openl.rules.common.CommonVersion;
import org.openl.rules.common.impl.CommonVersionImpl;
import org.openl.rules.context.IRulesRuntimeContext;
import org.openl.rules.context.RulesRuntimeContextFactory;
import org.openl.rules.project.abstraction.AProject;
import org.openl.rules.project.abstraction.Deployment;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.model.ModuleType;
import org.openl.rules.project.model.PathEntry;
import org.openl.rules.project.model.ProjectDescriptor;
import org.openl.rules.ruleservice.core.DeploymentDescription;
import org.openl.rules.ruleservice.core.ModuleDescription;
import org.openl.rules.ruleservice.core.OpenLService;
import org.openl.rules.ruleservice.core.RuleServiceInstantiationFactoryHelper;
import org.openl.rules.ruleservice.core.RuleServiceOpenLServiceInstantiationFactoryImpl;
import org.openl.rules.ruleservice.core.ServiceDescription;
import org.openl.rules.ruleservice.core.interceptors.annotations.ServiceCallAfterInterceptor;
import org.openl.rules.ruleservice.loader.RuleServiceLoader;
import org.openl.rules.ruleservice.management.ServiceDescriptionHolder;

public class ServiceInterfaceMethodInterceptingTest {
    public static class ResultConvertor extends AbstractServiceMethodAfterReturningAdvice<Double> {
        @Override
        public Double afterReturning(Method method, Object result, Object... args) throws Exception {
            return ((DoubleValue) result).doubleValue();
        }

    }

    public static interface OverloadInterface {
        @ServiceCallAfterInterceptor(value = ResultConvertor.class)
        Double driverRiskScoreOverloadTest(IRulesRuntimeContext runtimeContext, String driverRisk);

        @ServiceCallAfterInterceptor(value = ResultConvertor.class)
        Double driverRiskScoreNoOverloadTest(IRulesRuntimeContext runtimeContext, String driverRisk);
    }

    ServiceDescription serviceDescription;
    RuleServiceLoader ruleServiceLoader;
    List<Module> modules = new ArrayList<Module>();

    @Before
    public void before() {
        CommonVersion version = new CommonVersionImpl(0, 0, 1);
        DeploymentDescription deploymentDescription = new DeploymentDescription("someDeploymentName", version);

        Collection<ModuleDescription> moduleDescriptions = new ArrayList<ModuleDescription>();
        ModuleDescription moduleDescription = new ModuleDescription.ModuleDescriptionBuilder().setProjectName("Overload")
            .setModuleName("service")
            .build();
        moduleDescriptions.add(moduleDescription);

        serviceDescription = new ServiceDescription.ServiceDescriptionBuilder().setServiceClassName(OverloadInterface.class.getName())
            .setName("service")
            .setUrl("/")
            .setProvideRuntimeContext(true)
            .setProvideVariations(false)
            .setDeployment(deploymentDescription)
            .setModules(moduleDescriptions)
            .build();
        ServiceDescriptionHolder.getInstance().setServiceDescription(serviceDescription);

        ruleServiceLoader = mock(RuleServiceLoader.class);

        Module module = new Module();
        module.setName("Overload");
        module.setType(ModuleType.API);
        ProjectDescriptor projectDescriptor = new ProjectDescriptor();
        projectDescriptor.setName("service");
        projectDescriptor.setModules(modules);
        module.setProject(projectDescriptor);
        module.setRulesRootPath(new PathEntry("./test-resources/ServiceInterfaceMethodInterceptingTest/Overload.xls"));
        modules.add(module);
        when(ruleServiceLoader.getModulesByServiceDescription(serviceDescription)).thenReturn(modules);
        List<Deployment> deployments = new ArrayList<Deployment>();
        Deployment deployment = mock(Deployment.class);
        List<AProject> projects = new ArrayList<AProject>();
        AProject project = mock(AProject.class);
        projects.add(project);
        when(project.getName()).thenReturn("service");
        when(deployment.getProjects()).thenReturn(projects);
        when(deployment.getDeploymentName()).thenReturn(deploymentDescription.getName());
        when(deployment.getCommonVersion()).thenReturn(deploymentDescription.getVersion());
        deployments.add(deployment);
        when(ruleServiceLoader.getDeployments()).thenReturn(deployments);
        when(ruleServiceLoader.resolveModulesForProject(deploymentDescription.getName(), deploymentDescription.getVersion(), "service")).thenReturn(modules);
    }

    @Test
    public void testResultConvertorInterceptor() throws Exception {
        RuleServiceOpenLServiceInstantiationFactoryImpl instantiationFactory = new RuleServiceOpenLServiceInstantiationFactoryImpl();
        instantiationFactory.setRuleServiceLoader(ruleServiceLoader);
        OpenLService service = instantiationFactory.createService(serviceDescription);
        assertTrue(service.getServiceBean() instanceof OverloadInterface);
        OverloadInterface instance = (OverloadInterface) service.getServiceBean();
        IRulesRuntimeContext runtimeContext = RulesRuntimeContextFactory.buildRulesRuntimeContext();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2009, 5, 15);
        runtimeContext.setCurrentDate(calendar.getTime());
        Assert.assertEquals(100, instance.driverRiskScoreOverloadTest(runtimeContext, "High Risk Driver"), 0.1);
    }

    @Test
    public void testServiceClassUndecorating() throws Exception {
        RuleServiceOpenLServiceInstantiationFactoryImpl instantiationFactory = new RuleServiceOpenLServiceInstantiationFactoryImpl();
        instantiationFactory.setRuleServiceLoader(ruleServiceLoader);
        Class<?> interfaceForInstantiationStrategy = RuleServiceInstantiationFactoryHelper.getInterfaceForInstantiationStrategy(instantiationFactory.getInstantiationStrategyFactory()
            .getStrategy(modules, null),
            OverloadInterface.class);
        for (Method method : OverloadInterface.class.getMethods()) {
            Method methodGenerated = interfaceForInstantiationStrategy.getMethod(method.getName(),
                method.getParameterTypes());
            Assert.assertNotNull(methodGenerated);
            Assert.assertEquals(Object.class, methodGenerated.getReturnType());
        }
    }

}
