package org.openl.rules.webstudio.configuration;

import org.openl.config.ConfigurationManager;
import org.openl.rules.testmethod.TestSuiteExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;

/**
 * This class contains system-wide application configuration that needs some java code.
 */
@Configuration
public class SystemConfigurer {
    @Autowired
    private ConfigurationManager systemConfigManager;

    @PostConstruct
    public void init() {
        Map<String, Object> systemConfig = systemConfigManager.getProperties();
        TestSuiteExecutor.setExternalParameters(systemConfig);
    }

    @PreDestroy
    public void destroy() {
        TestSuiteExecutor.shutDown();
    }
}
