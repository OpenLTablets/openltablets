package org.openl.rules.table.properties;

import java.util.Map;

import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.properties.def.DefaultPropertyDefinitions;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;
import org.openl.rules.table.properties.InheritanceLevel;

public interface ITableProperties {
    
    /**
     * <code>{@link Map}</code> of properties that includes all properties for current table. It includes:
     *          - all properties physically defined in table with system ones;
     *          - inherited properties from category and module scopes;
     *          - properties set by default;
     * 
     * @return <code>{@link Map}</code> of all properties relevant to current table.
     */
    Map<String, Object> getPropertiesAll();
    
    /**
     * Gets the <code>{@link Map}</code> of properties with name as key and value as value, this map contains all 
     * properties defined in source table. No inherited and no default properties.
     * 
     * @return <code>{@link Map}</code> of properties defined in table.
     */
    Map<String, Object> getPropertiesDefinedInTable();
    
    /**
     * Gets the <code>{@link Map}</code> of properties with name as key and value as value, this map contains 
     * properties defined in source table, excluding system properties. No inherited and no default properties.
     * To find out which property is system see property definitions 
     * {@link TablePropertyDefinitionUtils#getSystemProperties()}.
     * 
     * @return <code>{@link Map}</code> of properties defined in table excluding system properties.
     */
    Map<String, Object> getPropertiesDefinedInTableIgnoreSystem();
    
    /**
     * <code>{@link Map}</code> of properties applied to the category this table belongs to.
     * 
     * @return <code>{@link Map}</code> of properties applied to the category this table belongs to.
     */
    Map<String, Object> getPropertiesAppliedForCategory();
    
    /**
     * <code>{@link Map}</code> of properties applied to the module this table belongs to.
     * 
     * @return <code>{@link Map}</code> of properties applied to the module this table belongs to.
     */
    Map<String, Object> getPropertiesAppliedForModule();
    
    /**
     * <code>{@link Map}</code> of properties that must be set by default. Default properties are set to the table when 
     * there is no such property defined on TABLE, CATEGORY and MODULE levels. 
     * To find out which property is default see property definitions 
     * {@link TablePropertyDefinitionUtils#getSystemProperties()}.
     * 
     * @return <code>{@link Map}</code> of properties that must be set by default.
     */
    Map<String, Object> getPropertiesAppliedByDefault();
    
    /**
     * Gets the value of the property by its name.
     * 
     * @param propertyName Property name.
     * 
     * @return Property value.
     */
    Object getPropertyValue(String propertyName);
    
    /**
     * Returns the value of the property as <code>String</code>. If the current property value is of 
     * <code>Date</code> type, gets the format of date from {@link DefaultPropertyDefinitions}.
     * @param propertyName Name of the property.
     * 
     * @return Value formatted to string. <code>Null</code> when there is
     * no property with such name.
     */
    String getPropertyValueAsString(String propertyName);
    
    /**
     * Gets the logical table of the properties defined in table.
     */
    ILogicalTable getPropertiesSection();

    ILogicalTable getModulePropertiesTable();
    void setModulePropertiesTable(ILogicalTable modulePropertiesTable);

    ILogicalTable getCategoryPropertiesTable();
    void setCategoryPropertiesTable(ILogicalTable categoryPropertiesTable);

    /**
     * Goes through the hierarchy of properties from TABLE to CATEGORY and then to MODULE and returns the level
     * on which property is inherited or defined.  
     * @param propertyName Name of the property.
     * 
     * @return level on which property is defined. <code>NULL</code> when there is no such property on all these levels.
     * Or it can be set by default. So check is it applied as default. @see {@link #isPropertyAppliedByDefault(String)
     *   
     */
    InheritanceLevel getPropertyLevelDefinedOn(String propertyName);
    
    /**
     * Check if the property with given name is applied for current table by default.  
     * @param propertyName name of the property.
     * 
     * @return <code>TRUE</code> if the property with given name is applied for current table by default.
     */
    boolean isPropertyAppliedByDefault(String propertyName);
	
	// <<< INSERT >>>
	java.lang.String getName();
	void setName(java.lang.String name);
	java.lang.String getCategory();
	void setCategory(java.lang.String category);
	java.lang.String getDescription();
	void setDescription(java.lang.String description);
	java.lang.String[] getTags();
	void setTags(java.lang.String[] tags);
	java.util.Date getEffectiveDate();
	void setEffectiveDate(java.util.Date effectiveDate);
	java.util.Date getExpirationDate();
	void setExpirationDate(java.util.Date expirationDate);
	java.lang.String getCreatedBy();
	void setCreatedBy(java.lang.String createdBy);
	java.util.Date getCreatedOn();
	void setCreatedOn(java.util.Date createdOn);
	java.lang.String getModifiedBy();
	void setModifiedBy(java.lang.String modifiedBy);
	java.util.Date getModifyOn();
	void setModifyOn(java.util.Date modifyOn);
	java.lang.String getBuildPhase();
	void setBuildPhase(java.lang.String buildPhase);
	java.lang.String getValidateDT();
	void setValidateDT(java.lang.String validateDT);
	java.lang.String getLob();
	void setLob(java.lang.String lob);
	java.lang.String getUsregion();
	void setUsregion(java.lang.String usregion);
	org.openl.rules.enumeration.CountriesEnum[] getCountry();
	void setCountry(org.openl.rules.enumeration.CountriesEnum[] country);
	org.openl.rules.enumeration.CurrenciesEnum getCurrency();
	void setCurrency(org.openl.rules.enumeration.CurrenciesEnum currency);
	java.lang.String getLang();
	void setLang(java.lang.String lang);
	java.lang.String getState();
	void setState(java.lang.String state);
	java.lang.String getRegion();
	void setRegion(java.lang.String region);
	java.lang.String getVersion();
	void setVersion(java.lang.String version);
	java.lang.Boolean getActive();
	void setActive(java.lang.Boolean active);
	java.lang.Boolean getFailOnMiss();
	void setFailOnMiss(java.lang.Boolean failOnMiss);
	java.lang.Boolean getReturnOnMiss();
	void setReturnOnMiss(java.lang.Boolean returnOnMiss);
	java.lang.String getScope();
	void setScope(java.lang.String scope);
	// <<< END INSERT >>>
	
	
    void setPropertiesAppliedForCategory(Map<String, Object> categoryProperties);
    
    void setPropertiesAppliedForModule(Map<String, Object> moduleProperties);    
    
    void setPropertiesAppliedByDefault(Map<String, Object> defaultProperties);    
	
}
