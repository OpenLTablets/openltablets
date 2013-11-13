package org.openl.rules.ruleservice.multimodule;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openl.rules.ruleservice.test.AbstractJavaClassRuleServiceTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:MultipleProjectsInDeploymentTest/openl-ruleservice-beans.xml" })
public class MultipleProjectsInDeploymentTest extends AbstractJavaClassRuleServiceTest {

    @Test
    public void testInvoke() throws Exception {
        assertEquals("Hello First world", execute("first-hello", "sayHello", new Object[0]));
        assertEquals("Hello Second world", execute("second-hello", "sayHello", new Object[0]));
        assertEquals("Hello First world", execute("third-hello", "sayHello", new Object[0]));
    }
}
