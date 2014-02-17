package org.openl.rules.webstudio.web.repository;

import static org.openl.rules.security.AccessManager.isGranted;
import static org.openl.rules.security.DefaultPrivileges.PRIVILEGE_CREATE_DEPLOYMENT;
import static org.openl.rules.security.DefaultPrivileges.PRIVILEGE_EDIT_DEPLOYMENT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.commons.web.jsf.FacesUtils;
import org.openl.config.ConfigurationManager;
import org.openl.config.ConfigurationManagerFactory;
import org.openl.rules.common.ProjectDescriptor;
import org.openl.rules.common.ProjectException;
import org.openl.rules.common.impl.CommonVersionImpl;
import org.openl.rules.project.abstraction.ADeploymentProject;
import org.openl.rules.project.abstraction.AProject;
import org.openl.rules.project.abstraction.AProjectArtefact;
import org.openl.rules.project.model.ProjectDependencyDescriptor;
import org.openl.rules.project.resolving.ProjectDescriptorArtefactResolver;
import org.openl.rules.project.resolving.ProjectResolvingException;
import org.openl.rules.webstudio.web.admin.RepositoryConfiguration;
import org.openl.rules.webstudio.web.repository.tree.TreeNode;
import org.openl.rules.workspace.deploy.DeployID;
import org.openl.rules.workspace.dtr.RepositoryException;
import org.openl.rules.workspace.uw.UserWorkspace;

/**
 * 
 * @author Aleh Bykhavets
 * 
 */
@ManagedBean
@ViewScoped
public class SmartRedeployController {

    private final Log log = LogFactory.getLog(SmartRedeployController.class);

    /** A controller which contains pre-built UI object tree. */
    @ManagedProperty(value = "#{repositoryTreeState}")
    private RepositoryTreeState repositoryTreeState;

    @ManagedProperty(value = "#{productionRepositoriesTreeController}")
    private ProductionRepositoriesTreeController productionRepositoriesTreeController;

    @ManagedProperty(value = "#{deploymentManager}")
    private DeploymentManager deploymentManager;

    @ManagedProperty(value = "#{productionRepositoryConfigManagerFactory}")
    private ConfigurationManagerFactory productionConfigManagerFactory;

    @ManagedProperty("#{projectDescriptorArtefactResolver}")
    private volatile ProjectDescriptorArtefactResolver projectDescriptorResolver;

    private List<DeploymentProjectItem> items;

    private String repositoryConfigName;

    private AProject currentProject;

    private boolean loading = true;

    public synchronized List<DeploymentProjectItem> getItems() {
        AProject project = getSelectedProject();
        if (project == null || project != currentProject) {
            reset();
            return null;
        }

        if (items == null) {
            items = getItems4Project(project, getRepositoryConfigName());
        }
        return items;
    }

    public synchronized boolean isProjectHasSelectedItems() {
        List<DeploymentProjectItem> items = getItems();
        if (items == null) {
            return false;
        }

        for (DeploymentProjectItem item : items) {
            if (item.isSelected()) {
                return true;
            }
        }

        return false;
    }

