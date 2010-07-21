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
import org.openl.binding.impl.module.ModuleBindingContext;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.domain.IIntIterator;
import org.openl.rules.annotations.Executable;
import org.openl.rules.dt.algorithm.DecisionTableOptimizedAlgorithm;
import org.openl.rules.dt.algorithm.FailOnMissException;
import org.openl.rules.dt.algorithm.evaluator.IConditionEvaluator;
import org.openl.rules.dt.data.DecisionTableDataType;
import org.openl.rules.dt.element.FunctionalRow;
import org.openl.rules.dt.element.IAction;
import org.openl.rules.dt.element.ICondition;
import org.openl.rules.dt.element.RuleRow;
import org.openl.rules.dt.trace.DecisionTableTraceObject;
import org.openl.rules.lang.xls.IXlsTableNames;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.ILogicalTable;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenMethodHeader;
import org.openl.types.impl.AMethod;
import org.openl.types.impl.CompositeMethod;
import org.openl.types.java.JavaOpenClass;
import org.openl.vm.IRuntimeEnv;
import org.openl.vm.Tracer;

/**
 * @author snshor
 * 
 */
@Executable
public class DecisionTable extends AMethod implements IMemberMetaInfo {    
    private ICondition[] conditionRows;
    private IAction[] actionRows;
    /**
     * Optional non-functional row with rule indexes.
     */
    private RuleRow ruleRow;

    private int columns;

    private TableSyntaxNode tableSyntaxNode;
    private DecisionTableOptimizedAlgorithm algorithm;

    public DecisionTable(IOpenMethodHeader header) {
        super(header);
    }

    public IAction[] getActionRows() {
        return actionRows;
    }

    public DecisionTableOptimizedAlgorithm getAlgorithm() {
        return algorithm;
    }

    public int getColumns() {
        return columns;
    }

    public ICondition[] getConditionRows() {
        return conditionRows;
    }

    public String getDisplayName(int mode) {
        return getHeader().getInfo().getDisplayName(mode);
    }

    public ILogicalTable getDisplayTable() {
        ILogicalTable table = tableSyntaxNode.getSubTables().get(IXlsTableNames.VIEW_BUSINESS);

        return table.getLogicalColumn(0);
    }

    public IMemberMetaInfo getInfo() {
        return this;
    }

    public IOpenMethod getMethod() {
        return this;
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
        ILogicalTable table = tableSyntaxNode.getSubTables().get(IXlsTableNames.VIEW_BUSINESS);

        return table.getLogicalColumn(col + 1);
    }
    
    /**
     * Returns logical table that contains rule column. The column will contain
     * all return, action and condition cells for rule specified by index.
     * 
     * @param ruleNumber Index of rule.
     * @return ILogicalTable that contains rule column.
     */
    public ILogicalTable getRuleByIndex(int ruleNumber) {
        ILogicalTable dt = actionRows[0].getDecisionTable();
        int starColumn = dt.getLogicalWidth() - columns;
        
        return dt.getLogicalColumn(starColumn + ruleNumber);
    }

    public String getSourceUrl() {
        return tableSyntaxNode.getUri();
    }

    public TableSyntaxNode getSyntaxNode() {
        return tableSyntaxNode;
    }

