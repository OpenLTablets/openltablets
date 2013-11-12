package org.openl.rules.validation.properties.dimentional;

import java.util.ArrayList;
import java.util.List;

import org.openl.exception.OpenlNotCheckedException;
import org.openl.rules.table.properties.ITableProperties;

/**
 * Class that handles list of dimension table properties.
 * Each element of this list is considered to be a value for one rule in dispatcher decision table.
 * 
 * @author DLiauchuk
 *
 */
public class DispatcherTableRules {
    
    private List<ITableProperties> dimensionTableProperties;
    
    public DispatcherTableRules(List<ITableProperties> dimensionTableProperties) {
        if (dimensionTableProperties == null || dimensionTableProperties.size() == 0) {
            throw new OpenlNotCheckedException("The list of dimension properties in dispatcher table cannot be empty");
        }
        this.dimensionTableProperties = new ArrayList<ITableProperties>(dimensionTableProperties);
    }
    
    public int getRulesNumber() {
        return dimensionTableProperties.size();
    }

    public ITableProperties getRule(int ruleIndex) {        
        return dimensionTableProperties.get(ruleIndex);
    }

}
