package org.openl.rules.workspace.lw.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.rules.common.MsgHelper;
import org.openl.rules.workspace.abstracts.ArtefactPath;
import org.openl.rules.workspace.abstracts.ProjectArtefact;
import org.openl.rules.workspace.abstracts.ProjectException;
import org.openl.rules.workspace.lw.LocalProjectArtefact;
import org.openl.rules.workspace.props.Property;
import org.openl.rules.workspace.props.PropertyException;
import org.openl.rules.workspace.props.impl.PropertiesContainerImpl;
import org.openl.rules.workspace.repository.RulesRepositoryArtefact;

public abstract class LocalProjectArtefactImpl implements LocalProjectArtefact {
    private static final Log log = LogFactory.getLog(LocalProjectArtefactImpl.class);

    private String name;
    private ArtefactPath path;
    private File location;

    private Date effectiveDate;
    private Date expirationDate;
    private String lineOfBusiness;

    private PropertiesContainerImpl properties;

    private boolean isNew;
    private boolean isChanged;

    public LocalProjectArtefactImpl(String name, ArtefactPath path, File location) {
        this.name = name;
        this.path = path;
        this.location = location;

        properties = new PropertiesContainerImpl();
    }

    public String getName() {
        return name;
    }

    public ArtefactPath getArtefactPath() {
        return path;
    }

    public boolean hasProperty(String name) {
        return properties.hasProperty(name);
    }

    public Property getProperty(String name) throws PropertyException {
        return properties.getProperty(name);
    }

    public Collection<Property> getProperties() {
        return properties.getProperties();
    }

    public void addProperty(Property property) throws PropertyException {
        properties.addProperty(property);
    }

    public Property removeProperty(String name) throws PropertyException {
        return properties.removeProperty(name);
    }

    public boolean isNew() {
        return isNew;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void remove() {
        File f = getLocation();
        if (!f.exists()) {
            String msg = MsgHelper.format("No file ''{0}'', nothing to remove.", f.getAbsolutePath());
            log.debug(msg);
        } else if (!f.delete()) {
            String msg = MsgHelper.format("Failed to remove file ''{0}''!", f.getAbsolutePath());
            log.warn(msg);
        }
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
    
    public void setEffectiveDate(Date date) throws ProjectException {
        effectiveDate = date;
    }
    
    public void setExpirationDate(Date date) throws ProjectException {
        expirationDate = date;
    }

    public void setLineOfBusiness(String lineOfBusiness) throws ProjectException {
        this.lineOfBusiness = lineOfBusiness;
    }
    
    // --- protected

    protected void setNew(boolean aNew) {
        isNew = aNew;
    }

    protected void setChanged(boolean changed) {
        isChanged = changed;
    }
    
    protected void resetNewAndChanged() {
	setNew(false);
	setChanged(false);
    }

    protected File getLocation() {
        return location;
    }
    
    protected void downloadArtefact(ProjectArtefact artefact) throws ProjectException {
        if (artefact instanceof RulesRepositoryArtefact) {
            RulesRepositoryArtefact rulesArtefact = (RulesRepositoryArtefact) artefact;
            
            effectiveDate = rulesArtefact.getEffectiveDate();
            expirationDate = rulesArtefact.getExpirationDate();
            lineOfBusiness = rulesArtefact.getLineOfBusiness();
        }
    }
    
    public StateHolder getState() {
        ArtefactStateHolder state = new ArtefactStateHolder();
        
        state.isNew = isNew;
        state.isChanged = isChanged;
        
        state.effectiveDate = effectiveDate;
        state.expirationDate = expirationDate;
        state.LOB = lineOfBusiness;
        
        state.properties = new ArrayList<Property>(properties.getProperties());
        
        return state;
    }
    
    public void setState(StateHolder aState) throws PropertyException {
        ArtefactStateHolder state = (ArtefactStateHolder) aState;
        
        effectiveDate = state.effectiveDate;
        expirationDate = state.expirationDate;
        lineOfBusiness = state.LOB;
        
        properties.removeAll();
        for (Property prop : state.properties) {
            properties.addProperty(prop);
        }

        isNew = state.isNew;
        isChanged = state.isChanged;
    }

    private static class ArtefactStateHolder implements StateHolder {
        private static final long serialVersionUID = 1049629652852513808L;

        boolean isNew;
        boolean isChanged;
        
        Date effectiveDate;
        Date expirationDate;
        String LOB;
        
        Collection<Property> properties;
    }
}
