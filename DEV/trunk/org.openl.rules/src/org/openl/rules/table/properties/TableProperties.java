package org.openl.rules.table.properties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;
import org.openl.rules.table.properties.InheritanceLevel;
import org.openl.rules.table.properties.inherit.InheritanceLevelChecker;
import org.openl.rules.table.properties.inherit.InvalidPropertyLevelException;
import org.openl.types.IOpenClass;
import org.openl.types.impl.DynamicObject;
import org.openl.types.java.JavaOpenClass;
import org.openl.util.EnumUtils;

public class TableProperties extends DynamicObject implements ITableProperties {

    /**
     * Table section that contains properties in appropriate table in data source.
     */
    private ILogicalTable propertySection;

    private ILogicalTable modulePropertiesTable;

    private ILogicalTable categoryPropertiesTable;

    private Map<String, Object> categoryProperties = new HashMap<String, Object>();

    private Map<String, Object> moduleProperties = new HashMap<String, Object>();

    private Map<String, Object> defaultProperties = new HashMap<String, Object>();

    /**
     * Check if property can be overriden on table level.
     * TODO: Decide what to do when it is impossible to override. 
     * 
     * @param name name of the property to check.
     * @return <code>TRUE</code> if property can be defined on TABLE level. 
     */
    private boolean canBeOverridenOnTableLevel(String name) {
        boolean result = false;
        try {
            InheritanceLevelChecker.checkPropertyLevel(InheritanceLevel.TABLE, name);
            result = true;
        } catch(InvalidPropertyLevelException ex) {
            // TODO: message to UI that current property can`t be overriden in table level.
        }
        return result;
    }

    /**
     * The result <code>{@link Map}</code> will contain all pairs from downLevelProperties and pairs from 
     * upLevelProperties that are not defined in downLevelProperties.
     * 
     * @param downLevelProperties properties that are on the down level. 
     * @param upLevelProperties properties that are on the up level. 
     *  
     * @return
     */
    private Map<String, Object> mergeLevelProperties(Map<String, Object> downLevelProperties, 
            Map<String, Object> upLevelProperties) {
        Map<String, Object> resultProperties = downLevelProperties;
        for (Entry<String, Object> upLevelProperty : upLevelProperties.entrySet()) {
            String upLevelPropertyName = upLevelProperty.getKey();
            Object upLevelPropertyValue = upLevelProperty.getValue();
            
            if (!downLevelProperties.containsKey(upLevelPropertyName)) {
                resultProperties.put(upLevelPropertyName, upLevelPropertyValue);
            }            
        }
        return resultProperties;
    }

	@Override
    public IOpenClass getType() {
        return JavaOpenClass.getOpenClass(getClass());
    }

    @Override
    public void setType(IOpenClass type) {
        throw new UnsupportedOperationException();
    }

