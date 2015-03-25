/**
 * Created Jul 11, 2007
 */
package org.openl.rules.dt2.index;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openl.rules.dt2.DecisionTableRuleNode;

/**
 * @author snshor
 *
 */
public abstract class ARuleIndex {
	
//	boolean hasMetaInfo = false;
//
//    public boolean isHasMetaInfo() {
//		return hasMetaInfo;
//	}
//
//	public void setHasMetaInfo(boolean hasMetaInfo) {
//		this.hasMetaInfo = hasMetaInfo;
//	}

	protected DecisionTableRuleNode emptyOrFormulaNodes;

    public ARuleIndex(DecisionTableRuleNode emptyOrFormulaNodes) {
        this.emptyOrFormulaNodes = emptyOrFormulaNodes;
    }

    public DecisionTableRuleNode getEmptyOrFormulaNodes() {
        return emptyOrFormulaNodes;
    }

    public DecisionTableRuleNode findNode(Object value) {
        if (value == null) {
            return emptyOrFormulaNodes;
        }

        DecisionTableRuleNode node = findNodeInIndex(value);

        return node == null ? emptyOrFormulaNodes : node;
    }

    public abstract DecisionTableRuleNode findNodeInIndex(Object value);

    public abstract Iterator<DecisionTableRuleNode> nodes();

	public int[] collectRules() {
		Set<Integer> set = new HashSet<Integer>();

		for (Iterator<DecisionTableRuleNode> iterator = nodes(); iterator.hasNext();) {
			DecisionTableRuleNode node = (DecisionTableRuleNode) iterator.next();
			
			int[] rules = node.getRules();
			for (int i = 0; i < rules.length; i++) {
				set.add(rules[i]);
			}
		} 
	
		if (emptyOrFormulaNodes != null)
		{
			int[] rules = emptyOrFormulaNodes.getRules();
			for (int i = 0; i < rules.length; i++) {
				set.add(rules[i]);
			}
			
		}	
		
		
		int[] res = new int[ set.size()];
		
		Iterator<Integer> it = set.iterator();
		
		for (int i = 0; i < res.length && it.hasNext(); i++) {
			res[i] = it.next();
		}
		
		Arrays.sort(res);
		
		return res;
	}

}
