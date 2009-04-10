package org.openl.rules.repository.jcr;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.openl.rules.repository.CommonUser;
import org.openl.rules.repository.CommonVersion;
import org.openl.rules.repository.RDeploymentDescriptorProject;
import org.openl.rules.repository.RLock;
import org.openl.rules.repository.RProjectDescriptor;
import org.openl.rules.repository.RVersion;
import org.openl.rules.repository.exceptions.RRepositoryException;

public class JcrOldDeploymentProject implements RDeploymentDescriptorProject {

    private String name;
    private JcrVersion version;
    
    private HashMap<String, RProjectDescriptor> projects;

    public JcrOldDeploymentProject(String name, Node node) throws RepositoryException {
	this.name = name;
        this.version = new JcrVersion(node);
        
        projects = new HashMap<String, RProjectDescriptor>();
        
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            
            JcrOldProjectDescriptor pd = new JcrOldProjectDescriptor(n);
            projects.put(pd.getProjectName(), pd);
        }
    }
    
    public String getName() {
	return name;
    }

    public RVersion getActiveVersion() {
	return version;
    }

    public Collection<RProjectDescriptor> getProjectDescriptors() {
	return projects.values();
    }

    public List<RVersion> getVersionHistory() throws RRepositoryException {
        LinkedList<RVersion> result = new LinkedList<RVersion>();
        
        // only current version
        result.add(version);
        return result;
    }

    public RProjectDescriptor createProjectDescriptor(String name) throws RRepositoryException {
	notSupported();
	return null;
    }

    public void setProjectDescriptors(Collection<RProjectDescriptor> projectDescriptors) throws RRepositoryException {
	notSupported();
    }

    public void commit(CommonUser user) throws RRepositoryException {
	notSupported();
    }

    public void delete(CommonUser user) throws RRepositoryException {
	notSupported();
    }

    public void erase(CommonUser user) throws RRepositoryException {
	notSupported();
    }

    public RLock getLock() throws RRepositoryException {
	return JcrOldProject.NO_LOCK;
    }

    public boolean isLocked() throws RRepositoryException {
	return false;
    }

    public boolean isMarked4Deletion() throws RRepositoryException {
	return false;
    }

    public void lock(CommonUser user) throws RRepositoryException {
	notSupported();
    }

    public void riseVersion(int major, int minor) throws RRepositoryException {
	notSupported();
    }

    public void undelete(CommonUser user) throws RRepositoryException {
	notSupported();
    }

    public void unlock(CommonUser user) throws RRepositoryException {
	notSupported();
    }

    public RDeploymentDescriptorProject getProjectVersion(CommonVersion version) throws RRepositoryException {
        throw new RRepositoryException("In versioned mode can work with one version only!", null);
    }

    // --- protected

    protected void notSupported() throws RRepositoryException {
        throw new RRepositoryException("Cannot modify artefact version!", null);
    }
}
