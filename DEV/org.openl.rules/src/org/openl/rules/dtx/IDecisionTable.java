package org.openl.rules.dtx;

import org.openl.binding.BindingDependencies;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.method.ITablePropertiesMethod;
import org.openl.rules.method.TableUriMethod;
import org.openl.rules.table.ILogicalTable;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;

public interface IDecisionTable extends ITablePropertiesMethod, TableUriMethod{

	BindingDependencies getDependencies();

	TableSyntaxNode getSyntaxNode();

	int getNumberOfRules();

	ILogicalTable getRuleTable(int row);

	void updateDependency(BindingDependencies bd);

	IBaseAction[] getActionRows();

	IBaseCondition[] getConditionRows();
	
	int getNumberOfConditions();

	IMethodSignature getSignature();

	IOpenMethod getMethod();

	IOpenClass getDeclaringClass();

	String getRuleName(int ruleIndex);

}
