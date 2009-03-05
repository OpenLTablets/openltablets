/**
 * Created Jul 22, 2007
 */
package org.openl.rules.dt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openl.IOpenSourceCodeModule;
import org.openl.domain.IIntIterator;
import org.openl.domain.IIntSelector;
import org.openl.rules.dt.ADTRuleIndex.DTRuleNode;
import org.openl.rules.dt.ADTRuleIndex.DTRuleNodeBuilder;
import org.openl.syntax.impl.StringSourceCodeModule;
import org.openl.util.IntervalMap;
import org.openl.util.OpenIterator;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 * 
 */
public class RangeIndexedEvaluator implements IDTConditionEvaluator {

    IRangeAdaptor<Object, Object> adaptor = null;

    public RangeIndexedEvaluator(IRangeAdaptor<Object, Object> adaptor) {
        this.adaptor = adaptor;
    }

    public IIntSelector getSelector(IDTCondition condition, Object target, Object[] dtparams, IRuntimeEnv env) {
        Object value = condition.getEvaluator().invoke(target, dtparams, env);
        return new RangeSelector(condition, value, target, dtparams, env);
    }

    class RangeSelector implements IIntSelector {
        IDTCondition condition;

        Object value;

        Object target;

        Object[] dtparams;

        IRuntimeEnv env;

        public RangeSelector(IDTCondition condition, Object value, Object target, Object[] dtparams, IRuntimeEnv env) {
            this.condition = condition;
            this.value = value;
            this.dtparams = dtparams;
            this.env = env;
            this.target = target;
        }

        @SuppressWarnings("unchecked")
        public boolean select(int rule) {
            Object[][] params = condition.getParamValues();
            Object[] ruleParams = params[rule];

            if (ruleParams == null)
                return true;

            Object[] realParams = new Object[ruleParams.length];

            FunctionalRow.loadParams(realParams, 0, ruleParams, target, dtparams, env);

            // if (ruleParams[0] == null)
            // return true;

            Comparable<Object> vFrom = null;
            Comparable<Object> vTo = null;

            if (adaptor == null) {

                vFrom = (Comparable<Object>) realParams[0];
                vTo = (Comparable<Object>) realParams[1];
            } else {
                vFrom = adaptor.getMin(realParams[0]);
                vTo = adaptor.getMax(realParams[0]);
            }

            return vFrom.compareTo(value) <= 0 && ((Comparable<Object>) value).compareTo(vTo) < 0;
        }
    }

    @SuppressWarnings("unchecked")
    public ADTRuleIndex makeIndex(Object[][] indexedparams, IIntIterator it) {

        if (it.size() < 1)
            return null;

        IntervalMap<Object, Integer> map = new IntervalMap<Object, Integer>();
        DTRuleNodeBuilder emptyBuilder = new DTRuleNodeBuilder();

        List<Integer> firstEmptyRules = new ArrayList<Integer>();

        for (; it.hasNext();) {
            int i = it.nextInt();

            if (indexedparams[i] == null || indexedparams[i][0] == null) {
                emptyBuilder.addRule(i);

                boolean mapHasAnyValue = false;
                for (Iterator<List<Integer>> iter = map.treeMap().values().iterator(); iter.hasNext();) {
                    List<Integer> list = iter.next();
                    list.add(i);
                    mapHasAnyValue = true;
                }
                if (!mapHasAnyValue)
                    firstEmptyRules.add(i);

                continue;
            }

            Comparable<Object> vFrom = null;
            Comparable<Object> vTo = null;

            if (adaptor == null) {

                vFrom = (Comparable<Object>) indexedparams[i][0];
                vTo = (Comparable<Object>) indexedparams[i][1];
            } else {
                vFrom = adaptor.getMin(indexedparams[i][0]);
                vTo = adaptor.getMax(indexedparams[i][0]);
            }
            for (Integer fe : firstEmptyRules) {
                map.putInterval(vFrom, vTo, fe);
            }

            map.putInterval(vFrom, vTo, new Integer(i));

        }

        TreeMap<Comparable<Object>, List<Integer>> tm = map.treeMap();

        // int n = tm.size();

        // Comparable<Object>[] index = new Comparable[n];
        // DTRuleNode[] rules = new DTRuleNode[n];

        List<Comparable<?>> index = new ArrayList<Comparable<?>>();
        List<DTRuleNode> rules = new ArrayList<DTRuleNode>();

        int i = 0;
        for (Iterator<Map.Entry<Comparable<Object>, List<Integer>>> iter = tm.entrySet().iterator(); iter.hasNext(); ++i) {
            Map.Entry<Comparable<Object>, List<Integer>> element = iter.next();
            Comparable<?> indexObj = element.getKey();

            List<Integer> list = element.getValue();

            int[] idxAry = new int[list.size()];
            for (int j = 0; j < idxAry.length; j++) {
                idxAry[j] = list.get(j);
            }

            if (idxAry.length > 0) {
                rules.add(new DTRuleNode(idxAry));
                index.add(indexObj);
            }
        }

        RangeIndex rix = new RangeIndex(emptyBuilder.makeNode(), index.toArray(new Comparable[0]), rules
                .toArray(new DTRuleNode[0]));

        return rix;

    }

    static class RangeIndex extends ADTRuleIndex {

        Comparable<Object>[] index;

        DTRuleNode[] rules;

        /**
         * @param emptyOrFormulaNodes
         */
        public RangeIndex(DTRuleNode emptyOrFormulaNodes, Comparable<Object>[] index, DTRuleNode[] rules) {
            super(emptyOrFormulaNodes);
            this.index = index;
            this.rules = rules;
        }

        public DTRuleNode findNodeInIndex(Object value) {
            int idx = Arrays.binarySearch(index, value);
            if (idx >= 0)
                return rules[idx];

            idx = -(idx + 1) - 1;
            return idx < 0 ? null : rules[idx];

        }

        public Iterator<DTRuleNode> nodes() {
            return OpenIterator.fromArray(rules);
        }

    }

    public boolean isIndexed() {
        return true;
    }

    public IOpenSourceCodeModule getFormalSourceCode(IDTCondition condition) {
        IOpenSourceCodeModule condSource = condition.getSourceCodeModule();
        return new StringSourceCodeModule(condition.getParams()[1].getName() + "<=" + "(" + condSource.getCode()
                + ") && (" + condSource.getCode() + ") < " + condition.getParams()[1].getName(), condSource.getUri(0));
    }

}
