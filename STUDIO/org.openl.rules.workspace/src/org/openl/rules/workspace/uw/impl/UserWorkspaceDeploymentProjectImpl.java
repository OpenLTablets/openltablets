package org.openl.rules.workspace.uw.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import org.openl.rules.repository.CommonVersion;
import org.openl.rules.workspace.WorkspaceUser;
import org.openl.rules.workspace.abstracts.ArtefactPath;
import org.openl.rules.workspace.abstracts.ProjectArtefact;
import org.openl.rules.workspace.abstracts.ProjectDependency;
import org.openl.rules.workspace.abstracts.ProjectDescriptor;
import org.openl.rules.workspace.abstracts.ProjectException;
import org.openl.rules.workspace.abstracts.ProjectResource;
import org.openl.rules.workspace.abstracts.ProjectVersion;
import org.openl.rules.workspace.abstracts.impl.ArtefactPathImpl;
import org.openl.rules.workspace.dtr.RepositoryDDProject;
import org.openl.rules.workspace.props.Property;
import org.openl.rules.workspace.props.PropertyException;
import org.openl.rules.workspace.uw.UserWorkspaceDeploymentProject;
import org.openl.rules.workspace.uw.UserWorkspaceProjectArtefact;
import org.openl.rules.workspace.uw.UserWorkspaceProjectFolder;
import org.openl.rules.workspace.uw.UserWorkspaceProjectResource;

public class UserWorkspaceDeploymentProjectImpl implements UserWorkspaceDeploymentProject {
    private UserWorkspaceImpl userWorkspace;
    private RepositoryDDProject dtrDProject;

    private String name;
    private ArtefactPath path;
    private HashMap<String, ProjectDescriptor> descriptors;

    protected UserWorkspaceDeploymentProjectImpl(UserWorkspaceImpl userWorkspace, RepositoryDDProject dtrDProject) {
        this.userWorkspace = userWorkspace;
        this.dtrDProject = dtrDProject;

        name = dtrDProject.getName();
        path = new ArtefactPathImpl(new String[]{name});

        descriptors = new HashMap<String, ProjectDescriptor>();
        updateDescriptors(dtrDProject.getProjectDescriptors());
    }

    public ProjectDescriptor addProjectDescriptor(String name, CommonVersion version) throws ProjectException {
        UserWorkspaceProjectDescriptorImpl uwpd = new UserWorkspaceProjectDescriptorImpl(this, name, version);
        descriptors.put(name, uwpd);

        return uwpd;
    }

    public ProjectDescriptor getProjectDescriptor(String name) throws ProjectException {
        ProjectDescriptor pd = descriptors.get(name);

        if (pd == null) {
            throw new ProjectException("Cannot find descriptor for project {0} in {1}", null, name, getName());
        }

        return pd;
    }

    public Collection<ProjectDescriptor> getProjectDescriptors() {
        return descriptors.values();
    }

    public void setProjectDescriptors(Collection<ProjectDescriptor> projectDescriptors) throws ProjectException {
        if (isReadOnly()) {
            throw new ProjectException("Cannot change deployment descriptor in read only mode");
        }
        updateDescriptors(projectDescriptors);
    }

    public void checkIn(int major, int minor) throws ProjectException {
        if (!isCheckedOut()) {
            throw new ProjectException("Project ''{0}'' must be checked-out before checking-in", null, getName());
        }

        if (major != 0 || minor != 0) {
            dtrDProject.riseVersion(major, minor);
        }

        dtrDProject.commit(this, userWorkspace.getUser());
        dtrDProject.unlock(userWorkspace.getUser());
        updateDescriptors(dtrDProject.getProjectDescriptors());
    }

    public void checkIn() throws ProjectException {
        // do not rise version
        checkIn(0, 0);
    }

    public void checkOut() throws ProjectException {
        if (isCheckedOut()) {
            throw new ProjectException("Project ''{0}'' is already checked-out", null, getName());
        }

        if (isLocked()) {
            throw new ProjectException("Project ''{0}'' is locked by ''{1}'' since ''{2}''", null, getName(),
                    dtrDProject.getlLockInfo().getLockedBy().getUserName(), dtrDProject.getlLockInfo().getLockedAt());
        }

        if (isOpened()) {
            close();
        }

        dtrDProject.lock(userWorkspace.getUser());
        updateDescriptors(dtrDProject.getProjectDescriptors());
    }

    public void close() throws ProjectException {
        dtrDProject.unlock(userWorkspace.getUser());
        updateDescriptors(dtrDProject.getProjectDescriptors());
    }

    public Collection<ProjectVersion> getVersions() {
        return dtrDProject.getVersions();
    }

