package org.openl.rules.webstudio.web.trace;

import org.apache.commons.collections4.CollectionUtils;
import org.openl.commons.web.jsf.FacesUtils;
import org.openl.message.OpenLMessage;
import org.openl.message.OpenLMessagesUtils;
import org.openl.rules.calc.SpreadsheetResult;
import org.openl.rules.calc.result.SpreadsheetResultHelper;
import org.openl.rules.dt.trace.DTRuleTracerLeaf;
import org.openl.rules.dt.trace.DecisionTableTraceObject;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.lang.xls.syntax.TableSyntaxNodeAdapter;
import org.openl.rules.method.ExecutableRulesMethod;
import org.openl.rules.table.ATableTracerNode;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.IOpenLTable;
import org.openl.rules.table.ITableTracerObject;
import org.openl.rules.table.ui.RegionGridSelector;
import org.openl.rules.table.ui.filters.ColorGridFilter;
import org.openl.rules.table.ui.filters.IColorFilter;
import org.openl.rules.table.ui.filters.IGridFilter;
import org.openl.rules.testmethod.ParameterWithValueDeclaration;
import org.openl.rules.ui.DecisionTableTraceFilterFactory;
import org.openl.rules.ui.ObjectViewer;
import org.openl.rules.ui.TraceHelper;
import org.openl.rules.ui.WebStudio;
import org.openl.rules.webstudio.web.util.Constants;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.types.IParameterDeclaration;
import org.openl.vm.trace.ITracerObject;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Request scope managed bean for showTraceTable page.
 */
@ManagedBean
@RequestScoped
public class ShowTraceTableBean {

    private ITableTracerObject tto;

    public ShowTraceTableBean() {
        WebStudio studio = WebStudioUtils.getWebStudio();
        TraceHelper traceHelper = studio.getTraceHelper();

        String traceElementIdParam = FacesUtils.getRequestParameter(Constants.REQUEST_PARAM_ID);
        int traceElementId = -100;
        if (traceElementIdParam != null) {
            traceElementId = Integer.parseInt(traceElementIdParam);
        }
        tto = traceHelper.getTableTracer(traceElementId);
        if (tto == null) {
            throw new NullPointerException("A trace object with ID=[" + traceElementIdParam + "] is absent.");
        }
    }

    public IOpenLTable getTraceTable() {
        TableSyntaxNode tsn = tto.getTableSyntaxNode();
        return new TableSyntaxNodeAdapter(tsn);
    }

    public IGridFilter[] getTraceFilters() {

        IColorFilter defaultColorFilter = WebStudioUtils.getProjectModel().getFilterHolder().makeFilter();
        if (tto instanceof DecisionTableTraceObject || tto instanceof DTRuleTracerLeaf) {
            return new DecisionTableTraceFilterFactory(tto, defaultColorFilter).createFilters();
        }

        List<IGridRegion> regions = new ArrayList<IGridRegion>();

        List<IGridRegion> r = tto.getGridRegions();
        if (CollectionUtils.isNotEmpty(r)) {
            regions.addAll(r);
        } else {
            fillRegions(tto, regions);
        }

        IGridRegion[] aRegions = new IGridRegion[regions.size()];
        aRegions = regions.toArray(aRegions);

        RegionGridSelector gridSelector = new RegionGridSelector(aRegions, true);
        ColorGridFilter colorGridFilter = new ColorGridFilter(gridSelector, defaultColorFilter);
        return new IGridFilter[]{colorGridFilter};
    }

    public ParameterWithValueDeclaration[] getInputParameters() {
        ATableTracerNode tracerNode = null;
        if (tto instanceof ATableTracerNode) {
            tracerNode = (ATableTracerNode) tto;
        } else if (tto != null && tto.getParent() instanceof ATableTracerNode) {
            // ATableTracerLeaf
            tracerNode = (ATableTracerNode) tto.getParent();
        }
        if (tracerNode == null || !(tracerNode.getTraceObject() instanceof ExecutableRulesMethod)) {
            return null;
        }

        ExecutableRulesMethod tracedMethod = (ExecutableRulesMethod) tracerNode.getTraceObject();
        Object[] parameters = tracerNode.getParameters();
        ParameterWithValueDeclaration[] paramDescriptions = new ParameterWithValueDeclaration[parameters.length];
        for (int i = 0; i < paramDescriptions.length; i++) {
            paramDescriptions[i] = new ParameterWithValueDeclaration(tracedMethod.getSignature().getParameterName(i),
                    parameters[i], IParameterDeclaration.IN);
        }
        return paramDescriptions;
    }

    public ParameterWithValueDeclaration getReturnResult() {
        ParameterWithValueDeclaration returnResult = null;
        Object result = tto.getResult();
        if (result != null) {
            returnResult = new ParameterWithValueDeclaration("return", result, IParameterDeclaration.OUT);
        }
        return returnResult;
    }

    public List<OpenLMessage> getErrors() {
        if (tto instanceof ATableTracerNode) {
            Throwable error = ((ATableTracerNode) tto).getError();

            if (error != null) {
                Throwable cause = error.getCause();
                if (cause != null) {
                    return OpenLMessagesUtils.newMessages(cause);
                }
                return OpenLMessagesUtils.newMessages(error);
            }
        }

        return Collections.emptyList();
    }

    public static boolean isSpreadsheetResult(Object value) {
        return value != null && SpreadsheetResultHelper.isSpreadsheetResult(value.getClass());
    }

    public String getFormattedSpreadsheetResult(Object value) {
        return ObjectViewer.displaySpreadsheetResultNoFilters((SpreadsheetResult) value);
    }

    private void fillRegions(ITracerObject tto, List<IGridRegion> regions) {
        for (ITracerObject child : tto.getChildren()) {
            List<IGridRegion> r = ((ITableTracerObject)child).getGridRegions();
            if (CollectionUtils.isNotEmpty(r)) {
                regions.addAll(r);
            } else if (!child.isLeaf()) {
                fillRegions(child, regions);
            }
        }
    }
}
