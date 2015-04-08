package org.openl.rules.datatype.binding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openl.binding.IBindingContext;
import org.openl.binding.impl.BindHelper;
import org.openl.exception.OpenLCompilationException;
import org.openl.rules.datatype.binding.TopologicalSort.TopoGraphNode;
import org.openl.rules.lang.xls.XlsNodeTypes;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;

/**
 * Created by dl on 6/13/14.
 */
public class DatatypesSorter {

    public TableSyntaxNode[] sort(TableSyntaxNode[] datatypeNodes, IBindingContext bindingContext) {
        if (datatypeNodes == null) {
            return null;
        }

        Map<String, TableSyntaxNode> datatypes = createTypesMap(datatypeNodes);

        List<TopoGraphNode<TableSyntaxNode>> nodes = wrapAll(datatypes, bindingContext);
        Set<TopoGraphNode<TableSyntaxNode>> sorted = new TopologicalSort<TableSyntaxNode>().sort(nodes);
        TableSyntaxNode[] tableSyntaxNodes = unwrapAll(sorted);

        return tableSyntaxNodes;
    }

    private List<TopoGraphNode<TableSyntaxNode>> wrapAll(Map<String, TableSyntaxNode> datatypes,
            IBindingContext bindingContext) {
        List<TopoGraphNode<TableSyntaxNode>> toSort = new ArrayList<TopoGraphNode<TableSyntaxNode>>();
        for (TableSyntaxNode datatype : datatypes.values()) {
            toSort.add(wrap(datatype, datatypes, bindingContext, new HashMap<String, TopoGraphNode<TableSyntaxNode>>()));
        }
        return toSort;
    }

    private TableSyntaxNode[] unwrapAll(Set<TopoGraphNode<TableSyntaxNode>> sorted) {
        TableSyntaxNode[] result = new TableSyntaxNode[sorted.size()];
        Iterator<TopoGraphNode<TableSyntaxNode>> iterator = sorted.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            TopoGraphNode<TableSyntaxNode> element = iterator.next();
            if (element != null) {
                result[i] = element.getObj();
            }
        }
        return result;
    }

    private TopologicalSort.TopoGraphNode<TableSyntaxNode> wrap(TableSyntaxNode datatype,
            Map<String, TableSyntaxNode> datatypes,
            IBindingContext bindingContext,
            Map<String, TopoGraphNode<TableSyntaxNode>> dependentQueue) {

        DependentTypesExtractor dependeciesExtractor = new DependentTypesExtractor();
        Set<String> dependencies = dependeciesExtractor.extract(datatype, bindingContext);
        TopoGraphNode<TableSyntaxNode> forSort = new TopoGraphNode<TableSyntaxNode>(datatype);
        if (dependencies.isEmpty()) {
            return forSort;
        } else {
            String currentName = null;
            try {
                currentName = DatatypeHelper.getDatatypeName(datatype);
            } catch (OpenLCompilationException e) {
                // Suppress the exception
                //
            }
            for (String dependency : dependencies) {
                if (datatypes.containsKey(dependency)
                // Avoid recursive dependencies
                //
                && !dependency.equals(currentName)) {
                    if (dependentQueue.containsKey(dependency)) {
                        // Avoid recursive dependencies
                        forSort.addDependency(dependentQueue.get(dependency));
                    } else {
                        dependentQueue.put(currentName, forSort);
                        forSort.addDependency(wrap(datatypes.get(dependency), datatypes, bindingContext, dependentQueue));
                        dependentQueue.remove(currentName);
                    }
                }
            }
            return forSort;
        }

    }

    private Map<String, TableSyntaxNode> createTypesMap(TableSyntaxNode[] nodes) {

        Map<String, TableSyntaxNode> map = new LinkedHashMap<String, TableSyntaxNode>();

        for (TableSyntaxNode tsn : nodes) {

            if (XlsNodeTypes.XLS_DATATYPE.equals(tsn.getNodeType())) {

                try {
                    String datatypeName = DatatypeHelper.getDatatypeName(tsn);

                    if (datatypeName != null) {
                        if (map.containsKey(datatypeName)) {
                            String message = String.format("Type with name '%s' already exists", datatypeName);
                            SyntaxNodeException error = SyntaxNodeExceptionUtils.createError(message, tsn);
                            tsn.addError(error);
                            BindHelper.processError(error);
                        } else {
                            map.put(datatypeName, tsn);
                        }
                    } else {
                        String message = "Cannot recognize type name";
                        SyntaxNodeException error = SyntaxNodeExceptionUtils.createError(message, tsn);
                        tsn.addError(error);
                        BindHelper.processError(error);
                    }
                } catch (OpenLCompilationException e) {
                    String message = "An error has occurred during compilation";
                    SyntaxNodeException error = SyntaxNodeExceptionUtils.createError(message, e, tsn);
                    tsn.addError(error);
                    BindHelper.processError(error);
                }
            }
        }

        return map;
    }

}
