package org.openl.rules.table.properties;

import java.util.Map;

import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.properties.def.DefaultPropertyDefinitions;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;
import org.openl.rules.table.properties.inherit.InheritanceLevel;

public interface ITableProperties {
    
    /**
     * <code>{@link Map}</code> of properties that includes all properties for current table. It includes:
     *          - all properties physically defined in table with system ones;
     *          - inherited properties from category and module scopes;
     *          - properties set by default;
     * 
     * @return <code>{@link Map}</code> of all properties relevant to current table.
     */
    Map<String, Object> getAllProperties();
    
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
    
    Map<String, Object> getExternalPropertiesAppliedForModule();
    
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

    ILogicalTable getInheritedPropertiesTable(InheritanceLevel inheritanceLevel);

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
	java.util.Date getStartRequestDate();
	void setStartRequestDate(java.util.Date startRequestDate);
	java.util.Date getEndRequestDate();
	void setEndRequestDate(java.util.Date endRequestDate);
	java.lang.String getCreatedBy();
	void setCreatedBy(java.lang.String createdBy);
	java.util.Date getCreatedOn();
	void setCreatedOn(java.util.Date createdOn);
	java.lang.String getModifiedBy();
	void setModifiedBy(java.lang.String modifiedBy);
	java.util.Date getModifiedOn();
	void setModifiedOn(java.util.Date modifiedOn);
	java.lang.String getId();
	void setId(java.lang.String id);
	java.lang.String getBuildPhase();
	void setBuildPhase(java.lang.String buildPhase);
	java.lang.String getValidateDT();
	void setValidateDT(java.lang.String validateDT);
	java.lang.String getLob();
	void setLob(java.lang.String lob);
	org.openl.rules.enumeration.UsRegionsEnum[] getUsregion();
	void setUsregion(org.openl.rules.enumeration.UsRegionsEnum[] usregion);
	org.openl.rules.enumeration.CountriesEnum[] getCountry();
	void setCountry(org.openl.rules.enumeration.CountriesEnum[] country);
	org.openl.rules.enumeration.CurrenciesEnum[] getCurrency();
	void setCurrency(org.openl.rules.enumeration.CurrenciesEnum[] currency);
	org.openl.rules.enumeration.LanguagesEnum[] getLang();
	void setLang(org.openl.rules.enumeration.LanguagesEnum[] lang);
	org.openl.rules.enumeration.UsStatesEnum[] getState();
	void setState(org.openl.rules.enumeration.UsStatesEnum[] state);
	org.openl.rules.enumeration.RegionsEnum[] getRegion();
	void setRegion(org.openl.rules.enumeration.RegionsEnum[] region);
	java.lang.String getVersion();
	void setVersion(java.lang.String version);
	java.lang.Boolean getActive();
	void setActive(java.lang.Boolean active);
	java.lang.Boolean getFailOnMiss();
	void setFailOnMiss(java.lang.Boolean failOnMiss);
	java.lang.String getScope();
	void setScope(java.lang.String scope);
	java.lang.String getDatatypePackage();
	void setDatatypePackage(java.lang.String datatypePackage);
	java.lang.String[] getTransaction();
	void setTransaction(java.lang.String[] transaction);
	java.lang.String[] getCustom1();
	void setCustom1(java.lang.String[] custom1);
	java.lang.String[] getCustom2();
	void setCustom2(java.lang.String[] custom2);
	java.lang.Boolean getCacheable();
	void setCacheable(java.lang.Boolean cacheable);
	org.openl.rules.enumeration.RecalculateEnum getRecalculate();
	void setRecalculate(org.openl.rules.enumeration.RecalculateEnum recalculate);
	java.lang.String getPrecision();
	void setPrecision(java.lang.String precision);
	java.lang.Boolean getAutoType();
	void setAutoType(java.lang.Boolean autoType);
// <<< END INSERT >>>
	
	
    void setPropertiesAppliedForCategory(Map<String, Object> categoryProperties);
    
    void setPropertiesAppliedForModule(Map<String, Object> moduleProperties);  
    
    void setExternalPropertiesAppliedForModule(Map<String, Object> moduleProperties);
    
    void setPropertiesAppliedByDefault(Map<String, Object> defaultProperties);    
    
    void setCurrentTableType(String currentTableType);

    String getCurrentTableType();

    void setPropertiesSection(ILogicalTable propertySection);
	
}
