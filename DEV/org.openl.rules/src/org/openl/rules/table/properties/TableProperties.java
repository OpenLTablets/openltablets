package org.openl.rules.table.properties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;
import org.openl.rules.table.properties.inherit.InheritanceLevel;
import org.openl.rules.table.properties.inherit.PropertiesChecker;
import org.openl.types.IOpenClass;
import org.openl.types.impl.DynamicObject;
import org.openl.types.java.JavaOpenClass;
import org.openl.util.ArrayTool;
import org.openl.util.EnumUtils;

public class TableProperties extends DynamicObject implements ITableProperties {
    
    private String currentTableType;
    /**
     * Table section that contains properties in appropriate table in data
     * source.
     */
    private ILogicalTable propertySection;

    private ILogicalTable modulePropertiesTable;

    private ILogicalTable categoryPropertiesTable;

    private Map<String, Object> categoryProperties = new HashMap<String, Object>();

    private Map<String, Object> externalModuleProperties = new HashMap<String, Object>();

    private Map<String, Object> moduleProperties = new HashMap<String, Object>();

    private Map<String, Object> defaultProperties = new HashMap<String, Object>();
    
    /**
     * The result <code>{@link Map}</code> will contain all pairs from
     * downLevelProperties and pairs from upLevelProperties that are not defined
     * in downLevelProperties. Ignore properties from upper level that can`t be defined for current table type.
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
            
            if (PropertiesChecker.isPropertySuitableForTableType(upLevelPropertyName, currentTableType)) {
                if (!downLevelProperties.containsKey(upLevelPropertyName)) {
                    resultProperties.put(upLevelPropertyName, upLevelPropertyValue);
                }
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
	public java.util.Date getStartRequestDate() {
		return (java.util.Date) getPropertyValue("startRequestDate"); 
	}
	public void setStartRequestDate(java.util.Date startRequestDate) {
		setFieldValue("startRequestDate", startRequestDate);
	}	
	public java.util.Date getEndRequestDate() {
		return (java.util.Date) getPropertyValue("endRequestDate"); 
	}
	public void setEndRequestDate(java.util.Date endRequestDate) {
		setFieldValue("endRequestDate", endRequestDate);
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
	public java.util.Date getModifiedOn() {
		return (java.util.Date) getPropertyValue("modifiedOn"); 
	}
	public void setModifiedOn(java.util.Date modifiedOn) {
		setFieldValue("modifiedOn", modifiedOn);
	}	
	public java.lang.String getId() {
		return (java.lang.String) getPropertyValue("id"); 
	}
	public void setId(java.lang.String id) {
		setFieldValue("id", id);
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
	public org.openl.rules.enumeration.UsRegionsEnum[] getUsregion() {
		return (org.openl.rules.enumeration.UsRegionsEnum[]) getPropertyValue("usregion"); 
	}
	public void setUsregion(org.openl.rules.enumeration.UsRegionsEnum[] usregion) {
		setFieldValue("usregion", usregion);
	}	
	public org.openl.rules.enumeration.CountriesEnum[] getCountry() {
		return (org.openl.rules.enumeration.CountriesEnum[]) getPropertyValue("country"); 
	}
	public void setCountry(org.openl.rules.enumeration.CountriesEnum[] country) {
		setFieldValue("country", country);
	}	
	public org.openl.rules.enumeration.CurrenciesEnum[] getCurrency() {
		return (org.openl.rules.enumeration.CurrenciesEnum[]) getPropertyValue("currency"); 
	}
	public void setCurrency(org.openl.rules.enumeration.CurrenciesEnum[] currency) {
		setFieldValue("currency", currency);
	}	
	public org.openl.rules.enumeration.LanguagesEnum[] getLang() {
		return (org.openl.rules.enumeration.LanguagesEnum[]) getPropertyValue("lang"); 
	}
	public void setLang(org.openl.rules.enumeration.LanguagesEnum[] lang) {
		setFieldValue("lang", lang);
	}	
	public org.openl.rules.enumeration.UsStatesEnum[] getState() {
		return (org.openl.rules.enumeration.UsStatesEnum[]) getPropertyValue("state"); 
	}
	public void setState(org.openl.rules.enumeration.UsStatesEnum[] state) {
		setFieldValue("state", state);
	}	
	public org.openl.rules.enumeration.RegionsEnum[] getRegion() {
		return (org.openl.rules.enumeration.RegionsEnum[]) getPropertyValue("region"); 
	}
	public void setRegion(org.openl.rules.enumeration.RegionsEnum[] region) {
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
	public java.lang.String getScope() {
		return (java.lang.String) getPropertyValue("scope"); 
	}
	public void setScope(java.lang.String scope) {
		setFieldValue("scope", scope);
	}	
	public java.lang.String getDatatypePackage() {
		return (java.lang.String) getPropertyValue("datatypePackage"); 
	}
	public void setDatatypePackage(java.lang.String datatypePackage) {
		setFieldValue("datatypePackage", datatypePackage);
	}	
	public java.lang.String[] getTransaction() {
		return (java.lang.String[]) getPropertyValue("transaction"); 
	}
	public void setTransaction(java.lang.String[] transaction) {
		setFieldValue("transaction", transaction);
	}	
	public java.lang.String[] getCustom1() {
		return (java.lang.String[]) getPropertyValue("custom1"); 
	}
	public void setCustom1(java.lang.String[] custom1) {
		setFieldValue("custom1", custom1);
	}	
	public java.lang.String[] getCustom2() {
		return (java.lang.String[]) getPropertyValue("custom2"); 
	}
	public void setCustom2(java.lang.String[] custom2) {
		setFieldValue("custom2", custom2);
	}	
	public java.lang.Boolean getCacheable() {
		return (java.lang.Boolean) getPropertyValue("cacheable"); 
	}
	public void setCacheable(java.lang.Boolean cacheable) {
		setFieldValue("cacheable", cacheable);
	}	
	public org.openl.rules.enumeration.RecalculateEnum getRecalculate() {
		return (org.openl.rules.enumeration.RecalculateEnum) getPropertyValue("recalculate"); 
	}
	public void setRecalculate(org.openl.rules.enumeration.RecalculateEnum recalculate) {
		setFieldValue("recalculate", recalculate);
	}	
	public java.lang.String getPrecision() {
		return (java.lang.String) getPropertyValue("precision"); 
	}
	public void setPrecision(java.lang.String precision) {
		setFieldValue("precision", precision);
	}	
	public java.lang.Boolean getAutoType() {
		return (java.lang.Boolean) getPropertyValue("autoType"); 
	}
	public void setAutoType(java.lang.Boolean autoType) {
		setFieldValue("autoType", autoType);
	}	
// <<< END INSERT >>>

    /**
     * {@inheritDoc}
     */
    public Object getPropertyValue(String key) {
        return getAllProperties().get(key);
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

                if (!ArrayTool.isEmpty(enums)) {

                    String[] names = EnumUtils.getNames(enums);
                    result = StringUtils.join(names, ",");
                } else {
                    result = "";
                }
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
        }else if (getExternalPropertiesAppliedForModule().containsKey(propertyName)) {
            result = InheritanceLevel.EXTERNAL;
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

    public ILogicalTable getInheritedPropertiesTable(InheritanceLevel inheritanceLevel) {
    	if (InheritanceLevel.MODULE.equals(inheritanceLevel)) {
    		return modulePropertiesTable;
    	} else if (InheritanceLevel.CATEGORY.equals(inheritanceLevel)) {
    		return categoryPropertiesTable;
    	}
    	return null;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getAllProperties() {
        Map<String, Object> tableAndCategoryProp = mergeLevelProperties(super.getFieldValues(), categoryProperties);
        Map<String, Object> tableAndCategoryAndModuleProp = mergeLevelProperties(tableAndCategoryProp, moduleProperties);
        Map<String, Object> tableAndCategoryAndModuleAndExteranlProp = mergeLevelProperties(tableAndCategoryAndModuleProp, externalModuleProperties);
        return mergeLevelProperties(tableAndCategoryAndModuleAndExteranlProp, defaultProperties);
    }

    @Override
    public void setFieldValue(String name, Object value) {
        PropertiesChecker.isPropertySuitableForLevel(InheritanceLevel.TABLE, name);
        PropertiesChecker.isPropertySuitableForTableType(name, currentTableType);
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

    public void setCurrentTableType(String currentTableType) {
        this.currentTableType = currentTableType;
    }

    public String getCurrentTableType() {
        return currentTableType;
    }

    public Map<String, Object> getExternalPropertiesAppliedForModule() {
        return externalModuleProperties;
    }

    public void setExternalPropertiesAppliedForModule(Map<String, Object> moduleProperties) {
        this.externalModuleProperties = moduleProperties;
    }
    
}
