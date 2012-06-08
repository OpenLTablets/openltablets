package org.openl.rules.ui.tree.richfaces;

import java.util.Iterator;

import org.openl.commons.web.jsf.FacesUtils;
import org.openl.rules.ui.TraceHelper;
import org.openl.rules.webstudio.web.util.Constants;
import org.openl.util.tree.ITreeElement;
import org.openl.rules.dt.trace.*;

public class TraceTreeBuilder extends TreeBuilder {
    private static final int UNSUCCESSFUL = 0;
    private static final int SUCCESSFUL_WITHOUT_RESULT = 1;
    private static final int SUCCESSFUL_WITH_RESULT = 2;

    private TraceHelper traceHelper;

    public TraceTreeBuilder(ITreeElement<?> root, TraceHelper traceHelper) {
        super(root);
        this.traceHelper = traceHelper;
    }

    @Override
    protected String getUrl(ITreeElement<?> element) {
        return FacesUtils.getContextPath() + "/faces/pages/modules/trace/showTraceTable.xhtml?"
            + Constants.REQUEST_PARAM_ID + "=" + traceHelper.getNodeKey(element);
    }

    @Override
    protected int getState(ITreeElement<?> element) {
        if (element instanceof DTConditionTraceObject) {
            DTConditionTraceObject condition = (DTConditionTraceObject) element;
            return condition.isSuccessful() ? (condition.hasRuleResult() ? SUCCESSFUL_WITH_RESULT : SUCCESSFUL_WITHOUT_RESULT) : UNSUCCESSFUL;
        }

        return super.getState(element);
    }

    @Override
    protected Iterator<?> getChildrenIterator(ITreeElement<?> source) {
        if (source instanceof DecisionTableTraceObject) {
            DecisionTableTraceObject parent = (DecisionTableTraceObject) source;
            return traceHelper.isDetailedTraceTree() ? parent.getChildren() : parent.getTraceResults();
        }

        return super.getChildrenIterator(source);
    }

}
