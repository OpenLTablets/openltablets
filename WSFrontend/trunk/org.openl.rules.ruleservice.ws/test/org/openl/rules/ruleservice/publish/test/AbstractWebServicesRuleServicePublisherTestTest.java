package org.openl.rules.ruleservice.publish.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.cxf.endpoint.Client;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openl.rules.ruleservice.core.OpenLService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:openl-ruleservice-beans.xml" })
@DirtiesContext
public class AbstractWebServicesRuleServicePublisherTestTest extends AbstractWebServicesRuleServicePublisherTest {
    private static final String SERVICE_NAME_TUTORIAL4 = "tutorial4";

    @Test
    public void testClientInvocation() throws Exception {
        Object client = getClient(SERVICE_NAME_TUTORIAL4);
        assertNotNull(client);
        OpenLService service = getServiceByName(SERVICE_NAME_TUTORIAL4);
        boolean isInstance = service.getServiceClass().isInstance(client);
        assertTrue(isInstance);
        /*
         * Tutorial4Interface tutorial4 = (Tutorial4Interface) client; String[]
         * coverages = tutorial4.getCoverage(); assertEquals(2,
         * coverages.length); String[] theftRating =
         * tutorial4.getTheft_rating(); assertEquals(3, theftRating.length);
         */
        Method method = service.getServiceClass().getMethod("getCoverage");
        Object result = method.invoke(client);
        assertTrue(result instanceof String[]);
        String[] coverages = (String[]) result;
        assertEquals(2, (long)coverages.length);
    }

    @Test
    public void testDynamicClientInvocation() throws Exception {
        Client client = getDynamicClientByServiceName(SERVICE_NAME_TUTORIAL4);
        assertNotNull(client);
        Object result = client.invoke("getCoverage")[0];
        Method method = Thread.currentThread().getContextClassLoader()
                .loadClass("org.openl.rules.tutorial4.ArrayOfString").getMethod("getString");
        List<?> listResult = (List<?>) method.invoke(result);
        assertEquals(2, listResult.size());
    }
}
