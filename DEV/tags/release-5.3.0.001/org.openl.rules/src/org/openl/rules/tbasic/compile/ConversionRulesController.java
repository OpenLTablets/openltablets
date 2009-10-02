package org.openl.rules.tbasic.compile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openl.IOpenSourceCodeModule;
import org.openl.binding.impl.BoundError;
import org.openl.rules.tbasic.AlgorithmTableParserManager;
import org.openl.rules.tbasic.AlgorithmTreeNode;

public class ConversionRulesController {
    private static ConversionRulesController instance;
    private ConversionRuleBean[] conversionRules;

    public static ConversionRulesController getInstance() {
        if (instance == null) {
            return new ConversionRulesController();
        }
        return instance;
    }

    private ConversionRulesController() {
        conversionRules = AlgorithmTableParserManager.instance().getFixedConversionRules();
    }

    /**
     * @throws BoundError
     */
    public ConversionRuleBean getConvertionRule(List<AlgorithmTreeNode> nodesToCompile) throws BoundError {
        assert nodesToCompile.size() > 0;

        List<String> groupedOperationNames = new ArrayList<String>(nodesToCompile.size());

        for (AlgorithmTreeNode node : nodesToCompile) {
            groupedOperationNames.add(node.getSpecification().getKeyword().toUpperCase());
        }

        String operationGroupName = AlgorithmTableParserManager.instance().whatIsOperationsGroupName(
                groupedOperationNames);

        boolean isMultilineOperation;
        // we assume that all the operations are either all multiline or not
        isMultilineOperation = nodesToCompile.get(0).getSpecification().isMultiline();

        for (ConversionRuleBean conversionRule : conversionRules) {
            if (conversionRule.getOperation().equals(operationGroupName)
                    && (conversionRule.isMultiLine() == isMultilineOperation)) {
                return conversionRule;
            }
        }

        // No conversion rule found.

        List<String> predecessorOperations = Arrays.asList(nodesToCompile.get(0).getSpecification()
                .getPredecessorOperations());
        String errorMessage = String.format(
                "The operations sequence is wrong: %2$s. Operations %1$s must precede the %2$s", predecessorOperations,
                groupedOperationNames);
        IOpenSourceCodeModule errorSource = nodesToCompile.get(0).getAlgorithmRow().getOperation().asSourceCodeModule();
        throw new BoundError(errorMessage, errorSource);
    }

}
