package org.openl.rules.validation;

import org.openl.OpenL;
import org.openl.message.OpenLErrorMessage;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.constraints.RegexpValueConstraint;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;
import org.openl.rules.table.properties.inherit.InheritanceLevel;
import org.openl.rules.table.properties.inherit.PropertiesChecker;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.types.IOpenClass;
import org.openl.validation.ValidationResult;
import org.openl.validation.ValidationStatus;
import org.openl.validation.ValidationUtils;

/**
 * Validator for string properties that have to correspond to some regexp
 * pattern
 * 
 * @author PUdalau
 */
public class RegexpPropertyValidator extends TablesValidator {
    private String propertyName;
    private String constraintsStr;

    public RegexpPropertyValidator(String propertyName, String constraintsStr) {
        this.propertyName = propertyName;
        this.constraintsStr = RegexpValueConstraint.getRegexPattern(constraintsStr);
    }

    @Override
    public ValidationResult validateTables(OpenL openl, TableSyntaxNode[] tableSyntaxNodes, IOpenClass openClass) {
        ValidationResult validationResult = null;
        for (TableSyntaxNode tsn : tableSyntaxNodes) {
            if (!tsn.hasErrors()) {
                if (PropertiesChecker.isPropertySuitableForTableType(propertyName, tsn.getType()) && tsn.getTableProperties()
                    .getPropertyLevelDefinedOn(propertyName) == InheritanceLevel.TABLE) {
                    String propertyValue = (String) tsn.getTableProperties().getPropertyValue(propertyName);
                    if (propertyValue == null || !propertyValue.matches(constraintsStr)) {
                        if (validationResult == null) {
                            validationResult = new ValidationResult(ValidationStatus.FAIL);
                        }
                        SyntaxNodeException exception = new SyntaxNodeException(String.format(
                                "Incorrect value \"%s\" for property \"%s\"", propertyValue,
                                TablePropertyDefinitionUtils.getPropertyDisplayName(propertyName)), null, tsn);
                        tsn.addError(exception);
                        ValidationUtils.addValidationMessage(validationResult, new OpenLErrorMessage(exception));
                    }
                }
            } else {
                if (validationResult == null) {
                    validationResult = new ValidationResult(ValidationStatus.FAIL);
                }

                ValidationUtils.addValidationMessage(validationResult, new OpenLErrorMessage(tsn.getErrors()[0]));
            }
        }
        if (validationResult != null) {
            return validationResult;
        } else {
            return ValidationUtils.validationSuccess();
        }
    }

}
