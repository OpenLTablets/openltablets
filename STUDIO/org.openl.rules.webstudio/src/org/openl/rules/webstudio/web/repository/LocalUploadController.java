package org.openl.rules.webstudio.web.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Service;

import org.openl.rules.common.ProjectException;
import org.openl.rules.project.abstraction.AProject;
import org.openl.rules.project.abstraction.Comments;
import org.openl.rules.project.abstraction.RulesProject;
import org.openl.rules.project.abstraction.UserWorkspaceProject;
import org.openl.rules.project.resolving.ProjectResolver;
import org.openl.rules.project.resolving.ResolvingStrategy;
import org.openl.rules.repository.api.FileData;
import org.openl.rules.repository.api.Repository;
import org.openl.rules.ui.WebStudio;
import org.openl.rules.webstudio.util.NameChecker;
import org.openl.rules.webstudio.web.admin.ProjectTagsBean;
import org.openl.rules.webstudio.web.jsf.annotation.ViewScope;
import org.openl.rules.webstudio.web.repository.event.ProjectDeletedEvent;
import org.openl.rules.webstudio.web.servlet.RulesUserSession;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.rules.workspace.dtr.DesignTimeRepository;
import org.openl.rules.workspace.dtr.impl.FileMappingData;
import org.openl.rules.workspace.lw.LocalWorkspace;
import org.openl.rules.workspace.uw.UserWorkspace;
import org.openl.util.StringUtils;

@Service("localUpload")
@ViewScope
public class LocalUploadController {
    public static class UploadBean {
        private final String projectName;

        private boolean selected;

        UploadBean(String projectName) {
            this.projectName = projectName;
        }

