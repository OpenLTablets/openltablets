package org.openl.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openl.exception.OpenLCompilationException;
import org.openl.extension.xmlrules.model.ExtensionModule;
import org.openl.extension.xmlrules.model.TableGroup;
import org.openl.message.OpenLMessagesUtils;
import org.openl.rules.lang.xls.*;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.lang.xls.syntax.WorkbookSyntaxNode;
import org.openl.rules.lang.xls.syntax.WorksheetSyntaxNode;
import org.openl.rules.lang.xls.syntax.XlsModuleSyntaxNode;
import org.openl.rules.table.IGridTable;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.syntax.ISyntaxNode;
import org.openl.syntax.code.IDependency;
import org.openl.syntax.code.IParsedCode;
import org.openl.syntax.code.impl.ParsedCode;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.syntax.impl.IdentifierNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base extension parser
 */
public abstract class ExtensionParser extends BaseParser {
    private final Logger log = LoggerFactory.getLogger(ExtensionParser.class);

    @Override
    public IParsedCode parseAsModule(IOpenSourceCodeModule source) {
        ExtensionModule module = load(source);

        ISyntaxNode syntaxNode = null;
        List<SyntaxNodeException> errors = new ArrayList<SyntaxNodeException>();

        try {
            XlsWorkbookSourceCodeModule workbookSourceCodeModule = getWorkbookSourceCodeModule(module, source);

            WorkbookSyntaxNode[] workbooksArray = getWorkbooks(module, workbookSourceCodeModule);
            syntaxNode = new XlsModuleSyntaxNode(workbooksArray,
                    workbookSourceCodeModule,
                    null,
                    null,
                    Collections.<String>emptyList(),
                    Collections.<IdentifierNode>emptyList());
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            String message = String.format("Failed to open extension module: %s. Reason: %s",
                    source.getUri(0),
                    e.getMessage());
            SyntaxNodeException error = SyntaxNodeExceptionUtils.createError(message, e, null);
            errors.add(error);
        }

        SyntaxNodeException[] parsingErrors = errors.toArray(new SyntaxNodeException[errors.size()]);

        return new ParsedCode(syntaxNode,
                source,
                parsingErrors,
                new IDependency[] {});
    }

    protected WorkbookSyntaxNode[] getWorkbooks(ExtensionModule module, XlsWorkbookSourceCodeModule workbookSourceCodeModule) {
        TablePartProcessor tablePartProcessor = new TablePartProcessor();

        List<WorksheetSyntaxNode> sheetNodeList = new ArrayList<WorksheetSyntaxNode>();
        List<TableGroup> tableGroups = module.getTableGroups();
        for (int i = 0; i < tableGroups.size(); i++) {
            TableGroup tableGroup = tableGroups.get(i);
            // Sheet name is used as category name in WebStudio
            XlsSheetSourceCodeModule sheetSource = new XlsSheetSourceCodeModule(i, workbookSourceCodeModule);
            sheetNodeList.add(getWorksheet(sheetSource, tableGroup, tablePartProcessor));
        }
        WorksheetSyntaxNode[] sheetNodes = sheetNodeList.toArray(new WorksheetSyntaxNode[sheetNodeList.size()]);

        TableSyntaxNode[] mergedNodes = {};
        try {
            List<TablePart> tableParts = tablePartProcessor.mergeAllNodes();
            int n = tableParts.size();
            mergedNodes = new TableSyntaxNode[n];
            for (int i = 0; i < n; i++) {
                mergedNodes[i] = preprocessTable(tableParts.get(i).getTable(), tableParts.get(i).getSource(),
                        tablePartProcessor);
            }
        } catch (OpenLCompilationException e) {
            OpenLMessagesUtils.addError(e);
        }

        return new WorkbookSyntaxNode[] { new WorkbookSyntaxNode(sheetNodes, mergedNodes, workbookSourceCodeModule) };
    }

    protected WorksheetSyntaxNode getWorksheet(XlsSheetSourceCodeModule sheetSource, TableGroup tableGroup, TablePartProcessor tablePartProcessor) {
        IGridTable[] tables = getAllGridTables(sheetSource, tableGroup);
        List<TableSyntaxNode> tableNodes = new ArrayList<TableSyntaxNode>();

        for (IGridTable table : tables) {
            try {
                tableNodes.add(preprocessTable(table, sheetSource, tablePartProcessor));
            } catch (OpenLCompilationException e) {
                OpenLMessagesUtils.addError(e);
            }
        }

        return new WorksheetSyntaxNode(tableNodes.toArray(new TableSyntaxNode[tableNodes.size()]), sheetSource);
    }

    private TableSyntaxNode preprocessTable(IGridTable table,
            XlsSheetSourceCodeModule source,
            TablePartProcessor tablePartProcessor) throws
                                                                                                    OpenLCompilationException {
        TableSyntaxNode tsn = XlsHelper.createTableSyntaxNode(table, source);
        String type = tsn.getType();
        if (type.equals(XlsNodeTypes.XLS_TABLEPART.toString())) {
            try {
                tablePartProcessor.register(table, source);
            } catch (Throwable t) {
                SyntaxNodeException sne = SyntaxNodeExceptionUtils.createError(t, tsn);
                tsn.addError(sne);
                OpenLMessagesUtils.addError(sne.getMessage());
            }
        }
        return tsn;
    }

    /**
     * Load the project from source
     *
     * @param source source
     * @return loaded project
     */
    protected abstract ExtensionModule load(IOpenSourceCodeModule source);

    /**
     * Wrap source to XlsWorkbookSourceCodeModule
     */
    protected abstract XlsWorkbookSourceCodeModule getWorkbookSourceCodeModule(ExtensionModule project,
            IOpenSourceCodeModule source) throws OpenLCompilationException;

    /**
     * Gets all grid tables from the sheet.
     */
    protected abstract IGridTable[] getAllGridTables(XlsSheetSourceCodeModule sheetSource, TableGroup tableGroup);
}
