package org.openl.rules.ui.tree;

import java.util.HashMap;
import java.util.Map;

import org.openl.rules.lang.xls.ITableNodeTypes;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.ui.IProjectTypes;

/**
 * Builds tree node using table type.
 * 
 */
public class TableTreeNodeBuilder extends BaseTableTreeNodeBuilder {

    private static final String OTHER_NODE_KEY = "Other";
    private static final String TABLE_TYPE_NAME = "Table Type";

    /**
     * Internal map that represent dictionary of available table types.
     */
    private Map<String, NodeKey> nodeKeysMap;

    /**
     * Default constructor.
     */
    public TableTreeNodeBuilder() {
        init();
    }

    /**
     * Initialize instance of class.
     */
    private void init() {

        nodeKeysMap = new HashMap<String, NodeKey>();

        nodeKeysMap.put(ITableNodeTypes.XLS_DT, new NodeKey(0, new String[] { "Decision", "Decision Tables", "" }));
        nodeKeysMap.put(ITableNodeTypes.XLS_SPREADSHEET, new NodeKey(1, new String[] { "Spreadsheet",
                "Spreadsheet Tables", "" }));
        nodeKeysMap.put(ITableNodeTypes.XLS_SPREADSHEET, new NodeKey(1, new String[] { "Spreadsheet",
                "Spreadsheet Tables", "" }));
        nodeKeysMap.put(ITableNodeTypes.XLS_TBASIC, new NodeKey(2, new String[] { "TBasic",
                "Structured Algorithm Tables", "" }));
        nodeKeysMap.put(ITableNodeTypes.XLS_COLUMN_MATCH, new NodeKey(3, new String[] { "Column Match",
                "Column Match Tables", "" }));
        nodeKeysMap.put(ITableNodeTypes.XLS_DATA, new NodeKey(4, new String[] { "Data", "Data Tables", "" }));
        nodeKeysMap.put(ITableNodeTypes.XLS_TEST_METHOD, new NodeKey(5, new String[] { "Test",
                "Tables with data for method unit tests", "" }));
        nodeKeysMap.put(ITableNodeTypes.XLS_RUN_METHOD, new NodeKey(5, new String[] { "Run",
                "Tables with run data for methods", "" }));
        nodeKeysMap.put(ITableNodeTypes.XLS_DATATYPE,
                new NodeKey(6, new String[] { "Datatype", "OpenL Datatypes", "" }));
        nodeKeysMap.put(ITableNodeTypes.XLS_METHOD, new NodeKey(7, new String[] { "Method", "OpenL Methods", "" }));
        nodeKeysMap.put(ITableNodeTypes.XLS_ENVIRONMENT, new NodeKey(8, new String[] { "Configuration",
                "Environment table, used to configure OpenL project", "" }));

        nodeKeysMap.put(OTHER_NODE_KEY, new NodeKey(10, new String[] { "Other",
                "The Tables that do not belong to any known OpenL type", "" }));        
        nodeKeysMap.put(ITableNodeTypes.XLS_PROPERTIES, new NodeKey(11, new String[] { "Properties", 
                "Properties Tables", "" }));
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getDisplayValue(Object nodeObject, int i) {

        NodeKey nodeKey = getNodeKey(nodeObject);

        return nodeKey.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return TABLE_TYPE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType(Object nodeObject) {
        /*TableSyntaxNode tableSyntaxNode = (TableSyntaxNode) nodeObject;
        return IProjectTypes.PT_FOLDER + "." + tableSyntaxNode.getType();*/
        return IProjectTypes.PT_FOLDER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl(Object nodeObject) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWeight(Object nodeObject) {

        NodeKey nodeKey = getNodeKey(nodeObject);

        return nodeKey.getWeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object makeObject(TableSyntaxNode tableSyntaxNode) {

        return tableSyntaxNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProblems(Object nodeObject) {
        return null;
    }

    /**
     * Gets node key for node object.
     * 
     * @param nodeObject node object
     * @return node key
     */
    private NodeKey getNodeKey(Object nodeObject) {

        TableSyntaxNode tsn = (TableSyntaxNode) nodeObject;
        String type = tsn.getType();

        NodeKey nodeKey = nodeKeysMap.get(type);

        if (nodeKey == null) {
            nodeKey = nodeKeysMap.get(OTHER_NODE_KEY);
        }

        return nodeKey;
    }
}
