package org.openl.rules.workspace.dtr.impl;

import org.openl.rules.repository.RFile;
import org.openl.rules.repository.exceptions.RDeleteException;
import org.openl.rules.repository.exceptions.RModifyException;
import org.openl.rules.repository.exceptions.RRepositoryException;
import org.openl.rules.workspace.abstracts.ArtefactPath;
import org.openl.rules.workspace.abstracts.ProjectArtefact;
import org.openl.rules.workspace.abstracts.ProjectException;
import org.openl.rules.workspace.abstracts.ProjectResource;
import org.openl.rules.workspace.dtr.RepositoryProjectArtefact;
import org.openl.rules.workspace.dtr.RepositoryProjectResource;
import org.openl.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class RepositoryProjectResourceImpl extends RepositoryProjectArtefactImpl implements RepositoryProjectResource {
    private RFile rulesFile;
    private String resourceType;

    protected RepositoryProjectResourceImpl(RFile rulesFile, ArtefactPath path) {
        super(rulesFile, path);
        this.rulesFile = rulesFile;
        // TODO fix me
        this.resourceType = "some-file";
    }

    public RepositoryProjectArtefact getArtefact(String name) throws ProjectException {
        throw new ProjectException("Cannot find project artefact ''{0}''", null, name);
    }

    public InputStream getContent() throws ProjectException {
        try {
            return rulesFile.getContent();
        } catch (RRepositoryException e) {
            throw new ProjectException("Cannot get content", e);
        }        
    }

    public String getResourceType() {
        return resourceType;
    }

    public void update(ProjectArtefact srcArtefact) throws ProjectException {
        ProjectResource srcResource = (ProjectResource) srcArtefact;
        super.update(srcArtefact);
        
//        String resType = srcResource.getResourceType();
        // TODO update resource type
        
        InputStream is = null;
        
        try {
            is = srcResource.getContent();
            
            rulesFile.setContent(is);
        } catch (RModifyException e) {
            throw new ProjectException("Failed to update project resource ", e, getArtefactPath().getStringValue());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.error("Failed to close input stream", e);
                    // ignore
                }                
            }
        }
    }

    public void delete() throws ProjectException {
        try {
            rulesFile.delete();
        } catch (RDeleteException e) {
            throw new ProjectException("Failed to delete project resource ''{0}''", e, getArtefactPath().getStringValue());
        }        
    }

    public boolean isFolder() {
        return false;
    }

    public boolean hasArtefact(String name) {
        return false;
    }
}
