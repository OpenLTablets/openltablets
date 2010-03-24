package org.openl.rules.ui.tree.richfaces;

import org.openl.base.INamedThing;
import org.openl.meta.DoubleValue;
import org.openl.rules.web.jsf.util.FacesUtils;
import org.openl.rules.webstudio.web.util.Constants;
import org.openl.util.StringTool;
import org.openl.util.tree.ITreeElement;

public class ExplainTreeBuilder extends TreeBuilder {

    public ExplainTreeBuilder(ITreeElement<?> root) {
        super(root);
    }

    @Override
    protected String getDisplayName(Object obj, int mode) {
        return super.getDisplayName(obj, mode + 1);
    }

    @Override
    protected String getUrl(ITreeElement<?> element) {
        DoubleValue dv = (DoubleValue) element;
        String url = dv.getMetaInfo() == null ? null : dv.getMetaInfo().getSourceUrl();
        return FacesUtils.getContextPath() + "/jsp/showExplainTable.jsp?"
            + Constants.REQUEST_PARAM_URI + "=" + StringTool.encodeURL("" + url)
            + "&text=" + getDisplayName(element, INamedThing.REGULAR);
    }

}
