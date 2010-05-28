package org.openl.rules.webstudio.web;

import org.apache.commons.lang.StringUtils;
import org.openl.rules.ui.ProjectModel;
import org.openl.rules.ui.TraceHelper;
import org.openl.rules.ui.WebStudio;
import org.openl.rules.ui.tree.richfaces.TreeBuilder;
import org.openl.rules.ui.tree.richfaces.TraceTreeBuilder;
import org.openl.rules.webstudio.web.util.Constants;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.rules.webtools.jsf.FacesUtils;
import org.openl.util.tree.ITreeElement;
import org.openl.vm.Tracer;
import org.richfaces.model.TreeNode;

/**
 * Request scope managed bean providing logic for trace tree page of OpenL Studio.
 */
public class TraceTreeBean {

    public static final String TRACER_NAME = "tracer";

    public TraceTreeBean() {
    }

    public TreeNode<?> getTree() {
        WebStudio studio = WebStudioUtils.getWebStudio();
        String uri = FacesUtils.getRequestParameter(Constants.REQUEST_PARAM_URI);
        if (StringUtils.isNotBlank(uri)) {
            studio.setTableUri(uri);
            ProjectModel model = studio.getModel();
            Tracer tracer = model.traceElement(uri);
            TraceHelper traceHelper = (TraceHelper) FacesUtils.getSessionParam(TRACER_NAME);
            
            if (traceHelper == null) {
                traceHelper = new TraceHelper();
            }
            
            ITreeElement<?> tree = traceHelper.getTraceTree(tracer);
            if (tree != null) {
                TreeBuilder treeBuilder = new TraceTreeBuilder(tree, traceHelper);
                TreeNode<?> rfTree = treeBuilder.build();
                return rfTree;
            }
        }
        return null;
    }

}
