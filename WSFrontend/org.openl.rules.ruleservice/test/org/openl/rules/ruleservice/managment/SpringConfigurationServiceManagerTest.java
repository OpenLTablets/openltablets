package org.openl.rules.ruleservice.managment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openl.rules.context.DefaultRulesRuntimeContext;
import org.openl.rules.ruleservice.management.ServiceManagerImpl;
import org.openl.rules.ruleservice.simple.MethodInvocationException;
import org.openl.rules.ruleservice.simple.RulesFrontend;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:SpringConfigurationServiceManagerTest/openl-ruleservice-beans.xml" })
@DirtiesContext
public class SpringConfigurationServiceManagerTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testServiceManager() throws MethodInvocationException {
        assertNotNull(applicationContext);
        ServiceManagerImpl serviceManager = applicationContext.getBean("serviceManager", ServiceManagerImpl.class);
        assertNotNull(serviceManager);
        serviceManager.start();
        RulesFrontend frontend = applicationContext.getBean(RulesFrontend.class);
        assertNotNull(frontend);
        Object object = frontend.execute("org.openl.tablets.tutorial4_org.openl.tablets.tutorial4",
                "vehicleEligibilityScore", new Object[] { new DefaultRulesRuntimeContext(), "Provisional" });
        assertTrue(object instanceof org.openl.meta.DoubleValue);
        org.openl.meta.DoubleValue value = (org.openl.meta.DoubleValue) object;
        assertEquals(50.0, value.getValue(), 0.01);
    }

    @Test(expected = MethodInvocationException.class)
    public void testExceptionFramework() throws Exception {
        assertNotNull(applicationContext);
        ServiceManagerImpl serviceManager = applicationContext.getBean(ServiceManagerImpl.class);
        assertNotNull(serviceManager);
        serviceManager.start();
        RulesFrontend frontend = applicationContext.getBean(RulesFrontend.class);
        assertNotNull(frontend);
        frontend.execute("ErrorTest_ErrorTest", "vehicleEligibilityScore", new Object[] {
                new DefaultRulesRuntimeContext(), "test" });
    }
}
