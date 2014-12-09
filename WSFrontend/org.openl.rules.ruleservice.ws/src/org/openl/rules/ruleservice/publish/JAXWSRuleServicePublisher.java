package org.openl.rules.ruleservice.publish;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.openl.rules.ruleservice.core.OpenLService;
import org.openl.rules.ruleservice.core.RuleServiceDeployException;
import org.openl.rules.ruleservice.core.RuleServiceRedeployException;
import org.openl.rules.ruleservice.core.RuleServiceUndeployException;
import org.openl.rules.ruleservice.publish.jaxws.JAXWSInterfaceEnhancerHelper;
import org.openl.rules.ruleservice.servlet.AvailableServicesGroup;
import org.openl.rules.ruleservice.servlet.ServiceInfo;
import org.springframework.beans.factory.ObjectFactory;

/**
 * DeploymentAdmin to expose services via HTTP.
 * 
 * @author PUdalau, Marat Kamalov
 */
public class JAXWSRuleServicePublisher implements RuleServicePublisher, AvailableServicesGroup {

    // private final Log log =
    // LogFactory.getLog(WebServicesRuleServicePublisher.class);

    private ObjectFactory<?> serverFactory;
    private Map<OpenLService, ServiceServer> runningServices = new HashMap<OpenLService, ServiceServer>();
    private String baseAddress;
    private List<ServiceInfo> availableServices = new ArrayList<ServiceInfo>();

    public String getBaseAddress() {
        return baseAddress;
    }

    public void setBaseAddress(String address) {
        this.baseAddress = address;
    }

    public ObjectFactory<?> getServerFactory() {
        return serverFactory;
    }

    public void setServerFactory(ObjectFactory<?> serverFactory) {
        this.serverFactory = serverFactory;
    }

    /* internal for test */ServerFactoryBean getServerFactoryBean() {
        if (serverFactory != null) {
            return (ServerFactoryBean) serverFactory.getObject();
        }
        return new ServerFactoryBean();
    }

    protected Class<?> enhanceServiceClassWithJAXWSAnnotations(Class<?> serviceClass, OpenLService service) throws Exception {
        return JAXWSInterfaceEnhancerHelper.decorateInterface(serviceClass, service);
    }

    public void deploy(OpenLService service) throws RuleServiceDeployException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(service.getServiceClass().getClassLoader());

        try {
            ServerFactoryBean svrFactory = getServerFactoryBean();
            ClassLoader origClassLoader = svrFactory.getBus().getExtension(ClassLoader.class);
            try {
                String url = URLHelper.processURL(service.getUrl());
                String serviceAddress = getBaseAddress() + url;
                svrFactory.setAddress(serviceAddress);
                svrFactory.setServiceClass(enhanceServiceClassWithJAXWSAnnotations(service.getServiceClass(), service));
                svrFactory.setServiceBean(service.getServiceBean());

                svrFactory.getBus().setExtension(service.getServiceClass().getClassLoader(), ClassLoader.class);
                Server wsServer = svrFactory.create();

                ServiceServer serviceServer = new ServiceServer(wsServer, svrFactory.getDataBinding());
                runningServices.put(service, serviceServer);
                availableServices.add(createServiceInfo(service));
                
            } finally {
                svrFactory.getBus().setExtension(origClassLoader, ClassLoader.class);
            }
        } catch (Exception t) {
            throw new RuleServiceDeployException(String.format("Failed to deploy service \"%s\"", service.getName()), t);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public DataBinding getDataBinding(String serviceName) {
        OpenLService service = getServiceByName(serviceName);
        if (service == null) {
            return null;
        }
        return runningServices.get(service).getDatabinding();
    }

    public Collection<OpenLService> getServices() {
        return Collections.unmodifiableCollection(runningServices.keySet());
    }

    public OpenLService getServiceByName(String name) {
        for (OpenLService service : runningServices.keySet()) {
            if (service.getName().equals(name)) {
                return service;
            }
        }
        return null;
    }

    public void undeploy(String serviceName) throws RuleServiceUndeployException {
        OpenLService service = getServiceByName(serviceName);
        if (service == null) {
            throw new RuleServiceUndeployException(String.format("There is no running service with name \"%s\"",
                serviceName));
        }
        try {
            runningServices.get(service).getServer().destroy();
            runningServices.remove(service);
            removeServiceInfo(serviceName);
            service.destroy();
        } catch (Exception t) {
            throw new RuleServiceUndeployException(String.format("Failed to undeploy service \"%s\"", serviceName), t);
        }
    }

    public void redeploy(OpenLService service) throws RuleServiceRedeployException {
        if (service == null) {
            throw new IllegalArgumentException("service argument can't be null");
        }

        try {
            undeploy(service.getName());
            deploy(service);
        } catch (RuleServiceDeployException e) {
            throw new RuleServiceRedeployException("Service redeploy was failed", e);
        } catch (RuleServiceUndeployException e) {
            throw new RuleServiceRedeployException("Service redeploy was failed", e);
        }

    }

    @Override
    public String getGroupName() {
        return "SOAP";
    }

    @Override
    public List<ServiceInfo> getAvailableServices() {
        List<ServiceInfo> services = new ArrayList<ServiceInfo>(availableServices);
        Collections.sort(services, new Comparator<ServiceInfo>() {
            @Override
            public int compare(ServiceInfo o1, ServiceInfo o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        return services;
    }

    private ServiceInfo createServiceInfo(OpenLService service) {
        List<String> methodNames = new ArrayList<String>();
        for (Method method : service.getServiceClass().getMethods()) {
            methodNames.add(method.getName());
        }
        Collections.sort(methodNames, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        String url = URLHelper.processURL(service.getUrl());
        url = url + "?wsdl";
        return new ServiceInfo(new Date(), service.getName(), methodNames, url, "WSDL");
    }

    private void removeServiceInfo(String serviceName) {
        for (Iterator<ServiceInfo> iterator = availableServices.iterator(); iterator.hasNext();) {
            ServiceInfo serviceInfo = iterator.next();
            if (serviceInfo.getName().equals(serviceName)) {
                iterator.remove();
                break;
            }
        }
    }

    private static class ServiceServer {
        private Server server;
        private DataBinding databinding;

        public ServiceServer(Server server, DataBinding dataBinding) {
            if (server == null) {
                throw new IllegalArgumentException("server arg can't be null!");
            }

            this.server = server;
            this.databinding = dataBinding;
        }

        public DataBinding getDatabinding() {
            return databinding;
        }

        public Server getServer() {
            return server;
        }
    }
}
