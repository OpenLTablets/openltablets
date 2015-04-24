package org.openl.rules.webstudio.web;

import org.apache.commons.lang3.StringUtils;
import org.openl.message.OpenLMessage;
import org.openl.rules.lang.xls.XlsWorkbookSourceCodeModule;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.lang.xls.syntax.XlsModuleSyntaxNode;
import org.openl.rules.table.xls.XlsUrlParser;
import org.openl.rules.ui.ProjectModel;
import org.openl.rules.webstudio.web.util.Constants;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.syntax.ISyntaxNode;
import org.openl.util.StringTool;

public class MessageHandler {

    /**
     * Gets the url to the source of message.
     *
     * @param message {@link OpenLMessage} instance
     * @param model   project model for current module.
     * @return url to error source.
     */
    public String getSourceUrl(OpenLMessage message, ProjectModel model) {
        String url = null;
        String errorUri = getUri(message);
        if (StringUtils.isNotBlank(errorUri)) {
            url = getUrl(model, errorUri, message);
        }
        return url;
    }

    /**
     * Gets the url for messages that don`t have any sources.
     */
    public String getUrlForEmptySource(OpenLMessage message) {
        return WebStudioUtils.getWebStudio().url("message" + "?type" + "=" + message.getSeverity().name()
                + "&summary" + "=" + StringTool.encodeURL(message.getSummary()));
    }

    protected String getUri(OpenLMessage message) {
        // Default implementation
        return message.getSourceLocation();
    }

    protected String getUrl(ProjectModel model, String errorUri, OpenLMessage message) {
        String url;
        TableSyntaxNode node = model.getNode(errorUri);

        if (isCurrentModule(model, node)) {
            // Table belongs to current module
            url = getUrlForCurrentModule(errorUri, node.getId());
        } else {
            // Table belongs to dependent module
            if (node != null) {
                url = getUrlToDependentModule(node);
            } else {
                url = getErrorUrlForDependency(message);
            }
        }
        return url;
    }

    private boolean isCurrentModule(ProjectModel model, TableSyntaxNode node) {
        ISyntaxNode referencedModule = getModuleNode(node);

        XlsWorkbookSourceCodeModule currentModuleWorkbook = model.getCurrentModuleWorkbook();

        return referencedModule != null && currentModuleWorkbook != null && currentModuleWorkbook.getSource()
                .equals(referencedModule.getModule());
    }

    private ISyntaxNode getModuleNode(TableSyntaxNode node) {
        ISyntaxNode moduleNode = node;
        while (moduleNode != null) {
            if (moduleNode instanceof XlsModuleSyntaxNode) {
                break;
            } else {
                moduleNode = moduleNode.getParent();
            }
        }
        return moduleNode;
    }

    private String getUrlToDependentModule(TableSyntaxNode node) {
        String url = WebStudioUtils.getWebStudio().url("table", node.getUri());
        if (url.endsWith("table")) {
            url += "?" + Constants.REQUEST_PARAM_ID + "=" + node.getId();
        }
        return url;
    }

    private String getUrlForCurrentModule(String errorUri, String tableId) {
        XlsUrlParser uriParser = new XlsUrlParser();
        uriParser.parse(errorUri);
        String url = "table?id=" + tableId;
        if (StringUtils.isNotBlank(uriParser.cell)) {
            url += "&errorCell=" + uriParser.cell;
        }
        return WebStudioUtils.getWebStudio().url(url);
    }

    private String getErrorUrlForDependency(OpenLMessage message) {
        return WebStudioUtils.getWebStudio().url("message" + "?type" + "=" + message.getSeverity().name() + "&summary" + "="
                + StringTool.encodeURL(String.format("Dependency error: %s", message.getSummary())));
    }

}
