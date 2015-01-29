package org.openl.meta.number;

/**
 * Operations with descriptions for {@link org.openl.meta.explanation.ExplanationNumberValue}.<br>
 * Including formulas and functions.
 * 
 * @author DLiauchuk
 *
 */
public enum NumberOperations {
//    ADD("+"),
//    MULTIPLY("*"),
//    SUBTRACT("-"),
//    DIVIDE("/"),  
//    REM("%"),
//    EQ("equal"),
//    GE("greater or equal"),
//    GT("greater"),
//    LE("less or equal"),
//    LT("less"),
//    NE("not equal"),
    
    
    COPY("COPY"),
    MAX("max"), 
    MAX_IN_ARRAY("max"),
    MIN("min"),
    MIN_IN_ARRAY("min"),
    ROUND("round"),
    POW("pow"),
    ABS("abs"),
    AVG("average"),
    SUM("sum"), 
    MEDIAN("median"), 
    PRODUCT("product"), 
    QUOTIENT("quotient"), 
    MOD("mod"), 
    NEGATIVE("negative"),
    INC("inc"),
    POSITIVE("positive"),
    DEC("dec"),
    SMALL("small"),
    BIG("big");
    
    private String description;
    
    private NumberOperations(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return description;
    }
    
    public String getName() {
        return name().toLowerCase();
    }
    
    public String getFullName() {
        return this.getClass().getSimpleName() + "." + name();
    }

}
