package org.openl.rules.ruleservice.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.rules.ruleservice.publish.RuleServicePublisher;

/**
 * Default implementation of RulesService. Uses publisher and instantiation
 * factory. Publisher is responsible for service exposing. Instantiation factory
 * is responsible for build OpenLService instances from ServiceDescription. This
 * class designed for using it from Spring.
 * 
 * @author Marat Kamalov
 * 
 */
public class RuleServiceImpl implements RuleService {

    private final Log log = LogFactory.getLog(RuleServiceImpl.class);
    /**
     * Publisher.
     */
    private RuleServicePublisher ruleServicePublisher;

    /**
     * Instantiation factory.
     */
    private RuleServiceInstantiationFactory ruleServiceInstantiationFactory;

    private Map<String, ServiceDescription> mapping = new HashMap<String, ServiceDescription>();

    /** {@inheritDoc} */
    public void redeploy(ServiceDescription serviceDescription) throws RuleServiceRedeployException {
        OpenLService service = ruleServicePublisher.getServiceByName(serviceDescription.getName());
        if (service == null) {
            throw new RuleServiceRedeployException(String.format("There is no running service with name \"%s\"",
                serviceDescription.getName()));
        }
        try {
            ServiceDescription sd = mapping.get(serviceDescription.getName());
            if (sd == null) {
                throw new IllegalStateException("Invalid state!!!");
            }
            if (sd.getDeployment().getVersion().compareTo(serviceDescription.getDeployment().getVersion()) != 0) {
                ruleServicePublisher.redeploy(ruleServiceInstantiationFactory.createService(serviceDescription));
                mapping.put(serviceDescription.getName(), serviceDescription);
                if (log.isInfoEnabled()) {
                    log.info(String.format("Service \"%s\" with URL \"%s\" succesfully redeployed.",
                        service.getName(),
                        service.getUrl()));
                }
            }
        } catch (RuleServiceInstantiationException e) {
            throw new RuleServiceRedeployException("Failed on deploy service", e);
        }
    }

    /** {@inheritDoc} */
    public void undeploy(String serviceName) throws RuleServiceUndeployException {
        if (serviceName == null) {
            throw new IllegalArgumentException("serviceName arg can't be null");
        }
        OpenLService service = ruleServicePublisher.getServiceByName(serviceName);
        if (service == null) {
            throw new RuleServiceUndeployException(String.format("There is no running service with name \"%s\"",
                serviceName));
        }

        ServiceDescription serviceDescription = mapping.get(serviceName);
        if (serviceDescription == null) {
            throw new IllegalStateException("Illegal State!!!");
        }

        ruleServicePublisher.undeploy(serviceName);
        mapping.remove(serviceDescription.getName());
        if (ruleServiceInstantiationFactory instanceof RuleServiceOpenLServiceInstantiationFactoryImpl){ //NEED SOME FIX.
            ((RuleServiceOpenLServiceInstantiationFactoryImpl) ruleServiceInstantiationFactory).clear(serviceDescription.getDeployment());
        }
        if (log.isInfoEnabled()) {
            log.info(String.format("Service \"%s\" with URL \"%s\" succesfully undeployed.",
                service.getName(),
                service.getUrl()));
        }
    }

    /** {@inheritDoc} */
    public Collection<OpenLService> getServices() {
        return ruleServicePublisher.getServices();
    }

    /** {@inheritDoc} */
    public OpenLService getServiceByName(String serviceName) {
        return ruleServicePublisher.getServiceByName(serviceName);
    }

    /** {@inheritDoc} */
    public void deploy(ServiceDescription serviceDescription) throws RuleServiceDeployException {
        OpenLService service = ruleServicePublisher.getServiceByName(serviceDescription.getName());
        if (service != null) {
            throw new RuleServiceDeployException("The service with name \"" + serviceDescription.getName() + "\" has already deployed!");
        }
        try {
            OpenLService newService = ruleServiceInstantiationFactory.createService(serviceDescription);
            ServiceDescription sd = mapping.get(serviceDescription.getName());
            if (sd != null) {
                throw new IllegalStateException("Illegal State!!");
            }
            ruleServicePublisher.deploy(newService);
            mapping.put(serviceDescription.getName(), serviceDescription);
            if (log.isInfoEnabled()) {
                log.info(String.format("Service \"%s\" with URL \"%s\" succesfully deployed.",
                    serviceDescription.getName(),
                    serviceDescription.getUrl()));
            }
        } catch (RuleServiceInstantiationException e) {
            throw new RuleServiceDeployException("Failed on deploy service", e);
        }
    }

    public RuleServicePublisher getRuleServicePublisher() {
        return ruleServicePublisher;
    }

    public void setRuleServicePublisher(RuleServicePublisher ruleServicePublisher) {
        if (ruleServicePublisher == null) {
            throw new IllegalArgumentException("ruleServicePublisher arg can't be null");
        }
        this.ruleServicePublisher = ruleServicePublisher;
    }

    public RuleServiceInstantiationFactory getRuleServiceInstantiationFactory() {
        return ruleServiceInstantiationFactory;
    }

    public void setRuleServiceInstantiationFactory(RuleServiceInstantiationFactory ruleServiceInstantiationFactory) {
        if (ruleServiceInstantiationFactory == null) {
            throw new IllegalArgumentException("ruleServiceInstantiationFactory arg can't be null");
        }
        this.ruleServiceInstantiationFactory = ruleServiceInstantiationFactory;
    }

}
