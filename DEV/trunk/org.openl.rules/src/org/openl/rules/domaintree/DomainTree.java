package org.openl.rules.domaintree;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

import org.openl.base.INamedThing;
import org.openl.meta.IMetaInfo;
import org.openl.rules.dt.DecisionTable;
import org.openl.rules.dt.element.IAction;
import org.openl.rules.dt.element.ICondition;
import org.openl.rules.lang.xls.ITableNodeTypes;
import org.openl.rules.lang.xls.binding.XlsMetaInfo;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.IOpenMember;
import org.openl.types.IParameterDeclaration;

import static org.openl.types.java.JavaOpenClass.*;

/**
 * @author Aliaksandr Antonik.
 */
public class DomainTree {
    private static final Set<String> ignoredTypes;
    private static Map<String, IOpenClass> predefinedTypes = new TreeMap<String, IOpenClass>();

    private final Map<String, IOpenClass> treeElements;

    static {
        ignoredTypes = new HashSet<String>();
        ignoredTypes.add(OBJECT.getSimpleName());
        ignoredTypes.add(CLASS.getSimpleName());
        ignoredTypes.add(VOID.getSimpleName());
    }

    static {
        predefinedTypes = new HashMap<String, IOpenClass>();
        predefinedTypes.put(INT.getSimpleName(), INT);
        predefinedTypes.put(STRING.getSimpleName(), STRING);
        predefinedTypes.put(BOOLEAN.getSimpleName(), BOOLEAN);
        predefinedTypes.put(LONG.getSimpleName(), LONG);
        predefinedTypes.put(DOUBLE.getSimpleName(), DOUBLE);
        predefinedTypes.put(FLOAT.getSimpleName(), FLOAT);
        predefinedTypes.put(SHORT.getSimpleName(), SHORT);
        predefinedTypes.put(CHAR.getSimpleName(), CHAR);
    }

    /**
     * Builds a domain tree from excel rules project meta information.
     *
     * @param projectInfo project meta inforamtion.
     * @return <code>DomainTree</code> instance
     */
    public static DomainTree buildTree(IMetaInfo projectInfo) {
        if (projectInfo == null) {
            throw new NullPointerException("projectInfo is null");
        }

        if (projectInfo instanceof XlsMetaInfo) {
            DomainTree domainTree = new DomainTree();

            XlsMetaInfo xlsMetaInfo = (XlsMetaInfo) projectInfo;
            for (TableSyntaxNode node : xlsMetaInfo.getXlsModuleNode().getXlsTableSyntaxNodesWithoutErrors()) {
                String nodeType = node.getType();
                if (nodeType.equals(ITableNodeTypes.XLS_DT)
                        || nodeType.equals(ITableNodeTypes.XLS_DATATYPE)) {
                    domainTree.scanTable(node);
                }
            }

            return domainTree;
        } else {
            throw new IllegalArgumentException("Only XlsMetaInfo is currenty supported");
        }
    }

    private static boolean inspectTypeRecursively(IOpenClass type) {
        return !type.isSimple();
    }

    private static boolean isAppropriateProperty(IOpenField field) {
        return !field.isStatic() && !field.getType().isAbstract();
    }

    /**
     * Private constructor, it prevents direct instantiaion of the class.
     */
    private DomainTree() {
        treeElements = new HashMap<String, IOpenClass>(predefinedTypes);
    }

    private boolean addType(IOpenClass type) {
        String simpleTypeName = type.getDisplayName(INamedThing.SHORT);

        if (!treeElements.containsKey(simpleTypeName) && !ignoredTypes.contains(simpleTypeName)) {
            if (Collection.class.isAssignableFrom(type.getInstanceClass())) {
                return false;
            }

            treeElements.put(simpleTypeName, type);

            if (inspectTypeRecursively(type)) {
                // types of IOpenClass fields
                Iterator<IOpenField> fieldIterator = type.fields();
                while (fieldIterator.hasNext()) {
                    IOpenField field = fieldIterator.next();
                    if (isAppropriateProperty(field)) {
                        addType(field.getType());
                    }
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Flat list of class names.
     *
     * @param sorted if <code>true</code> returned collection is sorted,
     *            otherwise name order is not specified.
     * @return all class names of the domain tree
     */
    public Collection<String> getAllClasses(boolean sorted) {
        Collection<String> unsortedClasses = treeElements.keySet();
        if (sorted) {
            List<String> sortedClasses = new ArrayList<String>(unsortedClasses);
            Collections.sort(sortedClasses, new Comparator<String>() {
                public int compare(String s1, String s2) {
                    boolean primitive1 = predefinedTypes.containsKey(s1);
                    boolean primitive2 = predefinedTypes.containsKey(s2);
                    if (primitive1 == primitive2) {
                        boolean defPackage1 = s1.startsWith("java.");
                        boolean defPackage2 = s2.startsWith("java.");
                        if (defPackage1 != defPackage2) {
                            if (primitive1) {
                                return defPackage2 ? -1 : 1;
                            }
                            return defPackage1 ? -1 : 1;
                        }
                        return s1.compareTo(s2);
                    }
                    return primitive1 ? -1 : 1;
                }
            });

            return sortedClasses;
        } else {
            return unsortedClasses;
        }
    }

    /**
     * Returns properties of a given class.
     *
     * @param typename class to get properties for.
     * @return collection of property names or <code>null</code> if typename
     *         is unknown.
     */
    public Collection<String> getClassProperties(String typename) {
        IOpenClass openClass = treeElements.get(typename);
        if (openClass == null) {
            return null;
        }

        Collection<String> result = new ArrayList<String>();
        Iterator<IOpenField> fieldIterator = openClass.fields();
        while (fieldIterator.hasNext()) {
            IOpenField field = fieldIterator.next();
            if (isAppropriateProperty(field)) {
                result.add(field.getName());
            }
        }
        return result;
    }

    public String getTypename(DomainTreeContext context, String path) {
        if (path == null) {
            return null;
        }
        String[] parts = path.split("\\.");
        if (parts.length == 0) {
            return null;
        }
        String rootClassname = context.getObjectType(parts[0]);
        if (rootClassname == null) {
            return null;
        }
        IOpenClass openClass = treeElements.get(rootClassname);
        if (openClass == null) {
            return null;
        }

        for (int i = 1; i < parts.length; ++i) {
            IOpenField field = openClass.getField(parts[i]);
            if (field == null) {
                return null;
            }
            openClass = field.getType();
        }
        return openClass.getName();
    }

    private void scanTable(TableSyntaxNode node) {
        String nodeType = node.getType();

        if (nodeType.equals(ITableNodeTypes.XLS_DT)) {
            IOpenMember table = node.getMember();
            if (table != null) {
                scanDecisionTable((DecisionTable) table);
            }

        } else if (nodeType.equals(ITableNodeTypes.XLS_DATATYPE)) {
            // scan Datatype table
        }
    }

    /**
     * Scans given table, and adds classes that the table references (parameter
     * types, condition and action variable types) to the tree.
     *
     * @param decisionTable decision table to scan.
     */
    private void scanDecisionTable(DecisionTable decisionTable) {
        for (IOpenClass paramType : decisionTable.getHeader().getSignature().getParameterTypes()) {
            addType(paramType);
        }

        for (ICondition condition : decisionTable.getConditionRows()) {
            for (IParameterDeclaration param : condition.getParams()) {
                addType(param.getType());
            }
        }

        for (IAction action : decisionTable.getActionRows()) {
            for (IParameterDeclaration param : action.getParams()) {
                addType(param.getType());
            }
        }
    }

}
