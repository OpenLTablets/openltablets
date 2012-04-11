package org.openl.rules.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openl.OpenL;
import org.openl.message.OpenLErrorMessage;
import org.openl.message.OpenLWarnMessage;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.properties.DimensionPropertiesMethodKey;
import org.openl.rules.testmethod.TestSuiteMethod;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.types.IOpenClass;
import org.openl.rules.method.ExecutableRulesMethod;
import org.openl.validation.ValidationResult;
import org.openl.validation.ValidationStatus;
import org.openl.validation.ValidationUtils;

/**
 * Validator that checks correctness of "active" property. Only one active table
 * allowed. And if active table is absent warning will occur.
 * 
 * @author PUdalau
 */
public class ActivePropertyValidator extends TablesValidator {

    public static final String NO_ACTIVE_TABLE_MESSAGE = "No active table";
    public static final String ODD_ACTIVE_TABLE_MESSAGE = "There can be only one active table";

    @Override
    public ValidationResult validateTables(OpenL openl, TableSyntaxNode[] tableSyntaxNodes, IOpenClass openClass) {
        ValidationResult validationResult = null;

        // Group methods not TableSyntaxNodes as we may have dependent modules,
        // and no sources for them,
        // represented in current module. The only information about dependency
        // methods contains in openClass.
        //
        Map<DimensionPropertiesMethodKey, List<TableSyntaxNode>> groupedMethods = groupExecutableMethods(tableSyntaxNodes);

        for (DimensionPropertiesMethodKey key : groupedMethods.keySet()) {
            List<TableSyntaxNode> methodsGroup = groupedMethods.get(key);
            boolean activeTableWasFound = false;

            for (TableSyntaxNode executableMethodTable : methodsGroup) {
                if (executableMethodTable.getMember() instanceof TestSuiteMethod) {
                    // all tests are active by default
                    //
                    activeTableWasFound = true;
                    break;
                }
                if (executableMethodTable.getTableProperties() != null && Boolean.TRUE.equals(executableMethodTable.getTableProperties()
                    .getActive())) {
                    if (activeTableWasFound) {
                        if (validationResult == null) {
                            validationResult = new ValidationResult(ValidationStatus.FAIL);
                        }
                        SyntaxNodeException exception = new SyntaxNodeException(ODD_ACTIVE_TABLE_MESSAGE,
                            null,
                            executableMethodTable);
                        executableMethodTable.addError(exception);
                        ValidationUtils.addValidationMessage(validationResult, new OpenLErrorMessage(exception));
                    } else {
                        activeTableWasFound = true;
                    }
                }
            }
            if (!activeTableWasFound) {
                if (validationResult == null) {
                    validationResult = new ValidationResult(ValidationStatus.SUCCESS);
                }
                // warning is attached to any table syntax node
                ValidationUtils.addValidationMessage(validationResult, new OpenLWarnMessage(NO_ACTIVE_TABLE_MESSAGE,
                    methodsGroup.get(0)));
            }
        }

        if (validationResult != null) {
            return validationResult;
        } else {
            return ValidationUtils.validationSuccess();
        }
    }

    private Map<DimensionPropertiesMethodKey, List<TableSyntaxNode>> groupExecutableMethods(TableSyntaxNode[] tableSyntaxNodes) {
        Map<DimensionPropertiesMethodKey, List<TableSyntaxNode>> groupedMethods = new HashMap<DimensionPropertiesMethodKey, List<TableSyntaxNode>>();

        for (TableSyntaxNode tsn : tableSyntaxNodes) {
            if (tsn.getMember() instanceof ExecutableRulesMethod) {
                ExecutableRulesMethod executableMethod = (ExecutableRulesMethod) tsn.getMember();
                DimensionPropertiesMethodKey key = new DimensionPropertiesMethodKey(executableMethod);
                if (!groupedMethods.containsKey(key)) {
                    groupedMethods.put(key, new ArrayList<TableSyntaxNode>());
                }
                groupedMethods.get(key).add(tsn);
            }
        }
        return groupedMethods;
    }
}
