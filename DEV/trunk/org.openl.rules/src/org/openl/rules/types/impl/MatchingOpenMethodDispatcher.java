package org.openl.rules.types.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openl.exception.OpenLRuntimeException;
import org.openl.rules.context.IRulesRuntimeContext;
import org.openl.rules.dt.DecisionTable;
import org.openl.rules.lang.xls.binding.TableVersionComparator;
import org.openl.rules.lang.xls.binding.XlsMetaInfo;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.method.DefaultInvokerWithTrace;
import org.openl.rules.method.TracedObjectFactory;
import org.openl.rules.table.ATableTracerNode;
import org.openl.rules.table.properties.DimensionPropertiesMethodKey;
import org.openl.rules.table.properties.ITableProperties;
import org.openl.rules.table.properties.PropertiesHelper;
import org.openl.rules.types.OpenMethodDispatcher;
import org.openl.rules.validation.properties.dimentional.DispatcherTablesBuilder;
import org.openl.runtime.IRuntimeContext;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.vm.IRuntimeEnv;
import org.openl.vm.trace.Tracer;

/**
 * Represents group of methods(rules) overloaded by dimension properties.
 * 
 * TODO: refactor invoke functionality. Use {@link DefaultInvokerWithTrace}.
 */
public class MatchingOpenMethodDispatcher extends OpenMethodDispatcher {
    private IPropertiesContextMatcher matcher = new DefaultPropertiesContextMatcher();
    private ITablePropertiesSorter prioritySorter = new DefaultTablePropertiesSorter();

    private XlsModuleOpenClass moduleOpenClass;

    private List<IOpenMethod> candidatesSorted;
    
    private ATableTracerNode traceObject;

    public MatchingOpenMethodDispatcher(IOpenMethod method, XlsModuleOpenClass moduleOpenClass) {
        super();
        decorate(method);
        this.moduleOpenClass = moduleOpenClass;
    }

    @Override
    public IOpenClass getDeclaringClass() {
        return moduleOpenClass;
    }

    @Override
    public void addMethod(IOpenMethod candidate) {
        int pos = searchForTheSameTable(candidate);
        if (pos == -1) {
            // add new candidate
            super.addMethod(candidate);
            candidatesSorted = null;
        } else {
            // replace by newer or active
            if (new TableVersionComparator().compare(getCandidates().get(pos), candidate) > 0) {
                getCandidates().set(pos, candidate);
            }
        }
    }

    /**
     * For different versions of the some table we should use in dispatching
     * only the newest or active table.
     */
    private int searchForTheSameTable(IOpenMethod method) {
        DimensionPropertiesMethodKey methodKey = new DimensionPropertiesMethodKey(method);
        for (int i = 0; i < getCandidates().size(); i++) {
            IOpenMethod candidate = getCandidates().get(i);
            if (methodKey.equals(new DimensionPropertiesMethodKey(candidate))) {
                return i;
            }
        }
        return -1;
    }
    

    public Object invokeTraced(Object target, Object[] params, IRuntimeEnv env) {
    	Object returnResult = null;
    	traceObject = null;
        Tracer tracer = Tracer.getTracer();

        /**
         * this block is for overloaded by active property tables without any
         * dimension property. all not active tables should be ignored.
         */
        List<IOpenMethod> methods = getCandidates();
        Set<IOpenMethod> selected = new HashSet<IOpenMethod>(methods);

        traceObject = getTracedObject(selected, params);
        tracer.push(traceObject);
        try {
        	returnResult = super.invoke(target, params, env);
        } catch (RuntimeException e) {
            traceObject.setError(e);            
            throw e;
        } finally {
            tracer.pop();
        }
        return returnResult;
    }

    public Object invoke(Object target, Object[] params, IRuntimeEnv env) {
        if (Tracer.isTracerOn()) {
            return invokeTraced(target, params, env);
        } else {
            return super.invoke(target, params, env);
        }
    }

    private ATableTracerNode getTracedObject(Set<IOpenMethod> selected, Object[] params) {
        if (selected.size() == 1) {
            /**
             * if only one table left, we need traced object for this type of
             * table.
             */
            return TracedObjectFactory.getTracedObject((IOpenMethod) selected.toArray()[0], params);
        } else {
            /**
             * in other case trace object for overloaded methods.
             */
            try {
                DecisionTable dispatcherTable = (DecisionTable) getDispatcherTable().getMember();
                return new OverloadedMethodChoiceTraceObject(dispatcherTable, params, getCandidates());
            } catch (OpenLRuntimeException e) {
                ATableTracerNode traceObject = TracedObjectFactory.getTracedObject((IOpenMethod) selected.toArray()[0],
                        params);
                traceObject.setError(e);
                return traceObject;
            }

        }
    }