    private List<DeploymentProjectItem> getItems4Project(AProject project, String repositoryConfigName) {
        String projectName = project.getName();
        UserWorkspace workspace = RepositoryUtils.getWorkspace();

        List<DeploymentProjectItem> result = new LinkedList<DeploymentProjectItem>();

        // FIXME take latest deployment projects from DTR not from user scope
        // get all deployment projects
        List<TreeNode> nodes = repositoryTreeState.getDeploymentRepository().getChildNodes();
        for (TreeNode node : nodes) {
            AProjectArtefact artefact = node.getData();
            if (!(artefact instanceof ADeploymentProject)) {
                continue; // should never happen
            }

            ADeploymentProject deploymentProject = (ADeploymentProject) artefact;
            if (deploymentProject.isDeleted()) {
                continue; // don't check marked for deletion projects
            }

            ADeploymentProject latestDeploymentVersion = deploymentProject;
            if (deploymentProject.isOpenedOtherVersion()) {
                try {
                    latestDeploymentVersion = workspace.getDesignTimeRepository()
                        .getDDProject(deploymentProject.getName());
                } catch (RepositoryException e) {
                    log.error("Failed to get latest version for deployment project '" + deploymentProject.getName() + "'",
                        e);
                }
            }

            ProjectDescriptor<?> projectDescriptor = null;

            // check all descriptors
            // we are interested in all Deployment projects that has the project
            @SuppressWarnings("rawtypes")
            Collection<ProjectDescriptor> descriptors = latestDeploymentVersion.getProjectDescriptors();
            for (ProjectDescriptor<?> descr : descriptors) {
                if (projectName.equals(descr.getProjectName())) {
                    projectDescriptor = descr;
                    break;
                }
            }

            if (projectDescriptor == null) {
                continue;
            }

            // create new item
            DeploymentProjectItem item = new DeploymentProjectItem();
            item.setName(deploymentProject.getName());

            DependencyChecker checker = new DependencyChecker(projectDescriptorResolver);
            // check against latest version of the deployment project
            checker.addProjects(latestDeploymentVersion);

            CommonVersionImpl descrVersion = new CommonVersionImpl(projectDescriptor.getProjectVersion());
            int cmp = descrVersion.compareTo(project.getVersion());

            if (cmp == 0) {
                try {
                    if (StringUtils.isEmpty(repositoryConfigName)) {
                        item.setDisabled(true);
                        item.setMessages("Repository is not selected");
                    } else if (deploymentManager.hasDeploymentProject(deploymentProject, repositoryConfigName)) {
                        item.setDisabled(true);
                        item.setMessages("Up to date");
                    } else if (deploymentProject.isOpenedForEditing()) {
                        // prevent loosing of user's changes
                        item.setDisabled(true);
                        item.setMessages("Opened for Editing");
                        item.setStyleForMessages(UiConst.STYLE_WARNING);
                        item.setStyleForName(UiConst.STYLE_WARNING);
                    } else {
                        if (checker.check()) {
                            item.setMessages("Can be deployed");
                        } else {
                            item.setMessages("Dependent projects should be added to deployment configuration!");
                            item.setStyleForMessages(UiConst.STYLE_ERROR);
                            item.setStyleForName(UiConst.STYLE_ERROR);
                            item.setDisabled(true);
                        }
                    }
                } catch (ProjectException e) {
                    item.setDisabled(true);
                    item.setMessages("Cannot connect to repository " + getRepositoryName(repositoryConfigName));
                    item.setStyleForMessages(UiConst.STYLE_ERROR);
                }
            } else if (cmp < 0) {
                if (!isGranted(PRIVILEGE_EDIT_DEPLOYMENT)) {
                    // Don't have permission to edit deployment configuration -
                    // skip it
                    continue;
                }
                if (deploymentProject.isOpenedForEditing()) {
                    // prevent loosing of user's changes
                    item.setDisabled(true);
                    item.setMessages("Opened for Editing");
                    item.setStyleForMessages(UiConst.STYLE_WARNING);
                    item.setStyleForName(UiConst.STYLE_WARNING);
                } else if (deploymentProject.isLocked()) {
                    // won't be able to modify anyway
                    item.setDisabled(true);
                    item.setMessages("Locked by other user");
                    item.setStyleForMessages(UiConst.STYLE_WARNING);
                    item.setStyleForName(UiConst.STYLE_WARNING);
                } else {
                    // overwrite settings
                    checker.addProject(project);
                    if (checker.check()) {
                        item.setMessages("Can be updated to " + project.getVersion().getVersionName() + " from " + descrVersion.getVersionName() + " and then deployed");
                    } else {
                        item.setMessages("Project version will be updated. Dependent projects should be added to deployment configuration!");
                        item.setStyleForMessages(UiConst.STYLE_ERROR);
                        item.setCanDeploy(false);
                    }
                }
            } else {
                item.setDisabled(true);
                item.setMessages("Deployment uses newer version " + descrVersion.getVersionName());
            }

            result.add(item);
        }

        if (!workspace.hasDDProject(projectName) && isGranted(PRIVILEGE_CREATE_DEPLOYMENT)) {
            // there is no deployment project with the same name...
            DeploymentProjectItem item = new DeploymentProjectItem();
            item.setName(projectName);
            try {
                List<ProjectDependencyDescriptor> dependencies = projectDescriptorResolver.getDependencies(project);
                if (dependencies == null || dependencies.isEmpty()) {
                    item.setMessages("Create deploy configuration and deploy");
                } else {
                    item.setMessages("Create deploy configuration. You should add dependent projects to created deployment configuration after that.");
                    item.setStyleForMessages(UiConst.STYLE_ERROR);
                    item.setCanDeploy(false);
                }
            } catch (ProjectException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
                item.setDisabled(true);
                item.setMessages("Internal error while reading the project from repository.");
                item.setStyleForMessages(UiConst.STYLE_ERROR);
            } catch (ProjectResolvingException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
                item.setDisabled(true);
                item.setMessages("Project descriptor is invalid.");
                item.setStyleForMessages(UiConst.STYLE_ERROR);
            }
            item.setStyleForName(UiConst.STYLE_WARNING);

            // place it first
            result.add(0, item);
        }

        return result;
    }

