/**
 *
 */
package org.openl.rules.tbasic.runtime.debug;

import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.formatters.FormattersManager;
import org.openl.rules.tbasic.runtime.Result;
import org.openl.rules.tbasic.runtime.operations.RuntimeOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TBasicOperationTraceObject extends ATBasicTraceObjectLeaf {

    private HashMap<String, Object> fieldValues;

    public TBasicOperationTraceObject(Object tracedObject) {
        super(tracedObject);
    }

    public String getDisplayName(int mode) {
        RuntimeOperation operation = (RuntimeOperation) getTraceObject();

        String operationName = operation.getSourceCode().getOperationName();
        String stepNameForDebug = (operation.getNameForDebug() != null ? operation.getNameForDebug() : "");
        String resultValue = "";
        if (getResult() != null && getResult() != null) {
            resultValue = "(" + getResult().toString() + ")";
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

    public HashMap<String, Object> getFieldValues() {
        return fieldValues;
    }

    private String getFieldValuesAsString() {
        StringBuilder fields = new StringBuilder();

        for (String fieldName : fieldValues.keySet()) {
            Object value = fieldValues.get(fieldName);
            String formattedValue = FormattersManager.format(value);
            fields.append(fieldName).append(" = ").append(formattedValue).append(", ");
        }

        // remove last ", "
        if (fields.length() > 2) {
            fields.delete(fields.length() - 2, fields.length());
        }

        return fields.toString();
    }

    public List<IGridRegion> getGridRegions() {
        List<IGridRegion> regions = new ArrayList<IGridRegion>();
        RuntimeOperation operation = (RuntimeOperation) getTraceObject();
        regions.add(operation.getSourceCode().getGridRegion());
        return regions;
    }


    public Object getResult() {
        /**
         * Extract the value from the result of the TBasic operation.
         */
        if (super.getResult() != null) {
            return ((Result) super.getResult()).getValue();
        } else {
            return null;
        }
    }

    public String getType() {
        return "tbasicOperation";
    }

    @Override
    public String getUri() {
        RuntimeOperation operation = (RuntimeOperation) getTraceObject();
        String operationUri = operation.getSourceCode().getSourceUri();

        return operationUri;
    }

    @SuppressWarnings("unchecked")
    public void setFieldValues(HashMap<String, Object> fieldValues) {
        this.fieldValues = (HashMap<String, Object>) fieldValues.clone();
    }
}
