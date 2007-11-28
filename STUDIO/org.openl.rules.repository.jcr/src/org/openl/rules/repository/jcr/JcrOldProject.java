package org.openl.rules.repository.jcr;

import java.util.Collection;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.openl.rules.repository.CommonUser;
import org.openl.rules.repository.CommonVersion;
import org.openl.rules.repository.RDependency;
import org.openl.rules.repository.RFolder;
import org.openl.rules.repository.RLock;
import org.openl.rules.repository.RProject;
import org.openl.rules.repository.exceptions.RRepositoryException;

public class JcrOldProject extends JcrOldEntity implements RProject {

    public final static RLock NO_LOCK = new RLock() {
        public Date getLockedAt() {return null;}
        public CommonUser getLockedBy() {return null;}
        public boolean isLocked() {return false;}
    };

    private JcrOldFolder rootFolder;
    private JcrDependencies dependencies;
    
    public JcrOldProject(String name, Node node, CommonVersion version) throws RepositoryException {
        super(null, name, node);
        checkNodeType(JcrNT.NT_PROJECT);
        
        Node files = NodeUtil.normalizeOldNode(node.getNode(JcrProject.NODE_FILES), version);
        rootFolder = new JcrOldFolder(this, null, files, version);
        
        Node deps = node.getNode(JcrProject.NODE_DEPENDENCIES);
        PropertyIterator pi = deps.getProperties();
        while(pi.hasNext()) {
            Property p = pi.nextProperty();
            System.out.println(" p " + p.getName());
        }
        NodeIterator ni = deps.getNodes();
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            System.out.println(" n " + n.getName());
        }
        
        dependencies = new JcrDependencies(deps);
    }

    public Collection<RDependency> getDependencies() throws RRepositoryException {
        return dependencies.getDependencies();
    }

    public RFolder getRootFolder() {
        return rootFolder;
    }

    public void setDependencies(Collection<? extends RDependency> dependencies) throws RRepositoryException {
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
        // not supported
        return NO_LOCK;
    }

    public boolean isLocked() throws RRepositoryException {
        // cannot be locked
        return false;
    }

    public boolean isMarked4Deletion() throws RRepositoryException {
        // not supported
        return false;
    }

    public void lock(CommonUser user) throws RRepositoryException {
        notSupported();
    }

    public void undelete(CommonUser user) throws RRepositoryException {
        notSupported();
    }

    public void unlock(CommonUser user) throws RRepositoryException {
        notSupported();
    }

    public RProject getProjectVersion(CommonVersion version) throws RRepositoryException {
        throw new RRepositoryException("In versioned mode can work with one version only!", null);
    }
}
