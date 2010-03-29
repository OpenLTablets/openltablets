/*
 * Created on Sep 10, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */
package org.openl.rules.dt;

import org.openl.OpenL;
import org.openl.base.INamedThing;
import org.openl.binding.BindingDependencies;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.MethodUtil;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.domain.IIntIterator;
import org.openl.rules.lang.xls.IXlsTableNames;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.ILogicalTable;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenMethodHeader;
import org.openl.types.impl.CompositeMethod;
import org.openl.types.java.JavaOpenClass;
import org.openl.vm.IRuntimeEnv;
import org.openl.vm.Tracer;

/**
 * @author snshor
 *
 */
public class DecisionTable implements IOpenMethod, IMemberMetaInfo {
    
    private IOpenMethodHeader header;

    private IDTCondition[] conditionRows;

    private IDTAction[] actionRows;

    private RuleRow ruleRow;
    
    private int columns;
    
    //private IDecisionTableStructure structure; 

    private TableSyntaxNode tableSyntaxNode;

    private DTOptimizedAlgorithm algorithm;

    public static DecisionTable createTable(IOpenMethodHeader header) {
        return new DecisionTable(header);
    }

    private DecisionTable(IOpenMethodHeader header) {
        this.header = header;
    }

    public void bindTable(IDTCondition[] conditionRows, IDTAction[] actionRows, RuleRow ruleRow,
    // IDecisionTableStructure structure,
            OpenL openl, ModuleOpenClass module, IBindingContextDelegator cxtd, int columns) throws Exception {
        this.conditionRows = conditionRows;
        this.actionRows = actionRows;
        this.ruleRow = ruleRow;
        // this.structure = structure;
        this.columns = columns;
        prepare(header, openl, module, cxtd);
    }

    public IDTAction[] getActionRows() {
        return actionRows;
    }

    public DTOptimizedAlgorithm getAlgorithm() {
        return algorithm;
    }

    public int getColumns() {
        return columns;
    }

    public IDTCondition[] getConditionRows() {
        return conditionRows;
    }

    public IOpenClass getDeclaringClass() {
        return header.getDeclaringClass();
    }

    public BindingDependencies getDependencies() {
        BindingDependencies bd = new BindingDependencies();
        updateDependency(bd);
        return bd;
    }

    public String getDisplayName(int mode) {
        return header.getInfo().getDisplayName(mode);
    }

    public ILogicalTable getDisplayTable() {
        ILogicalTable bView = tableSyntaxNode.getSubTables().get(IXlsTableNames.VIEW_BUSINESS);
        return bView.getLogicalColumn(0);
    }

    public IOpenMethodHeader getHeader() {
        return header;
    }

    public IMemberMetaInfo getInfo() {
        return this;
    }

    public IOpenMethod getMethod() {
        return this;
    }

    public String getName() {
        return header.getName();
    }

    public int getNumberOfRules() {
        if (conditionRows.length > 0) {
            return conditionRows[0].getParamValues().length;
        }
        if (actionRows.length > 0) {
            return actionRows[0].getParamValues().length;
        }
        return 0;
    }

    public String getRuleName(int col) {
        return ruleRow == null ? "R" + (col + 1) : ruleRow.getRuleName(col);
    }

    public RuleRow getRuleRow() {
        return ruleRow;
    }

    public ILogicalTable getRuleTable(int col) {
        ILogicalTable bView = tableSyntaxNode.getSubTables().get(IXlsTableNames.VIEW_BUSINESS);
        return bView.getLogicalColumn(col + 1);
    }

    public IMethodSignature getSignature() {
        return header.getSignature();
    }

    public String getSourceUrl() {
        return tableSyntaxNode.getUri();
    }

    public ISyntaxNode getSyntaxNode() {
        return tableSyntaxNode;
    }

    public TableSyntaxNode getTableSyntaxNode() {
        return tableSyntaxNode;
    }

    public IOpenClass getType() {
        return header.getType();
    }

    public Object invoke(Object target, Object[] params, IRuntimeEnv env) {
        if (Tracer.isTracerOn()) {
            return invokeTracedOptimized(target, params, env);
        }
        return invokeOptimized(target, params, env);
    }

    public Object invokeOptimized(Object target, Object[] params, IRuntimeEnv env) {

        IIntIterator rules = algorithm.checkedRules(target, params, env);

        Object returnValue = null;
        boolean atLeastOneRuleFired = false;
        
        for (; rules.hasNext();) {
            atLeastOneRuleFired = true;
            int ruleN = rules.nextInt();

            for (int j = 0; j < actionRows.length; j++) {
                returnValue = actionRows[j].executeAction(ruleN, target, params, env);
                if (actionRows[j].isReturnAction() &&  (returnValue != null || actionRows[j].getParamValues()[ruleN] != null)) {
                    return returnValue;
                }
            }
        }

        if (!atLeastOneRuleFired && shouldFailOnMiss()){
            String method = MethodUtil.printMethodWithParameterValues(getMethod(), params, INamedThing.REGULAR);
            String message = String.format("%s failed to match any rule condition", method);
            
            throw new FailOnMissException(message, this, params);
        }
        
        return returnValue;
    }    
    
    /**
     * Check whether execution of decision table should be failed if no rule fired.  
     */
    private boolean shouldFailOnMiss() {
      return getTableSyntaxNode().getTableProperties().getFailOnMiss();
    }

