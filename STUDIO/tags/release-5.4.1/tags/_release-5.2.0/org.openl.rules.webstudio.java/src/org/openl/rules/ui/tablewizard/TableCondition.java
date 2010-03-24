package org.openl.rules.ui.tablewizard;

import java.util.List;
import java.util.ArrayList;
import javax.faces.model.SelectItem;

/**
 * @author Aliaksandr Antonik.
 */
public class TableCondition extends TableArtifact {
    private boolean logicEditor;
    List<ConditionClause> logicClauses = new ArrayList<ConditionClause>();

    public TableCondition() {
        logicClauses.add(new ConditionClause(this));
    }

    public boolean isLogicEditor() {
        return logicEditor;
    }

    public void setLogicEditor(boolean logicEditor) {
        this.logicEditor = logicEditor;
    }

    public SelectItem[] getParamNames() {
        List<SelectItem> items = new ArrayList<SelectItem>();
        for (Parameter p : getParameters()) {
            if (WizardUtils.checkParameterName(p.getName()) == null) {
                items.add(new SelectItem(p.getName()));
            }
        }
        return items.toArray(new SelectItem[items.size()]);
    }

    public List<ConditionClause> getLogicClauses() {
        return logicClauses;
    }

    public int getLogicClauseCount() {
        return logicClauses.size();
    }

    public Parameter getParameterByName(String name) {
        if (name == null) {
            return null;
        }

        for (Parameter p : getParameters()) {
            if (name.equals(p.getName()))
                return p;
        }

        return null;
    }
}