    // <<< INSERT >>>
	public java.lang.String getName() {
		return (java.lang.String) getPropertyValue("name"); 
	}
	public void setName(java.lang.String name) {
		setFieldValue("name", name);
	}	
	public java.lang.String getCategory() {
		return (java.lang.String) getPropertyValue("category"); 
	}
	public void setCategory(java.lang.String category) {
		setFieldValue("category", category);
	}	
	public java.lang.String getDescription() {
		return (java.lang.String) getPropertyValue("description"); 
	}
	public void setDescription(java.lang.String description) {
		setFieldValue("description", description);
	}	
	public java.lang.String[] getTags() {
		return (java.lang.String[]) getPropertyValue("tags"); 
	}
	public void setTags(java.lang.String[] tags) {
		setFieldValue("tags", tags);
	}	
	public java.util.Date getEffectiveDate() {
		return (java.util.Date) getPropertyValue("effectiveDate"); 
	}
	public void setEffectiveDate(java.util.Date effectiveDate) {
		setFieldValue("effectiveDate", effectiveDate);
	}	
	public java.util.Date getExpirationDate() {
		return (java.util.Date) getPropertyValue("expirationDate"); 
	}
	public void setExpirationDate(java.util.Date expirationDate) {
		setFieldValue("expirationDate", expirationDate);
	}	
	public java.lang.String getCreatedBy() {
		return (java.lang.String) getPropertyValue("createdBy"); 
	}
	public void setCreatedBy(java.lang.String createdBy) {
		setFieldValue("createdBy", createdBy);
	}	
	public java.util.Date getCreatedOn() {
		return (java.util.Date) getPropertyValue("createdOn"); 
	}
	public void setCreatedOn(java.util.Date createdOn) {
		setFieldValue("createdOn", createdOn);
	}	
	public java.lang.String getModifiedBy() {
		return (java.lang.String) getPropertyValue("modifiedBy"); 
	}
	public void setModifiedBy(java.lang.String modifiedBy) {
		setFieldValue("modifiedBy", modifiedBy);
	}	
	public java.util.Date getModifyOn() {
		return (java.util.Date) getPropertyValue("modifyOn"); 
	}
	public void setModifyOn(java.util.Date modifyOn) {
		setFieldValue("modifyOn", modifyOn);
	}	
	public java.lang.String getBuildPhase() {
		return (java.lang.String) getPropertyValue("buildPhase"); 
	}
	public void setBuildPhase(java.lang.String buildPhase) {
		setFieldValue("buildPhase", buildPhase);
	}	
	public java.lang.String getValidateDT() {
		return (java.lang.String) getPropertyValue("validateDT"); 
	}
	public void setValidateDT(java.lang.String validateDT) {
		setFieldValue("validateDT", validateDT);
	}	
	public java.lang.String getLob() {
		return (java.lang.String) getPropertyValue("lob"); 
	}
	public void setLob(java.lang.String lob) {
		setFieldValue("lob", lob);
	}	
	public java.lang.String getUsregion() {
		return (java.lang.String) getPropertyValue("usregion"); 
	}
	public void setUsregion(java.lang.String usregion) {
		setFieldValue("usregion", usregion);
	}	
	public org.openl.rules.enumeration.CountriesEnum[] getCountry() {
		return (org.openl.rules.enumeration.CountriesEnum[]) getPropertyValue("country"); 
	}
	public void setCountry(org.openl.rules.enumeration.CountriesEnum[] country) {
		setFieldValue("country", country);
	}	
	public org.openl.rules.enumeration.CurrenciesEnum getCurrency() {
		return (org.openl.rules.enumeration.CurrenciesEnum) getPropertyValue("currency"); 
	}
	public void setCurrency(org.openl.rules.enumeration.CurrenciesEnum currency) {
		setFieldValue("currency", currency);
	}	
	public java.lang.String getLang() {
		return (java.lang.String) getPropertyValue("lang"); 
	}
	public void setLang(java.lang.String lang) {
		setFieldValue("lang", lang);
	}	
	public java.lang.String getState() {
		return (java.lang.String) getPropertyValue("state"); 
	}
	public void setState(java.lang.String state) {
		setFieldValue("state", state);
	}	
	public java.lang.String getRegion() {
		return (java.lang.String) getPropertyValue("region"); 
	}
	public void setRegion(java.lang.String region) {
		setFieldValue("region", region);
	}	
	public java.lang.String getVersion() {
		return (java.lang.String) getPropertyValue("version"); 
	}
	public void setVersion(java.lang.String version) {
		setFieldValue("version", version);
	}	
	public java.lang.Boolean getActive() {
		return (java.lang.Boolean) getPropertyValue("active"); 
	}
	public void setActive(java.lang.Boolean active) {
		setFieldValue("active", active);
	}	
	public java.lang.Boolean getFailOnMiss() {
		return (java.lang.Boolean) getPropertyValue("failOnMiss"); 
	}
	public void setFailOnMiss(java.lang.Boolean failOnMiss) {
		setFieldValue("failOnMiss", failOnMiss);
	}	
	public java.lang.Boolean getReturnOnMiss() {
		return (java.lang.Boolean) getPropertyValue("returnOnMiss"); 
	}
	public void setReturnOnMiss(java.lang.Boolean returnOnMiss) {
		setFieldValue("returnOnMiss", returnOnMiss);
	}	
	public java.lang.String getScope() {
		return (java.lang.String) getPropertyValue("scope"); 
	}
	public void setScope(java.lang.String scope) {
		setFieldValue("scope", scope);
	}	
	// <<< END INSERT >>>

