package org.openl.rules.dt2.validator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openl.OpenL;
import org.openl.binding.IBindingContext;
import org.openl.binding.impl.module.ModuleBindingContext;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.engine.OpenLManager;
import org.openl.exception.OpenLRuntimeException;
import org.openl.rules.dt2.DecisionTable;
import org.openl.rules.dt2.element.ICondition;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.IParameterDeclaration;
import org.openl.types.impl.MethodSignature;
import org.openl.types.impl.OpenMethodHeader;
import org.openl.types.impl.ParameterDeclaration;
import org.openl.types.java.JavaOpenClass;

import org.openl.ie.constrainer.Constrainer;
import org.openl.ie.constrainer.IntBoolExp;
import org.openl.ie.constrainer.IntBoolExpConst;
import org.openl.ie.constrainer.IntExp;
import org.openl.ie.constrainer.IntExpArray;
import org.openl.ie.constrainer.IntVar;
import org.openl.ie.constrainer.consistencyChecking.DTCheckerImpl;
import org.openl.ie.constrainer.consistencyChecking.Overlapping;
import org.openl.ie.constrainer.consistencyChecking.Uncovered;
import org.openl.ie.constrainer.consistencyChecking.DTCheckerImpl.CDecisionTableImpl;

public class ValidationAlgorithm {

    private IDecisionTableValidatedObject decisionTableToValidate;
    private IntExpArray vars;
    private OpenL openl;

    private Constrainer constrainer = new Constrainer("Validation");

    public ValidationAlgorithm(IDecisionTableValidatedObject validatedObject, OpenL openl) {
        this.decisionTableToValidate = validatedObject;
        this.openl = openl;
    }

    @SuppressWarnings("deprecation")
    public DesionTableValidationResult validate() {
        DecisionTable decisionTable = decisionTableToValidate.getDecisionTable();
        DecisionTableAnalyzer analyzer = new DecisionTableAnalyzer(decisionTable);
        
        DesionTableValidationResult result = null;
        
        if(canValidateDecisionTable(decisionTable, analyzer)) {
        	int n = decisionTable.getNumberOfConditions();
            IOpenMethod[] methodsForConditionValidation = new IOpenMethod[n];

            for (int i = 0; i < n; i++) {
                methodsForConditionValidation[i] = makeConditionMethod(decisionTable.getCondition(i), analyzer);
            }

            vars = makeVars(analyzer);

            IntBoolExp[][] expressions = makeExpressions(analyzer, methodsForConditionValidation);

            CDecisionTableImpl cdt = new CDecisionTableImpl(expressions, vars, decisionTableToValidate.isOverrideAscending());
//            System.out.println(" **** Checking " + decisionTable);
            DTCheckerImpl tableChecker = new DTCheckerImpl(cdt);

            List<Uncovered> completeness = tableChecker.checkCompleteness();
            List<Overlapping> overlappings = tableChecker.checkOverlappings();

            // System.out.println("C: " + completeness);
            // System.out.println("O:" + overlappings);

            result = new DesionTableValidationResult(decisionTable,
                overlappings.toArray(new Overlapping[overlappings.size()]),
                completeness.toArray(new Uncovered[completeness.size()]),
                decisionTableToValidate.getTransformer(),
                analyzer);
        } else {
            result = new DesionTableValidationResult(decisionTable);
        }
        
        return result;
    }
    
    private boolean canValidateDecisionTable(DecisionTable decisionTable, DecisionTableAnalyzer analyzer) {        
        
        // if there is no conditions in validated decision table, we don`t need to validate anything.
    	int ncond = decisionTable.getNumberOfConditions(); 
        if (ncond == 0) {
            return false;
        }        
        
        // if any value of a condition contains OpenL formula, we don`t validate anything! (we don't know how to do it now)
        
        for (int i = 0; i < ncond ; ++i) {
            if (analyzer.containsFormula(decisionTable.getCondition(i))) {
                return false;
            }
        }
        return true;
    }

    private Object findVar(IntExpArray vars, String name) {

        for (int i = 0; i < vars.size(); i++) {
            if (vars.elementAt(i).name().equals(name)) {
                return vars.elementAt(i);
            }
        }

        return null;
    }

    private IOpenMethod makeConditionMethod(ICondition condition, DecisionTableAnalyzer analyzer) {

        IMethodSignature newSignature = getNewSignature(condition, analyzer);

        IOpenClass methodType = JavaOpenClass.getOpenClass(IntBoolExp.class);
        IOpenClass declaringClass = analyzer.getDecisionTable().getDeclaringClass();
        String conditionName = condition.getName();

        OpenMethodHeader methodHeader = new OpenMethodHeader(conditionName, methodType, newSignature, declaringClass);

        IBindingContext bindingContext = new ModuleBindingContext(openl.getBinder().makeBindingContext(),
            (ModuleOpenClass) declaringClass);

        IOpenSourceCodeModule formulaSourceCode = condition.getConditionEvaluator().getFormalSourceCode(condition);

        return OpenLManager.makeMethod(openl, formulaSourceCode, methodHeader, bindingContext);
    }

    private IMethodSignature getNewSignature(ICondition condition, DecisionTableAnalyzer analyzer) {

        IParameterDeclaration[] paramDeclarations = condition.getParams(); // params from this column
        IParameterDeclaration[] referencedSignatureParams = analyzer.referencedSignatureParams(condition); // income params from the signature 

        return makeSignatureForCondition(paramDeclarations, referencedSignatureParams, analyzer);
    }