    @Override
    protected IOpenMethod findMatchingMethod(List<IOpenMethod> candidates, IRuntimeContext context) {

        Set<IOpenMethod> selected = new HashSet<IOpenMethod>(candidates);

        selectCandidates(selected, (IRulesRuntimeContext) context);        
        
        maxMinSelectCandidates(selected, (IRulesRuntimeContext) context);

        switch (selected.size()) {
            case 0:
                // TODO add more detailed information about error, consider
                // context values printout, may be log of constraints that
                // removed candidates
                throw new OpenLRuntimeException(String.format(
                        "No matching methods for the context. Details: \n%1$s\nContext: %2$s", toString(candidates),
                        context.toString()));

            case 1:
            	
            	IOpenMethod matchingMethod = selected.iterator().next();
            	// TODO : refactor
            	// traceObject shouldn`t be the field of the class.
            	// trace information should be set only into trace method.
            	//
            	if (Tracer.isTracerOn()) {
                    traceObject.setResult(matchingMethod);
                }
            	
                return matchingMethod;

            default:
                // TODO add more detailed information about error, consider
                // context values printout, may be log of constraints,
                // list of remaining methods with properties
                throw new OpenLRuntimeException(String.format(
                        "Ambiguous method dispatch. Details: \n%1$s\nContext: %2$s", toString(candidates),
                        context.toString()));
        }

    }

    private TableSyntaxNode[] getTableSyntaxNodes() {
        XlsMetaInfo xlsMetaInfo = moduleOpenClass.getXlsMetaInfo();
        return xlsMetaInfo.getXlsModuleNode().getXlsTableSyntaxNodes();
    }

    public TableSyntaxNode getDispatcherTable() {
        TableSyntaxNode[] tables = getTableSyntaxNodes();
        for (TableSyntaxNode tsn : tables) {
            if (DispatcherTablesBuilder.isDispatcherTable(tsn) && tsn.getMember().getName().endsWith(getName())) {
                return tsn;
            }
        }
        throw new OpenLRuntimeException(String.format("There is no dispatcher table for [%s] method.", getName()));
    }

    @Override
    public IMemberMetaInfo getInfo() {
        return getDispatcherTable().getMember().getInfo();
    }

    private void maxMinSelectCandidates(Set<IOpenMethod> selected, IRulesRuntimeContext context) {
        //If more that one method
        if (selected.size() > 1){
            //Find the most high priority method
            IOpenMethod mostPriority = null;
            for (IOpenMethod candidate : selected) {
                if (mostPriority == null){
                    mostPriority = candidate;
                }else{
                    if (prioritySorter.getMethodsComparator().compare(mostPriority, candidate) > 0){
                        mostPriority = candidate;
                    }
                }
            }
            List<IOpenMethod> notPriorMethods = new ArrayList<IOpenMethod>();
            //Remove methods those priority not equals to the most high priority method 
            for (IOpenMethod candidate : selected) {
                if (prioritySorter.getMethodsComparator().compare(candidate, mostPriority) != 0) {
                    notPriorMethods.add(candidate);
                }
            }
            selected.removeAll(notPriorMethods);
        }
    }

    // <<< INSERT MatchingProperties >>>
	private void selectCandidates(Set<IOpenMethod> selected, IRulesRuntimeContext context) {
		selectCandidatesByProperty("effectiveDate", selected, context);
		selectCandidatesByProperty("expirationDate", selected, context);
		selectCandidatesByProperty("startRequestDate", selected, context);
		selectCandidatesByProperty("endRequestDate", selected, context);
		selectCandidatesByProperty("lob", selected, context);
		selectCandidatesByProperty("usregion", selected, context);
		selectCandidatesByProperty("country", selected, context);
		selectCandidatesByProperty("currency", selected, context);
		selectCandidatesByProperty("lang", selected, context);
		selectCandidatesByProperty("state", selected, context);
		selectCandidatesByProperty("region", selected, context);
	}
    // <<< END INSERT MatchingProperties >>>

    private void selectCandidatesByProperty(String propName, Set<IOpenMethod> selected, IRulesRuntimeContext context) {

        List<IOpenMethod> nomatched = new ArrayList<IOpenMethod>();
        List<IOpenMethod> matchedByDefault = new ArrayList<IOpenMethod>();

        boolean matchExists = false;

        for (IOpenMethod method : selected) {
            ITableProperties props = PropertiesHelper.getTableProperties(method);
            MatchingResult res = matcher.match(propName, props, context);

            switch (res) {
                case NO_MATCH:
                    nomatched.add(method);
                    break;
                case MATCH_BY_DEFAULT:
                    matchedByDefault.add(method);
                    break;
                case MATCH:
                    matchExists = true;
            }
        }

        selected.removeAll(nomatched);

        if (matchExists) {
            selected.removeAll(matchedByDefault);
        }
    }

    private String toString(List<IOpenMethod> methods) {

        StringBuilder builder = new StringBuilder();
        builder.append("Candidates: {\n");

        for (IOpenMethod method : methods) {
            ITableProperties tableProperties = PropertiesHelper.getTableProperties(method);
            builder.append(tableProperties.toString());
            builder.append("\n");
        }

        builder.append("}\n");

        return builder.toString();
    }

    @Override
    public List<IOpenMethod> getCandidates() {
        if (candidatesSorted == null) {
            candidatesSorted = prioritySorter.sort(super.getCandidates());
        }
        return candidatesSorted;
    }
}