	/**
	 * {@inheritDoc}
	 */
    public Object getPropertyValue(String key) {        
        return getPropertiesAll().get(key);
    }

    /**
     * {@inheritDoc}
     */
    public String getPropertyValueAsString(String key) {
        String result = null;
        Object propValue = getPropertyValue(key);
        if (propValue != null) {
            if (propValue instanceof Date) {
                String format = TablePropertyDefinitionUtils.getPropertyByName(key).getFormat();
                if (format != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                    result = dateFormat.format((Date) propValue);
                }
            } else if (EnumUtils.isEnum(propValue)) {
                  result = ((Enum<?>) propValue).name();
            } else if (EnumUtils.isEnumArray(propValue)) {
                Object[] enums = (Object[]) propValue;
                String[] names = EnumUtils.getNames(enums);
                result = StringUtils.join(names, ",");
            } else {
                result = propValue.toString();
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public InheritanceLevel getPropertyLevelDefinedOn(String propertyName) {
        InheritanceLevel result = null;
        if (getPropertiesDefinedInTable().containsKey(propertyName)) {
            result = InheritanceLevel.TABLE;
        } else if (getPropertiesAppliedForCategory().containsKey(propertyName)) {
            result = InheritanceLevel.CATEGORY;
        } else if (getPropertiesAppliedForModule().containsKey(propertyName)) {
            result = InheritanceLevel.MODULE;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPropertyAppliedByDefault(String propertyName) {
        boolean result = false;
        if (getPropertyLevelDefinedOn(propertyName) == null && defaultProperties.containsKey(propertyName)) {
            result = true;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public ILogicalTable getPropertiesSection() {
       return propertySection;
    }

    public void setPropertiesSection(ILogicalTable propertySection) {
        this.propertySection = propertySection;
    }

    public ILogicalTable getModulePropertiesTable() {
        return modulePropertiesTable;
    }

    public void setModulePropertiesTable(ILogicalTable modulePropertiesTable) {
        this.modulePropertiesTable = modulePropertiesTable;
    }

    public ILogicalTable getCategoryPropertiesTable() {
        return categoryPropertiesTable;
    }

    public void setCategoryPropertiesTable(ILogicalTable categoryPropertiesTable) {
        this.categoryPropertiesTable = categoryPropertiesTable;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getPropertiesAll() {     
      Map<String, Object> tableAndCategoryProp = mergeLevelProperties(super.getFieldValues(), categoryProperties);
      Map<String, Object> tableAndCategoryAndModuleProp = mergeLevelProperties(tableAndCategoryProp, moduleProperties);     
      return mergeLevelProperties(tableAndCategoryAndModuleProp, defaultProperties); 
    }

    @Override
    public void setFieldValue(String name, Object value) {  
        canBeOverridenOnTableLevel(name);
        super.setFieldValue(name, value);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getPropertiesDefinedInTable() {    
        return super.getFieldValues();
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getPropertiesDefinedInTableIgnoreSystem() {
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> propDefinedInTable = getPropertiesDefinedInTable();
        for (Map.Entry<String, Object> property : propDefinedInTable.entrySet()) {
            String propName = property.getKey();
            TablePropertyDefinition propertyDefinition = TablePropertyDefinitionUtils.getPropertyByName(propName);
            if (!propertyDefinition.isSystem()) {
                result.put(propName, property.getValue());                
            }
        }
        return result;
    }

    public void setPropertiesAppliedForCategory(Map<String, Object> categoryProperties) {
        this.categoryProperties = categoryProperties;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getPropertiesAppliedForCategory() {
        return categoryProperties;
    }

    public void setPropertiesAppliedForModule(Map<String, Object> moduleProperties) {
        this.moduleProperties = moduleProperties;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getPropertiesAppliedForModule() {       
        return moduleProperties;
    }

    public void setPropertiesAppliedByDefault(Map<String, Object> defaultProperties) {
        this.defaultProperties = defaultProperties;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getPropertiesAppliedByDefault() {
        return defaultProperties;
    }

}
