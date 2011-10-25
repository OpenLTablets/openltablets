package org.openl.rules.ui.tree.view;

import org.openl.rules.ui.tree.BaseTableTreeNodeBuilder;
import org.openl.rules.ui.tree.CategoryPropertiesTableNodeBuilder;
import org.openl.rules.ui.tree.CategoryTreeNodeBuilder;
import org.openl.rules.ui.tree.OpenMethodInstancesGroupTreeNodeBuilder;
import org.openl.rules.ui.tree.ModulePropertiesTableNodeBuilder;
import org.openl.rules.ui.tree.TableInstanceTreeNodeBuilder;
import org.openl.rules.ui.tree.TableVersionTreeNodeBuilder;
import org.openl.rules.ui.tree.TreeNodeBuilder;

public class CategoryView implements RulesTreeView {

    private static final BaseTableTreeNodeBuilder[] sorters = {
        new ModulePropertiesTableNodeBuilder(), 
        new CategoryTreeNodeBuilder(),
        new CategoryPropertiesTableNodeBuilder(),
        new OpenMethodInstancesGroupTreeNodeBuilder(),
        new TableInstanceTreeNodeBuilder(),
        new TableVersionTreeNodeBuilder()
    };

    public String getName() {
    	return "category";
    }
    
    public String getDisplayName() {
    	return "Category";
    }

    public String getDescription() {
    	return "Provides categorized view";
    }

    @SuppressWarnings("unchecked")
    public TreeNodeBuilder[] getBuilders() {
        return sorters;
    }

}