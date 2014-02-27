package org.openl.rules.repository.jcr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.rules.common.impl.ArtefactPathImpl;
import org.openl.rules.repository.RDeploymentDescriptorProject;
import org.openl.rules.repository.RProject;
import org.openl.rules.repository.RRepositoryListener;
import org.openl.rules.repository.RTransactionManager;
import org.openl.rules.repository.RRepositoryListener.RRepositoryEvent;
import org.openl.rules.repository.api.FolderAPI;
import org.openl.rules.repository.api.ArtefactProperties;
import org.openl.rules.repository.exceptions.RRepositoryException;

/**
 * Implementation for JCR Repository. One JCR Repository instance per user.
 *
 * @author Aleh Bykhavets
 *
 */
public class JcrRepository extends BaseJcrRepository {
    private final Log log = LogFactory.getLog(JcrRepository.class);
    private static final String QUERY_PROJECTS = "//element(*, " + JcrNT.NT_PROJECT + ")";
    private static final String QUERY_PROJECTS_4_DEL = "//element(*, " + JcrNT.NT_PROJECT + ") [@"
            + ArtefactProperties.PROP_PRJ_MARKED_4_DELETION + "]";
    private static final String QUERY_DDPROJECTS = "//element(*, " + JcrNT.NT_DEPLOYMENT_PROJECT + ")";

    private Node defRulesLocation;
    private Node defDeploymentsLocation;

    private List<RRepositoryListener> listeners = new ArrayList<RRepositoryListener>();

    public JcrRepository(String name, Session session, RTransactionManager transactionManager, String defRulesPath, String defDeploymentsPath)
            throws RepositoryException {
        super(name, session, transactionManager);

        defRulesLocation = checkPath(defRulesPath);
        defDeploymentsLocation = checkPath(defDeploymentsPath);

        if (defRulesLocation.isNew() || defDeploymentsLocation.isNew()) {
            // save all at once
            session.save();
        }

        session.getWorkspace()
                .getObservationManager()
                .addEventListener(this, Event.PROPERTY_CHANGED | Event.NODE_REMOVED, session.getRootNode().getPath(),
                        true, null, null, false);

    }

    @Deprecated
    public RDeploymentDescriptorProject createDDProject(String nodeName) throws RRepositoryException {
        try {
            return JcrDeploymentDescriptorProject.createProject(defDeploymentsLocation, nodeName);
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to create DDProject ''{0}''.", e, nodeName);
        }
    }

    @Deprecated
    public RProject createProject(String nodeName) throws RRepositoryException {
        try {
            return JcrProject.createProject(defRulesLocation, nodeName);
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to create Project ''{0}''.", e, nodeName);
        }
    }

    @Deprecated
    public RDeploymentDescriptorProject getDDProject(String name) throws RRepositoryException {
        try {
            if (!defDeploymentsLocation.hasNode(name)) {
                throw new RRepositoryException("Cannot find Project ''{0}''.", null, name);
            }

            Node n = defDeploymentsLocation.getNode(name);
            return new JcrDeploymentDescriptorProject(n);
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to get DDProject ''{0}''.", e, name);
        }
    }

    @Deprecated
    public List<RDeploymentDescriptorProject> getDDProjects() throws RRepositoryException {
        NodeIterator ni = runQuery(QUERY_DDPROJECTS);

        LinkedList<RDeploymentDescriptorProject> result = new LinkedList<RDeploymentDescriptorProject>();
        while (ni.hasNext()) {
            Node n = ni.nextNode();

            try {
                JcrDeploymentDescriptorProject ddp = new JcrDeploymentDescriptorProject(n);
                result.add(ddp);
            } catch (RepositoryException e) {
                log.debug("Failed to add deployment project.");
            }
        }

        return result;
    }

    @Deprecated
    public RProject getProject(String name) throws RRepositoryException {
        try {
            if (!defRulesLocation.hasNode(name)) {
                throw new RRepositoryException("Cannot find project ''{0}''", null, name);
            }

            Node n = defRulesLocation.getNode(name);
            return new JcrProject(n);
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to get project ''{0}''", e, name);
        }
    }

