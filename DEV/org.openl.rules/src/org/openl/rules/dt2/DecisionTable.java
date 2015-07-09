/*
 * Created on Sep 10, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */
package org.openl.rules.dt2;

import org.openl.OpenL;
import org.openl.binding.BindingDependencies;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.impl.component.ComponentOpenClass;
import org.openl.rules.annotations.Executable;
import org.openl.rules.binding.RulesBindingDependencies;
import org.openl.rules.dt2.algorithm.DecisionTableAlgorithmBuilder;
import org.openl.rules.dt2.algorithm.IAlgorithmBuilder;
import org.openl.rules.dt2.algorithm.IDecisionTableAlgorithm;
import org.openl.rules.dt2.algorithm2.DecisionTableAlgorithmBuilder2;
import org.openl.rules.dt2.element.ArrayHolder;
import org.openl.rules.dt2.element.FunctionalRow;
import org.openl.rules.dt2.element.IAction;
import org.openl.rules.dt2.element.ICondition;
import org.openl.rules.dt2.element.RuleRow;
import org.openl.rules.dtx.IBaseAction;
import org.openl.rules.dtx.IBaseCondition;
import org.openl.rules.dtx.IDecisionTable;
import org.openl.rules.lang.xls.binding.AMethodBasedNode;
import org.openl.rules.method.ExecutableRulesMethod;
import org.openl.rules.table.ILogicalTable;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenMethodHeader;
import org.openl.types.Invokable;
import org.openl.types.impl.CompositeMethod;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 * 
 */
@Executable
public class DecisionTable extends ExecutableRulesMethod implements IDecisionTable {

    private IBaseCondition[] conditionRows;
    private IBaseAction[] actionRows;
    /**
     * Optional non-functional row with rule indexes.
     */
    private RuleRow ruleRow;

    private int columns;

    private IDecisionTableAlgorithm algorithm;

    /**
     * Object to invoke current method.
     */
    private Invokable invoker;
    
    private DTInfo dtInfo;
    
    public DecisionTable() {
        super(null, null);
    }

    public DecisionTable(IOpenMethodHeader header, AMethodBasedNode boundNode) {
        super(header, boundNode);
        initProperties(getSyntaxNode().getTableProperties());
    }

    public IBaseAction[] getActionRows() {
        return actionRows;
    }

    public IDecisionTableAlgorithm getAlgorithm() {
        return algorithm;
    }

    public int getColumns() {
        return columns;
    }

    public IBaseCondition[] getConditionRows() {
        return conditionRows;
    }

    public String getDisplayName(int mode) {
        IMemberMetaInfo metaInfo = getHeader().getInfo();
        if (metaInfo != null) {
            return metaInfo.getDisplayName(mode);
        }
        return toString();
    }

    public IOpenMethod getMethod() {
        return this;
    }

    public int getNumberOfRules() {

        if (actionRows.length > 0) {
            return actionRows[0].getNumberOfRules();
        }

        return 0;
    }

    public String getRuleName(int col) {
        return ruleRow == null ? "R" + (col + 1) : ruleRow.getRuleName(col);
    }

    public RuleRow getRuleRow() {
        return ruleRow;
    }

    /**
     * Returns logical table that contains rule column. The column will contain
     * all return, action and condition cells for rule specified by index.
     * 
     * @param ruleIndex Index of rule.
     * @return ILogicalTable that contains rule column.
     */
    public ILogicalTable getRuleTable(int ruleIndex) {
        ILogicalTable dt = actionRows[0].getDecisionTable();
        int starColumn = dt.getWidth() - columns;

        return dt.getColumn(starColumn + ruleIndex);
    }

    public String getSourceUrl() {
        return getSyntaxNode().getUri();
    }

