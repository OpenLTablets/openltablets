package org.openl.rules.calc.result.convertor;


/**
 * Class that holds the information about the column that need to be extracted from spreadsheet table.
 * 
 * @author DLiauchuk
 *
 */
public class ColumnToExtract {
    
    private String columnName;
    private Class<?> expectedType;
    private boolean containNested;
    
    /**
     * 
     * @param columnName name of the column as it is in Spreadsheet table
     * @param expectedType type of the value to store extracted value
     * @param containNested indicates if there is any row for this column with nested result.
     * For information which values considered to be nested see {@link NestedDataRowExtractorsFactory} 
     */
    public ColumnToExtract(String columnName, Class<?> expectedType, boolean containNested) {
        this.columnName = columnName;
        this.expectedType = expectedType;
        this.containNested = containNested;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Class<?> getExpectedType() {
        return expectedType;
    }

    public void setExpectedType(Class<?> expectedType) {
        this.expectedType = expectedType;
    }

    public boolean containNested() {
        return containNested;
    }

    public void setContainNested(boolean containNested) {
        this.containNested = containNested;
    }
}
