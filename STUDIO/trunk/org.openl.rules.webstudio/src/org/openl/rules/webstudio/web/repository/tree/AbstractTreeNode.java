package org.openl.rules.webstudio.web.repository.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openl.rules.common.ProjectVersion;
import org.openl.rules.project.abstraction.AProject;
import org.openl.rules.project.abstraction.AProjectArtefact;
import org.openl.rules.project.abstraction.RulesProject;
import org.openl.rules.webstudio.web.repository.DependencyBean;
import org.openl.rules.webstudio.web.repository.RepositoryUtils;

import com.google.common.collect.Iterators;

/**
 * This abstract class implements basic functionality of {@link TreeNode}
 * interface. Every particular tree node should extends this class and adds own
 * implementation for UI related methods: {@link #getType()},
 * {@link #getIcon()}, and {@link #getIconLeaf()}.
 *
 * <p>
 * Derived classes is going to be used in richfaces:
 *
 * <pre>
 * &lt;rich:tree value=&quot;...&quot; var=&quot;item&quot; nodeFace=&quot;#{item.type}&quot; &gt;
 *     &lt;rich:treeNode type=&quot;type1&quot; icon=&quot;#{item.icon}&quot; iconLeaf=&quot;#{item.iconLeaf}&quot;&gt;
 *         &lt;h:outputText value=&quot;#{item.name}&quot; /&gt;
 *     &lt;/rich:treeNode&gt;
 *     &lt;rich:treeNode type=&quot;type2&quot; icon=&quot;#{item.icon}&quot; iconLeaf=&quot;#{item.iconLeaf}&quot;&gt;
 *         &lt;h:outputText value=&quot;#{item.name}&quot; /&gt;
 *     &lt;/rich:treeNode&gt;
 *
 *     ... more node types
 *
 * &lt;/rich:tree&gt;
 * </pre>
 *
 * @see TreeNode
 *
 * @author Aleh Bykhavets
 *
 */
public abstract class AbstractTreeNode implements TreeNode {

    private static final long serialVersionUID = 1238954077308840345L;

    // private static final Comparator<AbstractTreeNode> CHILD_COMPARATOR
    // = new Comparator<AbstractTreeNode>() {
    // public int compare(AbstractTreeNode o1, AbstractTreeNode o2) {
    // ProjectArtefact p1 = o1.dataBean;
    // ProjectArtefact p2 = o2.dataBean;
    //
    // if (p1 == null || p2 == null || p1.isFolder() == p2.isFolder()) {
    // return o1.getName().compareToIgnoreCase(o2.getName());
    // }
    //
    // return (p1.isFolder() ? -1 : 1);
    // }
    // };
    //
    /**
     * Identifier of the node.
     */
    private String id;
    /**
     * Display name
     */
    private String name;

    /**
     * Reference on parent node. For upper level node(s) it is <code>null</code>
     */
    private TreeNode parent;

    /**
     * When <code>true</code> then the node cannot have children. Any
     * operation that implies adding/getting/removing children will lead to
     * {@link UnsupportedOperationException}.
     */
    private boolean isLeafOnly;

    private AProjectArtefact data;

    /**
     * Creates tree node that can have children.
     *
     * @param id id to distinguish the node among others
     * @param name display name of the node
     */
    public AbstractTreeNode(String id, String name) {
        this(id, name, false);
    }

    /**
     * Creates tree node. Can control whether the node is LeafOnly (i.e. cannot
     * have children) or usual one.
     *
     * @param id id to distinguish the node among others
     * @param name display name of the node
     * @param isLeafOnly whether the node is LeafOnly (true) or usual (false)
     */
    public AbstractTreeNode(String id, String name, boolean isLeafOnly) {
        this.id = id;
        this.name = name;
        this.isLeafOnly = isLeafOnly;
    }

    /**
     * Short for <code>addChild(child.getId(), child)</code>.
     * <p>
     * Can be use in a chain:
     *
     * <pre>
     * add(child1).add(child2)...add(childN);
     * </pre>
     *
     * @param child a node to be added as child
     * @return self-reference on the node
     */
    public TreeNode add(TreeNode child) {
        addChild(child.getId(), child);
        return this;
    }

    /**
     * @see TreeNode#addChild(Object, TreeNode)
     */
    public void addChild(Object id, TreeNode child) {
        checkLeafOnly();

        getElements().put(id, child);
        child.setParent(this);
    }

    public void addChild(Object id, org.richfaces.model.TreeNode child) {
    }

    public void insertChild(int index, Object id, org.richfaces.model.TreeNode child) {
    }

    public int indexOf(Object id) {
        return 0;
    }

