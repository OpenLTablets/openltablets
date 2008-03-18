package org.openl.rules.webstudio.web.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openl.rules.repository.CommonVersionImpl;
import org.openl.rules.webstudio.web.jsf.util.FacesUtils;
import org.openl.rules.workspace.abstracts.Project;
import org.openl.rules.workspace.abstracts.ProjectArtefact;
import org.openl.rules.workspace.abstracts.ProjectDescriptor;
import org.openl.rules.workspace.abstracts.ProjectException;
import org.openl.rules.workspace.abstracts.ProjectVersion;
import org.openl.rules.workspace.deploy.DeployID;
import org.openl.rules.workspace.deploy.DeploymentException;
import org.openl.rules.workspace.deploy.ProductionDeployer;
import org.openl.rules.workspace.uw.UserWorkspace;
import org.openl.rules.workspace.uw.UserWorkspaceDeploymentProject;
import org.openl.rules.workspace.uw.UserWorkspaceProject;
import org.openl.rules.workspace.uw.impl.UserWorkspaceDeploymentProjectImpl;
import org.openl.rules.workspace.uw.impl.UserWorkspaceProjectDescriptorImpl;
import org.openl.rules.workspace.dtr.DesignTimeRepository;
import org.openl.rules.workspace.dtr.RepositoryException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

/**
 * Deployment controller.
 * 
 * @author Andrey Naumenko
 */
public class DeploymentController {
    private final static Log log = LogFactory.getLog(DeploymentController.class);
    private List<DeploymentDescriptorItem> items;
    private String projectName;
    private String version;
    private RepositoryTreeState repositoryTreeState;
    private String cachedForProject;
    private DeploymentManager deploymentManager;

    public void setDeploymentManager(DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    public synchronized List<DeploymentDescriptorItem> getItems() {
        UserWorkspaceDeploymentProject project = getSelectedProject();
        if (project == null) {
            return null;
        }

        if (items != null && project.getName().equals(cachedForProject)) {
            return items;
        }

        cachedForProject = project.getName();
        Collection<ProjectDescriptor> descriptors = project.getProjectDescriptors();
        items = new ArrayList<DeploymentDescriptorItem>();

        for (ProjectDescriptor descriptor : descriptors) {
            DeploymentDescriptorItem item = new DeploymentDescriptorItem(descriptor.getProjectName(), descriptor
                    .getProjectVersion());
            items.add(item);
        }

        try {
            checkConflicts(items);
        } catch (ProjectException e) {
            log.error(e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
        }

        return items;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public synchronized String addItem() {
        UserWorkspaceDeploymentProject project = getSelectedProject();

        UserWorkspaceProjectDescriptorImpl newItem = new UserWorkspaceProjectDescriptorImpl(
                (UserWorkspaceDeploymentProjectImpl) project, projectName, new CommonVersionImpl(version));
        List<ProjectDescriptor> newDescriptors = replaceDescriptor(project, projectName, newItem);

        try {
            project.setProjectDescriptors(newDescriptors);
        } catch (ProjectException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "failed to add project descriptor", e.getMessage()));
            log.error(e);
        }

        return null;
    }

    private List<ProjectDescriptor> replaceDescriptor(UserWorkspaceDeploymentProject project, String projectName,
            UserWorkspaceProjectDescriptorImpl newItem) {
        List<ProjectDescriptor> newDescriptors = new ArrayList<ProjectDescriptor>();

        for (ProjectDescriptor pd : project.getProjectDescriptors()) {
            if (pd.getProjectName().equals(projectName)) {
                if (newItem != null) {
                    newDescriptors.add(newItem);
                    newItem = null;
                }
            } else {
                newDescriptors.add(pd);
            }
        }
        if (newItem != null) {
            newDescriptors.add(newItem);
        }
        items = null;
        return newDescriptors;
    }

    public String deleteItem() {
        String projectName = FacesUtils.getRequestParameter("key");
        UserWorkspaceDeploymentProject project = getSelectedProject();

        try {
            project.setProjectDescriptors(replaceDescriptor(project, projectName, null));
        } catch (ProjectException e) {
            log.error(e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "failed to add project descriptor", e.getMessage()));
        }
        return null;
    }