    private IntBoolExp[][] makeExpressions(DecisionTableAnalyzer analyzer, IOpenMethod[] methodsForConditionValidation) {

        int rulesNumber = decisionTableToValidate.getDecisionTable().getNumberOfRules();
        IntBoolExp[][] expressions = new IntBoolExp[rulesNumber][methodsForConditionValidation.length];

        for (int i = 0; i < rulesNumber; i++) {

            IntBoolExp[] ruleExpression = new IntBoolExp[methodsForConditionValidation.length];
            expressions[i] = ruleExpression;

            for (int j = 0; j < methodsForConditionValidation.length; j++) {
                ruleExpression[j] = makeExpression(i, decisionTableToValidate.getDecisionTable().getCondition(j), analyzer, 
                        methodsForConditionValidation[j]);
            }
        }

        return expressions;
    }

    private IntBoolExp makeExpression(int ruleN, ICondition conditionToValidate, DecisionTableAnalyzer analyzer, 
            IOpenMethod methodForConditionValidation) {
        

        if (conditionToValidate.isEmpty(ruleN)) {
            return new IntBoolExpConst(constrainer, true);
        }

        int paramsNum = methodForConditionValidation.getSignature().getNumberOfParameters();

        Object[] args = new Object[paramsNum];

        int tableArgsCount = paramsNum - conditionToValidate.getNumberOfParams();

        for (int i = 0; i < paramsNum; i++) {

            String name = methodForConditionValidation.getSignature().getParameterName(i);

            if (i < tableArgsCount) {
                args[i] = findVar(vars, name);
            } else {
                args[i] = transformValue(name, conditionToValidate, conditionToValidate.getParamValue(i - tableArgsCount, ruleN), analyzer);
            }
        }

        return (IntBoolExp) methodForConditionValidation.invoke(null, args, openl.getVm().getRuntimeEnv());
    }

    private IMethodSignature makeSignatureForCondition(IParameterDeclaration[] paramDeclarations,
            IParameterDeclaration[] referencedSignatureParams,
            DecisionTableAnalyzer analyzer) {

        List<IParameterDeclaration> parameters = new ArrayList<IParameterDeclaration>();

        parameters.addAll(getTransformedSignatureParams(referencedSignatureParams, analyzer));

        parameters.addAll(getTransformedLocalParams(paramDeclarations));

        return new MethodSignature(parameters.toArray(new IParameterDeclaration[parameters.size()]));
    }
    
    
    @SuppressWarnings("deprecation")
    private List<IParameterDeclaration> getTransformedLocalParams(IParameterDeclaration[] paramDeclarations) {
        
        List<IParameterDeclaration> transformeedParameters = new ArrayList<IParameterDeclaration>();
        
        for (IParameterDeclaration paramDeclaration : paramDeclarations) {

            IOpenClass newType = decisionTableToValidate.getTransformer().transformParameterType(paramDeclaration);

            if (newType == null) {
                transformeedParameters.add(paramDeclaration);
            } else {
                ParameterDeclaration parameter = new ParameterDeclaration(newType,
                    paramDeclaration.getName(),
                    paramDeclaration.getDirection());

                transformeedParameters.add(parameter);
            }
        }
        return transformeedParameters;
    }

    private List<IParameterDeclaration> getTransformedSignatureParams(IParameterDeclaration[] referencedSignatureParams, DecisionTableAnalyzer analyzer) {
        List<IParameterDeclaration> parameters = new ArrayList<IParameterDeclaration>();
        
        for (IParameterDeclaration paramDeclarationFromSignature : referencedSignatureParams) {

            IOpenClass newType = analyzer.transformSignatureType(paramDeclarationFromSignature, decisionTableToValidate);

            if (newType == null) {
                newType = paramDeclarationFromSignature.getType();
            }

            ParameterDeclaration parameter = new ParameterDeclaration(newType,
                paramDeclarationFromSignature.getName(),
                paramDeclarationFromSignature.getDirection());

            parameters.add(parameter);
        }
        return parameters;
    }

    @SuppressWarnings("deprecation")
    private IntExpArray makeVars(DecisionTableAnalyzer analyzer) {

        List<IntExp> vars = new ArrayList<IntExp>();

        Iterator<DecisionTableParamDescription> iterator = analyzer.tableParams();

        while (iterator.hasNext()) {

            DecisionTableParamDescription paramDescriptor = iterator.next();
            String varName = paramDescriptor.getParameterDeclaration().getName();
            IOpenClass varType = paramDescriptor.getParameterDeclaration().getType();

            IntVar var = decisionTableToValidate.getTransformer().makeSignatureVar(varName, varType, constrainer);

            if (var != null) {
                vars.add(var);
            } else {
                throw new OpenLRuntimeException(String.format("Could not create domain for %s", varName));
            }
        }

        return new IntExpArray(constrainer, vars);
    }

    @SuppressWarnings("deprecation")
    private Object transformValue(String name, ICondition condition, Object value, DecisionTableAnalyzer analyzer) {
        return decisionTableToValidate.getTransformer().transformLocalParameterValue(name, condition, value, analyzer);
    }

}
