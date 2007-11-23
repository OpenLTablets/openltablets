package org.openl.rules.ui.repository.tree;

import org.openl.rules.ui.repository.dependency.DependencyBean;
import org.openl.rules.ui.repository.UiConst;
import org.openl.rules.workspace.abstracts.Project;
import org.openl.rules.workspace.abstracts.ProjectVersion;
import org.openl.rules.workspace.abstracts.ProjectDependency;
import org.openl.rules.workspace.abstracts.VersionInfo;
import org.openl.rules.workspace.abstracts.ProjectException;
import org.openl.rules.workspace.uw.UserWorkspaceProject;

import java.util.Date;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Represents OpenL project in a tree.  
 * 
 * @author Aleh Bykhavets
 *
 */
public class TreeProject extends TreeFolder {

    private static final long serialVersionUID = -326805891782640894L;

    private List<DependencyBean> dependencies;

    public TreeProject(String id, String name) {
        super(id, name);
    }

    // ------ UI methods ------

    @Override
    public String getType() {
        return UiConst.TYPE_PROJECT;
    }

    @Override
    public String getIcon() {
        UserWorkspaceProject project = (UserWorkspaceProject) getDataBean();

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


    @Override
    public synchronized List<DependencyBean> getDependencies() {
        if (dependencies == null) {
            Collection<ProjectDependency> deps = getProject().getDependencies();
            dependencies = new ArrayList<DependencyBean>(deps.size());
            for (ProjectDependency pd : deps) {
                DependencyBean depBean = new DependencyBean();
                depBean.setProjectName(pd.getProjectName());
                depBean.setLowerVersion(pd.getLowerLimit().getVersionName());
                if (pd.hasUpperLimit()) {
                    depBean.setUpperVersion(pd.getUpperLimit().getVersionName());
                }
                dependencies.add(depBean);
            }
        }

        return dependencies;
    }

    private Project getProject() {
        return (Project) getDataBean();
    }

    public synchronized boolean addDependency(ProjectDependency dep) throws ProjectException {
        Collection<ProjectDependency> dependencies = getProject().getDependencies();
        if (dependencies.contains(dep)) {
            return false;
        }

        Collection<ProjectDependency> newDeps = new ArrayList<ProjectDependency>(dependencies);
        newDeps.add(dep);
        ((Project) getDataBean()).setDependencies(newDeps);
        this.dependencies = null;
        return true;
    }

    public synchronized void removeDependency(String dependency) throws ProjectException {
        Collection<ProjectDependency> dependencies = getProject().getDependencies();
        Collection<ProjectDependency> newDeps = new ArrayList<ProjectDependency>();
        boolean changed = false;
        for (ProjectDependency d : dependencies) {
            if (d.getProjectName().equals(dependency)) {
                changed = true;
            } else {
                newDeps.add(d);
            }
        }
        if (changed) {
            this.dependencies = null;
            getProject().setDependencies(newDeps);
        }
    }
}
