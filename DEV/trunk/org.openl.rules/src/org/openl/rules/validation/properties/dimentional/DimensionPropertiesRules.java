package org.openl.rules.validation.properties.dimentional;

import java.util.List;

import org.openl.rules.table.properties.ITableProperties;

public class DimensionPropertiesRules {
    
    private List<ITableProperties> tableProperties;
    
    public DimensionPropertiesRules(List<ITableProperties> tableProperties) {
        this.tableProperties = tableProperties;
    }
    
    public int getRulesNumber() {
        return tableProperties.size();
    }

    public ITableProperties getRule(int ruleIndex) {        
        return tableProperties.get(ruleIndex);
    }

}
