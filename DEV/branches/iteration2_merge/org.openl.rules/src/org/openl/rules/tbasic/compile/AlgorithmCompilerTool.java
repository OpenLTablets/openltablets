package org.openl.rules.tbasic.compile;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.openl.IOpenSourceCodeModule;
import org.openl.binding.impl.BoundError;
import org.openl.meta.StringValue;
import org.openl.rules.tbasic.AlgorithmRow;
import org.openl.rules.tbasic.AlgorithmTableParserManager;
import org.openl.rules.tbasic.AlgorithmTreeNode;
import org.openl.types.IOpenField;
import org.openl.types.java.JavaOpenClass;

public class AlgorithmCompilerTool {

    /**
     * 
     * @param nodes
     * @return
     */
    public static AlgorithmTreeNode getLastExecutableOperation(List<AlgorithmTreeNode> nodes) {
        AlgorithmTreeNode lastOperation = nodes.get(nodes.size() - 1);
        if (lastOperation.getSpecification().getKeyword().startsWith("END")) {
            lastOperation = getLastExecutableOperation(nodes.subList(0, nodes.size() - 1));
        } else if (lastOperation.getChildren().size() > 0) {
            lastOperation = getLastExecutableOperation(lastOperation.getChildren());
        }
        return lastOperation;
    }

    /**
     * @param nodesToProcess
     * @param firstNodeIndex
     * @return
     */
    public static int getLinkedNodesGroupSize(List<AlgorithmTreeNode> nodesToProcess, int firstNodeIndex) {
        int linkedNodesGroupSize = 1; // just one operation by default

        AlgorithmTreeNode currentNodeToProcess = nodesToProcess.get(firstNodeIndex);
        String currentNodeKeyword = currentNodeToProcess.getSpecification().getKeyword();

        String[] operationNamesToGroup = AlgorithmTableParserManager.instance().whatOperationsToGroup(
                currentNodeKeyword);

        if (operationNamesToGroup != null) {
            List<String> operationsToGroupWithCurrent = Arrays.asList(operationNamesToGroup);

            for (; linkedNodesGroupSize < nodesToProcess.size() - firstNodeIndex; linkedNodesGroupSize++) {
                AlgorithmTreeNode groupCandidateNode = nodesToProcess.get(firstNodeIndex + linkedNodesGroupSize);
                if (!operationsToGroupWithCurrent.contains(groupCandidateNode.getSpecification().getKeyword())) {
                    break;
                }
            }
        }

        return linkedNodesGroupSize;
    }

    /**
     * @param candidateNodes
     * @param conversionStep
     * @return
     * @throws BoundError
     */
    public static List<AlgorithmTreeNode> getNestedInstructionsBlock(List<AlgorithmTreeNode> candidateNodes,
            String instruction) throws BoundError {

        AlgorithmTreeNode executionNode = extractOperationNode(candidateNodes, instruction);

        return executionNode.getChildren();
    }

    /**
     * @param candidateNodes
     * @param instruction
     * @return
     * @throws BoundError
     */
    public static AlgorithmTreeNode extractOperationNode(List<AlgorithmTreeNode> candidateNodes, String instruction)
            throws BoundError {
        AlgorithmTreeNode executionNode = null;
        String operationName = extractOperationName(instruction);

        for (AlgorithmTreeNode node : candidateNodes) {
            if (operationName.equalsIgnoreCase(node.getAlgorithmRow().getOperation().getValue())) {
                executionNode = node;
            }
        }

        if (executionNode == null) {
            IOpenSourceCodeModule errorSource = candidateNodes.get(0).getAlgorithmRow().getOperation()
                    .asSourceCodeModule();
            throw new BoundError(String.format("Compilation failure. Can't find %s in operations sequence %s",
                    operationName, candidateNodes), errorSource);
        }
        return executionNode;
    }

    /**
     * @param nodesToCompile
     * @param instruction
     * @return
     * @throws BoundError
     */
    public static AlgorithmOperationSource getOperationSource(List<AlgorithmTreeNode> nodesToCompile, String instruction)
            throws BoundError {

        AlgorithmTreeNode sourceNode;
        String operationValueName = null;

        // TODO: set more precise source reference
        if (isOperationFieldInstruction(instruction)) {
            sourceNode = extractOperationNode(nodesToCompile, instruction);
            operationValueName = extractFieldName(instruction);
        } else {
            sourceNode = nodesToCompile.get(0);
        }

        return new AlgorithmOperationSource(sourceNode, operationValueName);
    }

    /**
     * @param candidateNodes
     * @param instruction
     * @return
     * @throws BoundError
     */
    public static StringValue getCellContent(List<AlgorithmTreeNode> candidateNodes, String instruction)
            throws BoundError {
        String fieldName = extractFieldName(instruction);

        AlgorithmTreeNode executionNode = extractOperationNode(candidateNodes, instruction);

        IOpenField codeField = JavaOpenClass.getOpenClass(AlgorithmRow.class).getField(fieldName);

        if (codeField == null) {
            IOpenSourceCodeModule errorSource = candidateNodes.get(0).getAlgorithmRow().getOperation()
                    .asSourceCodeModule();
            throw new BoundError(String.format("Compilation failure. Can't find %s field", fieldName), errorSource);
        }

        StringValue openLCode = (StringValue) codeField.get(executionNode.getAlgorithmRow(), null);

        return openLCode;
    }

    /**
     * @param instruction
     * @return
     */
    public static boolean isOperationFieldInstruction(String instruction) {
        boolean isInstruction = false;

        if (instruction != null) {
            isInstruction = instruction.split(Pattern.quote(FIELD_SEPARATOR)).length == 2;
        }

        return isInstruction;
    }

    public static final String FIELD_SEPARATOR = ".";

    private static String extractOperationName(String instruction) {
        // Get the first token before ".", it will be the name of operation
        return instruction.split(Pattern.quote(FIELD_SEPARATOR))[0];
    }

    private static String extractFieldName(String instruction) {
        // Get the first token after ".", it will be the field name
        return instruction.split(Pattern.quote(FIELD_SEPARATOR))[1];
    }

}
