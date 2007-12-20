package org.openl.rules.workspace.lw.impl;

import org.openl.rules.workspace.WorkspaceUser;
import org.openl.rules.workspace.abstracts.ArtefactPath;
import org.openl.rules.workspace.abstracts.Project;
import org.openl.rules.workspace.abstracts.ProjectArtefact;
import org.openl.rules.workspace.abstracts.ProjectException;
import org.openl.rules.workspace.abstracts.ProjectVersion;
import org.openl.rules.workspace.abstracts.impl.ArtefactPathImpl;
import org.openl.rules.workspace.lw.LocalProject;
import org.openl.rules.workspace.lw.LocalWorkspace;
import org.openl.rules.workspace.lw.LocalWorkspaceListener;
import org.openl.util.Log;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class LocalWorkspaceImpl implements LocalWorkspace {
    private WorkspaceUser user;
    private File location;
    private Map<String, LocalProject> localProjects;
    private List<LocalWorkspaceListener> listeners = new ArrayList<LocalWorkspaceListener>();

    public LocalWorkspaceImpl(WorkspaceUser user, File location) {
        this.user = user;
        this.location = location;
        
        localProjects = new HashMap<String, LocalProject>();

        loadProjects();
    }

    public Collection<LocalProject> getProjects() {
        return localProjects.values();
    }

    public LocalProject getProject(String name) throws ProjectException {
        LocalProject lp = localProjects.get(name);
        if (lp == null) {
            throw new ProjectException("Cannot find project ''{0}''", null, name);
        }

        return lp;
    }

    public boolean hasProject(String name) {
        return (localProjects.get(name) != null);
    }

    public ProjectArtefact getArtefactByPath(ArtefactPath artefactPath) throws ProjectException {
        String projectName = artefactPath.segment(0);
        LocalProject lp = getProject(projectName);

        ArtefactPath pathInProject = artefactPath.withoutFirstSegment();
        return lp.getArtefactByPath(pathInProject);
    }

    public LocalProject addProject(Project project) throws ProjectException {
        String name = project.getName();

        if (hasProject(name)) {
            // remove it
            getProject(name).remove();
            // TODO smart update (if it is reasonable)
        }

        return downloadProject(project);
    }

    public void removeProject(String name) throws ProjectException {
        getProject(name).remove();
    }

    public void refresh() {
        // check existing
        Iterator<LocalProject> i = localProjects.values().iterator();
        while (i.hasNext()) {
            LocalProjectImpl lp = (LocalProjectImpl)i.next();

            File location = lp.getLocation();
            if (location.exists()) {
                // still here
                lp.refresh();
            } else {
                // deleted externally
                i.remove();
            }
        }

        // check new
        File[] folders = location.listFiles(FolderHelper.getFoldersOnlyFilter());
        if (folders == null) return;

        for (File folder : folders) {
            String name = folder.getName();
            if (!localProjects.containsKey(name)) {
                // new project detected
                ArtefactPath ap = new ArtefactPathImpl(new String[]{name});
                LocalProjectImpl newlyDetected = new LocalProjectImpl(name, ap, folder, this);

                try {
                    newlyDetected.load();
                } catch (ProjectException e) {
                    Log.error("Error loading just detected local project ''{0}''", e, name);
                }

                // add it
                localProjects.put(name, newlyDetected);
            }
        }
    }

    public void saveAll() {
        for (LocalProject lp : localProjects.values()) {
            try {
                lp.save();
            } catch (ProjectException e) {
                Log.error("error saving local project {0}", e, lp.getName());
            }
        }
    }

    public void release() {
        saveAll();
        localProjects.clear();

        for (LocalWorkspaceListener lwl : listeners)
            lwl.workspaceReleased(this);
    }

    public File getLocation() {
        return location;
    }

    public void addWorkspaceListener(LocalWorkspaceListener listener) {
        listeners.add(listener);
    }

    public boolean removeWorkspaceListener(LocalWorkspaceListener listener) {
        return listeners.remove(listener);
    }

// --- protected

    protected void notifyRemoved(LocalProject project) {
        localProjects.remove(project.getName());
    }

    protected WorkspaceUser getUser() {
        return user;
    }

    protected LocalProjectImpl downloadProject(Project project) throws ProjectException {
        String name = project.getName();

        ArtefactPath ap = new ArtefactPathImpl(new String[]{name});
        File f = new File(location, name);

        LocalProjectImpl lpi = new LocalProjectImpl(name, ap, f, this);
        lpi.downloadArtefact(project);
        
        lpi.save();

        // add project
        localProjects.put(name, lpi);
        return lpi;
    }

    protected void loadProjects() {
        File[] folders = location.listFiles(FolderHelper.getFoldersOnlyFilter());
        for (File f : folders) {
            String name = f.getName();
            ArtefactPath ap = new ArtefactPathImpl(new String[]{name});

            LocalProjectImpl lpi = new LocalProjectImpl(name, ap, f, this);
            try {
                lpi.load();
            } catch (ProjectException e) {
                Log.error("Error loading local project {0}", e, lpi.getName());
            }

            localProjects.put(name, lpi);
        }
    }
}
