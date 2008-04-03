package org.openl.rules.repository.jcr;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.openl.rules.repository.CommonVersion;
import org.openl.rules.repository.CommonVersionImpl;
import org.openl.rules.repository.exceptions.RRepositoryException;

/**
 * JCR Node Utility
 * 
 * @author Aleh Bykhavets
 *
 */
public class NodeUtil {

    /**
     * Inquire whether given version is 'root'.
     * 
     * @param v version to be checked
     * @return <code>true</code> if the version is root;
     *         <code>false</code> otherwise;
     * @throws RepositoryException if operation failed
     */
    protected static boolean isRootVersion(Version v) throws RepositoryException {
        String name = v.getName();
        return "jcr:rootVersion".equals(name);
    }

    /**
     * Creates node of given node type.
     * 
     * @param parentNode parent node, where new node is going to be added
     * @param name name of new node
     * @param type node type of new node
     * @param isVersionable whether new node is versionable
     * @return reference on newly created node
     * @throws RepositoryException if operation failed
     */
    protected static Node createNode(Node parentNode, String name, String type, boolean isVersionable) throws RepositoryException {
        if (parentNode.hasNode(name)) {
            throw new RepositoryException("Node '" + name + "' exists at '" + parentNode.getPath() + "' already!");
        }

        Node p = parentNode;
        while (p != null) {
            if (p.isCheckedOut()) {
                break;
            } else {
                if (p.isNodeType(JcrNT.MIX_VERSIONABLE)) {
                    p.checkout();
                    break;
                }
            }
            
            p = p.getParent();
        }

        Node n = parentNode.addNode(name, type);
        if (isVersionable) {
            n.addMixin(JcrNT.MIX_VERSIONABLE);
        }

        return n;
    }

    /**
     * Checkout node and parent (if needed).
     * 
     * @param node reference on node to be checked in
     * @param openParent whether parent should be checked out
     * @throws RepositoryException if operation failed
     */
    protected static void smartCheckout(Node node, boolean openParent) throws RepositoryException {
        if (!node.isCheckedOut()) {
            if (node.isNodeType(JcrNT.MIX_VERSIONABLE)) { 
                node.checkout();
            }
        }

        if (openParent) {
            Node parentNode = node.getParent();
            
            if (!parentNode.isCheckedOut()) {
                parentNode.checkout();
            }
        }
    }
    
    protected static Node getNode4Version(Node node, CommonVersion version) throws RepositoryException {
        Node result = null;

        VersionHistory vh = node.getVersionHistory();
        VersionIterator vi = vh.getAllVersions();
        
        while (vi.hasNext()) {
            Version jcrVersion = vi.nextVersion();

            if (NodeUtil.isRootVersion(jcrVersion)) {
                //TODO Shall we add first (0) version? (It is marker like, no real values)
            } else {
                JcrVersion jvi = new JcrVersion(jcrVersion);
                CommonVersionImpl cv = new CommonVersionImpl(jvi.getMajor(), jvi.getMinor(), jvi.getRevision());
                
                if (cv.compareTo(version) == 0) {
                    result = jcrVersion.getNode(JcrNT.FROZEN_NODE);
                    break;
                }
            }
        }
        
        if (result == null) {
            throw new RepositoryException("Cannot find version '" + version.getVersionName() + "'!");
        }
        
        return result;
    }
    
