package org.openl.rules.repository.jcr;

import org.openl.rules.common.*;
import org.openl.rules.common.impl.RepositoryProjectVersionImpl;
import org.openl.rules.common.impl.RepositoryVersionInfoImpl;
import org.openl.rules.repository.RLock;
import org.openl.rules.repository.RTransactionManager;
import org.openl.rules.repository.RVersion;
import org.openl.rules.repository.api.ArtefactAPI;
import org.openl.rules.repository.api.ArtefactProperties;
import org.openl.rules.repository.exceptions.RRepositoryException;
import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.Property;
import javax.transaction.UserTransaction;
import java.util.*;

/**
 * Implementation of JCR Entity. It is linked with node in JCR implementation,
 * always.
 *
 * @author Aleh Bykhavets
 */
public class JcrEntityAPI extends JcrCommonArtefact implements ArtefactAPI {
    private final Logger log = LoggerFactory.getLogger(JcrEntityAPI.class);
    /*
    private static final String[] ALLOWED_PROPS = {ArtefactProperties.PROP_EFFECTIVE_DATE, ArtefactProperties.PROP_EXPIRATION_DATE, 
        ArtefactProperties.PROP_LINE_OF_BUSINESS, ArtefactProperties.VERSION_COMMENT};
    */
    private Map<String, org.openl.rules.common.Property> properties;
    private Map<String, Object> props;
    private List<TablePropertyDefinition> bussinedDimensionProps;

    private JcrVersion version;
    private ArtefactPath path;
    private RTransactionManager transactionManager;

    // ------ protected methods ------
    public JcrEntityAPI(Node node, RTransactionManager transactionManager, ArtefactPath path, boolean oldVersion) throws RepositoryException {
        super(node, path.segment(path.segmentCount() - 1), oldVersion);
        this.path = path;
        this.transactionManager = transactionManager;
        version = new JcrVersion(node);

        bussinedDimensionProps = TablePropertyDefinitionUtils.getDimensionalTableProperties();

        properties = new HashMap<String, org.openl.rules.common.Property>();
        initProperties();
        props = new HashMap<String, Object>();
        loadProps();
    }

    public void addProperty(String name, ValueType type, Object value) throws PropertyException {
        if (hasProperty(name)) {
            removeProperty(name);
        }

        if (value != null) {
            try {
                NodeUtil.smartCheckout(node(), false);
                JcrProperty jp;
                try {
                    jp = new JcrProperty(node(), name, type, value);
                } catch (RRepositoryException e) {
                    throw new PropertyException("Internal error.", e);
                }
                properties.put(name, jp);
                node().save();
            } catch (RepositoryException e) {
                throw new PropertyException("Internal error.", e);
            }
        }
    }

    private void buildRelPath(StringBuilder sb, Node n) throws RepositoryException {
        if (!n.isNodeType(JcrNT.NT_PROJECT)) {
            buildRelPath(sb, n.getParent());
        }

        if (!n.isNodeType(JcrNT.NT_FILES)) {
            sb.append('/');
            sb.append(n.getName());
        }
    }

    public String getPath() throws RRepositoryException {
        StringBuilder sb = new StringBuilder(128);
        try {
            buildRelPath(sb, node());
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to get relative path.", e);
        }

        return sb.toString();
    }

    public Collection<org.openl.rules.common.Property> getProperties() {
        return properties.values();
    }

