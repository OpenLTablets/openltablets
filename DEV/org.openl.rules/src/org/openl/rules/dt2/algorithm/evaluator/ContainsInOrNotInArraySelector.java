package org.openl.rules.dt2.algorithm.evaluator;

import org.openl.domain.IIntSelector;
import org.openl.rules.dt2.element.ICondition;
import org.openl.rules.dt2.type.BooleanTypeAdaptor;
import org.openl.util.ArrayTool;
import org.openl.vm.IRuntimeEnv;

public class ContainsInOrNotInArraySelector implements IIntSelector {

    private ICondition condition;
    private Object value;
    private Object target;
    private Object[] params;
    private IRuntimeEnv env;
    private BooleanTypeAdaptor adaptor;

    public ContainsInOrNotInArraySelector(ICondition condition,
            Object value,
            Object target,
            Object[] params,
            BooleanTypeAdaptor adaptor,
            IRuntimeEnv env) {

        this.condition = condition;
        this.value = value;
        this.params = params;
        this.env = env;
        this.target = target;
        this.adaptor = adaptor;
    }

    public boolean select(int ruleN) {


        if (condition.isEmpty(ruleN)) {
            return true;
        }

        Object[] realParams = new Object[condition.getNumberOfParams()];

        condition.loadValues(realParams, 0, ruleN, target, this.params, env);



        if (realParams.length < 2 || realParams[1] == null) {
            return true;
        }

        boolean isIn = realParams[0] == null || adaptor.extractBooleanValue(realParams[0]);

        return ArrayTool.contains(realParams[1], value) ^ isIn;
    }
}
