package org.openl.rules.dt2.algorithm;

import org.openl.domain.IIntIterator;
import org.openl.rules.dt2.trace.DecisionTableTraceObject;
import org.openl.vm.IRuntimeEnv;
import org.openl.vm.trace.TraceStack;

public interface IDecisionTableAlgorithm {

	void removeParamValuesForIndexedConditions();

	IIntIterator checkedRules(Object target, Object[] params, IRuntimeEnv env);

	IDecisionTableAlgorithm asTraceDecorator(TraceStack conditionsStack,
			DecisionTableTraceObject traceObject);


}
