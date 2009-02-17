package org.openl.rules.cmatch;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.openl.rules.cmatch.algorithm.Argument;
import org.openl.rules.cmatch.matcher.IMatcher;

public class MatchNode {
    private final List<MatchNode> children;
    private final int rowIndex;

    private MatchNode parent;

    private IMatcher matcher;

    private Argument argument;

    /** Actual values in a row */
    private Object[] checkValues;

    public MatchNode(int rowIndex) {
        children = new LinkedList<MatchNode>();
        this.rowIndex = rowIndex;
    }

    public void add(MatchNode child) {
        children.add(child);
        child.parent = this;
    }

    public MatchNode getParent() {
        return parent;
    }

    public boolean isLeaf() {
        return (children.isEmpty());
    }

    public void clearChildren() {
        children.clear();
    }

    public List<MatchNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public IMatcher getMatcher() {
        return matcher;
    }

    public void setMatcher(IMatcher matcher) {
        this.matcher = matcher;
    }

    public Object[] getCheckValues() {
        return checkValues;
    }

    public void setCheckValues(Object[] checkValues) {
        this.checkValues = checkValues;
    }

    public Argument getArgument() {
        return argument;
    }

    public void setArgument(Argument argument) {
        this.argument = argument;
    }

    public int getRowIndex() {
        return rowIndex;
    }
}
