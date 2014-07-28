package org.openl.rules.ui.tree;

import org.apache.commons.lang3.StringUtils;
import org.openl.rules.lang.xls.XlsNodeTypes;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.properties.ITableProperties;
import org.openl.rules.table.properties.inherit.InheritanceLevel;
import org.openl.rules.ui.IProjectTypes;

/**
 * Builder for module properties table. 
 * 
 * @author DLiauchuk
 *
 */
public class ModulePropertiesTableNodeBuilder extends BaseTableTreeNodeBuilder {
    
    private static final String FOLDER_NAME = "Module Properties";
    private static final String MODULE_PROPERTIES_TABLE = "Module Properties Table";
    
    @Override
    public String[] getDisplayValue(Object nodeObject, int i) {
        return new String[]{FOLDER_NAME, FOLDER_NAME, FOLDER_NAME};
    }

    @Override
    public String getName() {
        return MODULE_PROPERTIES_TABLE;
    }

    @Override
    public Object getProblems(Object nodeObject) {        
        TableSyntaxNode tsn = (TableSyntaxNode) nodeObject;
        return tsn.getErrors() != null ? tsn.getErrors() : tsn.getValidationResult();
    }

    @Override
    public String getType(Object nodeObject) {
        return IProjectTypes.PT_TABLE_GROUP;
    }

    @Override
    public String getUrl(Object nodeObject) {
        TableSyntaxNode tableSyntaxNode = (TableSyntaxNode) nodeObject;
        return tableSyntaxNode.getUri();
    }

    @Override
    public int getWeight(Object nodeObject) {        
        return 0;
    }

    @Override
    protected Object makeObject(TableSyntaxNode tableSyntaxNode) {
        return tableSyntaxNode;
    }
    
    @Override
    public boolean isBuilderApplicableForObject(TableSyntaxNode tableSyntaxNode) {
        if (XlsNodeTypes.XLS_PROPERTIES.toString().equals(tableSyntaxNode.getType()) && isModulePropertyTable(tableSyntaxNode)) {
            return true;
        }
        return false;
    }
    
    @Override
    public ProjectTreeNode makeNode(TableSyntaxNode tableSyntaxNode, int i) {
        String folderName = FOLDER_NAME;
        return makeFolderNode(folderName);
    }
    
    public static boolean isModulePropertyTable(TableSyntaxNode tableSyntaxNode) {
        boolean result = false;
        ITableProperties tableProperties = tableSyntaxNode.getTableProperties();
        if (tableProperties != null) {
            String propValue = tableProperties.getScope();
            if (StringUtils.isNotEmpty(propValue) && InheritanceLevel.MODULE.getDisplayName().equals(propValue)) {
                result = true;
            }
        }
        return result;
    }
    
    private ProjectTreeNode makeFolderNode(String folderName) {
        return new ProjectTreeNode(new String[] { folderName, folderName, folderName }, IProjectTypes.PT_FOLDER, null, null, 0, null);
    }

}
