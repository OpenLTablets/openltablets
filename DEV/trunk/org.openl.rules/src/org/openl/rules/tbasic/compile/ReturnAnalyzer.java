package org.openl.rules.tbasic.compile;

import java.util.List;

import org.openl.base.INamedThing;
import org.openl.binding.impl.BoundError;
import org.openl.meta.StringValue;
import org.openl.rules.tbasic.AlgorithmTableParserManager;
import org.openl.rules.tbasic.AlgorithmTreeNode;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.types.IOpenClass;
import org.openl.types.java.JavaOpenClass;

/**
 * The <code>ReturnAnalyzer</code> class analyzes body of some TBasic function
 * for correctness of returns sequence and return types and detects unreachable
 * code.
 *
 */
public class ReturnAnalyzer {
    private IOpenClass returnType;
    private AlgorithmCompiler compiler;

    /**
     * Create an instance of <code>ReturnAnalyzer</code> for analysis of some
     * function from TBasic compiler.
     *
     * @param returnType Expected return type of function
     * @param compiler Associated TBasic compiler.
     */
    public ReturnAnalyzer(IOpenClass returnType, AlgorithmCompiler compiler) {
        this.returnType = returnType;
        this.compiler = compiler;
    }

    /**
     * Make full analysis of correctness of returns sequence and return types
     * and detects unreachable code.
     *
     * @param nodesToAnalyze Body of some function to analyze.
     * @return Correctness of code.
     * @throws BoundError If function contains unreachable code or incorrect
     *             return type.
     */
    public SuitablityAsReturn analyze(List<AlgorithmTreeNode> nodesToAnalyze) throws BoundError {
        SuitablityAsReturn result = analyzeSequence(nodesToAnalyze);
        if (returnType == JavaOpenClass.VOID) {
            // not requires result
            result = SuitablityAsReturn.RETURN;
        }
        return result;
    }

    private SuitablityAsReturn analyzeGroup(List<AlgorithmTreeNode> nodesToAnalyze) throws BoundError {
        if (nodesToAnalyze.get(0).getSpecification().getKeyword().equals("IF")
                && nodesToAnalyze.get(1).getSpecification().getKeyword().equals("ELSE")) {
            return analyzeIFOperation(nodesToAnalyze, nodesToAnalyze.get(0).getSpecification().isMultiline());
        } else {
            return SuitablityAsReturn.NONE;
        }
    }

    private SuitablityAsReturn analyzeIFOperation(List<AlgorithmTreeNode> nodesToAnalyze, boolean isMultiline)
            throws BoundError {
        SuitablityAsReturn result = SuitablityAsReturn.RETURN;
        // checks only IF and ELSE branches
        for (int i = 0; i < 2; i++) {
            SuitablityAsReturn suitablityOfNode = SuitablityAsReturn.NONE;
            if (isMultiline) {
                suitablityOfNode = analyzeSequence(nodesToAnalyze.get(i).getChildren());
            } else {
                if (hasTypeAsReturn(nodesToAnalyze.get(i).getAlgorithmRow().getAction())) {
                    suitablityOfNode = SuitablityAsReturn.SUITABLE;
                } else {
                    suitablityOfNode = SuitablityAsReturn.NONE;
                }
            }
            result = SuitablityAsReturn.lessSuitable(result, suitablityOfNode);
        }
        return result;
    }

    private SuitablityAsReturn analyzeNode(AlgorithmTreeNode nodeToAnalyze) throws BoundError {
        if (nodeToAnalyze.getSpecification().getKeyword().equals("RETURN")) {
            if (hasTypeAsReturn(nodeToAnalyze.getAlgorithmRow().getCondition())) {
                return SuitablityAsReturn.RETURN;
            } else {
                IOpenSourceCodeModule errorSource = nodeToAnalyze.getAlgorithmRow().getCondition().asSourceCodeModule();
                throw new BoundError("Incorrect return type. Return type of function declared as '"
                        + returnType.getDisplayName(INamedThing.REGULAR) + "'", errorSource);
            }
        } else if (canBeGrouped(nodeToAnalyze)) {
            // for loops and single IF without ELSE
            return SuitablityAsReturn.NONE;
        } else if (hasTypeAsReturn(nodeToAnalyze.getAlgorithmRow().getAction())) {
            return SuitablityAsReturn.SUITABLE;
        } else {
            return SuitablityAsReturn.NONE;
        }
    }

    private SuitablityAsReturn analyzeSequence(List<AlgorithmTreeNode> nodesToAnalyze) throws BoundError {
        SuitablityAsReturn result = SuitablityAsReturn.RETURN;
        for (int i = 0, linkedNodesGroupSize; i < nodesToAnalyze.size(); i += linkedNodesGroupSize) {
            linkedNodesGroupSize = AlgorithmCompilerTool.getLinkedNodesGroupSize(nodesToAnalyze, i);

            if (linkedNodesGroupSize == 1) {
                result = analyzeNode(nodesToAnalyze.get(i));
            } else {
                result = analyzeGroup(nodesToAnalyze.subList(i, i + linkedNodesGroupSize));
            }

            if (result == SuitablityAsReturn.RETURN && i + linkedNodesGroupSize < nodesToAnalyze.size()) {
                IOpenSourceCodeModule errorSource = nodesToAnalyze.get(i + linkedNodesGroupSize).getAlgorithmRow()
                        .getOperation().asSourceCodeModule();
                throw new BoundError("Unreachable code. Operations after RETURN not allowed.", errorSource);
            }
        }
        return result;
    }

    private boolean canBeGrouped(AlgorithmTreeNode nodeToAnalyze) {
        String currentNodeKeyword = nodeToAnalyze.getSpecification().getKeyword();
        String[] operationNamesToGroup = AlgorithmTableParserManager.instance().whatOperationsToGroup(
                currentNodeKeyword);
        if (operationNamesToGroup != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return Expected return type of function
     */
    public IOpenClass getReturnType() {
        return returnType;
    }

    private IOpenClass getTypeOfField(StringValue fieldContent) {
        return compiler.getTypeOfField(fieldContent);
    }

    private boolean hasTypeAsReturn(StringValue fieldContent) {
        if (returnType == JavaOpenClass.VOID) {
            // for void functions return must be empty
            if (fieldContent.equals("")) {
                return true;
            } else {
                return false;
            }
        }
        IOpenClass typeOfField = getTypeOfField(fieldContent);
        return returnType.equals(typeOfField);
    }
}
