package org.openl.rules.dt.validator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openl.OpenL;
import org.openl.binding.IBindingContext;
import org.openl.binding.impl.module.ModuleBindingContext;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.engine.OpenLManager;
import org.openl.exception.OpenLRuntimeException;
import org.openl.rules.dt.DecisionTable;
import org.openl.rules.dt.element.ICondition;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.IParameterDeclaration;
import org.openl.types.impl.MethodSignature;
import org.openl.types.impl.OpenMethodHeader;
import org.openl.types.impl.ParameterDeclaration;
import org.openl.types.java.JavaOpenClass;

import com.exigen.ie.constrainer.Constrainer;
import com.exigen.ie.constrainer.IntBoolExp;
import com.exigen.ie.constrainer.IntBoolExpConst;
import com.exigen.ie.constrainer.IntExp;
import com.exigen.ie.constrainer.IntExpArray;
import com.exigen.ie.constrainer.IntVar;
import com.exigen.ie.constrainer.consistencyChecking.DTCheckerImpl;
import com.exigen.ie.constrainer.consistencyChecking.Overlapping;
import com.exigen.ie.constrainer.consistencyChecking.Uncovered;
import com.exigen.ie.constrainer.consistencyChecking.DTCheckerImpl.CDecisionTableImpl;

public class ValidationAlgorithm {

    private IDecisionTableValidatedObject decisionTableToValidate;
    private ICondition[] originalDecisionTableConditions;
    private IntExpArray vars;
    private OpenL openl;

    private Constrainer constrainer = new Constrainer("Validation");

    public ValidationAlgorithm(IDecisionTableValidatedObject validatedObject, OpenL openl) {
        this.decisionTableToValidate = validatedObject;
        this.originalDecisionTableConditions = validatedObject.getDecisionTable().getConditionRows();
        this.openl = openl;
    }

    public DesionTableValidationResult validate() {
        DecisionTable decisionTable = decisionTableToValidate.getDecisionTable();
        DecisionTableAnalyzer analyzer = new DecisionTableAnalyzer(decisionTable);
        
        DesionTableValidationResult result = null;
        
        if(canValidateDecisionTable(decisionTable, analyzer)) {
            IOpenMethod[] methodsForConditionValidation = new IOpenMethod[originalDecisionTableConditions.length];

            for (int i = 0; i < originalDecisionTableConditions.length; i++) {
                methodsForConditionValidation[i] = makeConditionMethod(originalDecisionTableConditions[i], analyzer);
            }

            vars = makeVars(analyzer);

            IntBoolExp[][] expressions = makeExpressions(analyzer, methodsForConditionValidation);

            CDecisionTableImpl cdt = new CDecisionTableImpl(expressions, vars);
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
        if (originalDecisionTableConditions.length == 0) {
            return false;
        }        
        
        // if any value of a condition contains OpenL formula, we don`t validate anything! (we don't know how to do it now)
        for (ICondition condition : originalDecisionTableConditions) {
            if (analyzer.containsFormula(condition)) {
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
                ruleExpression[j] = makeExpression(i, originalDecisionTableConditions[j], analyzer, 
                        methodsForConditionValidation[j]);
            }
        }

        return expressions;
    }

    private IntBoolExp makeExpression(int rule, ICondition conditionToValidate, DecisionTableAnalyzer analyzer, 
            IOpenMethod methodForConditionValidation) {
        
        Object[] values = conditionToValidate.getParamValues()[rule];

        if (values == null) {
            return new IntBoolExpConst(constrainer, true);
        }

        int paramsNum = methodForConditionValidation.getSignature().getNumberOfParameters();

        Object[] args = new Object[paramsNum];

        int tableArgsCount = paramsNum - values.length;

        for (int i = 0; i < paramsNum; i++) {

            String name = methodForConditionValidation.getSignature().getParameterName(i);

            if (i < tableArgsCount) {
                args[i] = findVar(vars, name);
            } else {
                args[i] = transformValue(name, conditionToValidate, values[i - tableArgsCount], analyzer);
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

    private Object transformValue(String name, ICondition condition, Object value, DecisionTableAnalyzer analyzer) {
        return decisionTableToValidate.getTransformer().transformParameterValue(name, condition, value, analyzer);
    }

}