    private AProject getSelectedProject() {
        AProjectArtefact artefact = repositoryTreeState.getSelectedNode().getData();
        if (artefact instanceof AProject) {
            return (AProject) artefact;
        }
        return null;
    }

    public String redeploy() {
        AProject project = getSelectedProject();
        if (project == null) {
            return UiConst.OUTCOME_FAILURE;
        }

        List<ADeploymentProject> toDeploy = new LinkedList<ADeploymentProject>();
        // update selected deployment projects
        List<DeploymentProjectItem> items = getItems();
        for (DeploymentProjectItem item : items) {
            if (!item.isSelected()) {
                continue;
            }

            ADeploymentProject deploymentProject = update(item.getName(), project);
            if (deploymentProject != null && item.isCanDeploy()) {
                // OK, it was updated
                toDeploy.add(deploymentProject);
            }
        }

        // redeploy takes more time
        String repositoryName = getRepositoryName(repositoryConfigName);

        for (ADeploymentProject deploymentProject : toDeploy) {
            try {
                DeployID id = deploymentManager.deploy(deploymentProject, repositoryConfigName);
                String message = String.format("Project '%s' successfully deployed with id '%s' to repository '%s'",
                    project.getName(),
                    id.getName(),
                    repositoryName);
                FacesUtils.addInfoMessage(message);
            } catch (Exception e) {
                String msg = String.format("Failed to deploy '%s' to repository '%s'",
                    project.getName(),
                    repositoryName);
                log.error(msg, e);
                FacesUtils.addErrorMessage(msg, e.getMessage());
            }
        }

        reset();
        productionRepositoriesTreeController.refreshTree();

        return UiConst.OUTCOME_SUCCESS;
    }

    protected String getRepositoryName(String repositoryConfigName) {
        ConfigurationManager productionConfig = productionConfigManagerFactory.getConfigurationManager(repositoryConfigName);
        RepositoryConfiguration repo = new RepositoryConfiguration(repositoryConfigName, productionConfig);
        String repositoryName = repo.getName();
        return repositoryName;
    }

