package org.openl.rules.repository.factories;

import static org.apache.commons.io.FileUtils.getTempDirectoryPath;

import java.io.File;
import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.core.TransientRepository;
import org.openl.config.ConfigPropertyString;
import org.openl.rules.repository.RProductionRepository;
import org.openl.rules.repository.RRepositoryFactory;
import org.openl.rules.repository.RTransactionManager;
import org.openl.rules.repository.RulesRepositoryFactory;
import org.openl.rules.repository.RulesRepositoryFactoryAware;
import org.openl.rules.repository.exceptions.RRepositoryException;
import org.openl.rules.repository.jcr.JcrProductionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalJackrabbitProductionRepositoryFactory extends LocalJackrabbitRepositoryFactory implements RulesRepositoryFactoryAware {

    private final Logger log = LoggerFactory.getLogger(LocalJackrabbitProductionRepositoryFactory.class);

    private final ConfigPropertyString confRepositoryHome = new ConfigPropertyString("production-repository.local.home",
        "../local-repository");
    private final ConfigPropertyString confNodeTypeFile = new ConfigPropertyString("production-repository.jcr.nodetypes",
        DEFAULT_NODETYPE_FILE);
    private final ConfigPropertyString confRepositoryName = new ConfigPropertyString("production-repository.name",
        "Local Jackrabbit");
    private final ConfigPropertyString login = new ConfigPropertyString("production-repository.login", "user");
    private final ConfigPropertyString password = new ConfigPropertyString("production-repository.password", "pass");
    private final ConfigPropertyString repoConfigFile = new ConfigPropertyString("production-repository.config",
        "/jackrabbit-repository.xml");

    private RulesRepositoryFactory rulesRepositoryFactory;

    public LocalJackrabbitProductionRepositoryFactory() {
        setConfRepositoryHome(confRepositoryHome);
        setConfNodeTypeFile(confNodeTypeFile);
        setConfRepositoryName(confRepositoryName);
        setLogin(login);
        setPassword(password);
        setRepoConfigFile(repoConfigFile);
        setProductionRepositoryMode(true);
    }

    /**
     * Checks whether jcr repository on filesystem will be used by production
     * and design repositories simultaneously.
     *
     * @return <code>true</code> if repository is used by local design
     *         repository from current process.
     */
    private boolean isUsedByMyLocalDesignRepository() {
        if (rulesRepositoryFactory == null) {
            return false;
        }

        RRepositoryFactory repFactory = rulesRepositoryFactory.getRepositoryFactory();
        if (repFactory instanceof LocalJackrabbitDesignRepositoryFactory) {
            return this.repHome.equals(((LocalJackrabbitDesignRepositoryFactory) repFactory).repHome);
        }
        return false;

    }

    protected void createTransientRepo(String fullPath) throws RepositoryException {
        if (isRepositoryLocked(repHome)) {
            if (isUsedByMyLocalDesignRepository()) {
                repository = ((LocalJackrabbitDesignRepositoryFactory) rulesRepositoryFactory.getRepositoryFactory()).repository;
            } else {
                throw new RepositoryException("Repository is already locked.");
            }
        } else {
            repository = new TransientRepository(fullPath, repHome);
        }
    }

    protected void convert() throws RRepositoryException {
        RProductionRepository repositoryInstance = null;
        String tempRepoHome = getTempDirectoryPath() + "/.openl/prod_repo/";
        try {
            // FIXME: do not hardcode credential info
            Session session = createSession();
            RTransactionManager transactionManager = getTrasactionManager(session);
            repositoryInstance = new JcrProductionRepository(repositoryName, session, transactionManager);
            // FIXME
            ProductionRepositoryConvertor repositoryConvertor = new ProductionRepositoryConvertor(tempRepoHome);
            log.info("Converting production repository. Please, be patient.");
            repositoryConvertor.convert(repositoryInstance);
        } catch (Exception e) {
            throw new RRepositoryException("Failed to convert repository.", e);
        } finally {
            if (repositoryInstance != null) {
                repositoryInstance.release();
            }
        }

        File repoHome = new File(repHome);
        File tmpRepoHome = new File(tempRepoHome);
        try {
            FileUtils.deleteDirectory(repoHome);
            FileUtils.copyDirectory(tmpRepoHome, repoHome);
        } catch (IOException e) {
            throw new RRepositoryException("Failed to convert repository.", e);
        } finally {
            FileUtils.deleteQuietly(tmpRepoHome);
        }
    }

    @Override
    public void setRulesRepositoryFactory(RulesRepositoryFactory factory) {
        this.rulesRepositoryFactory = factory;
    }
}
