package org.openl.rules.ruleservice.conf;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openl.rules.ruleservice.conf.LastVersionProjectsServiceConfigurer;
import org.openl.rules.ruleservice.core.ServiceDescription;
import org.openl.rules.ruleservice.loader.RuleServiceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:LastVersionProjectsServiceConfigurerTest/openl-ruleservice-filesystemdatasource-beans.xml" })
public class LastVersionProjectsServiceConfigurerTest {
    private static final String PROJECT_NAME = "openl-project";
    
    @Autowired
    private RuleServiceLoader rulesLoader;

    public RuleServiceLoader getRulesLoader() {
        return rulesLoader;
    }

    public void setRulesLoader(RuleServiceLoader rulesLoader) {
        this.rulesLoader = rulesLoader;
    }

    @Test
    public void testConfigurer() {
        LastVersionProjectsServiceConfigurer configurer = new LastVersionProjectsServiceConfigurer();
        Collection<ServiceDescription> servicesToBeDeployed = configurer.getServicesToBeDeployed(rulesLoader);
        assertEquals(2, servicesToBeDeployed.size());
        Set<String> serviceNames = new HashSet<String>();
        for (ServiceDescription description : servicesToBeDeployed) {
            serviceNames.add(description.getName());
        }
        assertEquals(serviceNames.size(), servicesToBeDeployed.size());
        assertTrue(serviceNames.contains(PROJECT_NAME));
    }

}
