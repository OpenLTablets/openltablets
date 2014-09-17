package org.openl.rules.ui.tree.richfaces;

import org.apache.commons.lang3.StringUtils;
import org.openl.base.INamedThing;
import org.openl.commons.web.jsf.FacesUtils;
import org.openl.meta.explanation.ExplanationNumberValue;
import org.openl.meta.number.Formulas;
import org.openl.meta.number.NumberValue;
import org.openl.rules.webstudio.web.util.Constants;
import org.openl.util.StringTool;
import org.openl.util.tree.ITreeElement;

public class ExplainTreeBuilder extends TreeBuilder {

    private static final String showTablePage = "/faces/pages/modules/explain/showExplainTable.xhtml?";

    @Override
    String getType(ITreeElement<?> element) {
        String type = super.getType(element);
        String filtered = type
                .replace('.', '_')
                .replace(Formulas.ADD.toString(), "plus")
                .replace(Formulas.SUBTRACT.toString(), "minus")
                .replace(Formulas.MULTIPLY.toString(), "mul")
                .replace(Formulas.DIVIDE.toString(), "div")
                .replace(Formulas.REM.toString(), "rem");
        return filtered;
    }

    @Override
    String getDisplayName(Object obj, int mode) {
        String result = super.getDisplayName(obj, mode + 1);
        ExplanationNumberValue<?> explanationValue = (ExplanationNumberValue<?>) obj;
        if (NumberValue.ValueType.FUNCTION.equals(explanationValue.getValueType())
                && mode == INamedThing.SHORT) {
            String functionName = explanationValue.getFunction().getFunctionName().toUpperCase();
            result = functionName + " = " + result;
        }
        return result;
    }

    @Override
    String getUrl(ITreeElement<?> element) {
        ExplanationNumberValue<?> explanationValue = (ExplanationNumberValue<?>) element;
        String url = explanationValue == null || explanationValue.getMetaInfo() == null ? null : explanationValue.getMetaInfo().getSourceUrl();
        if (StringUtils.isNotBlank(url)) {
            return getUrlToElement(element, url);
        }
        return FacesUtils.getContextPath() + showTablePage;
    }

    private String getUrlToElement(ITreeElement<?> element, String url) {
        return FacesUtils.getContextPath() + showTablePage
                + Constants.REQUEST_PARAM_URI + "=" + StringTool.encodeURL("" + url)
                + "&text=" + StringTool.encodeURL(getDisplayName(element, INamedThing.REGULAR));
    }
}
