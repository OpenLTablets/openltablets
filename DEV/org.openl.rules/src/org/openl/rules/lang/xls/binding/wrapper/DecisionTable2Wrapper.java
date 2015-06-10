package org.openl.rules.lang.xls.binding.wrapper;

import java.util.Map;

import org.openl.OpenL;
import org.openl.binding.BindingDependencies;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.impl.component.ComponentOpenClass;
import org.openl.rules.dt2.DTInfo;
import org.openl.rules.dt2.DecisionTable;
import org.openl.rules.dt2.algorithm.IDecisionTableAlgorithm;
import org.openl.rules.dt2.element.IAction;
import org.openl.rules.dt2.element.ICondition;
import org.openl.rules.dt2.element.RuleRow;
import org.openl.rules.dtx.IBaseAction;
import org.openl.rules.dtx.IBaseCondition;
import org.openl.rules.lang.xls.binding.ATableBoundNode;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.properties.ITableProperties;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenMethodHeader;
import org.openl.vm.IRuntimeEnv;

public class DecisionTable2Wrapper extends DecisionTable implements DispatchWrapper{
    DecisionTable delegate;
    XlsModuleOpenClass xlsModuleOpenClass;
    
    public DecisionTable2Wrapper(XlsModuleOpenClass xlsModuleOpenClass, DecisionTable delegate) {
        this.delegate = delegate;
        this.xlsModuleOpenClass = xlsModuleOpenClass;
    }
    
    public Object invoke(Object target, Object[] params, IRuntimeEnv env) {
        return DispatcherLogic.dispatch(xlsModuleOpenClass, delegate, target, params, env);
    }

    public IOpenClass getDeclaringClass() {
        return delegate.getDeclaringClass();
    }

    public IOpenMethodHeader getHeader() {
        return delegate.getHeader();
    }

    public String getTableUri() {
        return delegate.getTableUri();
    }

    public String getName() {
        return delegate.getName();
    }

    public IMethodSignature getSignature() {
        return delegate.getSignature();
    }

    public IOpenClass getType() {
        return delegate.getType();
    }

    public boolean isStatic() {
        return delegate.isStatic();
    }
    
    @Override
    public IOpenMethod getDelegate() {
        return delegate;
    }

    public IBaseAction[] getActionRows() {
        return delegate.getActionRows();
    }

    public IDecisionTableAlgorithm getAlgorithm() {
        return delegate.getAlgorithm();
    }

    public int getColumns() {
        return delegate.getColumns();
    }

    public IBaseCondition[] getConditionRows() {
        return delegate.getConditionRows();
    }

    public String getDisplayName(int mode) {
        return delegate.getDisplayName(mode);
    }

    public IOpenMethod getMethod() {
        return delegate.getMethod();
    }

    public int getNumberOfRules() {
        return delegate.getNumberOfRules();
    }

    public String getRuleName(int col) {
        return delegate.getRuleName(col);
    }

    public RuleRow getRuleRow() {
        return delegate.getRuleRow();
    }

    public ILogicalTable getRuleTable(int ruleIndex) {
        return delegate.getRuleTable(ruleIndex);
    }

    public String getSourceUrl() {
        return delegate.getSourceUrl();
    }

    public void setActionRows(IAction[] actionRows) {
        delegate.setActionRows(actionRows);
    }

    public void setColumns(int columns) {
        delegate.setColumns(columns);
    }

    public void setConditionRows(IBaseCondition[] conditionRows) {
        delegate.setConditionRows(conditionRows);
    }

    public void setRuleRow(RuleRow ruleRow) {
        delegate.setRuleRow(ruleRow);
    }

    public void bindTable(IBaseCondition[] conditionRows,
            IBaseAction[] actionRows,
            RuleRow ruleRow,
            OpenL openl,
            ComponentOpenClass componentOpenClass,
            IBindingContextDelegator cxtd,
            int columns) throws Exception {
        delegate.bindTable(conditionRows, actionRows, ruleRow, openl, componentOpenClass, cxtd, columns);
    }

    public void setBoundNode(ATableBoundNode node) {
        delegate.setBoundNode(node);
    }

    public ATableBoundNode getBoundNode() {
        return delegate.getBoundNode();
    }

    public BindingDependencies getDependencies() {
        return delegate.getDependencies();
    }

    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    public ITableProperties getMethodProperties() {
        return delegate.getMethodProperties();
    }

    public IMemberMetaInfo getInfo() {
        return delegate.getInfo();
    }

    public boolean shouldFailOnMiss() {
        return delegate.shouldFailOnMiss();
    }

    public TableSyntaxNode getSyntaxNode() {
        return delegate.getSyntaxNode();
    }

    public String toString() {
        return delegate.toString();
    }

    public void updateDependency(BindingDependencies dependencies) {
        delegate.updateDependency(dependencies);
    }

    public ICondition getCondition(int n) {
        return delegate.getCondition(n);
    }

    public IAction getAction(int n) {
        return delegate.getAction(n);
    }

    public DTInfo getDtInfo() {
        return delegate.getDtInfo();
    }

    public void setDtInfo(DTInfo dtInfo) {
        delegate.setDtInfo(dtInfo);
    }

    public int getNumberOfConditions() {
        return delegate.getNumberOfConditions();
    }
    
    
}
