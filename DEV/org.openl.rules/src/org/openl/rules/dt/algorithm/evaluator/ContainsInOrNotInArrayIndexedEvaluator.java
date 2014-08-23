/**
 * Created Jul 11, 2007
 */
package org.openl.rules.dt.algorithm.evaluator;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.openl.domain.IDomain;
import org.openl.domain.IIntIterator;
import org.openl.domain.IIntSelector;
import org.openl.domain.IntArrayIterator;
import org.openl.rules.dt.DecisionTableRuleNode;
import org.openl.rules.dt.DecisionTableRuleNodeBuilder;
import org.openl.rules.dt.element.ICondition;
import org.openl.rules.dt.index.ARuleIndex;
import org.openl.rules.dt.index.EqualsIndex;
import org.openl.rules.dt.type.BooleanTypeAdaptor;
import org.openl.rules.helpers.NumberUtils;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 * 
 */
public class ContainsInOrNotInArrayIndexedEvaluator implements IConditionEvaluator {

    private BooleanTypeAdaptor adaptor;

    public ContainsInOrNotInArrayIndexedEvaluator(BooleanTypeAdaptor adaptor) {
        this.adaptor = adaptor;
    }

    // TODO fix
    public IOpenSourceCodeModule getFormalSourceCode(ICondition condition) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public IIntSelector getSelector(ICondition condition, Object target, Object[] params, IRuntimeEnv env) {

        Object value = condition.getEvaluator().invoke(target, params, env);

        return new ContainsInOrNotInArraySelector(condition, value, target, params, this.adaptor, env);
    }

    public boolean isIndexed() {
        return true;
    }

    public ARuleIndex makeIndex(Object[][] indexedParams, IIntIterator iterator) {

        if (iterator.size() < 1) {
            return null;
        }

        Set<Object> allValues = null;

        DecisionTableRuleNodeBuilder copyRules = new DecisionTableRuleNodeBuilder();
        List<Set<?>> valueSets = new ArrayList<Set<?>>();

        boolean globalComparatorBasedSet = false;
        boolean globalSmartFloatComparatorIsUsed = false;

        while (iterator.hasNext()) {

            int i = iterator.nextInt();
            copyRules.addRule(i);

            if (indexedParams[i] == null || indexedParams[i].length < 2 || indexedParams[i][1] == null) {
                valueSets.add(Collections.EMPTY_SET);
                continue;
            }

            Set<Object> values = null;
            Object valuesArray = indexedParams[i][1];

            int length = Array.getLength(valuesArray);
            boolean comparatorBasedSet = false;
            boolean smartFloatComparatorIsUsed = false;

            for (int j = 0; j < length; j++) {
                Object value = Array.get(valuesArray, j);
                if (comparatorBasedSet) {
                    if (!(value instanceof Comparable<?>)) {
                        throw new IllegalArgumentException("Invalid state! Index based on comparable interface!");
                    }
                }
                if (allValues == null) {
                    if (NumberUtils.isFloatPointNumber(value)) {
                        if (value instanceof BigDecimal) {
                            allValues = new TreeSet<Object>();
                        } else {
                            allValues = new TreeSet<Object>(FloatTypeComparator.getInstance());
                            smartFloatComparatorIsUsed = true;
                        }
                        comparatorBasedSet = true;
                    } else {
                        allValues = new HashSet<Object>();
                    }
                }
                if (comparatorBasedSet) {
                    if (smartFloatComparatorIsUsed) {
                        values = new TreeSet<Object>(FloatTypeComparator.getInstance());
                    } else {
                        values = new TreeSet<Object>();
                    }
                }
                allValues.add(value);
                values.add(value);
            }
            globalComparatorBasedSet = globalComparatorBasedSet || comparatorBasedSet;
            globalSmartFloatComparatorIsUsed = globalSmartFloatComparatorIsUsed || smartFloatComparatorIsUsed;
            valueSets.add(values);
        }

        int[] rules = copyRules.makeRulesAry();
        iterator = new IntArrayIterator(rules);

        Map<Object, DecisionTableRuleNodeBuilder> map = null;
        Map<Object, DecisionTableRuleNode> nodeMap = null;

        if (globalComparatorBasedSet) {
            if (globalSmartFloatComparatorIsUsed) {
                map = new TreeMap<Object, DecisionTableRuleNodeBuilder>(FloatTypeComparator.getInstance());
                nodeMap = new TreeMap<Object, DecisionTableRuleNode>(FloatTypeComparator.getInstance());
            } else {
                nodeMap = new TreeMap<Object, DecisionTableRuleNode>();
                map = new TreeMap<Object, DecisionTableRuleNodeBuilder>();
            }

        } else {
            map = new HashMap<Object, DecisionTableRuleNodeBuilder>();
            nodeMap = new HashMap<Object, DecisionTableRuleNode>();
        }

        DecisionTableRuleNodeBuilder emptyBuilder = new DecisionTableRuleNodeBuilder();

        while (iterator.hasNext()) {

            int i = iterator.nextInt();

            if (indexedParams[i] == null || indexedParams[i].length < 2 || indexedParams[i][1] == null) {

                emptyBuilder.addRule(i);
                if (map != null) {
                    for (Iterator<DecisionTableRuleNodeBuilder> iter = map.values().iterator(); iter.hasNext();) {
                        DecisionTableRuleNodeBuilder dtrnb = iter.next();
                        dtrnb.addRule(i);
                    }
                }

                continue;
            }

            boolean isIn = indexedParams[i][0] == null || adaptor.extractBooleanValue(indexedParams[i][0]);

            Set<?> values = valueSets.get(i);

            if (isIn) {

                for (Iterator<?> iter = values.iterator(); iter.hasNext();) {

                    Object value = iter.next();
                    DecisionTableRuleNodeBuilder builder = map.get(value);

                    if (builder == null) {
                        builder = new DecisionTableRuleNodeBuilder(emptyBuilder);
                        map.put(value, builder);
                    }

                    builder.addRule(i);
                }
            } else {

                for (Iterator<?> iter = allValues.iterator(); iter.hasNext();) {

                    Object value = iter.next();

                    if (values.contains(value)) {
                        continue;
                    }

                    DecisionTableRuleNodeBuilder bilder = map.get(value);

                    if (bilder == null) {
                        bilder = new DecisionTableRuleNodeBuilder(emptyBuilder);
                        map.put(value, bilder);
                    }

                    bilder.addRule(i);
                }

                emptyBuilder.addRule(i); // !!!!!
            }
        }

        for (Iterator<Map.Entry<Object, DecisionTableRuleNodeBuilder>> iter = map.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<Object, DecisionTableRuleNodeBuilder> element = iter.next();
            nodeMap.put(element.getKey(), element.getValue().makeNode());
        }

        return new EqualsIndex(emptyBuilder.makeNode(), nodeMap);
    }

    public IDomain<? extends Object> getRuleParameterDomain(ICondition condition) throws DomainCanNotBeDefined {
        return null;
    }

    public IDomain<? extends Object> getConditionParameterDomain(int paramIdx, ICondition condition) throws DomainCanNotBeDefined {
        return null;
    }

    @Override
    public String getOptimizedSourceCode() {
        return null;
    }

    @Override
    public void setOptimizedSourceCode(String code) {
    }

}
