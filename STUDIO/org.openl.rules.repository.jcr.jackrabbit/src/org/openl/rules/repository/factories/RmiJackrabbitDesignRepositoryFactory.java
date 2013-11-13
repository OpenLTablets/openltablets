package org.openl.rules.repository.factories;

import org.openl.config.ConfigPropertyString;

public class RmiJackrabbitDesignRepositoryFactory extends RmiJackrabbitRepositoryFactory {

    private ConfigPropertyString confRmiUrl = new ConfigPropertyString(
            "design-repository.remote.rmi.url", "//localhost:1099/jackrabbit.repository");
    private final ConfigPropertyString login = new ConfigPropertyString(
            "design-repository.login", "user");
    private final ConfigPropertyString password = new ConfigPropertyString(
            "design-repository.pass", "pass");
    private final ConfigPropertyString repoConfigFile = new ConfigPropertyString(
            "design-repository.config", "/jackrabbit-repository.xml");

    public RmiJackrabbitDesignRepositoryFactory() {
        setConfRmiUrl(confRmiUrl);
        setLogin(login);
        setPassword(password);
        setRepoConfigFile(repoConfigFile);
    }

}
