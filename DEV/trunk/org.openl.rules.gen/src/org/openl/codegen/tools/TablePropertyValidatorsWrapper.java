package org.openl.codegen.tools;

import java.util.ArrayList;
import java.util.List;

import org.openl.rules.table.constraints.Constraint;
import org.openl.rules.table.constraints.Constraints;
import org.openl.rules.table.constraints.UniqueInModuleConstraint;
import org.openl.rules.table.properties.TablePropertyDefinition;
import org.openl.rules.validation.UniquePropertyValueValidator;
import org.openl.validation.IOpenLValidator;

public class TablePropertyValidatorsWrapper {

    private TablePropertyDefinition tablePropertyDefinition;
    private List<Class<? extends IOpenLValidator>> validatorClasses = new ArrayList<Class<? extends IOpenLValidator>>();

    public TablePropertyValidatorsWrapper(TablePropertyDefinition tablePropertyDefinition) {
        this.tablePropertyDefinition = tablePropertyDefinition;

        init();
    }

    private void init() {

        Constraints constraintsManager = tablePropertyDefinition.getConstraints();

        if (constraintsManager != null) {

            List<Constraint> constraints = constraintsManager.getAll();

            for (Constraint constraint : constraints) {

                if (constraint instanceof UniqueInModuleConstraint) {
                    validatorClasses.add(UniquePropertyValueValidator.class);
                }
            }
        }
    }

    public List<Class<? extends IOpenLValidator>> getValidatorClasses() {
        return validatorClasses;
    }

    public String getPropertyName() {
        return tablePropertyDefinition.getName();
    }
}
