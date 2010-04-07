package org.openl.rules.webstudio.web;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openl.CompiledOpenClass;
import org.openl.exception.OpenLException;
import org.openl.main.SourceCodeURLTool;
import org.openl.message.OpenLErrorMessage;
import org.openl.message.OpenLMessage;
import org.openl.message.OpenLMessagesUtils;
import org.openl.message.Severity;
import org.openl.rules.ui.ProjectModel;
import org.openl.rules.ui.WebStudio;
import org.openl.rules.ui.tree.TreeNodeData;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.rules.webtools.XlsUrlParser;
import org.openl.util.StringTool;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

/**
 * Request scope managed bean providing logic for problems tree page of OpenL Studio.
 * 
 * @author Andrei Astrouski
 */
public class ProblemsBean {

    public static final String ERRORS_ROOT_NAME = "Errors";
    public static final String WARNINGS_ROOT_NAME = "Warnings";

    public static final String ERROR_NODE_NAME = "error";
    public static final String WARNING_NODE_NAME = "warning";

    public ProblemsBean() {
    }

    public TreeNode<?> getTree() {
        
        int nodeCount = 1;
        
        WebStudio studio = WebStudioUtils.getWebStudio();
        ProjectModel model = studio.getModel();
        CompiledOpenClass compiledOpenClass = model.getWrapper().getCompiledOpenClass();
        
        List<OpenLMessage> messages = compiledOpenClass.getMessages();

        TreeNode<TreeNodeData> root = new TreeNodeImpl<TreeNodeData>();

        List<OpenLMessage> errorMessages = OpenLMessagesUtils.filterMessagesBySeverity(messages, Severity.ERROR);
        if (CollectionUtils.isNotEmpty(errorMessages)) {
            TreeNode<TreeNodeData> errorsRoot = createMessagesRoot(ERRORS_ROOT_NAME, errorMessages.size());
            addMessageNodes(errorsRoot, ERROR_NODE_NAME, errorMessages);
            root.addChild(nodeCount++, errorsRoot);
        }

        List<OpenLMessage> warningMessages = OpenLMessagesUtils.filterMessagesBySeverity(messages, Severity.WARN);
        if (CollectionUtils.isNotEmpty(warningMessages)) {
            TreeNode<TreeNodeData> warningsRoot = createMessagesRoot(WARNINGS_ROOT_NAME, warningMessages.size());
            addMessageNodes(warningsRoot, WARNING_NODE_NAME, warningMessages);
            root.addChild(nodeCount++, warningsRoot);
        }

        return root;
    }

    private TreeNode<TreeNodeData> createMessagesRoot(String rootName, int messagesNumber) {
        TreeNode<TreeNodeData> messagesRoot = new TreeNodeImpl<TreeNodeData>();
        TreeNodeData nodeData = new TreeNodeData(
                rootName + " [" + messagesNumber + "]", rootName, null, 0, rootName.toLowerCase(), true);
        messagesRoot.setData(nodeData);
        return messagesRoot;
    }

    private void addMessageNodes(TreeNode<TreeNodeData> parent, String nodeName, List<OpenLMessage> messages) {
        int nodeCount = 1;

        for (OpenLMessage message : messages) {
            TreeNode<TreeNodeData> messageNode = new TreeNodeImpl<TreeNodeData>();
            String url = getNodeUrl(message);
            TreeNodeData nodeData = new TreeNodeData(message.getSummary(), message.getDetails(),
                    url, 0, nodeName.toLowerCase(), true);
            messageNode.setData(nodeData);
            parent.addChild(nodeCount++, messageNode);
        }
    }

    private String getNodeUrl(OpenLMessage message) {
        String url = null;
        if (message instanceof OpenLErrorMessage) {
            OpenLErrorMessage errorMessage = (OpenLErrorMessage) message;
            OpenLException error = errorMessage.getError();
            String errorUri = SourceCodeURLTool.makeSourceLocationURL(error.getLocation(), error.getSourceModule(), "");
            String tableUri = WebStudioUtils.getWebStudio().getModel().findTableUri(errorUri);
            if (StringUtils.isNotBlank(tableUri)) {
                XlsUrlParser uriParser = new XlsUrlParser();
                uriParser.parse(errorUri);
                url = "tableeditor/showTable.xhtml"
                    + "?uri=" + StringTool.encodeURL(tableUri)
                    + "&errorCell=" + uriParser.cell;
            }
        }
        if (StringUtils.isBlank(url)) {
            url = "tableeditor/showMessage.xhtml"
                + "?type" + "=" + message.getSeverity().name()
                + "&summary" + "=" + message.getSummary();
        }
        return url;
    }

}
