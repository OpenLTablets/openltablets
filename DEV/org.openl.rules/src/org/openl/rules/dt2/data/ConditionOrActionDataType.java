package org.openl.rules.dt2.data;

import org.openl.OpenL;
import org.openl.binding.impl.component.ComponentOpenClass;
import org.openl.rules.dtx.IBaseDecisionRow;
import org.openl.types.IOpenSchema;
import org.openl.types.IParameterDeclaration;

public class ConditionOrActionDataType extends ComponentOpenClass {

    private IBaseDecisionRow conditionOrAction;

    public ConditionOrActionDataType(IBaseDecisionRow conditionOrAction, IOpenSchema schema,  OpenL openl) {
        super(schema, conditionOrAction.getName() + "Type", openl);
        this.conditionOrAction = conditionOrAction;
        initFields();
    }

    private void initFields() {
        IParameterDeclaration[] pdd = conditionOrAction.getParams();
        for (int i = 0;  i < pdd.length; ++i) {
            addField(new ConditionOrActionParameterField(conditionOrAction, i));
        }
    }
    
}
