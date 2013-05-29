package org.openl.rules.ui.tree.richfaces;

import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.properties.ITableProperties;
import org.openl.rules.ui.IProjectTypes;
import org.openl.rules.ui.ProjectModel;
import org.openl.rules.ui.tree.ProjectTreeNode;
import org.openl.rules.webstudio.web.util.Constants;
import org.openl.util.StringTool;
import org.openl.util.tree.ITreeElement;

public class ProjectTreeBuilder extends TreeBuilder {

    private ProjectModel projectModel;

    public ProjectTreeBuilder(ITreeElement<?> root, ProjectModel projectModel) {
        super(root);
        this.projectModel = projectModel;
    }

    @Override
    @Deprecated
    protected int getState(ITreeElement<?> element) {
        ProjectTreeNode pte = (ProjectTreeNode) element;
        if (pte.getTableSyntaxNode() != null
                && projectModel.isTestable(pte.getTableSyntaxNode())) {
            return 2; // has tests
        }
        return super.getState(element);
    }

    @Override
    protected int getNumErrors(ITreeElement<?> element) {
        ProjectTreeNode pte = (ProjectTreeNode) element;
        return pte.getNumErrors();
    }

    @Override
    protected boolean isActive(ITreeElement<?> element) {
        ProjectTreeNode projectNode = (ProjectTreeNode) element;
        TableSyntaxNode syntaxNode = projectNode.getTableSyntaxNode();
        if (syntaxNode != null) {
            ITableProperties tableProperties = syntaxNode.getTableProperties();
            if (tableProperties != null) {
                Boolean active = tableProperties.getActive();
                if (active != null) {
                    return active;
                }
            }
        }
        return super.isActive(element);
    }

    @Override
    protected String getUrl(ITreeElement<?> element) {
        String elementType = element.getType();
        if (elementType.startsWith(IProjectTypes.PT_TABLE + ".")) {
            String uri = ((ProjectTreeNode) element).getUri();
            return "#table.xhtml?" + Constants.REQUEST_PARAM_URI + "=" + StringTool.encodeURL(uri);
        }
        return null;
    }
}
