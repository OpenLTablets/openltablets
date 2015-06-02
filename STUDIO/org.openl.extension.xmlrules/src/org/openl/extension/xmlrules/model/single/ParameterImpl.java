package org.openl.extension.xmlrules.model.single;

import org.openl.extension.xmlrules.model.Parameter;

public class ParameterImpl implements Parameter {
    private String type;
    private String name;

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