    public boolean isCheckedOut() {
        if (dtrDProject.isLocked()) {
            WorkspaceUser lockedBy = dtrDProject.getlLockInfo().getLockedBy();

            if (lockedBy.equals(userWorkspace.getUser())) {
                return true;
            }
        }

        return false;
    }

    public boolean isReadOnly() {
        return !isCheckedOut();
    }

    public boolean isDeleted() {
        return dtrDProject.isMarkedForDeletion();
    }

    public boolean isDeploymentProject() {
        return true;
    }

    public void undelete() throws ProjectException {
        dtrDProject.undelete(userWorkspace.getUser());
    }

    public void erase() throws ProjectException {
        dtrDProject.erase(userWorkspace.getUser());
    }

    public boolean isLocalOnly() {
        return false;
    }

    public boolean isLocked() {
        return dtrDProject.isLocked();
    }

    public boolean isLockedByMe() {
        if (!isLocked()) return false;

        WorkspaceUser lockedBy = dtrDProject.getlLockInfo().getLockedBy();
        return lockedBy.equals(userWorkspace.getUser());
    }

    public boolean isOpened() {
        return isCheckedOut();
    }

    public boolean isRulesProject() {
        return false;
    }

    public void open() throws ProjectException {
        // TODO open latest version
    }

    public void openVersion(CommonVersion version) throws ProjectException {
        // TODO open specified version
    }

    public void delete() throws ProjectException {
        if (isLocked() && !isLockedByMe()) {
            throw new ProjectException("Cannot delete project ''{0}'' while it is locked by other user", null, getName());
        }

        if (isOpened()) {
            close();
        }

        dtrDProject.delete(userWorkspace.getUser());
    }

    public ProjectVersion getVersion() {
        return dtrDProject.getVersion();
    }

    public ArtefactPath getArtefactPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public ProjectArtefact getArtefactByPath(ArtefactPath artefactPath) throws ProjectException {
        notSupported();
        return null;
    }

    public Collection<ProjectDependency> getDependencies() {
        // not supported
        return new LinkedList<ProjectDependency>();
    }

    public void setDependencies(Collection<ProjectDependency> dependencies) {
        // not supported
    }

    public Collection<? extends UserWorkspaceProjectArtefact> getArtefacts() {
        // not supported
        return new LinkedList<UserWorkspaceProjectArtefact>();
    }

    public UserWorkspaceProjectArtefact getArtefact(String name) throws ProjectException {
        notSupported();
        return null;
    }

    public boolean hasArtefact(String name) {
        return false;
    }

    public boolean isFolder() {
        return false;
    }

    public void addProperty(Property property) throws PropertyException {
        notSupportedProps();
    }

    public Collection<Property> getProperties() {
        // not supported
        return null;
    }

    public Property getProperty(String name) throws PropertyException {
        notSupportedProps();
        return null;
    }

    public boolean hasProperty(String name) {
        return false;
    }

    public Property removeProperty(String name) throws PropertyException {
        notSupportedProps();
        return null;
    }

    public UserWorkspaceProjectFolder addFolder(String name) throws ProjectException {
        notSupported();
        return null;
    }

    public UserWorkspaceProjectResource addResource(String name, ProjectResource resource) throws ProjectException {
        notSupported();
        return null;
    }

    public Date getEffectiveDate() {
        // not supported
        return null;
    }

    public Date getExpirationDate() {
        // not supported
        return null;
    }

    public String getLineOfBusiness() {
        // not supported
        return null;
    }

    public void setEffectiveDate(Date date) throws ProjectException {
        notSupported();
    }

    public void setExpirationDate(Date date) throws ProjectException {
        notSupported();
    }

    public void setLineOfBusiness(String lineOfBusiness) throws ProjectException {
        notSupported();
    }



    protected void notSupported() throws ProjectException {
        throw new ProjectException("Not supported for deployment project");
    }

    protected void notSupportedProps() throws PropertyException {
        throw new PropertyException("Not supported for deployment project", null);
    }

    protected void removeProjectDescriptor(UserWorkspaceProjectDescriptorImpl pd) {
        descriptors.remove(pd.getProjectName());
    }

    protected void updateDescriptors(Collection<ProjectDescriptor> projectDescriptors) {
        HashMap<String, ProjectDescriptor> descrs = new HashMap<String, ProjectDescriptor>();

        for (ProjectDescriptor pd : projectDescriptors) {
            String name = pd.getProjectName();
            UserWorkspaceProjectDescriptorImpl uwpd = new UserWorkspaceProjectDescriptorImpl(this, name, pd.getProjectVersion());
            descrs.put(name, uwpd);
        }

        descriptors.clear();
        descriptors = descrs;
    }

    public UserWorkspaceDeploymentProjectImpl getProject() {
        return this;
    }
}
