package org.openl.rules.dt;

import org.openl.OpenL;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.types.IMethodCaller;
import org.openl.types.IMethodSignature;
import org.openl.vm.IRuntimeEnv;

public interface IDTCondition extends IDecisionRow {
    IDecisionValue calculateCondition(int col, Object target, Object[] dtParams, IRuntimeEnv env);

    public IDTConditionEvaluator prepareCondition(IMethodSignature signature, OpenL openl, ModuleOpenClass dtModule,
            IBindingContextDelegator cxtd, RuleRow ruleRow) throws Exception;

    /**
     * @return
     */
    IMethodCaller getEvaluator();

    IDTConditionEvaluator getConditionEvaluator();

}
