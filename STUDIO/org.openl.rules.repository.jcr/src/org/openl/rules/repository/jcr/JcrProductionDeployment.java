package org.openl.rules.repository.jcr;

import org.openl.rules.repository.RProductionDeployment;
import org.openl.rules.repository.RProject;
import org.openl.rules.repository.exceptions.RRepositoryException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.NodeIterator;
import java.util.Collection;
import java.util.ArrayList;

public class JcrProductionDeployment extends JcrProductionEntity implements RProductionDeployment {
    private Node node;

    public JcrProductionDeployment(Node node) throws RepositoryException {
        super(node);
        NodeUtil.checkNodeType(node, JcrNT.NT_DEPLOYMENT);

        this.node = node;
    }

    /**
     * Creates new deployment instance.
     * <p>
     * Note that OpenL project cannot be created inside other OpenL project.
     * I.e. nesting is not allowed for OpenL projects.
     *
     * @param parentNode parent node
     * @param nodeName name of node
     * @return newly created deployment
     * @throws javax.jcr.RepositoryException if fails
     */
    static JcrProductionDeployment createDeployment(Node parentNode, String nodeName) throws RepositoryException  {
        Node n = NodeUtil.createNode(parentNode, nodeName, JcrNT.NT_DEPLOYMENT, false);

        parentNode.save();
        n.save();

        return new JcrProductionDeployment(n);
    }


    public Collection<RProject> getProjects() throws RRepositoryException {
        Collection<RProject> result = new ArrayList<RProject>();
        try {
            NodeIterator nodeIterator = node().getNodes();
            while (nodeIterator.hasNext()) {
                Node node = nodeIterator.nextNode();
                if (node.getPrimaryNodeType().getName().equals(JcrNT.NT_PROJECT)) {
                    result.add(getProject(node.getName()));
                }
            }

            return result;
        } catch (RepositoryException e) {
            throw new RRepositoryException("failed to get projects", e);
        }
    }

    public RProject createProject(String projectName) throws RRepositoryException {
        try {
            return JcrProductionProject.createProject(node, projectName);
        } catch (RepositoryException e) {
            throw new RRepositoryException("failed to create project", e);
        }
    }

    public boolean hasProject(String name) throws RRepositoryException {
        try {
            return node.hasNode(name);
        } catch (RepositoryException e) {
            throw new RRepositoryException("failed to check if project exists", e);
        }
    }

    public RProject getProject(String name) throws RRepositoryException {
        try {
            return new JcrProductionProject(node.getNode(name));
        } catch (RepositoryException e) {
            throw new RRepositoryException("failed to get project " + name, e);
        }
    }

    public void save() throws RRepositoryException {
        try {
            node.save();
        } catch (RepositoryException e) {
            throw new RRepositoryException("failed to save deployment", e);
        }
    }
}
