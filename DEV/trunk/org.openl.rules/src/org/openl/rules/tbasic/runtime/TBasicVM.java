package org.openl.rules.tbasic.runtime;

import java.util.List;
import java.util.Map;

import org.openl.binding.impl.ControlSignal;
import org.openl.rules.tbasic.runtime.operations.RuntimeOperation;

/**
 * The <code>TBasicVM</code> class executes Algorithm logic. Besides execution
 * of operations list, the class provides logic to support GOTO to main method
 * and errors processing.
 * 
 */
public class TBasicVM {
    private TBasicVMDataContext mainContext;
    private TBasicVMDataContext currentContext;

    /**
     * Create an instance of <code>TBasicVM</code> initialized with main
     * Algorithm method operations and labels register.
     * 
     * @param operations
     * @param labels
     */
    public TBasicVM(List<RuntimeOperation> operations, Map<String, RuntimeOperation> labels) {
        mainContext = new TBasicVMDataContext(operations, labels, true);

        // in the first turn only main can be called
        currentContext = mainContext;
    }

    /**
     * Run the method of Algorithm. <code>TBasicVM</code> instance will run
     * operations which are considered in current context. <br>
     * Method also implements logic to handle all errors by user defined
     * handling method. <br>
     * Method should be called only for main Algorithm method, all sub methods
     * should be run using {@link #run(List, Map, TBasicContextHolderEnv)}.
     * 
     * @param environment The environment for execution.
     * @return The result of the method execution.
     */
    public Object run(TBasicContextHolderEnv environment, boolean debugMode) {
        assert environment != null;

        Object returnResult = null;

        // Run fail safe, in case of error allow user code to handle it
        // processing of error will be done in Algorithm main method
        try {

            returnResult = runAll(environment, debugMode);

        } catch (OpenLAlgorithmErrorSignal signal) {
            if (currentContext.isMainMethodContext()) {
                returnResult = AlgorithmErrorHelper.processError(signal.getCause(), environment);
            } else {
                throw signal;
            }
        } catch (ControlSignal signal) {
            // pass through all other OpenL signals
            throw signal;
        } catch (Throwable error) {
            if (currentContext.isMainMethodContext()) {
                returnResult = AlgorithmErrorHelper.processError(error, environment);
            } else {
                throw new OpenLAlgorithmErrorSignal(error);
            }
        }

        return returnResult;
    }

    /**
     * Run sub-method of Algorithm. <code>TBasicVM</code> instance will
     * execute the provided operations list. <br>
     * The sub-method can be called only within of execution of main Algorithm
     * method. However, the implementation doesn't put any restrictions.
     * 
     * @param methodSteps The list of operations to run.
     * @param methodLabels The labels register for sub-method.
     * @param environment The environment for execution.
     * @return The result of the method execution.
     */
    public Object run(List<RuntimeOperation> methodSteps, Map<String, RuntimeOperation> methodLabels,
            TBasicContextHolderEnv environment, boolean debugMode) {

        TBasicVMDataContext methodContext = new TBasicVMDataContext(methodSteps, methodLabels, false);

        TBasicVMDataContext previousContext = swapContext(methodContext);

        try {
            return run(environment, debugMode);
        } finally {
            swapContext(previousContext);
        }
    }

    /**
     * Run all operations in the current context.
     * 
     * @param environment The environment for execution.
     * @return The result of the method execution.
     */
    private Object runAll(TBasicContextHolderEnv environment, boolean debugMode) {

        RuntimeOperation operation = currentContext.getFirstOperation();
        Object previousStepResult = null;
        Object returnResult = null;

        while (operation != null) {
            Result operationResult;
            try {

                operationResult = operation.execute(environment, previousStepResult, debugMode);

                assert operationResult != null;
            } catch (OpenLAlgorithmGoToMainSignal signal) {
                operation = getLabeledOperation(signal.getLabel());
                continue;
            }

            if (operationResult.getReturnType() == ReturnType.GOTO) {
                assert operationResult.getValue() instanceof String;
                operation = getLabeledOperation((String) operationResult.getValue());
                continue;
            } else if (operationResult.getReturnType() == ReturnType.RETURN) {
                returnResult = operationResult.getValue();
                break;
            }

            operation = currentContext.getNextOperation(operation);
            previousStepResult = operationResult.getValue();
        }

        return returnResult;
    }

    /**
     * Searches for operation labeled with the provided label. <br>
     * The search will be done in current context and in the main context. If
     * label will be found in main context, the execution will be switched to
     * there.
     * 
     * @param label The label to switch to.
     * @return The labeled operation.
     */
    private RuntimeOperation getLabeledOperation(String label) {
        if (currentContext.isLabelInContext(label)) {
            return currentContext.getLabeledOperation(label);
        } else if (mainContext.isLabelInContext(label)) {
            goToLabelInMainContext(label);
        }
        throw new RuntimeException(String.format(
                "Unexpected error while execution of TBasic component: unknown label \"%s\"", label));
    }

    /**
     * Switch execution from current subroutine or function context to the main
     * Algorithm context. The OpenL control signal is used to do this. It passes
     * all the catches in OpenL and must be handled by <code>TBasicVM</code>
     * handle logic.
     * 
     * @param label The context to set as current.
     */
    private void goToLabelInMainContext(String label) {
        throw new OpenLAlgorithmGoToMainSignal(label);
    }

    /**
     * Set new current context and return the old one.
     * 
     * @param newContext The context to set as current.
     * @return The context which was set as current before.
     */
    private TBasicVMDataContext swapContext(TBasicVMDataContext newContext) {
        TBasicVMDataContext oldValue = currentContext;
        currentContext = newContext;
        return oldValue;
    }
}
