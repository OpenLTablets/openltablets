package org.openl.rules.repository.jcr;

import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.openl.rules.repository.RDeploymentDescriptorProject;
import org.openl.rules.repository.RProject;
import org.openl.rules.repository.RRepository;
import org.openl.rules.repository.exceptions.RRepositoryException;

/**
 * Implementation for JCR Repository.
 * One JCR Repository instance per user.
 * 
 * @author Aleh Bykhavets
 *
 */
public class JcrRepository implements RRepository {
    private static final String QUERY_PROJECTS = "//element(*, " + JcrNT.NT_PROJECT + ")";
    private static final String QUERY_PROJECTS_4_DEL = "//element(*, " + JcrNT.NT_PROJECT + ") [@" + JcrNT.PROP_PRJ_MARKED_4_DELETION + "]";
    private static final String QUERY_DDPROJECTS = "//element(*, " + JcrNT.NT_DEPLOYMENT_PROJECT + ")";

    private String name;
    /** JCR Session */
    private Session session;
    private Node defNewProjectLocation;

    public JcrRepository(String name, Session session, String defPath) throws RepositoryException {
        this.name = name;
        this.session = session;

        Node node = session.getRootNode();
        String[] paths = defPath.split("/");
        for (String path : paths) {
            if (path.length() == 0) continue; // first element (root folder) or illegal path

            if (node.hasNode(path)) {
                // go deeper
                node = node.getNode(path);
            } else {
                // create new
                node = node.addNode(path);
            }
        }

        if (node.isNew()) {
            // save all at once
            session.save();
        }
        defNewProjectLocation = node;
    }

    /** {@inheritDoc} */
    public RProject getProject(String name) throws RRepositoryException {
        try {
            if (!defNewProjectLocation.hasNode(name)) {
                throw new RRepositoryException("Cannot find project ''{0}''", null, name);
            }

            Node n = defNewProjectLocation.getNode(name);
            JcrProject p = new JcrProject(n);
            return p;
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to get project {0}", e, name);
        }        
    }
    
    /** {@inheritDoc} */
    public boolean hasProject(String name) throws RRepositoryException {
        try {
            return defNewProjectLocation.hasNode(name);
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to check project {0}", e, name);
        }        
    }

    /** {@inheritDoc} */
    public List<RProject> getProjects() throws RRepositoryException {
        // TODO list all or only that are active (not marked4deletion)?
        return runQuery(QUERY_PROJECTS);
    }

    /** {@inheritDoc} */
    public List<RProject> getProjects4Deletion() throws RRepositoryException {
        return runQuery(QUERY_PROJECTS_4_DEL);
    }

    /** {@inheritDoc} */
    public void release() {
        session.logout();
    }

    /** {@inheritDoc} */
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    public RProject createProject(String nodeName) throws RRepositoryException {
        try {
            return JcrProject.createProject(defNewProjectLocation, nodeName);
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to create Project {0}", e, nodeName);
        }
    }

    public RDeploymentDescriptorProject getDDProject(String name) throws RRepositoryException {
        try {
            if (!defNewProjectLocation.hasNode(name)) {
                throw new RRepositoryException("Cannot find Project ''{0}''", null, name);
            }

            Node n = defNewProjectLocation.getNode(name);
            JcrDeploymentDescriptorProject ddp = new JcrDeploymentDescriptorProject(n);
            return ddp;
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to get DDProject {0}", e, name);
        }        
    }

    public List<RDeploymentDescriptorProject> getDDProjects() throws RRepositoryException {
        return runQueryDDP();
    }

    public RDeploymentDescriptorProject createDDProject(String nodeName) throws RRepositoryException {
        try {
            return JcrDeploymentDescriptorProject.createProject(
                    defNewProjectLocation, nodeName);
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to create DDProject {0}", e, nodeName);
        }        
    }

    // ------ protected methods ------

    /**
     * Gets internal JCR Session.
     *
     * @return JCR Session
     */
    protected Session getSession() {
        return session;
    }

    /**
     * Runs query in JCR.
     *
     * @param statement query statement
     * @return list of OpenL projects
     * @throws RRepositoryException if failed
     */
    protected List<RProject> runQuery(String statement) throws RRepositoryException {
        try {
            QueryManager qm = session.getWorkspace().getQueryManager();
            Query query = qm.createQuery(statement, Query.XPATH);

            QueryResult qr = query.execute();

            LinkedList<RProject> result = new LinkedList<RProject>();
            for (NodeIterator ni = qr.getNodes(); ni.hasNext(); ) {
                Node n = ni.nextNode();

                JcrProject p = new JcrProject(n);
                result.add(p);
            }

            return result;
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to run query", e);
        }
    }

    protected List<RDeploymentDescriptorProject> runQueryDDP() throws RRepositoryException {
        try {
            QueryManager qm = session.getWorkspace().getQueryManager();
            Query query = qm.createQuery(QUERY_DDPROJECTS, Query.XPATH);

            QueryResult qr = query.execute();

            LinkedList<RDeploymentDescriptorProject> result = new LinkedList<RDeploymentDescriptorProject>();
            for (NodeIterator ni = qr.getNodes(); ni.hasNext(); ) {
                Node n = ni.nextNode();

                JcrDeploymentDescriptorProject ddp = new JcrDeploymentDescriptorProject(n);
                result.add(ddp);
            }

            return result;
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to run query", e);
        }
    }
}
