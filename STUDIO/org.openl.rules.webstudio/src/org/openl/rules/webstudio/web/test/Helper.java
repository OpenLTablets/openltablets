package org.openl.rules.webstudio.web.test;

import org.openl.meta.explanation.ExplanationNumberValue;
import org.openl.rules.calc.SpreadsheetResult;
import org.openl.rules.table.formatters.FormattersManager;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 * A helper class which contains utility methods.
 */
@ManagedBean
@RequestScoped
public final class Helper {

    public Helper() {
        // THIS CONSTRUCTOR MUST BE EMPTY!!!
    }

    public TreeNode getRoot(ParameterDeclarationTreeNode parameter) {
        TreeNodeImpl root = new TreeNodeImpl();
        root.addChild(parameter.getName(), parameter);
        return root;
    }

    public String format(Object value) {
        return FormattersManager.format(value);
    }

    public boolean isExplanationValue(Object value) {
        return value instanceof ExplanationNumberValue<?>;
    }

    public boolean isSpreadsheetResult(Object value) {
        return value instanceof SpreadsheetResult;
    }
}
