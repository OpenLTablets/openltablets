package org.openl.rules.webstudio.web.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.rules.workspace.abstracts.Project;
import org.openl.rules.workspace.abstracts.ProjectDescriptor;
import org.openl.rules.workspace.deploy.DeployID;
import org.openl.rules.workspace.deploy.DeploymentException;
import org.openl.rules.workspace.deploy.ProductionDeployer;
import org.openl.rules.workspace.dtr.DesignTimeRepository;
import org.openl.rules.workspace.dtr.RepositoryException;
import org.openl.rules.workspace.uw.UserWorkspaceDeploymentProject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Deployment manager
 * 
 * @author Andrey Naumenko
 */
public class DeploymentManager {
    private final static Log log = LogFactory.getLog(DeploymentManager.class);

    private ProductionDeployer deployer;

    public void setDeployer(ProductionDeployer deployer) {
        this.deployer = deployer;
    }

    public void deploy(UserWorkspaceDeploymentProject project) throws RepositoryException, DeploymentException {
        DesignTimeRepository dtr = RepositoryUtils.getWorkspace().getDesignTimeRepository();

        Collection<ProjectDescriptor> projectDescriptors = project.getProjectDescriptors();
        Collection<Project> projects = new ArrayList<Project>();

        for (ProjectDescriptor pd : projectDescriptors) {
            projects.add(dtr.getProject(pd.getProjectName(), pd.getProjectVersion()));
        }

        DeployID id = RepositoryUtils.getDeployID(project);
        deployer.deploy(id, projects);
    }
}
