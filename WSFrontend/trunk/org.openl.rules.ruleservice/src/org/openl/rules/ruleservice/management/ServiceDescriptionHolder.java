package org.openl.rules.ruleservice.management;

import org.openl.rules.ruleservice.core.ServiceDescription;

public class ServiceDescriptionHolder {
    private final static ServiceDescriptionHolder INSTANCE = new ServiceDescriptionHolder();

    private ThreadLocal<ServiceDescription> serviceDescriptionHolder = new ThreadLocal<ServiceDescription>();

    public static ServiceDescriptionHolder getInstance() {
        return INSTANCE;
    }

    public ServiceDescription getServiceDescription() {
        return serviceDescriptionHolder.get();
    }

    public void setServiceDescription(ServiceDescription serviceDescription) {
        serviceDescriptionHolder.set(serviceDescription);
    }

    public void remove() {
        serviceDescriptionHolder.remove();
    }

}