    public void setDeploymentManager(DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    public void setRepositoryTreeState(RepositoryTreeState repositoryTreeState) {
        this.repositoryTreeState = repositoryTreeState;
    }

    public void setProductionConfigManagerFactory(ConfigurationManagerFactory productionConfigManagerFactory) {
        this.productionConfigManagerFactory = productionConfigManagerFactory;
    }

    public void setProjectDescriptorResolver(ProjectDescriptorArtefactResolver projectDescriptorResolver) {
        this.projectDescriptorResolver = projectDescriptorResolver;
    }

    private ADeploymentProject update(String deploymentName, AProject project) {
        UserWorkspace workspace = RepositoryUtils.getWorkspace();
        try {
            if (deploymentName.equals(project.getName())) {
                // the same name
                if (!workspace.hasDDProject(deploymentName)) {
                    // create if absent
                    workspace.createDDProject(deploymentName);
                }
            }

            // get latest version
            // FIXME ADeploymentProject should be renamed to
            // ADeploymentConfiguration, because of the renaming 'Deployment
            // Project' to the 'Deployment configuration'
            ADeploymentProject deploymentConfiguration = workspace.getDDProject(deploymentName);

            boolean sameVersion = deploymentConfiguration.hasProjectDescriptor(project.getName()) && project.getVersion()
                .compareTo(deploymentConfiguration.getProjectDescriptor(project.getName()).getProjectVersion()) == 0;

            if (sameVersion) {
                return deploymentConfiguration;
            } else if (deploymentConfiguration.isLocked()) {
                // someone else is locked it while we were thinking
                FacesUtils.addWarnMessage("Deployment configuration '" + deploymentName + "' is locked by other user");
                return null;
            } else {
                deploymentConfiguration.edit();
                // rewrite project->version
                deploymentConfiguration.addProjectDescriptor(project.getName(), project.getVersion());

                deploymentConfiguration.save();

                FacesUtils.addInfoMessage("Deployment configuration '" + deploymentName + "' successfully updated");
                return deploymentConfiguration;
            }
        } catch (ProjectException e) {
            String msg = "Failed to update deployment configuration '" + deploymentName + "'";
            log.error(msg, e);
            FacesUtils.addErrorMessage(msg);
        }

        return null;
    }

    public String getRepositoryConfigName() {
        if (repositoryConfigName == null) {
            Iterator<RepositoryConfiguration> repos = getRepositories().iterator();
            if (repos.hasNext()) {
                repositoryConfigName = repos.next().getConfigName();
            }
        }

        return repositoryConfigName;
    }

    public void setRepositoryConfigName(String repositoryConfigName) {
        if (repositoryConfigName == null || !repositoryConfigName.equals(this.repositoryConfigName)) {
            this.items = null;
        }
        this.repositoryConfigName = repositoryConfigName;
    }

    public Collection<RepositoryConfiguration> getRepositories() {
        List<RepositoryConfiguration> repos = new ArrayList<RepositoryConfiguration>();
        Collection<String> repositoryConfigNames = deploymentManager.getRepositoryConfigNames();
        for (String configName : repositoryConfigNames) {
            ConfigurationManager productionConfig = productionConfigManagerFactory.getConfigurationManager(configName);
            RepositoryConfiguration config = new RepositoryConfiguration(configName, productionConfig);
            repos.add(config);
        }

        Collections.sort(repos, RepositoryConfiguration.COMPARATOR);

        return repos;
    }

    public boolean isSelectAll4SmartRedeploy() {
        List<DeploymentProjectItem> items = getItems();

        boolean hasSelectedItem = false;

        for (DeploymentProjectItem item : items) {
            if (!item.isDisabled() && !item.isSelected()) {
                return false;
            }
            if (item.isSelected()) {
                hasSelectedItem = true;
            }
        }

        return hasSelectedItem;
    }

    public void setSelectAll4SmartRedeploy(boolean newState) {
        List<DeploymentProjectItem> items = getItems();

        for (DeploymentProjectItem item : items) {
            if (!item.isDisabled()) {
                item.setSelected(newState);
            }
        }
    }

    public void reset() {
        setRepositoryConfigName(null);
        items = null;
        currentProject = null;
        loading = true;
    }

    public void openDialogListener(AjaxBehaviorEvent event) {
        reset();
        currentProject = getSelectedProject();
        loading = false;
    }

    public ProductionRepositoriesTreeController getProductionRepositoriesTreeController() {
        return productionRepositoriesTreeController;
    }

    public void setProductionRepositoriesTreeController(ProductionRepositoriesTreeController productionRepositoriesTreeController) {
        this.productionRepositoriesTreeController = productionRepositoriesTreeController;
    }

    public boolean isLoading() {
        return loading;
    }

}
