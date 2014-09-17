package org.openl.rules.repository.jcr;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.openl.rules.common.CommonUser;
import org.openl.rules.common.ValueType;
import org.openl.rules.repository.REntity;
import org.openl.rules.repository.RLock;
import org.openl.rules.repository.RVersion;
import org.openl.rules.repository.api.ArtefactProperties;
import org.openl.rules.repository.exceptions.RRepositoryException;

public class JcrOldEntity implements REntity {
    private Node node;
    private String name;

    private JcrVersion version;
    private JcrOldEntity parent;

    private Date effectiveDate;
    private Date expirationDate;
    private String lineOfBusiness;

    private Map<String, Object> props;

    public JcrOldEntity(JcrOldEntity parent, String name, Node node) throws RepositoryException {
        this.parent = parent;
        this.node = node;
        this.name = name;

        version = new JcrVersion(node);

        if (node.hasProperty(ArtefactProperties.PROP_EFFECTIVE_DATE)) {
            effectiveDate = node.getProperty(ArtefactProperties.PROP_EFFECTIVE_DATE).getDate().getTime();
        }
        if (node.hasProperty(ArtefactProperties.PROP_EXPIRATION_DATE)) {
            expirationDate = node.getProperty(ArtefactProperties.PROP_EXPIRATION_DATE).getDate().getTime();
        }
        if (node.hasProperty(ArtefactProperties.PROP_LINE_OF_BUSINESS)) {
            lineOfBusiness = node.getProperty(ArtefactProperties.PROP_LINE_OF_BUSINESS).getString();
        }

        props = new HashMap<String, Object>();
        loadProps();
    }

    public void addProperty(String name, ValueType type, Object value) throws RRepositoryException {
        notSupported();
    }

    /**
     * Checks whether type of the JCR node is correct. Checks FROZEN Type
     *
     * @param frozenNodeType expected node type
     * @throws RepositoryException if failed
     */
    protected void checkNodeType(String frozenNodeType) throws RepositoryException {
        if (!node.isNodeType(JcrNT.NT_FROZEN_NODE)) {
            throw new RepositoryException("Not a frozen node!");
        }

        String actualFrozenNodeType = node.getProperty("jcr:frozenPrimaryType").getString();
        if (JcrNT.NT_FILES.equals(actualFrozenNodeType) && JcrNT.NT_FOLDER.equals(frozenNodeType)) {
            // openl:files -- openl:folder
            return;
        }

        if (!frozenNodeType.equals(actualFrozenNodeType)) {
            throw new RepositoryException("Invalid NodeType '" + actualFrozenNodeType + "'. Expects '" + frozenNodeType
                    + "'.");
        }
    }

    public void delete() throws RRepositoryException {
        notSupported();
    }

    public RVersion getActiveVersion() {
        return version;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public String getLineOfBusiness() {
        return lineOfBusiness;
    }

    public String getName() {
        return name;
    }

    public String getPath() throws RRepositoryException {
        JcrOldEntity curr = this;
        StringBuilder sb = new StringBuilder(128);

        while (curr != null) {
            String s = curr.getName();
            if (s != null) {
                sb.insert(0, s);
                sb.insert(0, '/');
            }

            curr = curr.parent;
        }

        return sb.toString();
    }

    public Collection<org.openl.rules.common.Property> getProperties() {
        // not supported
        return null;
    }

    public org.openl.rules.common.Property getProperty(String name) throws RRepositoryException {
        try {
            if (node().hasProperty(name)) {
                Property p = node().getProperty(name);

                return new JcrProperty(node(), p);
            }else{
                return null;
            }
        } catch (Exception e) {
            throw new RRepositoryException("Cannot find property", e);
        }
    }

    public Map<String, Object> getProps() {
        return props;
    }

    public List<RVersion> getVersionHistory() throws RRepositoryException {
        LinkedList<RVersion> result = new LinkedList<RVersion>();

        // only current version
        result.add(version);
        return result;
    }

    public boolean hasProperty(String name) {
        // not supported
        return false;
    }

    private void loadProps() throws RepositoryException {
        Node n = node();
        for (int i = 1; i <= ArtefactProperties.PROPS_COUNT; i++) {
            String propName = ArtefactProperties.PROP_ATTRIBUTE + i;
            if (n.hasProperty(propName)) {
                Value value = n.getProperty(propName).getValue();
                Object propValue = null;
                int valueType = value.getType();
                switch (valueType) {
                    case PropertyType.DATE:
                        propValue = value.getDate().getTime();
                        break;
                    case PropertyType.DOUBLE:
                        propValue = value.getDouble();
                        break;
                    default:
                        propValue = value.getString();
                        break;
                }
                props.put(propName, propValue);
            }
        }
    }

    protected final Node node() {
        return node;
    }

    protected void notSupported() throws RRepositoryException {
        throw new RRepositoryException("Cannot modify artefact version!", null);
    }

    public void removeProperty(String name) throws RRepositoryException {
        notSupported();
    }

    // --- protected

    public void setEffectiveDate(Date date) throws RRepositoryException {
        notSupported();
    }

    public void setExpirationDate(Date date) throws RRepositoryException {
        notSupported();
    }

    public void setLineOfBusiness(String lineOfBusiness) throws RRepositoryException {
        notSupported();
    }

    public void setProps(Map<String, Object> props) throws RRepositoryException {
        notSupported();
    }

    public RLock getLock() throws RRepositoryException {
        // not supported
        return RLock.NO_LOCK;
    }

    public boolean isLocked() throws RRepositoryException {
        // cannot be locked
        return false;
    }

    public void lock(CommonUser user) throws RRepositoryException {
        notSupported();
    }

    public void unlock(CommonUser user) throws RRepositoryException {
        notSupported();
    }

    public void commit(CommonUser user) throws RRepositoryException {
        notSupported();
    }

    public void riseVersion(int major, int minor) throws RRepositoryException {
        notSupported();
    }
}
