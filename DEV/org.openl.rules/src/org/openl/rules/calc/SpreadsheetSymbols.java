package org.openl.rules.calc;

public enum SpreadsheetSymbols {
    
    /** cell name indicating return statement*/
    RETURN_NAME("RETURN"),
    TYPE_DELIMETER(":");
    
    private String symbols;
    
    private SpreadsheetSymbols(String symbols) {
        this.symbols = symbols;
    }
    
    @Override
    public String toString() {        
        return symbols;
    }

}
