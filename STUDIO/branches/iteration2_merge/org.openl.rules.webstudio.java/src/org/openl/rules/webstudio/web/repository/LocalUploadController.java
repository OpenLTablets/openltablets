package org.openl.rules.webstudio.web.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.rules.ui.WebStudio;
import org.openl.rules.webstudio.web.servlet.RulesUserSession;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.rules.workspace.WorkspaceException;
import org.openl.rules.workspace.dtr.DesignTimeRepository;
import org.openl.rules.workspace.abstracts.ProjectException;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class LocalUploadController {
    private final static Log log = LogFactory.getLog(LocalUploadController.class);

    private List<UploadBean> uploadBeans;

    public List<UploadBean> getProjects4Upload() {
        if (uploadBeans == null) {
            uploadBeans = new ArrayList<UploadBean>();
            RulesUserSession userRules = getRules();
            WebStudio webStudio = WebStudioUtils.getWebStudio();
            if (webStudio != null && userRules != null) {
                DesignTimeRepository dtr;
                try {
                    dtr = userRules.getUserWorkspace().getDesignTimeRepository();
                } catch (Exception e) {
                    return null;
                }

                List<File> projects = webStudio.getLocator().listOpenLFolders();
                for (File f : projects)
                    try {
                        if (!dtr.hasProject(f.getName()))
                            uploadBeans.add(new UploadBean(f.getName()));
                    } catch (Exception e) {
                        log.error("Failed to list projects for upload!", e);
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
                    }
            }
        }
        return uploadBeans;
    }

    private RulesUserSession getRules() {
        HttpSession session = ((HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest()).getSession(false);

        return WebStudioUtils.getRulesUserSession(session);
    }

    public String upload() {
        String workspacePath = WebStudioUtils.getWebStudio().getWorkspacePath();
        RulesUserSession rulesUserSession = getRules();

        List<UploadBean> beans = uploadBeans;
        uploadBeans = null; // force re-read.

        if (beans != null) {
            for (UploadBean bean : beans)
                if (bean.isSelected()) {
                    try {
                        createProject(new File(workspacePath, bean.getProjectName()), rulesUserSession);
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                                FacesMessage.SEVERITY_INFO, "project " + bean.getProjectName() +
                                                            " was uploaded succesfully", null
                        ));
                    } catch (Exception e) {
                        log.error("Failed to upload local project!", e);
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                                "could not upload project: " + bean.getProjectName(), e.getMessage()
                        ));
                    }
                }
        }

        return null;
    }

    private void createProject(File baseFolder, RulesUserSession rulesUserSession)
            throws ProjectException, WorkspaceException, FileNotFoundException
    {
        if (!baseFolder.isDirectory())
            throw new FileNotFoundException(baseFolder.getName());

        rulesUserSession.getUserWorkspace().uploadLocalProject(baseFolder.getName());

    }

    public static class UploadBean {
        public UploadBean(String projectName) {
            this.projectName = projectName;
        }

        public UploadBean() {}

        private String projectName;
        private boolean selected;

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }
}
