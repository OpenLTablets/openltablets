package org.openl.rules.validation.properties.dimentional;

public abstract class ADimensionPropertyColumn implements IDecisionTableColumn {

    public static final String LOCAL_PARAM_SUFFIX = "Local";
    
    private static final String CONDITION_NAME = "C";

    /**
     * default behavior says that just one value can exist for any rule.
     */
    public int getMaxNumberOfValuesForRules() {        
        return 1;
    }

    public String getRuleValue(int ruleIndex) {
        return getRuleValue(ruleIndex, 0);
    }
        
    public boolean isArrayCondition() {
        return false;
    }
    
    public String getColumnType() {
        return CONDITION_NAME;
    }
    
    
}
