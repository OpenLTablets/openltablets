package org.openl.rules.datatype.binding;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.openl.rules.datatype.binding.TopologicalSort.TopoGraphNode;

/**
 * Created by dl on 4/16/14.
 */
public class TopologicalSortTest {

    @Test
    public void test() {
        TopoGraphNode<String> node1 = new TopoGraphNode<String>("1");
        TopoGraphNode<String> node2 = new TopoGraphNode<String>("2");
        node2.addDependency(node1);

        TopoGraphNode<String> node3 = new TopoGraphNode<String>("3");
        node3.addDependency(node2);

        TopoGraphNode<String> node4 = new TopoGraphNode<String>("4");
        node4.addDependency(node3);

        List<TopoGraphNode<String>> list = Arrays.asList(node4, node3, node2, node1);

        TopologicalSort<String> sort = new TopologicalSort<String>();
        Set<TopoGraphNode<String>> sorted = sort.sort(list);
        List<TopoGraphNode<String>> sortedList = new ArrayList<TopoGraphNode<String>>(sorted);

        assertEquals(4, sortedList.size());
        assertEquals("1", sortedList.get(0).getObj());
        assertEquals("2", sortedList.get(1).getObj());
        assertEquals("3", sortedList.get(2).getObj());
        assertEquals("4", sortedList.get(3).getObj());
    }
}
