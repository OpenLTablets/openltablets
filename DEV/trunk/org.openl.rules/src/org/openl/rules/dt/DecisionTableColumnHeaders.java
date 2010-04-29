package org.openl.rules.dt;

public enum DecisionTableColumnHeaders {
    CONDITION("C"),
    ACTION("A"),
    RULE("RULE"),
    RETURN("RET");
    
    private String key;
    
    private DecisionTableColumnHeaders(String key) {
        this.key = key;
    }
    
    public String getHeaderKey() {
        return key;
    }

}
