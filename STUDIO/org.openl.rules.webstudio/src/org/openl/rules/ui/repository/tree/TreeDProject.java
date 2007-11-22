package org.openl.rules.ui.repository.tree;

import java.util.Date;

import org.openl.rules.ui.repository.UiConst;
import org.openl.rules.workspace.abstracts.Project;
import org.openl.rules.workspace.abstracts.ProjectVersion;
import org.openl.rules.workspace.abstracts.VersionInfo;
import org.openl.rules.workspace.uw.UserWorkspaceDeploymentProject;

public class TreeDProject extends TreeFile {
    private static final long serialVersionUID = -1058464776132912419L;

    public TreeDProject(long id, String name) {
        super(id, name);
    }

    // ------ UI methods ------

    @Override
    public String getType() {
        return UiConst.TYPE_DEPLOYMENT_PROJECT;
    }

    @Override
    public String getIconLeaf() {
        UserWorkspaceDeploymentProject project = (UserWorkspaceDeploymentProject) getDataBean();

        if (project.isLocalOnly()) {
            return UiConst.ICON_PROJECT_LOCAL;
        }
        
        if (project.isDeleted()) {
            return UiConst.ICON_PROJECT_DELETED;
        }
        
        if (project.isCheckedOut()) {
            return UiConst.ICON_PROJECT_CHECKED_OUT;
        }
        
        boolean isLocked = project.isLocked();
        if (project.isOpened()) {
            if (isLocked) {
                return UiConst.ICON_PROJECT_OPENED_LOCKED;
            } else {
                return UiConst.ICON_PROJECT_OPENED;
            }
        } else {
            if (isLocked) {
                return UiConst.ICON_PROJECT_CLOSED_LOCKED;
            } else {
                return UiConst.ICON_PROJECT_CLOSED;
            }
        }
    }

    public Date getCreatedAt() {
        ProjectVersion projectVersion = getProject().getVersion();
        if (projectVersion == null) return null;
        
        VersionInfo vi = projectVersion.getVersionInfo();
        return (vi != null) ? vi.getCreatedAt() : null;
    }

    public String getCreatedBy() {
        ProjectVersion projectVersion = (getProject()).getVersion();
        if (projectVersion == null) return null;

        VersionInfo vi = projectVersion.getVersionInfo();
        return (vi != null) ? vi.getCreatedBy() : null;
    }

    public String getVersion() {
        ProjectVersion projectVersion = (getProject()).getVersion();
        if (projectVersion == null) {
            return "unversioned";
        }
        return projectVersion.getVersionName();
    }

    private Project getProject() {
        return (Project) getDataBean();
    }
}
