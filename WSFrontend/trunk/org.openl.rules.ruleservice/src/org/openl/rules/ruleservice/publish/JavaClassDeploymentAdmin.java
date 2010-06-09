package org.openl.rules.ruleservice.publish;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.main.OpenLWrapper;
import org.openl.rules.ruleservice.instantiation.RulesInstantiationStrategy;
import org.openl.rules.ruleservice.instantiation.EngineFactoryInstantiationStrategy;
import org.openl.rules.ruleservice.instantiation.WebServiceEngineFactoryInstantiationStrategy;
import org.openl.rules.ruleservice.instantiation.WrapperAdjustingInstantiationStrategy;
import org.openl.rules.ruleservice.resolver.RuleServiceInfo;

public class JavaClassDeploymentAdmin implements DeploymentAdmin {
    private static final Log log = LogFactory.getLog(JavaClassDeploymentAdmin.class);

    private Map<String, Map<String, OpenLWrapper>> runningServices = new HashMap<String, Map<String, OpenLWrapper>>();

    private Collection<DeploymentListener> deploymentListeners = new ArrayList<DeploymentListener>();

    public void addDeploymentListener(DeploymentListener deploymentListener) {
        if (deploymentListener != null) {
            deploymentListeners.add(deploymentListener);
        }
    }

    public synchronized void deploy(String deploymentName, ClassLoader loader, List<RuleServiceInfo> infoList) {
        onBeforeDeployment(deploymentName);

        undeploy(deploymentName);

        Map<String, OpenLWrapper> projectWrappers = new HashMap<String, OpenLWrapper>();
        for (RuleServiceInfo wsInfo : infoList) {
            try {
                OpenLWrapper wrapper = deploy(deploymentName, loader, wsInfo);
                projectWrappers.put(wsInfo.getName(), wrapper);
            } catch (Exception e) {
                log.error("failed to create service", e);
            }
        }

        runningServices.put(deploymentName, projectWrappers);
        log.info(String.format("Deployed \"%s\" ", deploymentName));

        onAfterDeployment(deploymentName, projectWrappers);
    }

    private OpenLWrapper deploy(String serviceName, ClassLoader loader, RuleServiceInfo wsInfo) throws ClassNotFoundException,
                                                                                               IllegalAccessException,
                                                                                               InstantiationException {
        return (OpenLWrapper) getStrategy(wsInfo, wsInfo.getClassName(), loader).instantiate();

    }

    private RulesInstantiationStrategy getStrategy(RuleServiceInfo wsInfo, String className, ClassLoader classLoader) {

        switch (wsInfo.getServiceType()) {
            case DYNAMIC_WRAPPER:
                return new EngineFactoryInstantiationStrategy(wsInfo.getXlsFile(), className, classLoader);
            case STATIC_WRAPPER:
                String path = ".";
                try {
                    path = wsInfo.getProject().getCanonicalPath();
                } catch (IOException e) {
                    log.error("failed to get canonical path", e);
                }
                return new WrapperAdjustingInstantiationStrategy(path, className, classLoader);
            case AUTO_WRAPPER:
                return new WebServiceEngineFactoryInstantiationStrategy(wsInfo.getXlsFile(), className, classLoader);
        }
        
        throw new RuntimeException("Cannot resolve instantiation strategy");
    }

    private void onAfterDeployment(String deploymentName, Map<String, OpenLWrapper> projectWrappers) {
        for (DeploymentListener deploymentListener : deploymentListeners) {
            deploymentListener.afterDeployment(deploymentName, projectWrappers);
        }
    }

    private void onAfterUndeployment(String deploymentName) {
        for (DeploymentListener deploymentListener : deploymentListeners) {
            deploymentListener.afterUndeployment(deploymentName);
        }
    }

    private void onBeforeDeployment(String deploymentName) {
        for (DeploymentListener deploymentListener : deploymentListeners) {
            deploymentListener.beforeDeployment(deploymentName);
        }
    }

    private void onBeforeUndeployment(String deploymentName) {
        for (DeploymentListener deploymentListener : deploymentListeners) {
            deploymentListener.beforeUndeployment(deploymentName);
        }
    }

    public void removeDeploymentListener(DeploymentListener deploymentListener) {
        deploymentListeners.remove(deploymentListener);
    }

    public synchronized void undeploy(String deploymentName) {
        onBeforeUndeployment(deploymentName);

        runningServices.remove(deploymentName);

        onAfterUndeployment(deploymentName);
    }

}