package org.openl.rules.calc;

import org.openl.types.IOpenField;
import org.openl.types.java.JavaOpenClass;
import org.openl.vm.IRuntimeEnv;

public class SpreadsheetResultOpenClass extends JavaOpenClass {

    public SpreadsheetResultOpenClass(Class<?> type) {
        super(type, null);
    }

    @Override
    public IOpenField getField(String fieldName, boolean strictMatch) {
        IOpenField field = super.getField(fieldName, strictMatch);
        if (field == null && fieldName.startsWith("$")) {
            field = new SpreadsheetResultField(this, fieldName, JavaOpenClass.OBJECT);
        }
        return field;
    }

    @Override
    public Object newInstance(IRuntimeEnv env) {
        if (SpreadsheetResult.class.equals(getInstanceClass())) {
            return new StubSpreadSheetResult();
        } else {
            return super.newInstance(env);
        }
    }
}
