package org.openl.rules.dt.data;

import org.openl.OpenL;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.rules.dt.element.IDecisionRow;
import org.openl.types.IOpenSchema;
import org.openl.types.IParameterDeclaration;

public class ConditionOrActionDataType extends ModuleOpenClass {

    public ConditionOrActionDataType(IDecisionRow conditionOrAction, IOpenSchema schema,  OpenL openl) {
        super(schema, conditionOrAction.getName() + "Type", openl);
        this.conditionOrAction = conditionOrAction;
        initFields();
    }

    private void initFields() {
        IParameterDeclaration[] pdd = conditionOrAction.getParams();
        for (int i = 0;  i < pdd.length; ++i) {
            addField(new CondOrActionParameterField(conditionOrAction, i));
        }
    }
    

    private IDecisionRow conditionOrAction;

}
