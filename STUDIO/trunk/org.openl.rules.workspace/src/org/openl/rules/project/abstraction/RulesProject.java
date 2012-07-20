package org.openl.rules.project.abstraction;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.openl.rules.common.ArtefactPath;
import org.openl.rules.common.CommonUser;
import org.openl.rules.common.CommonVersion;
import org.openl.rules.common.LockInfo;
import org.openl.rules.common.ProjectException;
import org.openl.rules.common.ProjectVersion;
import org.openl.rules.project.impl.local.LocalFolderAPI;
import org.openl.rules.repository.api.ArtefactAPI;
import org.openl.rules.repository.api.FolderAPI;
import org.openl.rules.workspace.uw.UserWorkspace;

public class RulesProject extends UserWorkspaceProject {
    private LocalFolderAPI local;
    private FolderAPI repository;
    private UserWorkspace userWorkspace;

    public RulesProject(LocalFolderAPI local, FolderAPI repository, UserWorkspace userWorkspace) {
        super(local != null ? local : repository, userWorkspace.getUser());
        this.local = local;
        this.repository = repository;
        this.userWorkspace = userWorkspace;
    }
    
    public FolderAPI getRepositoryAPI() {
        return repository;
    }

    public LocalFolderAPI getLocalAPI() {
        return local;
    }

    public void edit(CommonUser user) throws ProjectException {
        super.edit(user);
        open();
    }

    public void save(CommonUser user, int major, int minor) throws ProjectException {
        smartUpdate(local, repository, user, major, minor);
        local.setCurrentVersion(repository.getVersion());
        local.commit(user, 0, 0, 0);// save persistence
        unlock(user);
        refresh();
    }

    @Override
    public void delete(CommonUser user) throws ProjectException {
        if (isLocalOnly()) {
            erase(user);
        } else {
            if (isOpened()) {
                close();
            }
            super.delete(user);
        }
    }

    public void close(CommonUser user) throws ProjectException {
        if (local != null) {
            local.delete(user);
        }
        if (isLockedByUser(user)) {
            unlock(user);
        }
        if (!isLocalOnly()) {
            setAPI(repository);
        }
        refresh();
    }

    public LockInfo getLockInfo() {
        if (repository != null) {
            return repository.getLockInfo();
        } else {
            return local.getLockInfo();
        }
    }

    @Override
    public void lock(CommonUser user) throws ProjectException {
        repository.lock(user);
    }

    @Override
    public void unlock(CommonUser user) throws ProjectException {
        repository.unlock(user);
    }

    public ProjectVersion getVersion() {
        // TODO ???
        if (isOpened()) {
            return local.getVersion();
        } else {
            return repository.getVersion();
        }
    }

    public List<ProjectVersion> getVersions() {
        if (repository != null) {
            return repository.getVersions();
        } else {
            return local.getVersions();
        }
    }

    public List<ProjectVersion> getVersionsForArtefact(ArtefactPath artefactPath) {
        ArtefactAPI artefact = repository;
        try {
            for (String pathElement : artefactPath.getSegments()) {
                artefact = ((FolderAPI) artefact).getArtefact(pathElement);
            }
            return artefact.getVersions();
        } catch (Exception e) {
            return new LinkedList<ProjectVersion>();
        }
    }

    public boolean isLocalOnly() {
        return repository == null;
    }

    public boolean isRepositoryOnly() {
        return local == null;
    }

    public boolean isOpened() {
        return getAPI() == local;
    }

    
    
    
    public void openVersion(CommonVersion version) throws ProjectException {
        FolderAPI openedProject = repository.getVersion(version);
        File source;
        if (local == null) {
            ArtefactPath path = repository.getArtefactPath();
            source = new File(userWorkspace.getLocalWorkspace().getLocation(), path.segment(path.segmentCount() - 1));
            local = new LocalFolderAPI(source, path, userWorkspace.getLocalWorkspace());
        } else {
            source = local.getSource();
        }
        source.mkdir();
        local.setCurrentVersion(openedProject.getVersion());
        update(openedProject, local, getUser(), version.getMajor(), version.getMinor());
        setAPI(local);
        refresh();
    }

    // FIXME
    private void update(FolderAPI from, FolderAPI to, CommonUser user, int major, int minor) throws ProjectException {
        new AProject(to).update(new AProject(from), user, major, minor);
    }
    
    private void smartUpdate(FolderAPI from, FolderAPI to, CommonUser user, int major, int minor) throws ProjectException {
        new AProject(to).smartUpdate(new AProject(from), user, major, minor);
    }

    // Is Opened for Editing by me? -- in LW + locked by me
    public boolean isOpenedForEditing() {
        if (isLocalOnly()) {
            return false;
        }
        return isLockedByMe() && local != null;
    }
}
