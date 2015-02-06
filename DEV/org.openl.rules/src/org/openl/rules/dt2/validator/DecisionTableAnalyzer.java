package org.openl.rules.dt2.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openl.binding.BindingDependencies;
import org.openl.binding.ILocalVar;
import org.openl.domain.IDomain;
import org.openl.domain.IntRangeDomain;
import org.openl.domain.StringDomain;
import org.openl.rules.binding.RulesBindingDependencies;
import org.openl.rules.dt2.DecisionTable;
import org.openl.rules.dt2.element.ICondition;
import org.openl.rules.dt2.element.IDecisionRow;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.IParameterDeclaration;
import org.openl.types.impl.CompositeMethod;
import org.openl.types.impl.OpenFieldDelegator;
import org.openl.types.impl.ParameterDeclaration;

public class DecisionTableAnalyzer {

    private DecisionTable decisionTable;

    private Map<IDecisionRow, ConditionAnalyzer> conditionAnalyzers = new HashMap<IDecisionRow, ConditionAnalyzer>();
    private Map<String, DecisionTableParamDescription> usedParamsFromSignature = new HashMap<String, DecisionTableParamDescription>();

    public DecisionTableAnalyzer(DecisionTable decisionTable) {

        this.decisionTable = decisionTable;

        init(decisionTable);
    }

    private void init(DecisionTable decisionTable) {

        ICondition[] conditionRows = decisionTable.getConditionRows();

        for (ICondition conditionRow : conditionRows) {
            conditionAnalyzers.put(conditionRow, new ConditionAnalyzer(conditionRow));
        }
    }

    public boolean containsFormula(IDecisionRow row) {


    	int len = row.getNumberOfRules();
        for (int ruleN = 0; ruleN < len; ruleN++) {

        	if (row.hasFormula(ruleN))
        		return true;
        	
        }

        return false;
    }

    public Iterator<DecisionTableParamDescription> tableParams() {
        return usedParamsFromSignature.values().iterator();
    }

    public DecisionTable getDecisionTable() {
        return decisionTable;
    }

    public IDomain<?> getParameterDomain(String parameterName, IDecisionRow condition) {
        return conditionAnalyzers.get(condition).getParameterDomain(parameterName);
    }

    public IDomain<?> getSignatureParameterDomain(String parameterName) {
        return usedParamsFromSignature.get(parameterName).getDomain();
    }
    
    public IDomain<?> gatherDomainFromValues(IParameterDeclaration parameter, ICondition condition) {
        IDomain<?> result = null;
        Class<?> type = parameter.getType().getInstanceClass();
        if (String.class.equals(type)) {
            result = gatherStringDomainFromValues(condition);
        } else if (int.class.equals(type)) {
            result = gatherIntDomainFromValues(condition);
        }        
        return result;
    }

    private StringDomain gatherStringDomainFromValues(ICondition condition) {
    	int nRules = condition.getNumberOfRules();
    	int np = condition.getNumberOfParams();
        String[] enumValues = new String[nRules * np];
        for (int ruleN = 0; ruleN < nRules; ruleN++) {
                for (int pidx = 0; pidx < np; pidx++) {
                    enumValues[ruleN * np + pidx] = (String) condition.getParamValue(pidx, ruleN);
                }
        }
        return new StringDomain(enumValues);
    }

    private IntRangeDomain gatherIntDomainFromValues(ICondition condition) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
    	int nRules = condition.getNumberOfRules();
    	int np = condition.getNumberOfParams();
        for (int ruleN = 0; ruleN < nRules; ruleN++) {
        	if (condition.isEmpty(ruleN))
        		continue;
                for (int pidx = 0; pidx < np; pidx++) {
                	Integer cand = (Integer) condition.getParamValue(pidx, ruleN);
                    if (min > cand) {
                        min = cand;
                    }
                    else if (max < cand) {
                        max = cand;
                    }
                }
        }
        return new IntRangeDomain(min, max);
    }

    /**
     * Goes through the condition in algorithm column and search the params that are income parameters from 
     * the signature.
     * 
     * @param row Full row of the each condition. It includes condition name, algorithm, initialization, and all rule
     * cells. 
     * @return parameters that are income(from the signature) that are using in current row. 
     */
    public IParameterDeclaration[] referencedSignatureParams(IDecisionRow row) {

        CompositeMethod method = (CompositeMethod) row.getMethod();

        BindingDependencies bindingDependecies = new RulesBindingDependencies();
        method.updateDependency(bindingDependecies);

        IMethodSignature methodSignature = decisionTable.getSignature();

        List<IParameterDeclaration> paramDeclarations = new ArrayList<IParameterDeclaration>();

         for (IOpenField openField : bindingDependecies.getFieldsMap().values()) {

             IOpenField anotherOpenField = getLocalField(openField);

            if (anotherOpenField instanceof ILocalVar) {

                for (int i = 0; i < methodSignature.getNumberOfParameters(); i++) {

                    if (methodSignature.getParameterName(i).equals(anotherOpenField.getName())) {
                        ParameterDeclaration parameterDeclaration = new ParameterDeclaration(methodSignature.getParameterTypes()[i],
                            methodSignature.getParameterName(i));
                        if (!paramDeclarations.contains(parameterDeclaration)) {
                            paramDeclarations.add(parameterDeclaration);
                        }
                        
                    }
                }
            }
        }

        return paramDeclarations.toArray(new IParameterDeclaration[paramDeclarations.size()]);
    }
    
    /**
     * Takes the paramDeclarationFromSignature and transform its type to appropriate for validating.
     * see {@link DecisionTableValidatedObject.#transformParameterType(IParameterDeclaration)}.
     * 
     * @param paramDeclarationFromSignature parameter declaration from the signature. 
     * @param decisionTableToValidate decision table that is being validated.
     * @return new type for paramDeclarationFromSignature appropriate for validation.
     */
    @SuppressWarnings("deprecation")
    public IOpenClass transformSignatureType(IParameterDeclaration paramDeclarationFromSignature,
            IDecisionTableValidatedObject decisionTableToValidate) {

        DecisionTableParamDescription paramDescription = usedParamsFromSignature.get(paramDeclarationFromSignature.getName());
 
        if (paramDescription == null) {
            IOpenClass newType = decisionTableToValidate.getTransformer().transformSignatureType(paramDeclarationFromSignature);
            paramDescription = new DecisionTableParamDescription(paramDeclarationFromSignature, newType);
            
            usedParamsFromSignature.put(paramDeclarationFromSignature.getName(), paramDescription);
        }

        return paramDescription.getNewType();
    }

    public Map<String, DecisionTableParamDescription> getUsedParams() {
        return usedParamsFromSignature;
    }

    private IOpenField getLocalField(IOpenField field) {
        
        if (field instanceof ILocalVar) {
            return field;
        }
        
        if (field instanceof OpenFieldDelegator) {
            OpenFieldDelegator delegator = (OpenFieldDelegator) field;
            
            return delegator.getField();
        }
        
        return field;
    }

}
