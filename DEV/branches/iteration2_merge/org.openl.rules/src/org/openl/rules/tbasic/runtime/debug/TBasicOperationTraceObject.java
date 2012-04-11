/**
 * 
 */
package org.openl.rules.tbasic.runtime.debug;

import java.util.HashMap;

import org.openl.rules.table.IGridRegion;
import org.openl.rules.tbasic.runtime.Result;
import org.openl.rules.tbasic.runtime.operations.RuntimeOperation;

/**
 * @author User
 * 
 */
public class TBasicOperationTraceObject extends ATBasicTraceObjectLeaf {

    private Result result;
    private HashMap<String, Object> fieldValues;

    public TBasicOperationTraceObject(Object tracedObject) {
        super(tracedObject);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openl.vm.ITracerObject.SimpleTracerObject#getUri()
     */
    @Override
    public String getUri() {
        RuntimeOperation operation = (RuntimeOperation) getTraceObject();
        String operationUri = operation.getSourceCode().getSourceUri();

        return operationUri;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openl.util.ITreeElement#getType()
     */
    public String getType() {
        return "tbasicOperation";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openl.base.INamedThing#getDisplayName(int)
     */
    public String getDisplayName(int mode) {
        RuntimeOperation operation = (RuntimeOperation) getTraceObject();

        String operationName = operation.getSourceCode().getOperationName();
        String stepNameForDebug = (operation.getNameForDebug() != null ? operation.getNameForDebug() : "");
        String resultValue = "";
        if (result != null && result.getValue() != null) {
            resultValue = "(" + result.getValue().toString() + ")";
        }
        int operationRow = operation.getSourceCode().getRowNumber();

        String fieldFormatedValues = getFieldValuesAsString();

        String displayFieldFormatedValues = "";
        if (!fieldFormatedValues.equals("")) {
            displayFieldFormatedValues = String.format("[Local vars: %s]", fieldFormatedValues);
        }

        return String.format("Step: row %d: %s %s %s %s", operationRow, operationName, stepNameForDebug, resultValue,
                displayFieldFormatedValues);
    }

    /**
     * @return
     */
    private String getFieldValuesAsString() {
        StringBuffer fields = new StringBuffer();

        for (String fieldName : fieldValues.keySet()) {
            fields.append(fieldName).append(" = ").append(fieldValues.get(fieldName)).append(", ");
        }

        // remove last ", "
        if (fields.length() > 2) {
            fields.delete(fields.length() - 2, fields.length());
        }

        return fields.toString();
    }

    public void setResult(Result result) {
        this.result = result;
    }

    /**
     * @return the result
     */
    public Result getResult() {
        return result;
    }

    @SuppressWarnings("unchecked")
    public void setFieldValues(HashMap<String, Object> fieldValues) {
        this.fieldValues = (HashMap<String, Object>) fieldValues.clone();
    }

    /**
     * @return the fieldValues
     */
    public HashMap<String, Object> getFieldValues() {
        return fieldValues;
    }

    public IGridRegion getGridRegion() {
        RuntimeOperation operation = (RuntimeOperation) getTraceObject();
        return operation.getSourceCode().getGridRegion();
    }

}