    public void setActionRows(IAction[] actionRows) {
        this.actionRows = actionRows;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public void setConditionRows(ICondition[] conditionRows) {
        this.conditionRows = conditionRows;
    }

    public void setRuleRow(RuleRow ruleRow) {
        this.ruleRow = ruleRow;
    }

    public void setTableSyntaxNode(TableSyntaxNode tsn) {
        tableSyntaxNode = tsn;
    }

    public void bindTable(ICondition[] conditionRows,
            IAction[] actionRows,
            RuleRow ruleRow,
            OpenL openl,
            ModuleOpenClass module,
            IBindingContextDelegator cxtd,
            int columns) throws Exception {

        this.conditionRows = conditionRows;
        this.actionRows = actionRows;
        this.ruleRow = ruleRow;
        this.columns = columns;

        prepare(getHeader(), openl, module, cxtd);
    }

    public BindingDependencies getDependencies() {

        BindingDependencies bindingDependencies = new BindingDependencies();
        updateDependency(bindingDependencies);

        return bindingDependencies;
    }

    public Object invoke(Object target, Object[] params, IRuntimeEnv env) {

        if (Tracer.isTracerOn()) {
            return invokeTracedOptimized(target, params, env);
        }

        return invokeOptimized(target, params, env);
    }

    private Object invokeOptimized(Object target, Object[] params, IRuntimeEnv env) {

        IIntIterator rules = algorithm.checkedRules(target, params, env);

        Object returnValue = null;
        boolean atLeastOneRuleFired = false;

        while (rules.hasNext()) {

            atLeastOneRuleFired = true;
            int ruleN = rules.nextInt();

            for (int j = 0; j < actionRows.length; j++) {

                returnValue = actionRows[j].executeAction(ruleN, target, params, env);

                if (actionRows[j].isReturnAction() && (returnValue != null || (actionRows[j].getParamValues()!= null && actionRows[j].getParamValues()[ruleN] != null))) {
                    return returnValue;
                }
            }
        }

        if (!atLeastOneRuleFired && shouldFailOnMiss()) {

            String method = MethodUtil.printMethodWithParameterValues(getMethod(), params, INamedThing.REGULAR);
            String message = String.format("%s failed to match any rule condition", method);

            throw new FailOnMissException(message, this, params);
        }

        return returnValue;
    }

    /**
     * Check whether execution of decision table should be failed if no rule
     * fired.
     */
    private boolean shouldFailOnMiss() {
        return getSyntaxNode().getTableProperties().getFailOnMiss();
    }

    /*
     * public Object invokeStandard(Object target, Object[] params, IRuntimeEnv
     * env) { int nRules = getColumns(); DecisionTableAlgorithm dta = new
     * DecisionTableAlgorithm(conditionRows.length, nRules, conditionRows,
     * target, params, env); Object ret = null;
     * 
     * for (int rule = 0; rule < nRules; rule++) { if (!dta.calcColumn(rule)) {
     * continue; } for (int j = 0; j < actionRows.length; j++) { ret =
     * actionRows[j].executeAction(rule, target, params, env); if (ret != null
     * && actionRows[j].isReturnAction()) { return ret; } } } return ret; }
     * 
     * public Object invokeTraced(Object target, Object[] params, IRuntimeEnv
     * env) {
     * 
     * Tracer t = Tracer.getTracer(); if (t == null) { return
     * invokeStandard(target, params, env); }
     * 
     * try { DecisionTableTraceObject dtto = new DecisionTableTraceObject(this,
     * params); t.push(dtto);
     * 
     * int nCol = getColumns(); DecisionTableAlgorithm dta = new
     * DecisionTableAlgorithm(conditionRows.length, nCol, conditionRows, target,
     * params, env); Object ret = null;
     * 
     * for (int i = 0; i < nCol; i++) { if (!dta.calcColumn(i)) { continue; }
     * 
     * try { t.push(dtto.traceRule(i)); for (int j = 0; j < actionRows.length;
     * j++) { ret = actionRows[j].executeAction(i, target, params, env); if (ret
     * != null && actionRows[j].isReturnAction()) { dtto.setResult(ret); return
     * ret; }
     * 
     * } } finally { t.pop(); }
     * 
     * } dtto.setResult(ret); return ret; } finally { t.pop(); } }
     */

    private Object invokeTracedOptimized(Object target, Object[] params, IRuntimeEnv env) {

        Tracer tracer = Tracer.getTracer();

        if (tracer == null) {
            return invokeOptimized(target, params, env);
        }

        Object ret = null;

        try {
            DecisionTableTraceObject traceObject = new DecisionTableTraceObject(this, params);
            tracer.push(traceObject);

            IIntIterator rules = algorithm.checkedRules(target, params, env);

            while (rules.hasNext()) {

                int ruleN = rules.nextInt();

                try {

                    tracer.push(traceObject.traceRule(ruleN));

                    for (int j = 0; j < actionRows.length; j++) {
                        ret = actionRows[j].executeAction(ruleN, target, params, env);

                        if (ret != null && actionRows[j].isReturnAction()) {
                            traceObject.setResult(ret);
                            return ret;
                        }
                    }
                } finally {
                    tracer.pop();
                }
            }

            traceObject.setResult(ret);

            return ret;
        } finally {
            tracer.pop();
        }
    }

    protected void makeAlgorithm(IConditionEvaluator[] evs) throws Exception {

        algorithm = new DecisionTableOptimizedAlgorithm(evs, this);
        algorithm.buildIndex();
    }

    private void prepare(IOpenMethodHeader header,
            OpenL openl,
            ModuleOpenClass module,
            IBindingContextDelegator bindingContextDelegator) throws Exception {

        IMethodSignature signature = header.getSignature();
        IConditionEvaluator[] evaluators = new IConditionEvaluator[conditionRows.length];

        for (int i = 0; i < conditionRows.length; i++) {
            evaluators[i] = conditionRows[i].prepareCondition(signature,
                openl,
                module,
                bindingContextDelegator,
                ruleRow);
        }

        makeAlgorithm(evaluators);
        
        IBindingContextDelegator actionBindingContextDelegator = new ModuleBindingContext(bindingContextDelegator, (ModuleOpenClass)getRuleExecutionType(openl));

        for (int i = 0; i < actionRows.length; i++) {
            IOpenClass methodType = actionRows[i].isReturnAction() ? header.getType() : JavaOpenClass.VOID;
            actionRows[i].prepareAction(methodType, signature, openl, module, actionBindingContextDelegator, ruleRow, getRuleExecutionType(openl));
        }
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
    
    
    IOpenClass ruleExecutionType;
    
    private synchronized IOpenClass getRuleExecutionType(OpenL openl)
    {
        if (ruleExecutionType == null)
        {
            ruleExecutionType = new DecisionTableDataType(this, null, getName()+ "Type", openl);
        }    
        return ruleExecutionType;
    }
    

}