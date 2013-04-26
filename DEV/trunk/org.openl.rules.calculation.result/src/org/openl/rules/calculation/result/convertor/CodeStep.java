package org.openl.rules.calculation.result.convertor;

/**
 * Spreadsheet step(row) that has the code value.
 * 
 * @author DLiauchuk
 * 
 */
public class CodeStep extends CalculationStep {

    private static final long serialVersionUID = 7372598798002605558L;

    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Step_Name: ").append(getStepName()).append(" Code: ").append(getCode());
        return stringBuffer.toString();
    }
}
