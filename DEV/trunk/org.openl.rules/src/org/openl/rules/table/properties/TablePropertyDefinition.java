package org.openl.rules.table.properties;

import org.openl.types.IOpenClass;

public class TablePropertyDefinition {

	private String displayName;
	private String name;
	private boolean	primaryKey;	
	private IOpenClass type;	
	private String group;	
	private boolean businessSearch;	
	private String securityFilter;	
	private String tableType;	
	private String defaultValue;	
	private String constraints;	
	private String format;	
	private String inheritable;	
	private String description;
	
	public boolean isStringValue() {	    
        boolean stringValue = false;
	    if((type.getInstanceClass()).equals(java.lang.String.class)) {
            stringValue = true;
        }        
        return stringValue;
    }

	public boolean isDateValue() {
        boolean dateValue = false;
        if((type.getInstanceClass()).equals(java.util.Date.class)) {
            dateValue = true;
        }
        return dateValue;
    }
	
    public boolean isBooleanValue() {
        boolean booleanValue = false;
        if((type.getInstanceClass()).equals(java.lang.Boolean.class)) {
            booleanValue = true;
        }        
        return booleanValue;
    }

    public String getDisplayName() {
		return displayName;
	}
    
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isPrimaryKey() {
		return primaryKey;
	}
	
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	public IOpenClass getType() {
		return type;
	}
	
	public void setType(IOpenClass type) {
		this.type = type;
	}
	
	public String getGroup() {
		return group;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	public boolean isBusinessSearch() {
		return businessSearch;
	}
	
	public void setBusinessSearch(boolean businessSearch) {
		this.businessSearch = businessSearch;
	}
	
	public String getSecurityFilter() {
		return securityFilter;
	}
	
	public void setSecurityFilter(String securityFilter) {
		this.securityFilter = securityFilter;
	}
	
	public String getTableType() {
		return tableType;
	}
	
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public String getConstraints() {
		return constraints;
	}
	
	public void setConstraints(String constraints) {
		this.constraints = constraints;
	}
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getInheritable() {
		return inheritable;
	}
	
	public void setInheritable(String inheritable) {
		this.inheritable = inheritable;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}	
	
}
