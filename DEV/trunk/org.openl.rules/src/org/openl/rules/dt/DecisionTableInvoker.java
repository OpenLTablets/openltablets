package org.openl.rules.dt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.base.INamedThing;
import org.openl.binding.MethodUtil;
import org.openl.domain.IIntIterator;
import org.openl.exception.OpenLRuntimeException;
import org.openl.rules.dt.algorithm.FailOnMissException;
import org.openl.rules.dt.element.IAction;
import org.openl.rules.dt.trace.DecisionTableTraceObject;
import org.openl.rules.method.RulesMethodInvoker;
import org.openl.vm.IRuntimeEnv;
import org.openl.vm.trace.Tracer;

/**
 * Invoker for {@link DecisionTable}.
 * 
 * @author DLiauchuk
 *
 */
public class DecisionTableInvoker extends RulesMethodInvoker {
    
    private final Log LOG = LogFactory.getLog(DecisionTableInvoker.class);
    
    public DecisionTableInvoker(DecisionTable decisionTable) {
        super(decisionTable);
    }
    
    @Override
    public DecisionTable getInvokableMethod() {    
        return (DecisionTable)super.getInvokableMethod();
    }
    
    public boolean canInvoke() {
        return getInvokableMethod().getAlgorithm() != null;
    }
    
    private Object invokeOptimized(Object target, Object[] params, IRuntimeEnv env) {

        IIntIterator rules = getInvokableMethod().getAlgorithm().checkedRules(target, params, env);

        Object returnValue = null;
        boolean atLeastOneRuleFired = false;

        while (rules.hasNext()) {
            atLeastOneRuleFired = true;
            int ruleN = rules.nextInt();

            returnValue = getReturn(target, params, env, ruleN);
            if (returnValue != null) {
                return returnValue;
            }
        }

        if (!atLeastOneRuleFired && getInvokableMethod().shouldFailOnMiss()) {

            String method = MethodUtil.printMethodWithParameterValues(getInvokableMethod().getMethod(), params, 
                INamedThing.REGULAR);
            String message = String.format("%s failed to match any rule condition", method);

            throw new FailOnMissException(message, getInvokableMethod(), params);
        }

        return returnValue;
    }
    
    private Object invokeTracedOptimized(Object target, Object[] params, IRuntimeEnv env) {        
        Tracer tracer = Tracer.getTracer();
        
        if (tracer == null) {
            return invokeOptimized(target, params, env);
        }

        Object returnValue = null;

        DecisionTableTraceObject traceObject = (DecisionTableTraceObject)getTraceObject(params);
        tracer.push(traceObject);

        try {
            IIntIterator rules = getInvokableMethod().getAlgorithm().checkedRules(target, params, env);

            while (rules.hasNext()) {

                int ruleN = rules.nextInt();

                try {
                    tracer.push(traceObject.traceRule(ruleN));

                    returnValue = getReturn(target, params, env, ruleN);
                    if (returnValue != null) {
                        traceObject.setResult(returnValue);
                        return returnValue;
                    }
                } finally {
                    tracer.pop();
                }
            }
        } catch (RuntimeException e) {
            addErrorToTrace(traceObject, e);
        } finally {
            tracer.pop();
        }

        return returnValue;
    }

	protected Object getReturn(Object target, Object[] params, IRuntimeEnv env, int ruleN) {
		Object returnValue = null;
		for (int j = 0; j < getInvokableMethod().getActionRows().length; j++) {
			IAction action = getInvokableMethod().getActionRows()[j];
			
		    Object actionResult = action.executeAction(ruleN, target, params, env);

		    if (action.isReturnAction() && returnValue == null && (actionResult != null || (action.getParamValues()!= null
		                    && action.getParamValues()[ruleN] != null))) {
		        returnValue = actionResult;
		    }
		}
		return returnValue;
	}
    
    private void addErrorToTrace(DecisionTableTraceObject traceObject, Throwable e) {
        traceObject.setError(e);
        LOG.error("Error when tracing DT rule", e);
        throw new OpenLRuntimeException(e);
    }
    
    public Object invokeTraced(Object target, Object[] params, IRuntimeEnv env) {
        return invokeTracedOptimized(target, params, env);
    }
    
    public Object invokeSimple(Object target, Object[] params, IRuntimeEnv env) {
        return invokeOptimized(target, params, env);
    }    
}