    protected static Node normalizeOldNode(Node node, CommonVersion version) throws RepositoryException {
        if (node.isNodeType(JcrNT.NT_FROZEN_NODE)) {
            // all is OK
            return node;
        }
        
        if (!node.isNodeType("nt:versionedChild")) {
            // ??? unknown
            return node;
        }
        
        Node versionHistoryNode = node.getProperty("jcr:childVersionHistory").getNode();

        int projectRevision = version.getRevision();

        int correctVRev = -1;
        Node correctVNode = null;

        NodeIterator versions = versionHistoryNode.getNodes();
        while (versions.hasNext()) {
            Node versionNode = versions.nextNode();
            if (!versionNode.isNodeType("nt:version")) continue;

            // old nodes, should be 1 per versionNode
            NodeIterator oldNodes = versionNode.getNodes();
            while(oldNodes.hasNext()) {
                Node oldNode = oldNodes.nextNode();

                int nodeRevision = 0;
                if (oldNode.hasProperty(JcrNT.PROP_REVISION)) {
                    nodeRevision = (int)oldNode.getProperty(JcrNT.PROP_REVISION).getLong();
                }

                if (nodeRevision <= projectRevision) {
                    if (nodeRevision > correctVRev) {
                        correctVNode = oldNode;
                        correctVRev = nodeRevision;
                    }
                }
            }
        }
        
        return correctVNode;
    }
    
    protected static void printNode(Node node) throws RepositoryException {
        System.out.println("Node: " + node.getName());
        
        PropertyIterator pi = node.getProperties();
        while (pi.hasNext()) {
            Property p = pi.nextProperty();
            
            boolean isProtected = p.getDefinition().isProtected();
            boolean isMultiple = p.getDefinition().isMultiple();

            String status = "";
            if (isProtected) {
                status = "protected";
            }
            
            if (isMultiple) {
                System.out.println(" p " + p.getName() + " multiple " + status);
            } else {
                System.out.println(" p " + p.getName() + " " + status + " =" + p.getString());
            }
        }
    }

    /**
     * Checks whether type of the JCR node is correct.
     *
     * @param nodeType expected node type
     * @throws RepositoryException if failed
     */
    protected static void checkNodeType(Node node, String nodeType) throws RepositoryException {
        if (!node.isNodeType(nodeType)) {
            String actualNodeType = node.getPrimaryNodeType().getName();
            throw new RepositoryException("Invalid NodeType '" + actualNodeType + "'. Expects '" + nodeType + "'!");
        }
    }

    public static boolean isSame(Object o1, Object o2) {
        // both are null (the same)
        if (o1 == null && o2 == null) return true;
        // at least one is null (other is not)
        if (o1 == null || o2 == null) return false;
        // equals or not?
        return o1.equals(o2);
    }

    public static Calendar convertDate2Calendar(Date date) {
        if (date == null) return null;

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    protected static Node createFileNode(Node parentNode, String nodeName) throws RepositoryException {
        Node n = createNode(parentNode, nodeName, JcrNT.NT_FILE, true);

        String mimeType = "text/plain";
        String encoding = "UTF-8";
        long lastModifiedTime = System.currentTimeMillis();
        Calendar lastModified = Calendar.getInstance ();
        lastModified.setTimeInMillis(lastModifiedTime);

        //create the file node - see section 6.7.22.6 of the spec
        //create the mandatory child node - jcr:content
        Node resNode = n.addNode (JcrNT.PROP_RES_CONTENT, JcrNT.NT_RESOURCE);
        resNode.setProperty (JcrNT.PROP_RES_MIMETYPE, mimeType);
        resNode.setProperty (JcrNT.PROP_RES_ENCODING, encoding);
        // TODO add real init-content
        resNode.setProperty (JcrNT.PROP_RES_DATA, new ByteArrayInputStream(new byte[0]));
        resNode.setProperty (JcrNT.PROP_RES_LASTMODIFIED, lastModified);
        return n;
    }

    protected static long getFileNodeSize(Node node) {
        long result;

        try {
            Node resNode = node.getNode ("jcr:content");
            result = resNode.getProperty("jcr:data").getLength();
        } catch (RepositoryException e) {
            // TODO: log exception
            result = -1;
        }

        return result;
    }

    protected static InputStream getFileNodeContent(Node node) throws RRepositoryException {
        try {
            return node.getNode ("jcr:content").getProperty("jcr:data").getStream();
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to get Content!", e);
        }
    }


}
