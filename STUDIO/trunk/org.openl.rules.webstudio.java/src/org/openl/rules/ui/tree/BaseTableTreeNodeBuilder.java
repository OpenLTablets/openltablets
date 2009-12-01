package org.openl.rules.ui.tree;

import org.openl.rules.lang.xls.syntax.TableSyntaxNode;

/**
 * Base class for building tree using tables meta information.
 * 
 */
public abstract class BaseTableTreeNodeBuilder implements TreeNodeBuilder<TableSyntaxNode> {

    /**
     * {@inheritDoc}
     */
    public ITreeNode<Object> makeNode(TableSyntaxNode tableSyntaxNode, int i) {

        Object nodeObject = makeObject(tableSyntaxNode);
        String[] displayNames = getDisplayValue(nodeObject, 0);

        ProjectTreeNode projectTreeNode = new ProjectTreeNode(displayNames, getType(nodeObject), getUrl(nodeObject),
                getProblems(nodeObject), i, tableSyntaxNode);

        projectTreeNode.setObject(nodeObject);

        return projectTreeNode;
    }

    /**
     * {@inheritDoc}
     */
    public Comparable<?> makeKey(TableSyntaxNode tableSyntaxNode) {
        return makeKey(tableSyntaxNode, 0);
    }

    /**
     * {@inheritDoc}
     */
    public Comparable<?> makeKey(TableSyntaxNode tableSyntaxNode, int i) {

        Object nodeObject = makeObject(tableSyntaxNode);

        return new NodeKey(getWeight(nodeObject), getDisplayValue(nodeObject, i));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUnique() {
        return false;
    }

    /**
     * Makes value object for node using table syntax node.
     * 
     * @param tableSyntaxNode table syntax node
     * @return node object
     */
    protected abstract Object makeObject(TableSyntaxNode tableSyntaxNode);

    /**
     * Gets display value (triple of possible names) of node object.
     * 
     * @param nodeObject node object
     * @param i display name mode
     * @return display value
     */
    public abstract String[] getDisplayValue(Object nodeObject, int i);

    /**
     * Gets name of node.
     * 
     * @return name of node
     */
    public abstract String getName();

    /**
     * Gets type of node.
     * 
     * @param nodeObject node object
     * @return string that represent node type
     */
    public abstract String getType(Object nodeObject);

    /**
     * Gets url of node.
     * 
     * @param nodeObject node object
     * @return string that represent node url
     */
    public abstract String getUrl(Object nodeObject);

    /**
     * Gets weight of node.
     * 
     * @param nodeObject node object
     * @return string that represent node weight
     */
    public abstract int getWeight(Object nodeObject);

    /**
     * Gets problems of node.
     * 
     * @param nodeObject node object
     * @return object that represent node problems
     */
    public abstract Object getProblems(Object nodeObject);
}
