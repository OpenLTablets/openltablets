package org.openl.rules.repository.jcr;

import org.apache.commons.lang.StringUtils;
import org.openl.rules.repository.RDeploymentDescriptorProject;
import org.openl.rules.repository.REntity;
import org.openl.rules.repository.RProductionDeployment;
import org.openl.rules.repository.RProductionRepository;
import org.openl.rules.repository.RProject;
import org.openl.rules.repository.exceptions.RRepositoryException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class JcrProductionRepository extends BaseJcrRepository implements RProductionRepository {
    private Node deployLocation;

    public JcrProductionRepository(String name, Session session) throws RepositoryException {
        super(name, session);

        deployLocation = checkPath("/deploy");
        if (deployLocation.isNew()) {
            session.save();
        }
    }

    /**
     * Gets project by name.
     *
     * @param name
     * @return project
     * @throws org.openl.rules.repository.exceptions.RRepositoryException
     *          if failed or no project with specified name
     */
    public RProject getProject(String name) throws RRepositoryException {
        throw new UnsupportedOperationException(); 
    }

    /**
     * Checks whether project with given name exists in the repository.
     *
     * @param name
     * @return <code>true</code> if project with such name exists
     * @throws org.openl.rules.repository.exceptions.RRepositoryException
     *
     */
    public boolean hasProject(String name) throws RRepositoryException {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets list of projects from the repository.
     *
     * @return list of projects
     * @throws org.openl.rules.repository.exceptions.RRepositoryException
     *          if failed
     */
    public List<RProject> getProjects() throws RRepositoryException {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets list of projects from the repository that are marked for deletion.
     *
     * @return list of projects that are marked for deletion
     */
    public List<RProject> getProjects4Deletion() throws RRepositoryException {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a project in the repository.
     * Name of new project must be unique.
     *
     * @param name name of new project
     * @return newly created project
     * @throws org.openl.rules.repository.exceptions.RRepositoryException
     *          if failed
     */
    public RProject createProject(String name) throws RRepositoryException {
        throw new UnsupportedOperationException();
    }

    public RDeploymentDescriptorProject getDDProject(String name) throws RRepositoryException {
        throw new UnsupportedOperationException();
    }

    public List<RDeploymentDescriptorProject> getDDProjects() throws RRepositoryException {
        throw new UnsupportedOperationException();
    }

    public RDeploymentDescriptorProject createDDProject(String name) throws RRepositoryException {
        throw new UnsupportedOperationException();
    }

    public boolean hasDDProject(String name) throws RRepositoryException {
        throw new UnsupportedOperationException();
    }

    public boolean hasDeployment(String name) throws RRepositoryException {
        try {
            return deployLocation.hasNode(name);
        } catch (RepositoryException e) {
            throw new RRepositoryException("failed to check project {0}", e, name);
        }
    }

    public RProductionDeployment createDeployment(String name) throws RRepositoryException {
        try {
            return JcrProductionDeployment.createDeployment(deployLocation, name);
        } catch (RepositoryException e) {
            throw new RRepositoryException("could not create deployment {0}", e, name);
        }
    }

    public RProductionDeployment getDeployment(String name) throws RRepositoryException {
        Node node;
        try {
            node = deployLocation.getNode(name);
        } catch (RepositoryException e) {
            throw new RRepositoryException("failed to get node", e);
        }

        try {
            return new JcrProductionDeployment(node);
        } catch (RepositoryException e) {
            throw new RRepositoryException("failed to wrap JCR node", e);
        }
    }

    public Collection<String> getDeploymentNames() throws RRepositoryException {
        List<String> result = new ArrayList<String>();
        try {
            NodeIterator iterator = deployLocation.getNodes();
            while (iterator.hasNext()) {
                Node node = iterator.nextNode();
                if (node.getPrimaryNodeType().getName().equals(JcrNT.NT_DEPLOYMENT)) {
                    result.add(node.getName());
                }
            }
        } catch (RepositoryException e) {
            throw new RRepositoryException("failed to enumerate deployments", e);
        }

        return result;
    }

    public Collection<REntity> findNodes(SearchParams params) throws RRepositoryException {
        try {
            Query query = session.getWorkspace().getQueryManager().createQuery(buildQuery(params), Query.XPATH);
            QueryResult queryResult = query.execute();

            NodeIterator nodeIterator = queryResult.getNodes();
            List<REntity> result = new ArrayList<REntity>();
            while (nodeIterator.hasNext()) {
                Node node = nodeIterator.nextNode();
                String type = node.getPrimaryNodeType().getName();
                if (type.equals(JcrNT.NT_DEPLOYMENT)) {
                    result.add(new JcrProductionDeployment(node));
                } else if (type.equals(JcrNT.NT_FOLDER)) {
                    result.add(new JcrProductionFolder(node));
                } else if (type.equals(JcrNT.NT_FILE)) {
                    result.add(new JcrProductionFile(node));
                } else if (type.equals(JcrNT.NT_PROJECT)) {
                    result.add(new JcrProductionProject(node));
                }
            }
                                  
            return result;
        } catch (RepositoryException e) {
            throw new RRepositoryException("failed to run query", e);
        }
    }

    private static String buildQuery(SearchParams params) {
        StringBuilder sb = new StringBuilder("//element(*, nt:base)");
        if (!StringUtils.isEmpty(params.getLineOfBusiness())) {
            // todo: check for injection
            sb.append("[@" + JcrNT.PROP_LINE_OF_BUSINESS + "='").append(params.getLineOfBusiness()).append("']");
        }

        appendDateCondition(params.getLowerEffectiveDate(), JcrNT.PROP_EFFECTIVE_DATE + " >= ", sb);
        appendDateCondition(params.getUpperEffectiveDate(), JcrNT.PROP_EFFECTIVE_DATE + " <= ", sb);
        appendDateCondition(params.getLowerExpirationDate(), JcrNT.PROP_EXPIRATION_DATE + " >= ", sb);
        appendDateCondition(params.getUpperExpirationDate(), JcrNT.PROP_EXPIRATION_DATE + " <= ", sb);

        return sb.toString();
    }

    private static void appendDateCondition(Date date, String condition, StringBuilder sb) {
        if (date != null) {
            sb.append("[@").append(condition).append(date.getTime()).append("]");
        }
    }
}