        public String getProjectName() {
            return projectName;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    private final Logger log = LoggerFactory.getLogger(LocalUploadController.class);

    private List<UploadBean> uploadBeans;

    private String repositoryId;

    private String projectFolder = "";

    private String createProjectCommentTemplate;

    private final PropertyResolver propertyResolver;

    private final ProjectTagsBean projectTagsBean;

    private static final String NONE_REPO = "none";

    public LocalUploadController(PropertyResolver propertyResolver, ProjectTagsBean projectTagsBean) {
        this.propertyResolver = propertyResolver;
        this.projectTagsBean = projectTagsBean;
    }

    private RulesProject createProject(File baseFolder,
                                       RulesUserSession rulesUserSession,
                                       String comment,
                                       String repositoryId) throws ProjectException, FileNotFoundException {
        if (!baseFolder.isDirectory()) {
            throw new FileNotFoundException(baseFolder.getName());
        }

        return rulesUserSession.getUserWorkspace().uploadLocalProject(repositoryId, baseFolder.getName(), projectFolder, comment);
    }

    public List<UploadBean> getProjects4Upload() {
        if (uploadBeans == null) {
            uploadBeans = new ArrayList<>();
            RulesUserSession userRules = WebStudioUtils.getRulesUserSession();
            WebStudio webStudio = WebStudioUtils.getWebStudio();
            if (webStudio != null && userRules != null) {
                UserWorkspace userWorkspace;
                DesignTimeRepository dtr;
                try {
                    userWorkspace = userRules.getUserWorkspace();
                    dtr = userWorkspace.getDesignTimeRepository();
                } catch (Exception e) {
                    log.error("Cannot get DTR.", e);
                    return null;
                }
                ProjectResolver projectResolver = webStudio.getProjectResolver();

                LocalWorkspace localWorkspace = userWorkspace.getLocalWorkspace();
                List<AProject> localProjects = new ArrayList<>(localWorkspace.getProjects());
                localProjects.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
                for (AProject project : localProjects) {
                    try {
                        File projectFolder = new File(localWorkspace.getLocation(), project.getFolderPath());

                        ResolvingStrategy strategy = projectResolver.isRulesProject(projectFolder);
                        if (strategy != null && !hasCorrespondingDesignProject(dtr, project)) {
                            uploadBeans.add(new UploadBean(project.getName()));
                        }
                    } catch (Exception e) {
                        log.error("Failed to list projects for upload.", e);
                        WebStudioUtils.addErrorMessage(e.getMessage());
                    }
                }
            }
        }
        return uploadBeans;
    }

    private boolean hasCorrespondingDesignProject(DesignTimeRepository dtr, AProject localProject) throws IOException {
        String repoId = localProject.getRepository().getId();
        Repository repository = dtr.getRepository(repoId);
        if (repository != null && repository.supports().mappedFolders()) {
            FileData fileData = localProject.getFileData();
            FileMappingData mappingData = fileData.getAdditionalData(FileMappingData.class);
            if (mappingData != null) {
                String realPath = mappingData.getInternalPath();
                return dtr.getProjectByPath(repoId, null, realPath, null) != null;
            } else {
                return false;
            }
        } else {
            return dtr.hasProject(repoId, localProject.getName());
        }
    }

    public String getRepositoryId() {
        return StringUtils.isBlank(repositoryId) ? NONE_REPO : repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = NONE_REPO.equals(repositoryId) ? null : repositoryId;
    }

    public String getProjectFolder() {
        String folderToShow = this.projectFolder;
        if (!folderToShow.startsWith("/")) {
            folderToShow = "/" + folderToShow;
        }
        return folderToShow;
    }

    public void setProjectFolder(String projectFolder) {
        String folder = StringUtils.trimToEmpty(projectFolder).replace('\\', '/');
        if (folder.startsWith("/")) {
            folder = folder.substring(1);
        }
        if (!folder.isEmpty() && !folder.endsWith("/")) {
            folder += '/';
        }
        this.projectFolder = folder;
    }

    /**
     * EPBDS-8384: JSF beans discovery does not work if the bean contains static field with lambda expression. Possibly
     * need to upgrade JSF version to fully support java 8. Until then use anonymous class instead.
     */
    private static final Comparator<File> fileNameComparator = Comparator.comparing(File::getName,
            String.CASE_INSENSITIVE_ORDER);

    public String upload() {
        if (StringUtils.isBlank(repositoryId)) {
            WebStudioUtils.addErrorMessage("Repository must be selected.");
            return null;
        }

        String workspacePath = WebStudioUtils.getWebStudio().getWorkspacePath();
        RulesUserSession rulesUserSession = WebStudioUtils.getRulesUserSession();

        List<UploadBean> beans = uploadBeans;
        uploadBeans = null; // force re-read.

        if (beans != null) {
            for (UploadBean bean : beans) {
                if (bean.isSelected()) {
                    try {
                        String comment = getDesignRepoComments().createProject(createProjectCommentTemplate,
                                bean.getProjectName());

                        UserWorkspace userWorkspace = WebStudioUtils.getRulesUserSession().getUserWorkspace();
                        if (userWorkspace.getDesignTimeRepository().hasProject(repositoryId, bean.getProjectName())) {
                            WebStudioUtils.addErrorMessage(
                                    "Cannot create project because project with such name already exists.");
                            return null;
                        }

                        RulesProject createdProject = createProject(new File(workspacePath, bean.getProjectName()), rulesUserSession, comment,
                                repositoryId);

                        projectTagsBean.saveTags(createdProject);

                        WebStudioUtils.addInfoMessage("Project " + bean.getProjectName() + " was created successfully");
                    } catch (Exception e) {
                        String msg;
                        if (!NameChecker.checkName(bean.getProjectName())) {
                            msg = "Failed to create the project '" + bean
                                    .getProjectName() + "'! " + NameChecker.BAD_PROJECT_NAME_MSG;
                        } else if (e.getCause() instanceof FileNotFoundException) {
                            if (e.getMessage().contains(".xls")) {
                                msg = "Failed to create the project. Close the module Excel file and try again.";
                            } else {
                                msg = "Failed to create the project because some resources are used";
                            }
                        } else {
                            msg = "Failed to create the project '" + bean.getProjectName() + "'.";
                            log.error(msg, e);
                        }
                        WebStudioUtils.addErrorMessage(msg);

                    }
                }
            }
        }

        return null;
    }

    @EventListener
    public void projectDeletedEventListener(ProjectDeletedEvent event) {
        var deletedProject = event.getProject();
        if (deletedProject instanceof UserWorkspaceProject && ((UserWorkspaceProject) deletedProject).isLocalOnly()) {
            // force reload
            uploadBeans = null;
        }
    }

    public String getCreateProjectCommentTemplate() {
        if (createProjectCommentTemplate == null) {
            return getDesignRepoComments().getCreateProjectTemplate();
        }
        return createProjectCommentTemplate;
    }

    public void setCreateProjectCommentTemplate(String createProjectComment) {
        this.createProjectCommentTemplate = createProjectComment;
    }

    public boolean isSelectAll() {
        return false;
    }

    public void setSelectAll(boolean selectAll) {
    }

    public boolean isSupportsMappedFolders() {
        if (StringUtils.isBlank(repositoryId)) {
            return false;
        }

        try {
            UserWorkspace userWorkspace = WebStudioUtils.getRulesUserSession().getUserWorkspace();
            return userWorkspace.getDesignTimeRepository().getRepository(repositoryId).supports().mappedFolders();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private Comments getDesignRepoComments() {
        return repositoryId == null ? new Comments(propertyResolver, Comments.DESIGN_CONFIG_REPO_ID)
                : new Comments(propertyResolver, repositoryId);
    }
}
