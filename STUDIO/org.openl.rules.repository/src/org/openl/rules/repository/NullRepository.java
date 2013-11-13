package org.openl.rules.repository;

import org.openl.rules.repository.RDeploymentDescriptorProject;
import org.openl.rules.repository.RRepository;
import org.openl.rules.repository.RProject;
import org.openl.rules.repository.api.FolderAPI;
import org.openl.rules.repository.exceptions.RRepositoryException;

import java.util.LinkedList;
import java.util.List;

/**
 * Stub to use when repository cannot be initialized.
 *
 * @author Aleh Bykhavets
 *
 */
public class NullRepository implements RRepository {
    private static final List<RProject> EMPTY_LIST = new LinkedList<RProject>();

    @Deprecated
    public RDeploymentDescriptorProject createDDProject(String name) throws RRepositoryException {
        fail();
        // will never reach
        return null;
    }

    @Deprecated
    public RProject createProject(String name) throws RRepositoryException {
        fail();
        // will never reach
        return null;
    }

    protected void fail() throws RRepositoryException {
        throw new RRepositoryException("Failed to initialize repository!", null);
    }

    @Deprecated
    public RDeploymentDescriptorProject getDDProject(String name) throws RRepositoryException {
        fail();
        // will never reach
        return null;
    }

    @Deprecated
    public List<RDeploymentDescriptorProject> getDDProjects() throws RRepositoryException {
        // empty list
        return new LinkedList<RDeploymentDescriptorProject>();
    }

    public String getName() {
        // TODO: may be put here something more consistent
        return "Failed to init Repository!";
    }

    @Deprecated
    public RProject getProject(String name) throws RRepositoryException {
        fail();
        // will never reach
        return null;
    }

    @Deprecated
    public List<RProject> getProjects() throws RRepositoryException {
        return EMPTY_LIST;
    }

    @Deprecated
    public List<RProject> getProjects4Deletion() throws RRepositoryException {
        return EMPTY_LIST;
    }

    public boolean hasDeploymentProject(String name) throws RRepositoryException {
        return false;
    }

    public boolean hasProject(String name) throws RRepositoryException {
        return false;
    }

    public void release() {
        // Do nothing
    }

    public FolderAPI createDeploymentProject(String name) throws RRepositoryException {
        fail();
        // will never reach
        return null;
    }

    public FolderAPI createRulesProject(String name) throws RRepositoryException {
        fail();
        // will never reach
        return null;
    }

    public FolderAPI getDeploymentProject(String name) throws RRepositoryException {
        fail();
        // will never reach
        return null;
    }

    public List<FolderAPI> getDeploymentProjects() throws RRepositoryException {
        // empty list
        return new LinkedList<FolderAPI>();
    }

    public FolderAPI getRulesProject(String name) throws RRepositoryException {
        fail();
        // will never reach
        return null;
    }

    public List<FolderAPI> getRulesProjects() throws RRepositoryException {
        return new LinkedList<FolderAPI>();
    }

    public List<FolderAPI> getRulesProjectsForDeletion() throws RRepositoryException {
        return new LinkedList<FolderAPI>();
    }


    
    public void addRepositoryListener(RRepositoryListener listener) {
    }

    public void removeRepositoryListener(RRepositoryListener listener) {
    }

    public List<RRepositoryListener> getRepositoryListeners() {
        return null;
    }

    public RTransactionManager getTransactionManager() {
        return null;
    }
}
