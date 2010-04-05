/**
 * Created Feb 11, 2007
 */
package org.openl.rules.dt.validator;

import java.util.Arrays;

import org.openl.rules.dt.DecisionTable;
import org.openl.rules.validator.IValidationResult;
import org.openl.util.ArrayOfNamedValues;

import com.exigen.ie.constrainer.consistencyChecking.Overlapping;
import com.exigen.ie.constrainer.consistencyChecking.Uncovered;

/**
 * @author snshor
 * 
 */
public class DesionTableValidationResult implements IValidationResult {

    private DecisionTable decisionTable;
    private DecisionTableOverlapping[] overlappings;
    private DecisionTableUncovered[] uncovered;

    public DesionTableValidationResult(DecisionTable decisionTable,
            DecisionTableOverlapping[] overlappings,
            DecisionTableUncovered[] uncovered) {

        this.decisionTable = decisionTable;
        this.overlappings = overlappings;
        this.uncovered = uncovered;
    }

    public DesionTableValidationResult(DecisionTable decisionTable,
            Overlapping[] overlappings,
            Uncovered[] uncovered,
            IConditionTransformer transformer,
            DecisionTableAnalyzer analyzer) {

        this.decisionTable = decisionTable;
        this.overlappings = convertOverlappings(overlappings, transformer, analyzer);
        this.uncovered = convertUncovered(uncovered, transformer, analyzer);
    }

    private DecisionTableOverlapping[] convertOverlappings(Overlapping[] overlappings,
            IConditionTransformer transformer,
            DecisionTableAnalyzer analyzer) {

        DecisionTableOverlapping[] tableOverlappings = new DecisionTableOverlapping[overlappings.length];

        for (int i = 0; i < overlappings.length; i++) {

            String[] names = overlappings[i].getSolutionNames();
            Object[] values = new Object[names.length];

            for (int j = 0; j < values.length; j++) {
                values[j] = transformer.transformSignatureValueBack(names[j],
                    overlappings[i].getSolutionValues()[j],
                    analyzer);
            }

            DecisionTableOverlapping tableOverlapping = new DecisionTableOverlapping(overlappings[i].getOverlapped(),
                new ArrayOfNamedValues(names, values));

            tableOverlappings[i] = tableOverlapping;
        }

        return tableOverlappings;
    }

    private DecisionTableUncovered[] convertUncovered(Uncovered[] uncovered,
            IConditionTransformer transformer,
            DecisionTableAnalyzer analyzer) {

        DecisionTableUncovered[] tableUncovered = new DecisionTableUncovered[uncovered.length];

        for (int i = 0; i < uncovered.length; i++) {

            String[] names = uncovered[i].getSolutionNames();
            Object[] values = new Object[names.length];

            for (int j = 0; j < values.length; j++) {
                values[j] = transformer.transformSignatureValueBack(names[j],
                    uncovered[i].getSolutionValues()[j],
                    analyzer);
            }

            tableUncovered[i] = new DecisionTableUncovered(new ArrayOfNamedValues(names, values));
        }

        return tableUncovered;
    }

    public DecisionTable getDecisionTable() {
        return decisionTable;
    }

    public DecisionTableOverlapping[] getOverlappings() {
        return overlappings;
    }

    public DecisionTableUncovered[] getUncovered() {
        return uncovered;
    }

    public boolean hasProblems() {
        return overlappings != null && overlappings.length > 0 || uncovered != null && uncovered.length > 0;
    }

    @Override
    public String toString() {
        return String.format("Uncovered: %s\nOverlapped: %s",
            Arrays.asList(getUncovered()),
            Arrays.asList(getOverlappings()));
    }

}
