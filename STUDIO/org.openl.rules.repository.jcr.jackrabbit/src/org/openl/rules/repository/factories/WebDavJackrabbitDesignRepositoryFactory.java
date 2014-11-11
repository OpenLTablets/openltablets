package org.openl.rules.repository.factories;

import org.openl.config.ConfigPropertyString;

/**
 * @author PUdalau
 */
public class WebDavJackrabbitDesignRepositoryFactory extends WebDavJacrabbitRepositoryFactory {

    private ConfigPropertyString confWebdavUrl = new ConfigPropertyString("design-repository.remote.webdav.url",
        "http://localhost:8080/jcr/server/");
    private final ConfigPropertyString login = new ConfigPropertyString("design-repository.login", "user");
    private final ConfigPropertyString password = new ConfigPropertyString("design-repository.pass", "pass");
    private final ConfigPropertyString repoConfigFile = new ConfigPropertyString("design-repository.config",
        "/jackrabbit-repository.xml");

    public WebDavJackrabbitDesignRepositoryFactory() {
        setConfWebdavUrl(confWebdavUrl);
        setLogin(login);
        setPassword(password);
        setRepoConfigFile(repoConfigFile);
    }
}
