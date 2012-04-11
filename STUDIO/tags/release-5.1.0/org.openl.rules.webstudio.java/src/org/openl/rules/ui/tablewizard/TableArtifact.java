package org.openl.rules.ui.tablewizard;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * Defines a table definition entity, which contains several parameters and logic. Examples of such artifacts are
 * conditions, action, return value for decision table.
 *
 * @author Aliaksandr Antonik.
 */
public class TableArtifact {
    private String name;
    private List<Parameter> parameters = new ArrayList<Parameter>();
    private String logic;
    private boolean dirty;

    public TableArtifact() {
        parameters.add(new Parameter());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public String getLogic() {
        return logic;
    }

    public void setLogic(String logic) {
        this.logic = logic;
    }

    public int getParamsCount() {
        return parameters.size();
    }

    protected boolean isDirty() {
        return dirty;
    }

    protected void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
