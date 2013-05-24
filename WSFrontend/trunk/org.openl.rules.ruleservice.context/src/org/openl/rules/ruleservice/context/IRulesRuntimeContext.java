package org.openl.rules.ruleservice.context;

/*
 * Important notice:
 * If you add any methods, verify org.openl.rules.validation.properties.dimentional.DispatcherTableBuilder class works properly.
 * Refer to static initialization section were context attributes are gathered.
 * Add your method to exclusions the same way as it's done for "Object getValue(String variable)"
 */

public interface IRulesRuntimeContext {
    
    Object getValue(String variable);
    void setValue(String name, Object value);
    
    // <<< INSERT >>>
	java.util.Date getCurrentDate();
	void setCurrentDate(java.util.Date currentDate);	
	java.util.Date getRequestDate();
	void setRequestDate(java.util.Date requestDate);	
	java.lang.String getLob();
	void setLob(java.lang.String lob);	
	org.openl.rules.ruleservice.context.enumeration.UsStatesEnum getUsState();
	void setUsState(org.openl.rules.ruleservice.context.enumeration.UsStatesEnum usState);	
	org.openl.rules.ruleservice.context.enumeration.CountriesEnum getCountry();
	void setCountry(org.openl.rules.ruleservice.context.enumeration.CountriesEnum country);	
	org.openl.rules.ruleservice.context.enumeration.UsRegionsEnum getUsRegion();
	void setUsRegion(org.openl.rules.ruleservice.context.enumeration.UsRegionsEnum usRegion);	
	org.openl.rules.ruleservice.context.enumeration.CurrenciesEnum getCurrency();
	void setCurrency(org.openl.rules.ruleservice.context.enumeration.CurrenciesEnum currency);	
	org.openl.rules.ruleservice.context.enumeration.LanguagesEnum getLang();
	void setLang(org.openl.rules.ruleservice.context.enumeration.LanguagesEnum lang);	
	org.openl.rules.ruleservice.context.enumeration.RegionsEnum getRegion();
	void setRegion(org.openl.rules.ruleservice.context.enumeration.RegionsEnum region);	
// <<< END INSERT >>>
}
