package org.openl.rules.table.properties.expressions.match;

public class LEMatchingExpression extends AMatchingExpression {
        
    public static final String OPERATION_NAME = "LE";
    private static final String OPERATION = "<=";
    
    public LEMatchingExpression(String contextAttribute) {
        super(OPERATION_NAME, OPERATION, contextAttribute);        
    }
}
