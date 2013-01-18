package org.openl.rules.workspace.deploy.impl.jcr;

import java.util.Collection;

import org.openl.rules.common.ProjectException;
import org.openl.rules.common.ProjectVersion;
import org.openl.rules.common.PropertyException;
import org.openl.rules.common.RulesRepositoryArtefact;
import org.openl.rules.project.abstraction.ADeploymentProject;
import org.openl.rules.project.abstraction.AProject;
import org.openl.rules.project.abstraction.AProjectArtefact;
import org.openl.rules.repository.ProductionRepositoryFactoryProxy;
import org.openl.rules.repository.RProductionRepository;
import org.openl.rules.repository.api.FolderAPI;
import org.openl.rules.repository.exceptions.RRepositoryException;
import org.openl.rules.workspace.WorkspaceUser;
import org.openl.rules.workspace.deploy.DeployID;
import org.openl.rules.workspace.deploy.DeploymentException;
import org.openl.rules.workspace.deploy.ProductionDeployer;

/**
 * Implementation of <code>ProductionDeployer</code> that uses <i>JCR</i> as
 * production repository.
 */
public class JcrProductionDeployer implements ProductionDeployer {
    private final ProductionRepositoryFactoryProxy repositoryFactoryProxy;
    private final String repositoryConfigName;

    public JcrProductionDeployer(ProductionRepositoryFactoryProxy repositoryFactoryProxy, String repositoryConfigName) {
        this.repositoryFactoryProxy = repositoryFactoryProxy;
        this.repositoryConfigName = repositoryConfigName;
    }

    private void copyProperties(AProjectArtefact newArtefact, RulesRepositoryArtefact artefact) throws RRepositoryException {
        try {
            newArtefact.setProps(artefact.getProps());
        } catch (PropertyException e) {
            throw new RRepositoryException("", e);
        }
    }

    /**
     * Deploys a collection of <code>Project</code>s to the production
     * repository with given ID. Overwrites deployment with given <i>id</i> if
     * it already exists.
     *
     * @param projects projects to deploy
     * @return <code>id</code> parameter
     * @throws DeploymentException if any deployment error occures
     */

    public synchronized DeployID deploy(ADeploymentProject deploymentProject, Collection<AProject> projects, WorkspaceUser user) throws DeploymentException {
      DeployID id = generateDeployID(deploymentProject);
      
        boolean alreadyDeployed = false;
        try {
            RProductionRepository rRepository = repositoryFactoryProxy.getRepositoryInstance(repositoryConfigName);

            if (rRepository.hasDeploymentProject(id.getName())) {
                alreadyDeployed = true;
            } else {
                FolderAPI deployment = rRepository.createDeploymentProject(id.getName());

                AProject deploymentPRJ = new AProject(deployment);
                deploymentPRJ.lock(user);
                for (AProject p : projects) {
                    deployProject(deploymentPRJ, p, user);
                }

                copyProperties(deploymentPRJ, deploymentProject);

                deploymentPRJ.save(user);
                rRepository.notifyChanges();
            }
        } catch (Exception e) {
            throw new DeploymentException("Failed to deploy: " + e.getMessage(), e);
        }

        if (alreadyDeployed) {
            throw new DeploymentException("Project is already deployed to production repository, id: " + id.getName(),
                    null);
        }

        return id;
    }
    
    /**
     * Checks if deploymentConfiguration is already deployed to this production
     * repository.
     * 
     * @param deploymentConfiguration deployment configuration for project
     *            trying to deploy
     * @return true if deploymentConfiguration with its id already exists in
     *         production repository
     * @throws RRepositoryException if cannot get info from repository for some
     *             reason
     */
    @Override
    public synchronized boolean hasDeploymentProject(ADeploymentProject deploymentConfiguration) throws RRepositoryException {
        RProductionRepository repository = repositoryFactoryProxy.getRepositoryInstance(repositoryConfigName);
        DeployID id = generateDeployID(deploymentConfiguration);
        return repository.hasDeploymentProject(id.getName());
    }

    private void deployProject(AProject deployment, AProject project, WorkspaceUser user) throws RRepositoryException,
            ProjectException {
        FolderAPI rProject = deployment.addFolder(project.getName()).getAPI();
        AProject copiedProject = new AProject(rProject);

        /*Update and set project revision*/
        copiedProject.update(project, user, project.getVersion().getRevision());
    }

    private DeployID generateDeployID(ADeploymentProject ddProject) {
        StringBuilder sb = new StringBuilder(ddProject.getName());
        ProjectVersion projectVersion = ddProject.getVersion();
        if (projectVersion != null) {
            sb.append('#').append(projectVersion.getVersionName());
        }
        return new DeployID(sb.toString());
    }

    @Override
    public void destroy() throws RRepositoryException {
        if (repositoryFactoryProxy != null) {
            repositoryFactoryProxy.releaseRepository(repositoryConfigName);
        }
    }
}