    public Object invokeStandard(Object target, Object[] params, IRuntimeEnv env) {
        int nRules = getColumns();
        DecisionTableAlgorithm dta = new DecisionTableAlgorithm(conditionRows.length, nRules, conditionRows, target,
                params, env);
        Object ret = null;

        for (int rule = 0; rule < nRules; rule++) {
            if (!dta.calcColumn(rule)) {
                continue;
            }
            for (int j = 0; j < actionRows.length; j++) {
                ret = actionRows[j].executeAction(rule, target, params, env);
                if (ret != null && actionRows[j].isReturnAction()) {
                    return ret;
                }
            }
        }
        return ret;
    }

    public Object invokeTraced(Object target, Object[] params, IRuntimeEnv env) {

        Tracer t = Tracer.getTracer();
        if (t == null) {
            return invokeStandard(target, params, env);
        }

        try {
            DecisionTableTraceObject dtto = new DecisionTableTraceObject(this, params);
            t.push(dtto);

            int nCol = getColumns();
            DecisionTableAlgorithm dta = new DecisionTableAlgorithm(conditionRows.length, nCol, conditionRows, target,
                    params, env);
            Object ret = null;

            for (int i = 0; i < nCol; i++) {
                if (!dta.calcColumn(i)) {
                    continue;
                }

                try {
                    t.push(dtto.traceRule(i));
                    for (int j = 0; j < actionRows.length; j++) {
                        ret = actionRows[j].executeAction(i, target, params, env);
                        if (ret != null && actionRows[j].isReturnAction()) {
                            dtto.setResult(ret);
                            return ret;
                        }

                    }
                } finally {
                    t.pop();
                }

            }
            dtto.setResult(ret);
            return ret;
        } finally {
            t.pop();
        }
    }

    public Object invokeTracedOptimized(Object target, Object[] params, IRuntimeEnv env) {

        Tracer t = Tracer.getTracer();
        if (t == null) {
            return invokeOptimized(target, params, env);
        }
        Object ret = null;

        try {
            DecisionTableTraceObject dtto = new DecisionTableTraceObject(this, params);
            t.push(dtto);
            IIntIterator rules = algorithm.checkedRules(target, params, env);

            for (; rules.hasNext();) {

                int ruleN = rules.nextInt();

                try {
                    t.push(dtto.traceRule(ruleN));
                    for (int j = 0; j < actionRows.length; j++) {
                        ret = actionRows[j].executeAction(ruleN, target, params, env);
                        if (ret != null && actionRows[j].isReturnAction()) {
                            dtto.setResult(ret);
                            return ret;
                        }

                    }
                } finally {
                    t.pop();
                }

            }
            dtto.setResult(ret);
            return ret;
        } finally {
            t.pop();
        }

    }

    public boolean isStatic() {
        return header.isStatic();
    }

    protected void makeAlgorithm(IDTConditionEvaluator[] evs) {
        algorithm = new DTOptimizedAlgorithm(evs, this);
        algorithm.buildIndex();
    }

    public void prepare(IOpenMethodHeader header, OpenL openl, ModuleOpenClass dtModule, IBindingContextDelegator cxtd)
            throws Exception {
        IMethodSignature signature = header.getSignature();

        IDTConditionEvaluator[] evs = new IDTConditionEvaluator[conditionRows.length];

        for (int i = 0; i < conditionRows.length; i++) {

            evs[i] = conditionRows[i].prepareCondition(signature, openl, dtModule, cxtd, ruleRow);
        }

        makeAlgorithm(evs);

        for (int i = 0; i < actionRows.length; i++) {
            IOpenClass methodType = actionRows[i].isReturnAction() ? header.getType() : JavaOpenClass.VOID;

            actionRows[i].prepareAction(methodType, signature, openl, dtModule, cxtd, ruleRow);
        }
    }
    
    @Deprecated
    private String printMask(boolean[] mask) {
        StringBuffer buf = new StringBuffer();
        buf.append('[');
        for (int i = 0; i < mask.length; i++) {
            if (i % 5 == 0 && i != 0) {
                buf.append(' ');
            }
            buf.append(mask[i] ? 'T' : 'f');
        }
        buf.append(']');
        return buf.toString();
    }

    public void setActionRows(IDTAction[] actionRows) {
        this.actionRows = actionRows;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public void setConditionRows(IDTCondition[] conditionRows) {
        this.conditionRows = conditionRows;
    }

    public void setHeader(IOpenMethodHeader header) {
        this.header = header;
    }

    public void setRuleRow(RuleRow ruleRow) {
        this.ruleRow = ruleRow;
    }

    public void setTableSyntaxNode(TableSyntaxNode tsn) {
        tableSyntaxNode = tsn;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void updateDependency(BindingDependencies dependencies) {
        for (int i = 0; i < conditionRows.length; i++) {
            ((CompositeMethod) conditionRows[i].getMethod()).updateDependency(dependencies);
            updateValueDependency((FunctionalRow) conditionRows[i], dependencies);
        }

        for (int i = 0; i < actionRows.length; i++) {
            ((CompositeMethod) actionRows[i].getMethod()).updateDependency(dependencies);
            updateValueDependency((FunctionalRow) actionRows[i], dependencies);

        }
    }

    protected void updateValueDependency(FunctionalRow frow, BindingDependencies dependencies) {
        Object[][] values = frow.getParamValues();

        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                continue;
            }
            for (int j = 0; j < values[i].length; j++) {
                if (values[i][j] instanceof CompositeMethod) {
                    ((CompositeMethod) values[i][j]).updateDependency(dependencies);
                }
            }
        }
    }
}