package org.openl.rules.project.abstraction;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openl.rules.common.ArtefactPath;
import org.openl.rules.common.CommonUser;
import org.openl.rules.common.InheritedProperty;
import org.openl.rules.common.LockInfo;
import org.openl.rules.common.ProjectException;
import org.openl.rules.common.ProjectVersion;
import org.openl.rules.common.PropertiesContainer;
import org.openl.rules.common.Property;
import org.openl.rules.common.PropertyException;
import org.openl.rules.common.RulesRepositoryArtefact;
import org.openl.rules.common.impl.PropertyImpl;
import org.openl.rules.common.impl.RepositoryProjectVersionImpl;
import org.openl.rules.repository.api.ArtefactAPI;
import org.openl.rules.repository.api.ArtefactProperties;

public class AProjectArtefact implements PropertiesContainer, RulesRepositoryArtefact {
    private ArtefactAPI impl;
    private AProject project;

    public AProjectArtefact(ArtefactAPI api, AProject project) {
        this.impl = api;
        this.project = project;
    }

    public AProject getProject() {
        return project;
    }

    public ArtefactAPI getAPI() {
        return impl;
    }

    public void setAPI(ArtefactAPI impl) {
        this.impl = impl;
    }

    public Map<String, Object> getProps() {
        return getAPI().getProps();
    }

    public Map<String, InheritedProperty> getInheritedProps() {
        return getAPI().getInheritedProps();
    }

    public void setProps(Map<String, Object> props) throws PropertyException {
        getAPI().setProps(props);
    }

    public void addProperty(Property property) throws PropertyException {
        if (property.getValue() == null) {
            if (hasProperty(property.getName())) {
                removeProperty(property.getName());
            }
        } else {
            getAPI().addProperty(property.getName(), property.getType(), property.getValue());
        }
    }

    public Collection<Property> getProperties() {
        return getAPI().getProperties();
    }

    public Property getProperty(String name) throws PropertyException {
        return getAPI().getProperty(name);
    }

    public boolean hasProperty(String name) {
        return getAPI().hasProperty(name);
    }

    public Property removeProperty(String name) throws PropertyException {
        return getAPI().removeProperty(name);
    }

    public void delete() throws ProjectException {
        getAPI().delete(null);
    }

    public ArtefactPath getArtefactPath() {
        return getAPI().getArtefactPath();
    }

    public String getName() {
        return getAPI().getName();
    }

    public boolean isFolder() {
        return getAPI().isFolder();
    }

    // current version
    public ProjectVersion getVersion() {
        return getAPI().getVersion();
    }

    public ProjectVersion getLastVersion() {
        List<ProjectVersion> versions = getVersions();
        if (versions.size() == 0) {
            return new RepositoryProjectVersionImpl(0, null);
        }
        ProjectVersion max = versions.get(versions.size() - 1);
        return max;
    }

    public List<ProjectVersion> getVersions() {
        return getAPI().getVersions();
    }

    public void update(AProjectArtefact artefact, CommonUser user) throws ProjectException {
        /*
        try {
            setProps(artefact.getProps());
        } catch (PropertyException e1) {
            // TODO log
            e1.printStackTrace();
        }*/
        try {
            getAPI().removeAllProperties();
            
            setProps(artefact.getProps());
            
            // set all properties
            for (Property property : artefact.getProperties()) {
                addProperty(property);
            }
        } catch (PropertyException e) {
            // TODO log
            e.printStackTrace();
        }
        refresh();
    }

    /**
     * As usual update but this update will use only artefacts which is modified.
     * 
     * @param artefact A source artefact to extract data from.
     * @throws ProjectException
     */
    public void smartUpdate(AProjectArtefact artefact, CommonUser user) throws ProjectException {
        if (artefact.isModified()) {
            try {
                getAPI().removeAllProperties();
                setProps(artefact.getProps());
            
                // set all properties
                for (Property property : artefact.getProperties()) {
                    if(!artefact.getProps().containsKey(property.getName())){
                        addProperty(property);
                    }
                }
            } catch (PropertyException e) {
                // TODO log
                e.printStackTrace();
            }
            refresh();
        }
    }

    protected void commit(CommonUser user) throws ProjectException {
        getAPI().commit(user, getProject().getVersion().getRevision() + 1);
    }

    public void refresh() {
        // TODO
    }

    public void lock(CommonUser user) throws ProjectException {
        getAPI().lock(user);
    }

    public void unlock(CommonUser user) throws ProjectException {
        getAPI().unlock(user);
    }

    public boolean isLocked() {
        return getLockInfo().isLocked();
    }

    public boolean isLockedByUser(CommonUser user) {
        if (isLocked()) {
            CommonUser lockedBy = getLockInfo().getLockedBy();
            if (lockedBy.getUserName().equals(user.getUserName())) {
                return true;
            }
        }
        return false;
    }

    public LockInfo getLockInfo() {
        return getAPI().getLockInfo();
    }
    
    public boolean isModified(){
        return impl.isModified();
    }

    public void setVersionComment(String versionComment) throws PropertyException {
        addProperty(new PropertyImpl(ArtefactProperties.VERSION_COMMENT, versionComment));
    }
    
    public String getVersionComment() {
        try {
            return getProperty(ArtefactProperties.VERSION_COMMENT).getString();
        } catch (PropertyException e) {
            return null;
        }
    }
}
