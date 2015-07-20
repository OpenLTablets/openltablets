package org.openl.rules.dt2.algorithm2.nodes;

import org.openl.rules.dt2.algorithm2.DecisionTableSearchTree.SearchContext;
import org.openl.rules.dt2.algorithm2.ISearchTreeNode;
import org.openl.util.trie.IARTNode;

public abstract class BaseSearchNode extends IARTNode.EmptyARTNode implements ISearchTreeNode {

	@Override
	public IARTNode compact() {
		return compactSearchNode();
	}

	
	public abstract static class Compact extends BaseSearchNode
	{

		@Override
		public ISearchTreeNode compactSearchNode() {
			return this;
		}
	}
	
	public static abstract class CompactUnique extends Compact
	{


		@Override
		public Object findNextNodeOrValue(SearchContext scxt) {
			return null;
		}
	}

	public static abstract class Unique extends BaseSearchNode
	{
		@Override
		public Object findNextNodeOrValue(SearchContext scxt) {
			return null;
		}
	}
}
