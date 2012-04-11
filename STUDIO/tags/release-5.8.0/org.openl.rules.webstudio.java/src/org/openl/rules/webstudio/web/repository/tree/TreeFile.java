package org.openl.rules.webstudio.web.repository.tree;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openl.rules.webstudio.web.repository.UiConst;

/**
 * Represents OpenL file in a tree.
 *
 * @author Aleh Bykhavets
 *
 */
public class TreeFile extends AbstractTreeNode {
    private static final long serialVersionUID = -4563895481021883236L;
    private static final List<AbstractTreeNode> EMPTY_LIST = new LinkedList<AbstractTreeNode>();

    public TreeFile(String id, String name) {
        // File cannot have children !!!
        super(id, name, true);
    }

    // ------ UI methods ------

    @Override
    public List<AbstractTreeNode> getChildNodes() {
        return EMPTY_LIST;
    }

    /** {@inheritDoc} */
    @Override
    public String getIcon() {
        // file is always leaf node
        return getIconLeaf();
    }

    /** {@inheritDoc} */
    @Override
    public String getIconLeaf() {
        // TODO: different types of files should have own icons
        return UiConst.ICON_FILE;
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return UiConst.TYPE_FILE;
    }
    
    @Override
    public String getId() {
        return AbstractTreeNode.FILE_PREFIX + super.getId();
    }

    @Override
    protected Map<Object, AbstractTreeNode> getElements() {
        return null;
    }
}
