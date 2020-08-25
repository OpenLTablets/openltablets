package org.openl.rules.ruleservice.loader;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PreDestroy;

import org.openl.rules.common.CommonVersion;
import org.openl.rules.common.impl.CommonVersionImpl;
import org.openl.rules.project.abstraction.Deployment;
import org.openl.rules.repository.api.FileData;
import org.openl.rules.repository.api.FolderRepository;
import org.openl.rules.repository.api.Repository;
import org.openl.util.RuntimeExceptionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository data source. Uses ProductionRepositoryFactoryProxy.getRepositoryInstance() repository. Thread safe
 * implementation.
 *
 * @author Marat Kamalov
 */
public class ProductionRepositoryDataSource implements DataSource {
    private final Logger log = LoggerFactory.getLogger(ProductionRepositoryDataSource.class);

    private Repository repository;
    private String deployPath;

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Deployment> getDeployments() {
        Collection<FileData> fileDatas;
        try {
            if (repository.supports().folders()) {
                // All deployments
                fileDatas = ((FolderRepository) repository).listFolders(deployPath);
            } else {
                // Projects inside all deployments
                fileDatas = repository.list(deployPath);
            }
        } catch (IOException ex) {
            throw RuntimeExceptionWrapper.wrap(ex);
        }
        ConcurrentMap<String, Deployment> deployments = new ConcurrentHashMap<>();
        for (FileData fileData : fileDatas) {
            String deploymentFolderName = fileData.getName().substring(deployPath.length()).split("/")[0];

            String version = fileData.getVersion();
            CommonVersionImpl commonVersion = new CommonVersionImpl(version == null ? "0.0.0" : version);

            String folderPath = deployPath + deploymentFolderName;

            boolean folderStructure = isFolderStructure(folderPath);

            Deployment deployment = new Deployment(repository,
                folderPath, deploymentFolderName,
                commonVersion,
                folderStructure);
            deployments.putIfAbsent(deploymentFolderName, deployment);
        }

        return deployments.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Deployment getDeployment(String deploymentName, CommonVersion deploymentVersion) {
        Objects.requireNonNull(deploymentName, "deploymentName cannot be null");
        Objects.requireNonNull(deploymentVersion, "deploymentVersion cannot be null");

        log.debug("Getting deployement with name='{}' and version='{}'",
            deploymentName,
            deploymentVersion.getVersionName());

        String folderPath = deployPath + deploymentName;
        boolean folderStructure = isFolderStructure(folderPath);
        return new Deployment(repository, folderPath, deploymentName, deploymentVersion, folderStructure);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setListener(final DataSourceListener dataSourceListener) {
        if (dataSourceListener == null) {
            repository.setListener(null);
        } else {
            repository.setListener(dataSourceListener::onDeploymentAdded);
        }
    }

    /**
     * For Spring framework
     */
    @PreDestroy
    public void destroy() throws Exception {
        log.debug("Data source releasing");
        if (repository instanceof Closeable) {
            ((Closeable) repository).close();
        }
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void setDeployPath(String deployPath) {
        this.deployPath = deployPath.isEmpty() || deployPath.endsWith("/") ? deployPath : deployPath + "/";
    }

    private boolean isFolderStructure(String deploymentFolderPath) {
        boolean folderStructure;
        try {
            if (repository.supports().folders()) {
                folderStructure = !((FolderRepository) repository).listFolders(deploymentFolderPath + "/").isEmpty();
            } else {
                folderStructure = false;
            }
        } catch (IOException e) {
            throw RuntimeExceptionWrapper.wrap(e);
        }
        return folderStructure;
    }
}