    public void setActionRows(IAction[] actionRows) {
        this.actionRows = actionRows;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public void setConditionRows(IBaseCondition[] conditionRows) {
        this.conditionRows = conditionRows;
    }

    public void setRuleRow(RuleRow ruleRow) {
        this.ruleRow = ruleRow;
    }

    public void bindTable(IBaseCondition[] conditionRows, IBaseAction[] actionRows, RuleRow ruleRow, OpenL openl,
            ComponentOpenClass componentOpenClass, IBindingContextDelegator cxtd, int columns) throws Exception {

        this.conditionRows = conditionRows;
        this.actionRows = actionRows;

        if (!cxtd.isExecutionMode()) {
            this.ruleRow = ruleRow;
        }
        this.columns = columns;

        prepare(getHeader(), openl, componentOpenClass, cxtd);
    }

    public BindingDependencies getDependencies() {

        BindingDependencies bindingDependencies = new RulesBindingDependencies();
        updateDependency(bindingDependencies);

        return bindingDependencies;
    }

    protected Object innerInvoke(Object target, Object[] params, IRuntimeEnv env) {
        if (invoker == null) {
            invoker = new DecisionTableInvoker(this);

        }
        return invoker.invoke(target, params, env);
    }

    /**
     * Check whether execution of decision table should be failed if no rule
     * fired.
     */
    public boolean shouldFailOnMiss() {
        if (getMethodProperties() != null) {
            return (Boolean) getMethodProperties().getPropertyValue("failOnMiss");
        }
        return false;
    }


    private void prepare(IOpenMethodHeader header, OpenL openl, ComponentOpenClass module,
            IBindingContextDelegator bindingContextDelegator) throws Exception {

        
        algorithm = getAlgorithmBuilder(header, openl, module, bindingContextDelegator).prepareAndBuildAlgorithm();

    }
    
    
    public static boolean ALG2 = false;

    private IAlgorithmBuilder getAlgorithmBuilder(IOpenMethodHeader header, OpenL openl,
			ComponentOpenClass module,
			IBindingContextDelegator bindingContextDelegator) {
		return ALG2 ? new DecisionTableAlgorithmBuilder2(this, header, openl, module, bindingContextDelegator) 
		            : new DecisionTableAlgorithmBuilder(this, header, openl, module, bindingContextDelegator);
	}


    @Override
    public String toString() {
        return getName();
    }

    public void updateDependency(BindingDependencies dependencies) {
        if (conditionRows != null) {
            for (IBaseCondition condition : conditionRows) {
                CompositeMethod method = (CompositeMethod) condition.getMethod();
                if (method != null) {
                    method.updateDependency(dependencies);
                }

                updateValueDependency((FunctionalRow) condition, dependencies);
            }
        }

        if (actionRows != null) {
            for (IBaseAction action : actionRows) {
                CompositeMethod method = (CompositeMethod) action.getMethod();
                if (method != null) {
                    method.updateDependency(dependencies);
                }

                updateValueDependency((FunctionalRow) action, dependencies);
            }
        }
    }

    protected void updateValueDependency(FunctionalRow frow, BindingDependencies dependencies) {


    	int len = frow.getNumberOfRules();
    	int np = frow.getNumberOfParams();
    	for (int ruleN = 0; ruleN < len; ruleN++) {

                if (frow.isEmpty(ruleN)) {
                    continue;
                }

                for (int paramIndex = 0; paramIndex < np; paramIndex++) {
                	Object value = frow.getParamValue(paramIndex, ruleN);
                	
                    if (value instanceof CompositeMethod) {
                        ((CompositeMethod) value).updateDependency(dependencies);
                    }
                    else if (value instanceof ArrayHolder) {
						ArrayHolder ah = (ArrayHolder) value;
						ah.updateDependency(dependencies);
					}
                }
            }

    }

    
    public ICondition getCondition(int n)
    {
    	return (ICondition)conditionRows[n];
    }

    public IAction getAction(int n)
    {
    	return (IAction)actionRows[n];
    }
    
    
 
	public DTInfo getDtInfo() {
		return dtInfo;
	}

	public void setDtInfo(DTInfo dtInfo) {
		this.dtInfo = dtInfo;
	}

	@Override
	public int getNumberOfConditions() {
		
		return conditionRows.length;
	}

	public int getNumberOfActions() {
		
		return actionRows.length;
	}
	
	

}