    @Deprecated
    public List<RProject> getProjects() throws RRepositoryException {
        // TODO list all or only that are active (not marked4deletion)?
        NodeIterator ni = runQuery(QUERY_PROJECTS);
        LinkedList<RProject> result = new LinkedList<RProject>();
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            try {
                JcrProject p = new JcrProject(n);
                result.add(p);
            } catch (RepositoryException e) {
                log.debug("Failed to add rules project.");
            }
        }

        return result;
    }

    @Deprecated
    public List<RProject> getProjects4Deletion() throws RRepositoryException {
        NodeIterator ni = runQuery(QUERY_PROJECTS_4_DEL);
        LinkedList<RProject> result = new LinkedList<RProject>();
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            try {
                JcrProject p = new JcrProject(n);
                result.add(p);
            } catch (RepositoryException e) {
                log.debug("Failed to add rules project for deletion.");
            }
        }

        return result;
    }

    public boolean hasDeploymentProject(String name) throws RRepositoryException {
        try {
            return defDeploymentsLocation.hasNode(name);
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to check project ''{0}''", e, name);
        }
    }

    // ------ protected methods ------

    /** {@inheritDoc} */
    public boolean hasProject(String name) throws RRepositoryException {
        try {
            return defRulesLocation.hasNode(name) && !defRulesLocation.getNode(name).isNodeType(JcrNT.NT_LOCK);
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to check project ''{0}''", e, name);
        }
    }

    /**
     * Runs query in JCR.
     *
     * @param statement query statement
     * @return list of OpenL projects
     * @throws RRepositoryException if failed
     */
    protected NodeIterator runQuery(String statement) throws RRepositoryException {
        try {
            QueryManager qm = getSession().getWorkspace().getQueryManager();
            Query query = qm.createQuery(statement, Query.XPATH);

            QueryResult qr = query.execute();
            return qr.getNodes();

        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to run query.", e);
        }
    }

    public FolderAPI createDeploymentProject(String name) throws RRepositoryException {
        try {
            Node node = NodeUtil.createNode(defDeploymentsLocation, name,
                    JcrNT.NT_APROJECT, true);
            defDeploymentsLocation.save();
            node.checkin();
            return new JcrFolderAPI(node, getTransactionManager(), new ArtefactPathImpl(new String[] { name })); 
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to create deployment project.", e);
        }
    }

    public FolderAPI createRulesProject(String name) throws RRepositoryException {
        try {
            Node node = NodeUtil.createNode(defRulesLocation, name,
                    JcrNT.NT_APROJECT, true);
            defRulesLocation.save();
            node.checkin();
            return new JcrFolderAPI(node, getTransactionManager(), new ArtefactPathImpl(new String[] { name })); 
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to create rules project.", e);
        }
    }

    public FolderAPI getDeploymentProject(String name) throws RRepositoryException {
        try {
            if (!defDeploymentsLocation.hasNode(name)) {
                throw new RRepositoryException("Cannot find Project ''{0}''.", null, name);
            }

            Node n = defDeploymentsLocation.getNode(name);
            return new JcrFolderAPI(n, getTransactionManager(), new ArtefactPathImpl(new String[] { name }));
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to get DDProject ''{0}''.", e, name);
        }
    }

    public List<FolderAPI> getDeploymentProjects() throws RRepositoryException {
        NodeIterator ni;
        try {
            ni = defDeploymentsLocation.getNodes();
        } catch (RepositoryException e) {
            throw new RRepositoryException("Cannot get any deployment project", e); 
        }

        LinkedList<FolderAPI> result = new LinkedList<FolderAPI>();
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            try {
                if (!n.isNodeType(JcrNT.NT_LOCK)) {
                    result.add(new JcrFolderAPI(n, getTransactionManager(), new ArtefactPathImpl(new String[] { n.getName() })));
                }
            } catch (RepositoryException e) {
                log.debug("Failed to add deployment project.");
            }
        }

        return result;
    }

    public FolderAPI getRulesProject(String name) throws RRepositoryException {
        try {
            if (!defRulesLocation.hasNode(name)) {
                throw new RRepositoryException("Cannot find project ''{0}''", null, name);
            }

            Node n = defRulesLocation.getNode(name);
            return new JcrFolderAPI(n, getTransactionManager(), new ArtefactPathImpl(new String[] { name }));
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to get project ''{0}''", e, name);
        }
    }

    public List<FolderAPI> getRulesProjects() throws RRepositoryException {
        NodeIterator ni;
        try {
            ni = defRulesLocation.getNodes();
        } catch (RepositoryException e) {
            throw new RRepositoryException("Cannot get any rules project", e); 
        }

        LinkedList<FolderAPI> result = new LinkedList<FolderAPI>();
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            try {
                if (!n.isNodeType(JcrNT.NT_LOCK)) {
                    result.add(new JcrFolderAPI(n, getTransactionManager(), new ArtefactPathImpl(new String[] { n.getName() })));
                }
            } catch (RepositoryException e) {
                log.debug("Failed to add rules project.");
            }
        }

        return result;
    }

    public List<FolderAPI> getRulesProjectsForDeletion() throws RRepositoryException {
        NodeIterator ni = runQuery("//element(*, " + JcrNT.NT_APROJECT + ") [@"
                + ArtefactProperties.PROP_PRJ_MARKED_4_DELETION + "]");

        LinkedList<FolderAPI> result = new LinkedList<FolderAPI>();
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            try {
                result.add(new JcrFolderAPI(n, getTransactionManager(), new ArtefactPathImpl(new String[] { n.getName() })));
            } catch (RepositoryException e) {
                log.debug("Failed to add rules project for deletion.");
            }
        }

        return result;
    }
    
    private static final String CHECKED_OUT_PROPERTY = "jcr:isCheckedOut";
    
    private String extractProjectName(String relativePath){
        return new ArtefactPathImpl(relativePath).segment(0);
    }
    
    private boolean isProjectDeletedEvent(Event event, String relativePath){
        ArtefactPathImpl path = new ArtefactPathImpl(relativePath);
        return path.segmentCount() == 1 && event.getType() == Event.NODE_REMOVED;
    }
    
    private boolean isProjectModifiedEvent(Event event, String relativePath){
        return relativePath.contains(CHECKED_OUT_PROPERTY);
    }

    public void onEvent(EventIterator eventIterator) {
        while (eventIterator.hasNext()) {
            Event event = eventIterator.nextEvent();
            try {
                if (event.getPath().startsWith(defRulesLocation.getPath())) {
                    String relativePath = StringUtils.removeStart(event.getPath(), defRulesLocation.getPath() + "/");
                    if (isProjectDeletedEvent(event, relativePath) || isProjectModifiedEvent(event, relativePath)) {
                        for (RRepositoryListener listener : listeners) {
                            listener.onEventInRulesProjects(new RRepositoryEvent(extractProjectName(relativePath)));
                        }
                    }
                } else if (event.getPath().startsWith(defDeploymentsLocation.getPath())) {
                    String relativePath = StringUtils.removeStart(event.getPath(), defDeploymentsLocation.getPath()
                            + "/");
                    if (isProjectDeletedEvent(event, relativePath) || isProjectModifiedEvent(event, relativePath)) {
                        for (RRepositoryListener listener : listeners) {
                            listener.onEventInDeploymentProjects(new RRepositoryEvent(extractProjectName(relativePath)));
                        }
                    }
                }
            } catch (RepositoryException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void addRepositoryListener(RRepositoryListener listener) {
        listeners.add(listener);
    }

    public void removeRepositoryListener(RRepositoryListener listener) {
        listeners.remove(listener);
    }

    public List<RRepositoryListener> getRepositoryListeners() {
        return listeners;
    }
}
