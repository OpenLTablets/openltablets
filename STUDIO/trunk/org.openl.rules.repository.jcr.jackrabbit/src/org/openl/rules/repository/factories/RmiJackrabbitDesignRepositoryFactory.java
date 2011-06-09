package org.openl.rules.repository.factories;

import org.openl.config.ConfigPropertyString;

public class RmiJackrabbitDesignRepositoryFactory extends RmiJackrabbitRepositoryFactory {

    private ConfigPropertyString confRmiUrl = new ConfigPropertyString(
            "design-repository.jackrabbit.rmi.url", "//localhost:1099/jackrabbit.repository");

    public RmiJackrabbitDesignRepositoryFactory() {
        setConfRmiUrl(confRmiUrl);
    }

}
