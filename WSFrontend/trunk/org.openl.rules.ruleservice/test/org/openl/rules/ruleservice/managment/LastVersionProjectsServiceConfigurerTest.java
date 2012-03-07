package org.openl.rules.ruleservice.managment;

import static junit.framework.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openl.rules.ruleservice.core.ServiceDescription;
import org.openl.rules.ruleservice.loader.RuleServiceLoader;
import org.openl.rules.ruleservice.management.LastVersionProjectsServiceConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:openl-ruleservice-filesystemdatasource.xml" })
public class LastVersionProjectsServiceConfigurerTest {
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
        Set<String> serviceUrls = new HashSet<String>();
        for (ServiceDescription description : servicesToBeDeployed) {
            serviceNames.add(description.getName());
            serviceUrls.add(description.getName());
        }
        assertEquals(serviceNames.size(), servicesToBeDeployed.size());
        assertEquals(serviceUrls.size(), servicesToBeDeployed.size());
    }

}
