package org.openl.rules.dtx.trace;

import org.openl.rules.dtx.IDecisionTable;
import org.openl.rules.method.ExecutableRulesMethod;
import org.openl.rules.table.ATableTracerNode;

public class DecisionTableTraceObject extends ATableTracerNode implements IDecisionTableTraceObject {

    public DecisionTableTraceObject(IDecisionTable decisionTable, Object[] params) {
        super("decisiontable", "DT", (ExecutableRulesMethod)decisionTable, params);
    }

    public IDecisionTable getDecisionTable() {
        return (IDecisionTable) getTraceObject();
    }
}
