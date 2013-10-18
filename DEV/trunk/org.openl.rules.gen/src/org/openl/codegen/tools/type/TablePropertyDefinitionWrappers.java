package org.openl.codegen.tools.type;

import java.util.ArrayList;
import java.util.List;

import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.table.properties.expressions.match.ContainsMatchingExpression;
import org.openl.rules.table.properties.expressions.match.EQMatchingExpression;

public class TablePropertyDefinitionWrappers {

    private List<TablePropertyDefinitionWrapper> wrappers = new ArrayList<TablePropertyDefinitionWrapper>();

    public TablePropertyDefinitionWrappers(TablePropertyDefinition[] definitions) {
        init(definitions);
    }

    private void init(TablePropertyDefinition[] definitions) {

        for (TablePropertyDefinition definition : definitions) {
            TablePropertyDefinitionWrapper wrapper = new TablePropertyDefinitionWrapper(definition);
            wrappers.add(wrapper);
        }
    }

    public List<TablePropertyDefinitionWrapper> asList() {
        return new ArrayList<TablePropertyDefinitionWrapper>(wrappers);
    }

    public List<TablePropertyDefinitionWrapper> getDimensionalProperties() {

        List<TablePropertyDefinitionWrapper> dimensionalTablePropertyDefinitions = new ArrayList<TablePropertyDefinitionWrapper>();

        for (TablePropertyDefinitionWrapper wrapper : wrappers) {

            if (wrapper.getDefinition().isDimensional()) {
                dimensionalTablePropertyDefinitions.add(wrapper);
            }
        }

        return dimensionalTablePropertyDefinitions;
    }

    public List<TablePropertyDefinitionWrapper> getGapOverlapDimensionalProperties() {

        List<TablePropertyDefinitionWrapper> dimensionalTablePropertyDefinitions = new ArrayList<TablePropertyDefinitionWrapper>();

        for (TablePropertyDefinitionWrapper wrapper : wrappers) {

            if (wrapper.getDefinition().isDimensional()) {
                String operation = wrapper.getOperation();
                if (ContainsMatchingExpression.OPERATION_NAME.equalsIgnoreCase(operation)
                        || EQMatchingExpression.OPERATION_NAME.equalsIgnoreCase(operation)) {
                    dimensionalTablePropertyDefinitions.add(wrapper);
                }
            }
        }

        return dimensionalTablePropertyDefinitions;
    }
}
