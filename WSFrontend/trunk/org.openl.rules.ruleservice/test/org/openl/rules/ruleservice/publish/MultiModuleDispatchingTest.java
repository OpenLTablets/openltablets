package org.openl.rules.ruleservice.publish;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openl.engine.OpenLSystemProperties;
import org.openl.rules.context.DefaultRulesRuntimeContext;
import org.openl.rules.ruleservice.management.ServiceManager;
import org.openl.rules.ruleservice.simple.RulesFrontend;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:MultiModuleDispatchingTest/openl-ruleservice-beans.xml" })
public class MultiModuleDispatchingTest implements ApplicationContextAware{
    private static final String SERVICE_NAME = "MultiModuleDispatchingTest_multimodule";
    
    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Test
    public void testMultiModuleService2() throws Exception {
        assertNotNull(applicationContext);
        ServiceManager serviceManager = applicationContext.getBean("serviceManager", ServiceManager.class);
        assertNotNull(serviceManager);
        serviceManager.start();
        RulesFrontend frontend = applicationContext.getBean("frontend", RulesFrontend.class);
        DefaultRulesRuntimeContext cxt = new DefaultRulesRuntimeContext();

        // dispatcher table
        System.setProperty(OpenLSystemProperties.DISPATCHING_MODE_PROPERTY, OpenLSystemProperties.DISPATCHING_MODE_DT);
        cxt.setLob("lob1_1");
        //assertTrue(publisher.findServiceByName(SERVICE_NAME).getInstantiationStrategy() instanceof LazyMultiModuleInstantiationStrategy);
        assertEquals("Hello1", frontend.execute(SERVICE_NAME, "hello", new Object[] { cxt }));
        cxt.setLob("lob2_1");
        assertEquals("Hello2", frontend.execute(SERVICE_NAME, "hello", new Object[] { cxt }));
        cxt.setLob("lob3_1");
        assertEquals("Hello3", frontend.execute(SERVICE_NAME, "hello", new Object[] { cxt }));

        // dispatching by java code
        System.setProperty(OpenLSystemProperties.DISPATCHING_MODE_PROPERTY, OpenLSystemProperties.DISPATCHING_MODE_JAVA);
        cxt.setLob("lob1_1");
        //assertTrue(publisher.findServiceByName(SERVICE_NAME).getInstantiationStrategy() instanceof LazyMultiModuleInstantiationStrategy);
        assertEquals("Hello1", frontend.execute(SERVICE_NAME, "hello", new Object[] { cxt }));
        cxt.setLob("lob2_1");
        assertEquals("Hello2", frontend.execute(SERVICE_NAME, "hello", new Object[] { cxt }));
        cxt.setLob("lob3_1");
        assertEquals("Hello3", frontend.execute(SERVICE_NAME, "hello", new Object[] { cxt }));
        
    }

}