    /**
     * When the node is in LeafOnly mode, i.e. cannot have children, this method
     * will throw exception.
     * <p>
     * This method is used internally in {@link #addChild(Object, TreeNode)},
     * {@link #getChild(Object)}, and {@link #removeChild(Object)} methods.
     * <p>
     * Note, that method {@link #getChildrenKeysIterator()()} must work in any case.
     *
     * @throws UnsupportedOperationException if the node is LeafOnly and current
     *             operation implies work with children
     */
    private void checkLeafOnly() {
        if (isLeafOnly) {
            throw new UnsupportedOperationException("cannot have children");
        }
    }

    /**
     * Clears children. Works recursively.
     */
    public void clear() {
        if (getElements() != null) {
            // recursion
            for (TreeNode child : getElements().values()) {
                child.clear();
            }

            getElements().clear();
        }
    }

    /**
     * @see TreeNode#getChild(Object)
     */
    public TreeNode getChild(Object id) {
        checkLeafOnly();

        if (getElements().containsKey(id)){
            return getElements().get(id);
        } else if (id instanceof String){
            String idAsString = (String)id;
            if(getElements().containsKey(FOLDER_PREFIX + idAsString)){
                return getElements().get(FOLDER_PREFIX + idAsString);
            }
            if(getElements().containsKey(FILE_PREFIX + idAsString)){
                return getElements().get(FILE_PREFIX + idAsString);
            }
        }
        return null;
    }

    public List<TreeNode> getChildNodes() {
        List<TreeNode> list = new ArrayList<TreeNode>(getElements().values());
        // elements are sorted already
        // Collections.sort(list, CHILD_COMPARATOR);

        return list;
    }

    public Iterator<Object> getChildrenKeysIterator() {
        Iterator<Object> result;

        if (isLeafOnly) {
            checkLeafOnly();
            result = Iterators.emptyIterator();
            // work around limitation for TreeNode
        } else {
            result = getElements().keySet().iterator();
        }

        return result;
    }

    public AProjectArtefact getData() {
        return data;
    }

    public List<DependencyBean> getDependencies() {
        return null;
    }

    /**
     * Returns URL of image that will be displayed with the node when it has at
     * least one child.
     *
     * <pre>
     * &lt;rich:treeNode &lt;b&gt;icon=&quot;#{item.icon}&quot;&lt;/b&gt; ... &gt;
     * </pre>
     *
     * @return URL of image
     */
    public abstract String getIcon();

    /**
     * Returns URL of image that will be displayed with the node when
     * {@link #isLeaf()} is true.
     *
     * <pre>
     * &lt;rich:treeNode &lt;b&gt;iconLeaf=&quot;#{item.iconLeaf}&quot;&lt;/b&gt; ... &gt;
     * </pre>
     *
     * @return URL of image
     */
    public abstract String getIconLeaf();

    public String getId() {
        return id;
    }

    /**
     * Returns display name of the node.
     *
     * @return name of node in tree
     */
    public String getName() {
        return name;
    }

    /**
     * @see TreeNode#getParent()
     */
    public TreeNode getParent() {
        return parent;
    }

    public String getVersionName() {
        if (data instanceof AProject) {
            ProjectVersion version = ((AProject) data).getVersion();
            return (version == null) ? null : version.getVersionName();
        }
        return null;
    }

    private RulesProject findProjectContainingCurrentArtefact() {
        TreeNode node = this;
        while (node != null && !(node.getData() instanceof RulesProject)) {
            node = node.getParent();
        }
        return node == null ? null : (RulesProject) node.getData();
    }

    public Collection<ProjectVersion> getVersions() {
        if (data instanceof AProjectArtefact) {
            RulesProject project = findProjectContainingCurrentArtefact();
            
            List<ProjectVersion> result;
            if (project != null) {
                result = project.getVersionsForArtefact((getData()).getArtefactPath().withoutFirstSegment());
            } else {
                result = getData().getVersions();
            }
            Collections.sort(result, RepositoryUtils.VERSIONS_REVERSE_COMPARATOR);
            return result;
        } else {
            return new LinkedList<ProjectVersion>();
        }
    }

    public boolean isLeafOnly() {
        return isLeafOnly;
    }

    public boolean isLeaf() {
        boolean result;

        if (isLeafOnly) {
            result = true;
        } else {
            result = getElements().isEmpty();
        }

        return result;
    }

    public void removeChild(Object id) {
        checkLeafOnly();

        getElements().remove(id);
    }

    public void removeChildren() {
        checkLeafOnly();
        getElements().clear();
    }

    public void setData(AProjectArtefact data) {
        this.data = data;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public void refresh(){
        data.refresh();
    }

}
