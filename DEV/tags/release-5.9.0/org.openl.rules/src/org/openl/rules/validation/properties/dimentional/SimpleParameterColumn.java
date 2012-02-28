package org.openl.rules.validation.properties.dimentional;

import org.apache.commons.lang.StringUtils;
import org.openl.message.OpenLMessagesUtils;
import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.table.properties.expressions.match.MatchingExpression;

/**
 * Column that is used in the dispatching table, built by dimension properties of the group of tables.
 * Handles the column with simple dimension property(not of array type).
 * 
 * @author DLiauchuk
 *
 */
public class SimpleParameterColumn extends ADispatcherTableColumn {
    
    public SimpleParameterColumn(TablePropertyDefinition property, DispatcherTableRules rules) {
        super(property, rules);                
    }
    
    public String getCodeExpression() {
        String result = StringUtils.EMPTY;
        
        String propertyName = getProperty().getName();
        
        MatchingExpression matchExpression = getProperty().getExpression();
        
        if (matchExpression != null) {
            String parameterName = propertyName + ADispatcherTableColumn.LOCAL_PARAM_SUFFIX;
            result = getMatchByDefaultCodeExpression(matchExpression) + matchExpression.getMatchExpression().getCodeExpression(parameterName);
        } else {
            String message = String.format("Can`t create expression for \"%s\" property validation.", propertyName);
            OpenLMessagesUtils.addWarn(message);
        }
        return result;        
    }

    public String getTitle() {        
        return getProperty().getDisplayName();
    }

    public String getParameterDeclaration() {        
        String propertyTypeName = getProperty().getType().getInstanceClass().getSimpleName();
        return String.format("%s %s%s", propertyTypeName, getProperty().getName(), 
            ADispatcherTableColumn.LOCAL_PARAM_SUFFIX);
    }
    
    public String getRuleValue(int ruleIndex, int elementNum) {        
        return getRules().getRule(ruleIndex).getPropertyValueAsString(getProperty().getName());
    }    
}
