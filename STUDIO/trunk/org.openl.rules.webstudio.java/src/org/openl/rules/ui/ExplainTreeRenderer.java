/**
 * Created Jan 26, 2007
 */
package org.openl.rules.ui;

import org.openl.base.INamedThing;
import org.openl.meta.DoubleValue;
import org.openl.rules.webstudio.web.util.Constants;
import org.openl.util.StringTool;
import org.openl.util.tree.ITreeElement;

/**
 * @author snshor
 */
public class ExplainTreeRenderer extends DTreeRenderer {

    static String[][] icons = {
            { "value", "webresource/images/value.gif", "webresource/images/value.gif", "webresource/images/value.gif",
                    "webresource/images/value.gif", },
            { "formula.+", "webresource/images/operator-plus.gif", "webresource/images/operator-plus.gif",
                    "webresource/images/operator-plus.gif", "webresource/images/operator-plus.gif" },
            { "formula.*", "webresource/images/operator-mul.gif", "webresource/images/operator-mul.gif",
                    "webresource/images/operator-mul.gif", "webresource/images/operator-mul.gif", },
            { "formula./", "webresource/images/operator-div.gif", "webresource/images/operator-div.gif",
                    "webresource/images/operator-div.gif", "webresource/images/operator-div.gif", },
            { "formula.-", "webresource/images/operator-min.gif", "webresource/images/operator-min.gif",
                    "webresource/images/operator-min.gif", "webresource/images/operator-min.gif", },
            { "formula", "webresource/images/operator.gif", "webresource/images/operator.gif",
                    "webresource/images/operator.gif", "webresource/images/operator.gif" },
            { "function", "webresource/images/method.gif", "webresource/images/method.gif",
                    "webresource/images/method.gif", "webresource/images/method.gif" } };

    /**
     * @param jsp
     * @param frame
     * @param icons
     */
    public ExplainTreeRenderer(String jsp, String frame) {
        super(jsp, frame, icons);
    }

    @Override
    public void cacheElement(ITreeElement<?> element) {
        // TODO Auto-generated method stub
    }

    @Override
    protected String getDisplayName(Object obj, int mode) {

        return super.getDisplayName(obj, mode + 1);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.rules.ui.DTreeRenderer#makeURL(org.openl.util.ITreeElement)
     */
    @Override
    protected String makeURL(ITreeElement element) {
        DoubleValue dv = (DoubleValue) element;
        String url = dv.getMetaInfo() == null ? null : dv.getMetaInfo().getSourceUrl();
        // if (url != null)
        // {
        // url = WebTool.makeXlsOrDocUrl(url);
        // }
        return targetJsp + "?" + Constants.REQUEST_PARAM_URI + "=" + StringTool.encodeURL("" + url) + "&text="
                + getDisplayName(element, INamedThing.REGULAR);

        // return targetJsp + "?uri=" + map.getID(element);
    }

}
