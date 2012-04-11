package org.openl.rules.project.impl.local;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openl.rules.common.ArtefactPath;
import org.openl.rules.common.CommonVersion;
import org.openl.rules.common.ProjectException;
import org.openl.rules.repository.api.FolderAPI;
import org.openl.rules.workspace.lw.LocalWorkspace;
import org.openl.rules.workspace.lw.impl.LocalWorkspaceImpl;

public class LocalFolderAPI extends LocalArtefactAPI implements FolderAPI {

    public LocalFolderAPI(File source, ArtefactPath path, LocalWorkspace workspace) {
        super(source, path, workspace);
    }

    private LocalArtefactAPI getArtefactForChild(File child) {
        if (child.isDirectory()) {
            return new LocalFolderAPI(child, path.withSegment(child.getName()), workspace);
        } else {
            return new LocalResourceAPI(child, path.withSegment(child.getName()), workspace);
        }
    }

    public LocalArtefactAPI getArtefact(String name) throws ProjectException {
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals(name)) {
                    return getArtefactForChild(file);
                }
            }
        }
        throw new ProjectException(String.format("Artefact with name \"%s\" is" + " not found", name));
    }

    public boolean hasArtefact(String name) {
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public LocalFolderAPI addFolder(String name) throws ProjectException {
        File newFolder = new File(source, name);
        newFolder.mkdir();
        LocalFolderAPI localFolder = new LocalFolderAPI(newFolder, path.withSegment(name), workspace);
        notifyModified();
        return localFolder;
    }

    public LocalResourceAPI addResource(String name, InputStream content) throws ProjectException {
        File newFile = new File(source, name);
        try {
            newFile.createNewFile();
            LocalResourceAPI newResource = new LocalResourceAPI(newFile, path.withSegment(name), workspace);
            newResource.setContent(content);
            notifyModified();
            return newResource;
        } catch (IOException e) {
            throw new ProjectException("Failed to create resource", e);
        }
    }

    public Collection<? extends LocalArtefactAPI> getArtefacts() {
        List<LocalArtefactAPI> artefacts = new ArrayList<LocalArtefactAPI>();
        File[] files = source.listFiles(((LocalWorkspaceImpl) workspace).getLocalWorkspaceFileFilter());
        if (files != null) {
            for (File file : files) {
                artefacts.add(getArtefactForChild(file));
            }
        }
        return artefacts;
    }
    
    @Override
    public FolderAPI getVersion(CommonVersion version) {
        return (FolderAPI)super.getVersion(version);
    }
    
    @Override
    public boolean isModified() {
        if (super.isModified()) {
            return true;
        }
        for (LocalArtefactAPI child : getArtefacts()) {
            if (child.isModified()) {
                return true;
            }
        }
        return false;
    }
}
