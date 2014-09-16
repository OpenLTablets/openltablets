package org.openl.rules.webstudio.web;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.openl.commons.web.jsf.FacesUtils;
import org.openl.rules.ui.Explanation;
import org.openl.rules.ui.Explanator;
import org.openl.rules.ui.tree.richfaces.ExplainTreeBuilder;
import org.openl.rules.ui.tree.richfaces.TreeNode;
import org.openl.rules.webstudio.web.util.Constants;
import org.openl.util.tree.ITreeElement;

/**
 * Request scope managed bean providing logic for explain tree page of OpenL Studio.
 */
@ManagedBean
@RequestScoped
public class ExplainTreeBean {

    public ExplainTreeBean() {
    }

    public TreeNode getTree() {
        Explanator explanator = (Explanator) FacesUtils.getSessionParam(Constants.SESSION_PARAM_EXPLANATOR);
        String rootID = FacesUtils.getRequestParameter("rootID");
        Explanation explanation = explanator.getExplanation(rootID);
        ITreeElement<?> tree = explanation.getExplainTree();
        if (tree != null) {
            TreeNode rfTree = new ExplainTreeBuilder().buildWithRoot(tree);
            return rfTree;
        }
        return null;
    }

}
