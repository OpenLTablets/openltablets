package org.openl.rules.workspace.deploy.impl.jcr;

import org.openl.rules.repository.ProductionRepositoryFactoryProxy;
import org.openl.rules.repository.REntity;
import org.openl.rules.repository.RFile;
import org.openl.rules.repository.RFolder;
import org.openl.rules.repository.RProductionDeployment;
import org.openl.rules.repository.RProductionRepository;
import org.openl.rules.repository.RProject;
import org.openl.rules.repository.exceptions.RRepositoryException;
import org.openl.rules.workspace.WorkspaceUser;
import org.openl.rules.workspace.abstracts.Project;
import org.openl.rules.workspace.abstracts.ProjectArtefact;
import org.openl.rules.workspace.abstracts.ProjectException;
import org.openl.rules.workspace.abstracts.ProjectFolder;
import org.openl.rules.workspace.abstracts.ProjectResource;
import org.openl.rules.workspace.deploy.DeployID;
import org.openl.rules.workspace.deploy.DeploymentException;
import org.openl.rules.workspace.deploy.ProductionDeployer;
import org.openl.rules.workspace.repository.RulesRepositoryArtefact;

import java.util.Collection;

/**
 * Implementation of <code>ProductionDeployer</code> that uses <i>JCR</i> as
 * production repository.
 */
public class JcrProductionDeployer implements ProductionDeployer {
    /**
     * The user.
     */
    private final WorkspaceUser user;

    public JcrProductionDeployer(WorkspaceUser user) {
        this.user = user;
    }

    /**
     * Deploys a collection of <code>Project</code>s to the production
     * repository. Generates unique ID for the deployment.
     * 
     * @param projects projects to deploy
     * @return generated id for this deployment
     * @throws DeploymentException if any deployment error occures
     */
    public DeployID deploy(Collection<? extends Project> projects) throws DeploymentException {
        String name = generatedDeployID(projects);
        return deploy(new DeployID(name), projects);
    }

    private String generatedDeployID(Collection<? extends Project> projects) {
        StringBuilder name = new StringBuilder();
        for (Project p : projects) {
            name.append(p.getName());
            if (p.getVersion() != null) {
                name.append('-').append(p.getVersion().getVersionName());
            }
            name.append('_');
        }
        name.append(System.currentTimeMillis());
        return name.toString();
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

    public synchronized DeployID deploy(DeployID id, Collection<? extends Project> projects) throws DeploymentException {

        boolean alreadyDeployed = false;
        try {
            RProductionRepository rRepository = ProductionRepositoryFactoryProxy.getRepositoryInstance();

            if (rRepository.hasDeployment(id.getName())) {
                alreadyDeployed = true;
            } else {

                RProductionDeployment deployment = rRepository.createDeployment(id.getName());

                for (Project p : projects) {
                    deployProject(deployment, p);
                }

                deployment.save();
            }
        } catch (Exception e) {
            throw new DeploymentException("failed to deploy", e);
        }

        if (alreadyDeployed) {
            throw new DeploymentException("project is already deployed to production repository, id: " + id.getName(),
                    null);
        }

        return id;
    }

    private void deployProject(RProductionDeployment deployment, Project project) throws RRepositoryException,
            ProjectException {
        RProject rProject = deployment.createProject(project.getName());

        copy(rProject.getRootFolder(), project);
    }

    private void copy(RFolder dest, ProjectFolder source) throws RRepositoryException, ProjectException {
        for (ProjectArtefact artefact : source.getArtefacts()) {
            if (artefact.isFolder()) {
                RFolder folder = dest.createFolder(artefact.getName());
                copyProperties(folder, (RulesRepositoryArtefact) artefact);
                copy(folder, (ProjectFolder) artefact);
            } else {
                copy(dest, (ProjectResource) artefact);
            }
        }
    }

    private void copy(RFolder folder, ProjectResource artefact) throws RRepositoryException, ProjectException {
        RFile rFile = folder.createFile(artefact.getName());
        rFile.setContent(artefact.getContent());

        copyProperties(rFile, (RulesRepositoryArtefact) artefact);
    }

    private void copyProperties(REntity rEntity, RulesRepositoryArtefact artefact) throws RRepositoryException {
        rEntity.setEffectiveDate(artefact.getEffectiveDate());
        rEntity.setExpirationDate(artefact.getExpirationDate());
        rEntity.setLineOfBusiness(artefact.getLineOfBusiness());
    }
}
