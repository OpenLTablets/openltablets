package org.openl.rules.table.properties.expressions.match;

import org.apache.commons.lang.StringUtils;

public class MatchingExpressionFactory {
    
    public IMatchingExpression getMatchingExpression(String operationName, String contextAttribute) {
        IMatchingExpression matchExpression = null;
        operationName = operationName == null ? "" : operationName;
        
        if (StringUtils.isEmpty(contextAttribute))  {
            throw new RuntimeException("Can`t create matching expression with empty context attribute");
        }
        
        if (LEMatchingExpression.OPERATION_NAME.equalsIgnoreCase(operationName)){
            matchExpression = new LEMatchingExpression(contextAttribute);
        } else if (GTMatchingExpression.OPERATION_NAME.equalsIgnoreCase(operationName)){
            matchExpression = new GTMatchingExpression(contextAttribute);
        } else if (EQMatchingExpression.OPERATION_NAME.equalsIgnoreCase(operationName)){
            matchExpression = new EQMatchingExpression(contextAttribute);
        } else if (ContainsMatchingExpression.OPERATION_NAME.equalsIgnoreCase(operationName)){
            matchExpression = new ContainsMatchingExpression(contextAttribute);            
        } else {
            throw new RuntimeException(String.format("Unknown match expression operation \"%s\"", operationName));
        } 
        return matchExpression;
    }

}