    public org.openl.rules.common.Property getProperty(String name) throws PropertyException {
        org.openl.rules.common.Property rp = properties.get(name);

        try {
            if (node().hasProperty(name)) {
                rp = new JcrProperty(node(), node().getProperty(name));
                properties.put(name, rp);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }

        if (rp == null) {
            throw new PropertyException("No such property ''{0}''.", null, name);
        }

        return rp;
    }

    public Map<String, Object> getProps() {
        return props;
    }

    public boolean hasProperty(String name) {
        if (properties.containsKey(name)) {
            return true;
        }
        try {
            return node().hasProperty(name);
        } catch (RepositoryException e) {
            return false;
        }
    }

    private void initProperties() throws RepositoryException {
        properties.clear();

        Node n = node();

        for (TablePropertyDefinition prop : bussinedDimensionProps) {
            String propName = prop.getName();

            if (n.hasProperty(propName)) {
                Property p = n.getProperty(propName);

                properties.put(propName, new JcrProperty(node(), p));
            }
        }
    }

    private void loadProps() throws RepositoryException {
        Node n = node();
        
        /*Set dimension props*/
        for (TablePropertyDefinition prop : bussinedDimensionProps) {
            String propName = prop.getName();
            if (n.hasProperty(propName)) {
                props.put(propName, getPropValueByType(propName));
            }
        }
        
        /*Set attrs*/
        for (int i = 1; i <= ArtefactProperties.PROPS_COUNT; i++) {
            String propName = ArtefactProperties.PROP_ATTRIBUTE + i;
            if (n.hasProperty(propName)) {
                props.put(propName, getPropValueByType(propName));
            }
        }
    }

    private Object getPropValueByType(String propName) throws RepositoryException {
        Node n = node();

        return getPropValueByType(propName, n);
    }

    private Object getPropValueByType(String propName, Node n) throws RepositoryException {

        Value value = n.getProperty(propName).getValue();
        Object propValue;
        int valueType = value.getType();
        switch (valueType) {
            case PropertyType.DATE:
                propValue = value.getDate().getTime();
                break;
            case PropertyType.LONG:
                propValue = new Date(value.getLong());
                break;
            case PropertyType.DOUBLE:
                propValue = value.getDouble();
                break;
            default:
                propValue = value.getString();
                break;
        }

        return propValue;
    }

    public org.openl.rules.common.Property removeProperty(String name) throws PropertyException {
        org.openl.rules.common.Property rp = getProperty(name);

        if (rp == null) {
            throw new PropertyException("No such property ''{0}''.", null, name);
        }

        Node n = node();
        try {
            NodeUtil.smartCheckout(n, false);
            n.getProperty(name).remove();
            n.save();
        } catch (RepositoryException e) {
            throw new PropertyException("Cannot remove property ''{0}''.", e, name);
        }
        return properties.remove(name);
    }

    private void setProperty(String propName, Object propValue) throws RRepositoryException {
        Node n = node();
        try {
            NodeUtil.smartCheckout(n, false);
            if (propValue instanceof Date) {
                n.setProperty(propName, ((Date) propValue).getTime());
            } else if (propValue instanceof Double) {
                n.setProperty(propName, (Double) propValue);
            } else {
                n.setProperty(propName, propValue.toString());
            }
            n.save();
        } catch (RepositoryException e) {
            throw new RRepositoryException("Cannot set property " + propName + ".", e);
        }
    }

    public void setProps(Map<String, Object> props) throws PropertyException {
        if (props == null) {
            return;
        }

        Set<String> propNames = props.keySet();
        for (String propName : propNames) {
            Object propValue = props.get(propName);
            try {
                setProperty(propName, propValue);
            } catch (RRepositoryException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        this.props = props;
    }

    public RLock getLock() throws RRepositoryException {
        if (isOldVersion()) {
            return RLock.NO_LOCK;
        } else {
            try {
                return new JcrLock(node());
            } catch (RepositoryException e) {
                throw new RRepositoryException("Failed to get lock.", e);
            }
        }
    }

    public boolean isLocked() throws RRepositoryException {
        return getLock().isLocked();
    }

    public void lock(CommonUser user) throws ProjectException {
        try {
            getLock().lock(user);
        } catch (RRepositoryException e) {
            throw new ProjectException("", e);
        }
    }

    public void unlock(CommonUser user) throws ProjectException {
        try {
            getLock().unlock(user);
        } catch (RRepositoryException e) {
            throw new ProjectException("", e);
        }
    }

    private void saveParent(Node node) throws RepositoryException {
        Node parent = node.getParent();
        if (parent.isNew()) {
            saveParent(parent);
        } else if (parent.isModified()) {
            parent.save();
        }
    }

    public void delete(CommonUser user) throws ProjectException {
        try {
            if (isLocked()) {
                unlock(user);
            }
            delete();
        } catch (Exception e) {
            throw new ProjectException("Failed to delete node.", e);
        }
    }

    public ArtefactPath getArtefactPath() {
        return path;
    }

    public boolean isFolder() {
        return false;
    }

    public ProjectVersion getVersion() {
        // FIXME
        RVersion rv = getActiveVersion();
        RepositoryVersionInfoImpl rvii = new RepositoryVersionInfoImpl(rv.getCreated(), rv.getCreatedBy().getUserName());

        return new RepositoryProjectVersionImpl(rv, rvii);
    }

    public List<ProjectVersion> getVersions() {
        // FIXME
        LinkedList<ProjectVersion> vers = new LinkedList<ProjectVersion>();
        try {
            List<RVersion> verHist = getVersionHistory();

            RVersion lastVersion = verHist.get(verHist.size() - 1);
            Date modifiedAt = lastVersion.getCreated();
            String modifiedBy = lastVersion.getCreatedBy().getUserName();

            for (RVersion rv : verHist) {
                vers.add(createRepositoryProjectVersion(rv, modifiedAt, modifiedBy));
            }

        } catch (RRepositoryException e) {
            log.error("Failed to get version history!", e);
            // TODO exception should be rethrown
        }
        return vers;
    }

    private RepositoryProjectVersionImpl createRepositoryProjectVersion(RVersion rv, Date modifiedAt, String modifiedBy) {
        RepositoryVersionInfoImpl rvii = new RepositoryVersionInfoImpl(rv.getCreated(), rv.getCreatedBy()
                .getUserName(), modifiedAt, modifiedBy);
        String versionComment = "";
        Map<String, Object> versionProperties = new HashMap<String, Object>();

        try {
            JcrEntityAPI entity = getVersion(rv);
            versionProperties = getVersionProps(rv);

            if (entity.hasProperty(ArtefactProperties.VERSION_COMMENT)) {
                versionComment = entity.getProperty(ArtefactProperties.VERSION_COMMENT).getString();
            }
        } catch (Exception e) {
            log.error("Failed to get version properties!", e);
            // TODO exception should be rethrown
        }

        return new RepositoryProjectVersionImpl(rv, rvii, versionComment, versionProperties);
    }

    @Override
    public int getVersionsCount() {
        try {
            return getVersionHistory().size();
        } catch (RRepositoryException e) {
            log.error("Failed to get version history!", e);
            // TODO exception should be rethrown
            return 0;
        }
    }

    @Override
    public ProjectVersion getVersion(int index) throws RRepositoryException {
        List<RVersion> verHist = getVersionHistory();

        RVersion lastVersion = verHist.get(verHist.size() - 1);
        Date modifiedAt = lastVersion.getCreated();
        String modifiedBy = lastVersion.getCreatedBy().getUserName();

        return createRepositoryProjectVersion(verHist.get(index), modifiedAt, modifiedBy);
    }

    public LockInfo getLockInfo() {
        // FIXME
        try {
            return getLock();
        } catch (RRepositoryException e) {
            log.error("getLockInfo", e);
            return RLock.NO_LOCK;
        }
    }

    private Map<String, Object> getVersionProps(CommonVersion version) throws PropertyException, RepositoryException {
        Node frozenNode = NodeUtil.getNode4Version(node(), version);
        Map<String, Object> versProps = new HashMap<String, Object>();

        for (TablePropertyDefinition prop : bussinedDimensionProps) {
            String propName = prop.getName();

            if (frozenNode.hasProperty(propName)) {
                Value value = frozenNode.getProperty(propName).getValue();
                Object propValue;
                int valueType = value.getType();
                switch (valueType) {
                    case PropertyType.DATE:
                        propValue = value.getDate().getTime();
                        break;
                    case PropertyType.LONG:
                        propValue = new Date(value.getLong());
                        break;
                    case PropertyType.DOUBLE:
                        propValue = value.getDouble();
                        break;
                    default:
                        propValue = value.getString();
                        break;
                }
                versProps.put(propName, propValue);
            }
        }
        return versProps;
    }

    public void commit(CommonUser user, int revision) throws ProjectException {
        try {
            Node n = node();
            saveParent(n);

            NodeUtil.smartCheckout(n, false);
            version.set(revision);
            version.updateVersion(n);
            n.setProperty(ArtefactProperties.PROP_MODIFIED_BY, user.getUserName());

            if (NodeUtil.isVersionable(n)) {
                log.info("Checking in... {}", n.getPath());
                n.save();
                n.checkin();
            } else {
                n.save();
                log.info("Saving... {}", n.getPath());
            }
        } catch (RepositoryException e) {
            throw new ProjectException("Failed to check in artefact ''{0}''!", e, getPath());
        }
    }

    public JcrEntityAPI getVersion(CommonVersion version) throws RRepositoryException {
        try {
            Node frozenNode = NodeUtil.getNode4Version(node(), version);
            return new JcrEntityAPI(frozenNode, getTransactionManager(), getArtefactPath(), true);
        } catch (RepositoryException e) {
            throw new RRepositoryException("Failed to get version for node.", e);
        }
    }

    public void removeAllProperties() throws PropertyException {
        List<String> propertyNames = new ArrayList<String>(properties.keySet());
        for (String propertyName : propertyNames) {
            removeProperty(propertyName);
        }
        
        
        
        /*Delete all posible props*/
        try {
            for (TablePropertyDefinition prop : bussinedDimensionProps) {
                String propName = prop.getName();

                if (node().hasProperty(propName)) {
                    removeProperty(propName);
                }
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }

        try {
            for (int i = 1; i <= ArtefactProperties.PROPS_COUNT; i++) {
                String propName = ArtefactProperties.PROP_ATTRIBUTE + i;

                if (node().hasProperty(propName)) {
                    removeProperty(propName);
                }
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }

        properties.clear();
    }

    public boolean isModified() {
        //FIXME always false
        return node().isModified();
    }

    public RTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public UserTransaction createTransaction() throws RRepositoryException {
        return getTransactionManager().getTransaction();
    }

    @Override
    public Map<String, InheritedProperty> getInheritedProps() {
        Map<String, InheritedProperty> inhProps = new HashMap<String, InheritedProperty>();

        try {
            if (node().getDepth() > 3 && node().getParent() != null) {
                inhProps = getParentProps(node().getParent());
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }

        return inhProps;
    }

    private Map<String, InheritedProperty> getParentProps(Node n) {
        Map<String, InheritedProperty> inhProps = new HashMap<String, InheritedProperty>();

        try {
            if (n.getDepth() > 3 && n.getParent() != null) {
                inhProps = getParentProps(n.getParent());
            }

            for (TablePropertyDefinition prop : bussinedDimensionProps) {
                String propName = prop.getName();
                if (n.hasProperty(propName)) {
                    InheritedProperty inhProp;

                    if (n instanceof JcrFolderAPI) {
                        inhProp = new InheritedProperty(getPropValueByType(propName, n), ArtefactType.FOLDER, n.getName());
                    } else {
                        inhProp = new InheritedProperty(getPropValueByType(propName, n), ArtefactType.PROJECT, n.getName());
                    }

                    inhProps.put(propName, inhProp);
                }
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }

        return inhProps;
    }

    @Override
    public void clearModifyStatus() {

    }

}