    private UserWorkspaceDeploymentProject getSelectedProject() {
        ProjectArtefact artefact = repositoryTreeState.getSelectedNode().getDataBean();
        if (artefact instanceof UserWorkspaceDeploymentProject) {
            return (UserWorkspaceDeploymentProject) artefact;
        }
        return null;
    }

    public SelectItem[] getProjects() {
        UserWorkspace workspace = RepositoryUtils.getWorkspace();
        Collection<UserWorkspaceProject> workspaceProjects = workspace.getProjects();
        List<SelectItem> selectItems = new ArrayList<SelectItem>();

        List<DeploymentDescriptorItem> existingItems = getItems();
        Set<String> existing = new HashSet<String>();
        if (existingItems != null) {
            for (DeploymentDescriptorItem ddItem : existingItems) {
                existing.add(ddItem.getName());
            }
        }

        for (UserWorkspaceProject project : workspaceProjects) {
            if (!(project.isDeploymentProject() || existing.contains(project.getName()) || project.isLocalOnly())) {
                selectItems.add(new SelectItem(project.getName()));
            }
        }

        return selectItems.toArray(new SelectItem[selectItems.size()]);
    }

    public SelectItem[] getProjectVersions() {
        UserWorkspace workspace = RepositoryUtils.getWorkspace();
        if (projectName != null) {
            try {
                UserWorkspaceProject project = workspace.getProject(projectName);
                // sort project versions in descending order (1.1 -> 0.0)
                List<ProjectVersion> versions = new ArrayList<ProjectVersion>(project.getVersions());
                Collections.sort(versions, RepositoryUtils.VERSIONS_REVERSE_COMPARATOR);

                List<SelectItem> selectItems = new ArrayList<SelectItem>();
                for (ProjectVersion version : versions) {
                    selectItems.add(new SelectItem(version.getVersionName()));
                }
                return selectItems.toArray(new SelectItem[selectItems.size()]);
            } catch (ProjectException e) {
                log.error(e);
            }
        }
        return new SelectItem[0];
    }

    public String openSelectedProjects() {
        UserWorkspace workspace = RepositoryUtils.getWorkspace();
        for (DeploymentDescriptorItem item : items) {
            if (item.isSelected()) {
                String projectName = item.getName();
                try {
                    UserWorkspaceProject project = workspace.getProject(projectName);
                    if (!project.isOpened()) {
                        project.open();
                    }
                } catch (ProjectException e) {
                    log.error("Failed to open project " + projectName + " " + e.getMessage());
                }
            }
            item.setSelected(false);
        }
        return null;
    }

    public String deploy() {
        UserWorkspaceDeploymentProject project = getSelectedProject();
        if (project != null) {
            try {
                deploymentManager.deploy(project);
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Project '" + project.getName()
                                + "' successfully deployed with id: " + RepositoryUtils.getDeployID(project).getName(),
                                null));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to deploy '" + project.getName() + "'", e
                                .getMessage()));
                log.error(e);
            }
        }
        return null;
    }

    private void checkConflicts(List<DeploymentDescriptorItem> items) throws ProjectException {
        if (items == null) {
            return;
        }

        DependencyChecker checker = new DependencyChecker();
        UserWorkspaceDeploymentProject project = getSelectedProject();
        checker.addProjects(project);
        checker.check(items);
    }

    public String checkIn() {
        try {
            getSelectedProject().checkIn();
            items = null;
        } catch (ProjectException e) {
            log.error(e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "failed to check in", e.getMessage()));
        }

        return null;
    }

    public String close() {
        try {
            getSelectedProject().close();
            items = null;
        } catch (ProjectException e) {
            log.error(e);
            FacesContext.getCurrentInstance()
                    .addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "failed to close deployment project", e
                                    .getMessage()));
        }

        return null;
    }

    public String checkOut() {
        try {
            getSelectedProject().checkOut();
            items = null;
        } catch (ProjectException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "failed to check out", e.getMessage()));
            log.error(e);
        }

        return null;
    }

    public boolean isCheckinable() {
        return true;
    }

    public boolean isCheckoutable() {
        return true;
    }

    public void setRepositoryTreeState(RepositoryTreeState repositoryTreeState) {
        this.repositoryTreeState = repositoryTreeState;
    }
}
