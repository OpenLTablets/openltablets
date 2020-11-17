package org.openl.rules.webstudio.web.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.openl.message.OpenLMessage;
import org.openl.message.OpenLMessagesUtils;
import org.openl.rules.calc.SpreadsheetResult;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.lang.xls.syntax.TableSyntaxNodeAdapter;
import org.openl.rules.method.ExecutableRulesMethod;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.IOpenLTable;
import org.openl.rules.table.ui.RegionGridSelector;
import org.openl.rules.table.ui.filters.ColorGridFilter;
import org.openl.rules.table.ui.filters.IColorFilter;
import org.openl.rules.table.ui.filters.IGridFilter;
import org.openl.rules.testmethod.ParameterWithValueDeclaration;
import org.openl.rules.ui.ObjectViewer;
import org.openl.rules.ui.TraceHelper;
import org.openl.rules.webstudio.web.trace.node.*;
import org.openl.rules.webstudio.web.util.Constants;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.util.CollectionUtils;

/**
 * Request scope managed bean for showTraceTable page.
 */
@SuppressWarnings("deprecation")
@ManagedBean
@RequestScoped
public class ShowTraceTableBean {

    private ITracerObject tto;

    public ShowTraceTableBean() {
        TraceHelper traceHelper = WebStudioUtils.getTraceHelper();

        String traceElementIdParam = WebStudioUtils.getRequestParameter(Constants.REQUEST_PARAM_ID);
        int traceElementId = -100;
        if (traceElementIdParam != null) {
            traceElementId = Integer.parseInt(traceElementIdParam);
        }
        tto = traceHelper.getTableTracer(traceElementId);
        if (tto == null) {
            throw new NullPointerException(
                String.format("A trace object with ID=[%s] is absent.", traceElementIdParam));
        }
    }

    public IOpenLTable getTraceTable() {
        TableSyntaxNode tsn = getTableSyntaxNode(tto);
        return new TableSyntaxNodeAdapter(tsn);
    }

    private TableSyntaxNode getTableSyntaxNode(ITracerObject tto) {
        TableSyntaxNode syntaxNode = null;

        if (tto instanceof ATableTracerNode) {
            syntaxNode = ((ATableTracerNode) tto).getTraceObject().getSyntaxNode();
        } else if (tto instanceof RefToTracerNodeObject) {
            return getTableSyntaxNode(((RefToTracerNodeObject) tto).getOriginalTracerNode());
        }

        if (syntaxNode == null) {
            // Default approach for TBasic nodes or if traced object does not have syntax node
            String uri = tto.getUri();
            return WebStudioUtils.getProjectModel().findNode(uri);
        } else {
            return syntaxNode;
        }
    }

    public IGridFilter[] getTraceFilters() {

        IColorFilter defaultColorFilter = WebStudioUtils.getProjectModel().getFilterHolder().makeFilter();
        if (tto instanceof DecisionTableTraceObject || tto instanceof DTRuleTracerLeaf) {
            return new DecisionTableTraceFilterFactory(tto, defaultColorFilter).createFilters();
        }

        List<IGridRegion> regions = new ArrayList<>();

        List<IGridRegion> r = RegionsExtractor.getGridRegions(tto);
        if (CollectionUtils.isNotEmpty(r)) {
            regions.addAll(r);
        } else {
            fillRegions(tto, regions);
        }

        IGridRegion[] aRegions = new IGridRegion[regions.size()];
        aRegions = regions.toArray(aRegions);

        RegionGridSelector gridSelector = new RegionGridSelector(aRegions, true);
        ColorGridFilter colorGridFilter = new ColorGridFilter(gridSelector, defaultColorFilter);
        return new IGridFilter[] { colorGridFilter };
    }

    public ParameterWithValueDeclaration[] getInputParameters() {
        ATableTracerNode tracerNode = getTableTracerNode(this.tto);
        if (tracerNode == null || tracerNode.getTraceObject() == null) {
            return null;
        }

        ExecutableRulesMethod tracedMethod = tracerNode.getTraceObject();
        Object[] parameters = tracerNode.getParameters();
        ParameterWithValueDeclaration[] paramDescriptions = new ParameterWithValueDeclaration[parameters.length];
        for (int i = 0; i < paramDescriptions.length; i++) {
            paramDescriptions[i] = new ParameterWithValueDeclaration(tracedMethod.getSignature().getParameterName(i),
                parameters[i], tracedMethod.getSignature().getParameterType(i));
        }
        return paramDescriptions;
    }

    public ParameterWithValueDeclaration getContext() {
        ATableTracerNode tracerNode = getTableTracerNode(this.tto);
        if (tracerNode == null) {
            return null;
        }

        return new ParameterWithValueDeclaration("context", tracerNode.getContext());
    }

    private ATableTracerNode getTableTracerNode(ITracerObject tto) {
        ATableTracerNode tracerNode = null;
        if (tto instanceof RefToTracerNodeObject) {
            tracerNode = getTableTracerNode(((RefToTracerNodeObject) tto).getOriginalTracerNode());
        } else if (tto instanceof ATableTracerNode) {
            tracerNode = (ATableTracerNode) tto;
        } else if (tto != null && tto.getParent() instanceof ATableTracerNode) {
            tracerNode = (ATableTracerNode) tto.getParent();
        }
        return tracerNode;
    }

    public ParameterWithValueDeclaration getReturnResult() {
        ParameterWithValueDeclaration returnResult = null;
        Object result = tto.getResult();
        if (result != null) {
            returnResult = new ParameterWithValueDeclaration("return", result);
        }
        return returnResult;
    }

    public List<OpenLMessage> getErrors() {
        return getErrors(tto);
    }

    private List<OpenLMessage> getErrors(ITracerObject tto) {
        if (tto instanceof ATableTracerNode) {
            Throwable error = ((ATableTracerNode) tto).getError();

            if (error != null) {
                Throwable cause = error.getCause();
                if (cause != null) {
                    return OpenLMessagesUtils.newErrorMessages(cause);
                }
                return OpenLMessagesUtils.newErrorMessages(error);
            }
        } else if (tto instanceof RefToTracerNodeObject) {
            return getErrors(((RefToTracerNodeObject) tto).getOriginalTracerNode());
        }

        return Collections.emptyList();
    }

    public static boolean isSpreadsheetResult(Object value) {
        return value instanceof SpreadsheetResult && ((SpreadsheetResult) value).getLogicalTable() != null;
    }

    public String getFormattedSpreadsheetResult(Object value) {
        return ObjectViewer.displaySpreadsheetResultNoFilters((SpreadsheetResult) value);
    }

    private void fillRegions(ITracerObject tto, List<IGridRegion> regions) {
        for (ITracerObject child : tto.getChildren()) {
            List<IGridRegion> r = RegionsExtractor.getGridRegions(child);
            if (CollectionUtils.isNotEmpty(r)) {
                regions.addAll(r);
            } else if (!child.isLeaf()) {
                fillRegions(child, regions);
            }
        }
    }

}
