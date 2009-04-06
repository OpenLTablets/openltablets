package org.openl.rules.tbasic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openl.binding.impl.BoundError;
import org.openl.meta.StringValue;
import org.openl.rules.tbasic.TableParserSpecificationBean.ValueNecessity;

public class RowParser implements IRowParser {
    private static final String COMMENTS_REGEXP = "^(//)(.*)|^(/\\*)(.*)(\\*/)$";
    private List<AlgorithmRow> rows;
    private TableParserSpecificationBean[] specifications;

    public RowParser(List<AlgorithmRow> rows, TableParserSpecificationBean[] specifications) {
        assert rows != null;
        assert specifications != null;

        this.rows = rows;
        this.specifications = specifications;
    }

    private List<AlgorithmTreeNode> prepareNodes() {
        // cut off commented rows, pack labels
        List<AlgorithmTreeNode> nodes = new ArrayList<AlgorithmTreeNode>();

        AlgorithmTreeNode lastNode = new AlgorithmTreeNode();
        for (AlgorithmRow row : rows) {
            StringValue operation = row.getOperation();
            StringValue label = row.getLabel();

            if (operation.isEmpty()) {
                if (!label.isEmpty()) {
                    // stack up labels
                    lastNode.addLabel(label);
                }
            } else if (operation.getValue().matches(COMMENTS_REGEXP)) {
                // ignore
            } else {
                // has some operation
                if (!label.isEmpty()) {
                    lastNode.addLabel(label);
                } else {
                    // if no labels at all
                    if (lastNode.getLabels().isEmpty()) {
                        // add this empty label anyway
                        lastNode.addLabel(label);
                    }
                }

                lastNode.setAlgorithmRow(row);
                nodes.add(lastNode);
                lastNode = new AlgorithmTreeNode();
            }
        }

        if (lastNode.getAlgorithmRow() != null) {
            nodes.add(lastNode);
        }

        return nodes;
    }

    private boolean[] guessMuliline(List<AlgorithmTreeNode> nodes) {
        int size = nodes.size();
        boolean[] multilines = new boolean[size];
        for (int i = 0; i < size - 1; i++) {
            AlgorithmTreeNode node = nodes.get(i);
            AlgorithmRow row = node.getAlgorithmRow();
            int i1 = row.getOperationLevel();

            AlgorithmTreeNode nextNode = nodes.get(i + 1);
            AlgorithmRow nextRow = nextNode.getAlgorithmRow();
            int i2 = nextRow.getOperationLevel();

            multilines[i] = (i1 < i2);
        }

        return multilines;
    }

    public List<AlgorithmTreeNode> parse() throws BoundError {
        List<AlgorithmTreeNode> nodes = prepareNodes();
        boolean[] guessedMultilines = guessMuliline(nodes);

        List<AlgorithmTreeNode> treeNodes = new ArrayList<AlgorithmTreeNode>();
        Map<Integer, AlgorithmTreeNode> parentTree = new HashMap<Integer, AlgorithmTreeNode>();

        int prevIndent = 0;
        for (int i = 0; i < nodes.size(); i++) {
            AlgorithmTreeNode node = nodes.get(i);
            AlgorithmRow row = node.getAlgorithmRow();

            TableParserSpecificationBean specification = validateNode(node, guessedMultilines[i]);
            node.setSpecification(specification);

            int indent = row.getOperationLevel();
            if (indent == 0) {
                treeNodes.add(node);
                parentTree.clear();
            } else {
                StringValue operation = row.getOperation();
                if (indent > (prevIndent + 1)) {
                    String errMsg = String.format("Incorrect operation indention! Expected %d.", (prevIndent + 1));
                    throw new BoundError(errMsg, operation.asSourceCodeModule());
                }
                if (parentTree.isEmpty()) {
                    String errMsg = "Incorrect operation indention! Could not find parent operation with 0 indention.";
                    throw new BoundError(errMsg, operation.asSourceCodeModule());
                }

                parentTree.get(indent - 1).add(node);
            }
            parentTree.put(indent, node);
            prevIndent = indent;
        }

        return treeNodes;
    }

    private TableParserSpecificationBean validateNode(AlgorithmTreeNode node, boolean guessedMultiline) throws BoundError {
        AlgorithmRow row = node.getAlgorithmRow();
        StringValue operation = row.getOperation();
        TableParserSpecificationBean spec = getSpecification(operation, guessedMultiline);

        // check Label
        if (spec.getLabel() == ValueNecessity.REQUIRED && row.getLabel().isEmpty()) {
            String errMsg = "Label is obligatory for this operation!";
            throw new BoundError(errMsg, row.getLabel().asSourceCodeModule());
        }

        checkRowValue("Condition", row.getCondition(), spec.getCondition());
        checkRowValue("Action", row.getAction(), spec.getAction());
        checkRowValue("Before", row.getBefore(), spec.getBeforeAndAfter());
        checkRowValue("After", row.getAfter(), spec.getBeforeAndAfter());

        // check Top Level
        int indent = row.getOperationLevel();
        ValueNecessity specTopLevel = spec.getTopLevel();
        if (specTopLevel == ValueNecessity.PROHIBITED && indent == 0) {
            throw new BoundError("Operation can not be a top level element! It should be nested.", operation
                    .asSourceCodeModule());
        }
        if (specTopLevel == ValueNecessity.REQUIRED && indent > 0) {
            throw new BoundError("Operation can be a top level only!", operation.asSourceCodeModule());
        }

        // passed
        return spec;
    }

    private void checkRowValue(String columnName, StringValue columnValue, ValueNecessity columnNecessity)
            throws BoundError {

        if (columnNecessity == ValueNecessity.REQUIRED && columnValue.isEmpty()) {
            String errMsg = String.format("Operation must have value in %s!", columnName);
            throw new BoundError(errMsg, columnValue.asSourceCodeModule());
        }

        if (columnNecessity == ValueNecessity.PROHIBITED && !columnValue.isEmpty()) {
            String errMsg = String.format("Operation must not have value in %s!", columnName);
            throw new BoundError(errMsg, columnValue.asSourceCodeModule());
        }
    }

    private TableParserSpecificationBean getSpecification(StringValue operation, boolean multiline) throws BoundError {
        String operationName = operation.getValue();
        boolean foundButNotMatch = false;
        for (TableParserSpecificationBean specification : specifications) {
            String specKeyword = specification.getKeyword();
            if (operationName.equalsIgnoreCase(specKeyword)) {
                if (specification.isMultiline() == multiline) {
                    return specification;
                }
                foundButNotMatch = true;
            }
        }

        if (foundButNotMatch) {
            String errMsg = String.format("Operation %s can not be multiline! Nested operations are not allowed here.",
                    operationName);
            throw new BoundError(errMsg, operation.asSourceCodeModule());
        }

        String errMsg = "No such operation: " + operation.getValue();
        throw new BoundError(errMsg, operation.asSourceCodeModule());
    }
}
