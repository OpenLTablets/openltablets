package org.openl.rules.table.constraints;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.collections.CollectionUtils;

/**
 * @author Andrei Astrouski
 */
public class Constraints {

    private List<Constraint> constraints = new ArrayList<Constraint>();
    private String constraintsStr;

    public Constraints() {
    }

    public Constraints(List<Constraint> constraints) {
        setAll(constraints);
    }

    public Constraints(String constraintsStr) {
        setAll(constraintsStr);
    }

    public String getConstraintsStr() {
        return constraintsStr;
    }

    public void setAll(String constraintsStr) {
        this.constraintsStr = constraintsStr;
        List<Constraint> constraints = ConstraintsParser.parse(constraintsStr);
        setAll(constraints);
    }

    public void setAll(List<Constraint> constraints) {
        if (CollectionUtils.isNotEmpty(constraints)) {
            constraints = new ArrayList<Constraint>(this.constraints);
        }
    }

    public List<Constraint> getAll() {
        return new ArrayList<Constraint>(constraints);
    }

    public void addAll(String constraintsStr) {
        List<Constraint> constraints = ConstraintsParser.parse(constraintsStr);
        addAll(constraints);
    }

    public void addAll(List<Constraint> constraints) {
        if (CollectionUtils.isNotEmpty(constraints)) {
            constraints.addAll(constraints);
        }
    }

    public void add(Constraint constraint) {
        constraints.add(constraint);
    }

    public Constraint get(int index) {
        return constraints.get(index);
    }

    public void remove(Constraint constraint) {
        constraints.remove(constraint);
    }

    public void remove(int index) {
        constraints.remove(index);
    }

    public int size() {
        return constraints.size();
    }
}
