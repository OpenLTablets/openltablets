package org.openl.rules.table.constraints;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ConstraintsParser {
    public static String CONSTRAINTS_SEPARATOR = "&";

    public static List<Constraint> parse(String value) {
        List<Constraint> constraints = new ArrayList<Constraint>();
        ConstraintFactory constraintFactory = new ConstraintFactory();

        if (StringUtils.isNotBlank(value)) {
            for (String constraintExpression : value.split(CONSTRAINTS_SEPARATOR)) {
                Constraint constraint = constraintFactory.getConstraint(constraintExpression);
                if (constraint != null) {
                    constraints.add(constraint);
                }
            }
        }

        return constraints;
    }
}
