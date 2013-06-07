package org.openl.rules.ruleservice.publish;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import net.sf.cglib.core.ReflectUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openl.rules.context.DefaultRulesRuntimeContext;
import org.openl.rules.context.IRulesRuntimeContext;
import org.openl.rules.ruleservice.core.OpenLService;
import org.openl.rules.ruleservice.management.ServiceManager;
import org.openl.rules.ruleservice.simple.RulesFrontend;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:dynamic-interface/openl-ruleservice-beans.xml" })
public class DynamicInterfacePublishingTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext arg0) throws BeansException {
        this.applicationContext = arg0;
    }

    @Test
    public void publishWithDynamicInterface() {
        assertNotNull(applicationContext);
        ServiceManager serviceManager = applicationContext.getBean("serviceManager", ServiceManager.class);
        assertNotNull(serviceManager);
        serviceManager.start();
        RulesFrontend frontend = applicationContext.getBean("frontend", RulesFrontend.class);
        assertNotNull(frontend);
        OpenLService service = frontend.findServiceByName("dynamic-interface-test1");
        assertNotNull(service);
        assertNotNull(service.getServiceClass());
        String[] methods = {
                "method2(Lorg/openl/rules/context/IRulesRuntimeContext;Lorg/openl/generated/beans/MyType;)Lorg/openl/generated/beans/MyType;",
                "method2(Lorg/openl/rules/context/IRulesRuntimeContext;Lorg/openl/ruleservice/dynamicinterface/test/MyClass;)Lorg/openl/ruleservice/dynamicinterface/test/MyClass;",
                "method1(Lorg/openl/rules/context/IRulesRuntimeContext;Ljava/lang/Object;)Ljava/lang/String;",
                "method3(Lorg/openl/rules/context/IRulesRuntimeContext;Lorg/openl/ruleservice/dynamicinterface/test/MyClass;)Lorg/openl/ruleservice/dynamicinterface/test/MyClass;",
                "baseHello(Lorg/openl/rules/context/IRulesRuntimeContext;I)Ljava/lang/String;",
                "baseHello2(Lorg/openl/rules/context/IRulesRuntimeContext;I)Ljava/lang/String;" };
        Set<String> methodNames = new HashSet<String>();
        for (String s : methods) {
            methodNames.add(s);
        }
        int count = 0;
        for (Method method : service.getServiceClass().getMethods()) {
            if (methodNames.contains(ReflectUtils.getSignature(method).toString())) {
                count++;
            }
        }
        Assert.assertEquals(methods.length, count);
    }

    @Test
    public void publishWithDynamicInterfaceMethodFilter() {
        assertNotNull(applicationContext);
        ServiceManager serviceManager = applicationContext.getBean("serviceManager", ServiceManager.class);
        assertNotNull(serviceManager);
        serviceManager.start();
        RulesFrontend frontend = applicationContext.getBean("frontend", RulesFrontend.class);
        assertNotNull(frontend);
        OpenLService service = frontend.findServiceByName("dynamic-interface-test2");
        assertNotNull(service);
        assertNotNull(service.getServiceClass());
        String[] methods = {
                "method2(Lorg/openl/rules/context/IRulesRuntimeContext;Lorg/openl/generated/beans/MyType;)Lorg/openl/generated/beans/MyType;",
                "method2(Lorg/openl/rules/context/IRulesRuntimeContext;Lorg/openl/ruleservice/dynamicinterface/test/MyClass;)Lorg/openl/ruleservice/dynamicinterface/test/MyClass;" };
        Set<String> methodNames = new HashSet<String>();
        for (String s : methods) {
            methodNames.add(s);
        }
        int count = 0;
        for (Method method : service.getServiceClass().getMethods()) {
            if (methodNames.contains(ReflectUtils.getSignature(method).toString())) {
                count++;
            }
        }
        Assert.assertEquals(methods.length, count);
    }

    @Test
    public void publishWithDynamicInterfaceMethodInterceptingTest() throws Exception {
        assertNotNull(applicationContext);
        ServiceManager serviceManager = applicationContext.getBean("serviceManager", ServiceManager.class);
        assertNotNull(serviceManager);
        serviceManager.start();
        RulesFrontend frontend = applicationContext.getBean("frontend", RulesFrontend.class);
        assertNotNull(frontend);
        OpenLService service = frontend.findServiceByName("dynamic-interface-test3");
        assertNotNull(service);
        assertNotNull(service.getServiceClass());
        IRulesRuntimeContext context = new DefaultRulesRuntimeContext();
        Class<?> myClassClass = service.getServiceClass().getClassLoader()
                .loadClass("org.openl.ruleservice.dynamicinterface.test.MyClass");
        Object myClassIntance = myClassClass.newInstance();
        Method setNameMethod = myClassClass.getMethod("setName", String.class);
        final String someValue = "someValue";
        setNameMethod.invoke(myClassIntance, someValue);
        Object result = frontend
                .execute("dynamic-interface-test3", "method2", new Object[] { context, myClassIntance });
        Assert.assertTrue(myClassClass.isInstance(result));
        Method getNameMethod = myClassClass.getMethod("getName");
        Object name = getNameMethod.invoke(result);
        Assert.assertEquals(someValue, name);
        Class<?> myTypeClass = service.getServiceClass().getClassLoader().loadClass("org.openl.generated.beans.MyType");
        Object myTypeInstance = myTypeClass.newInstance();
        result = frontend.execute("dynamic-interface-test3", "method2", new Object[] { context, myTypeInstance });
        Assert.assertNull(result);
        result = frontend.execute("dynamic-interface-test3", "method3", new Object[] { context, myClassIntance });
        Object value = getNameMethod.invoke(myClassIntance);
        Assert.assertEquals("beforeAdviceWasInvoked", value);
    }
}
