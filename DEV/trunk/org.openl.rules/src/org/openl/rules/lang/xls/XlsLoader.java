/*
 * Created on Sep 23, 2003 Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.lang.xls;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openl.conf.IConfigurableResourceContext;
import org.openl.conf.IUserContext;
import org.openl.exception.OpenLCompilationException;
import org.openl.exception.OpenLRuntimeException;
import org.openl.message.OpenLMessagesUtils;
import org.openl.rules.dt.DecisionTableHelper;
import org.openl.rules.extension.load.IExtensionLoader;
import org.openl.rules.extension.load.NameConventionLoaderFactory;
import org.openl.rules.indexer.HeaderNodeFactory;
import org.openl.rules.lang.xls.syntax.HeaderSyntaxNode;
import org.openl.rules.lang.xls.syntax.OpenlSyntaxNode;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.lang.xls.syntax.WorkbookSyntaxNode;
import org.openl.rules.lang.xls.syntax.WorksheetSyntaxNode;
import org.openl.rules.lang.xls.syntax.XlsModuleSyntaxNode;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.rules.table.syntax.GridLocation;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.source.impl.URLSourceCodeModule;
import org.openl.syntax.ISyntaxNode;
import org.openl.syntax.code.Dependency;
import org.openl.syntax.code.DependencyType;
import org.openl.syntax.code.IDependency;
import org.openl.syntax.code.IParsedCode;
import org.openl.syntax.code.impl.ParsedCode;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.syntax.impl.IdentifierNode;
import org.openl.syntax.impl.Tokenizer;
import org.openl.util.PathTool;
import org.openl.util.StringTool;

/**
 * @author snshor
 * 
 */
public class XlsLoader {

    private final Log log = LogFactory.getLog(XlsLoader.class);

    private static final String[][] headerMapping = { { IXlsTableNames.DECISION_TABLE, XlsNodeTypes.XLS_DT.toString() },
            { IXlsTableNames.DECISION_TABLE2, XlsNodeTypes.XLS_DT.toString() },
            { IXlsTableNames.SIMPLE_DECISION_TABLE, XlsNodeTypes.XLS_DT.toString() },
            { IXlsTableNames.SIMPLE_DECISION_LOOKUP, XlsNodeTypes.XLS_DT.toString() },
            { IXlsTableNames.SPREADSHEET_TABLE, XlsNodeTypes.XLS_SPREADSHEET.toString() },
            { IXlsTableNames.SPREADSHEET_TABLE2, XlsNodeTypes.XLS_SPREADSHEET.toString() },
            { IXlsTableNames.TBASIC_TABLE, XlsNodeTypes.XLS_TBASIC.toString() },
            { IXlsTableNames.TBASIC_TABLE2, XlsNodeTypes.XLS_TBASIC.toString() },
            { IXlsTableNames.COLUMN_MATCH, XlsNodeTypes.XLS_COLUMN_MATCH.toString() },
            { IXlsTableNames.DATA_TABLE, XlsNodeTypes.XLS_DATA.toString() },
            { IXlsTableNames.DATATYPE_TABLE, XlsNodeTypes.XLS_DATATYPE.toString() },
            { IXlsTableNames.METHOD_TABLE, XlsNodeTypes.XLS_METHOD.toString() },
            { IXlsTableNames.METHOD_TABLE2, XlsNodeTypes.XLS_METHOD.toString() },
            { IXlsTableNames.ENVIRONMENT_TABLE, XlsNodeTypes.XLS_ENVIRONMENT.toString() },
            { IXlsTableNames.TEST_METHOD_TABLE, XlsNodeTypes.XLS_TEST_METHOD.toString() },
            { IXlsTableNames.TEST_TABLE, XlsNodeTypes.XLS_TEST_METHOD.toString() },
            { IXlsTableNames.RUN_METHOD_TABLE, XlsNodeTypes.XLS_RUN_METHOD.toString() },
            { IXlsTableNames.RUN_TABLE, XlsNodeTypes.XLS_RUN_METHOD.toString() },
            { IXlsTableNames.PERSISTENCE_TABLE, XlsNodeTypes.XLS_PERSISTENT.toString() },
            { IXlsTableNames.TABLE_PART, XlsNodeTypes.XLS_TABLEPART.toString() },
            { IXlsTableNames.PROPERTY_TABLE, XlsNodeTypes.XLS_PROPERTIES.toString() } };

    private static Map<String, String> tableHeaders;

    static {

        if (tableHeaders == null) {
            tableHeaders = new HashMap<String, String>();

            for (int i = 0; i < headerMapping.length; i++) {
                tableHeaders.put(headerMapping[i][0], headerMapping[i][1]);
            }
        }
    }

    private List<String> imports = new ArrayList<String>();
    
    private IncludeSearcher includeSeeker;
    
//    private IUserContext userContext;

    private OpenlSyntaxNode openl;

    private IdentifierNode vocabulary;

    private List<ISyntaxNode> nodesList = new ArrayList<ISyntaxNode>();

    private List<SyntaxNodeException> errors = new ArrayList<SyntaxNodeException>();

    private List<IdentifierNode> extensionNodes = new ArrayList<IdentifierNode>();

    private HashSet<String> preprocessedWorkBooks = new HashSet<String>();

    private List<WorkbookSyntaxNode> workbookNodes = new ArrayList<WorkbookSyntaxNode>();
    
    private List<IDependency> dependencies = new ArrayList<IDependency>();

    /**
     * @deprecated use {@link XlsLoader(IncludeSearcher includeSeeker, IUserContext userContext) {}
     * @param ucxt
     * @param searchPath
     */
    @Deprecated
    public XlsLoader(IConfigurableResourceContext ucxt, String searchPath) {
        this.includeSeeker = new IncludeSearcher(ucxt, searchPath);        
    }
    
    public XlsLoader(IncludeSearcher includeSeeker, IUserContext userContext) {
        this.includeSeeker = includeSeeker;
//        this.userContext = userContext;
    }
    
    public static Map<String, String> getTableHeaders() {
        return tableHeaders;
    }

    public void addError(SyntaxNodeException error) {
        errors.add(error);
    }

    public void addNode(ISyntaxNode node) {
        nodesList.add(node);
    }

    public void addExtensionNode(IdentifierNode node) {
        extensionNodes.add(node);
    }

    public Set<String> getPreprocessedWorkBooks() {
        return preprocessedWorkBooks;
    }    

    public IParsedCode parse(IOpenSourceCodeModule source) {

        preprocessWorkbook(source);
        
        addInnerImports();
        
        WorkbookSyntaxNode[] workbooksArray = workbookNodes.toArray(new WorkbookSyntaxNode[0]);
        XlsModuleSyntaxNode syntaxNode = new XlsModuleSyntaxNode(workbooksArray, source, openl, vocabulary, imports, extensionNodes);
        
        SyntaxNodeException[] parsingErrors = errors.toArray(new SyntaxNodeException[errors.size()]);
        IDependency[] dependenciesArray = dependencies.toArray(new IDependency[dependencies.size()]);

        return new ParsedCode(syntaxNode, source, parsingErrors, dependenciesArray);
    }

    private void preprocessEnvironmentTable(TableSyntaxNode tableSyntaxNode, XlsSheetSourceCodeModule source) {

        ILogicalTable logicalTable = tableSyntaxNode.getTable();

        int height = logicalTable.getHeight();

        for (int i = 1; i < height; i++) {
            ILogicalTable row = logicalTable.getRow(i);

            String name = row.getColumn(0).getSource().getCell(0, 0).getStringValue();
            
            if (IXlsTableNames.LANG_PROPERTY.equals(name)) {
                preprocessOpenlTable(row.getSource(), source);
            } else if (IXlsTableNames.DEPENDENCY.equals(name)) {
                // process module dependency
                //
                preprocessDependency(tableSyntaxNode, row.getSource(), source.getWorkbookSource().getSource());
            } else if (IXlsTableNames.INCLUDE_TABLE.equals(name)) {
                preprocessIncludeTable(tableSyntaxNode, row.getSource(), source);
            } else if (IXlsTableNames.IMPORT_PROPERTY.equals(name)) {
                preprocessImportTable(row.getSource(), source);

                // NOTE: A temporary implementation of multi-module feature.
                // } else if (IXlsTableNames.IMPORT_MODULE.equals(name)) {
                // preprocessModuleImportTable(row.getGridTable(), source);
            } else if (IXlsTableNames.VOCABULARY_PROPERTY.equals(name)) {            
                preprocessVocabularyTable(row.getSource(), source);
            } else if (StringUtils.isBlank(name) || DecisionTableHelper.isValidCommentHeader(name)) { // TODO:
                // DecisionTableHelper
                // rename
                // or
                // extract
                // common
                // methods
                ;// ignore comment
            } else {
                // TODO: why do we consider everything else an extension?
                IExtensionLoader loader = NameConventionLoaderFactory.INSTANCE.getLoader(name);

                if (loader != null) {
                    loader.process(this, tableSyntaxNode, row.getSource(), source);
                } else {
                    String message = String.format("Error in Environment table: can't find extension loader for '%s' keyword", name);
                    log.warn(message);
                    OpenLMessagesUtils.addWarn(message, tableSyntaxNode);
                }
            }
        }
    }

    private void preprocessDependency(TableSyntaxNode tableSyntaxNode, IGridTable gridTable,
        IOpenSourceCodeModule moduleSource) {

        int height = gridTable.getHeight();

        for (int i = 0; i < height; i++) {

            String dependency = gridTable.getCell(1, i).getStringValue();
            if (StringUtils.isNotBlank(dependency)) {
                dependency = dependency.trim();

                Dependency moduleDependency = new Dependency(DependencyType.MODULE, new IdentifierNode(
                    IXlsTableNames.DEPENDENCY, new GridLocation(gridTable), dependency, moduleSource));
                dependencies.add(moduleDependency);
            }
        }
    }

    private void preprocessImportTable(IGridTable table, XlsSheetSourceCodeModule sheetSource) {
        int height = table.getHeight();

        for (int i = 0; i < height; i++) {
            String singleImport = table.getCell(1, i).getStringValue();
            if (StringUtils.isNotBlank(singleImport)) {
                singleImport = singleImport.trim();
            }
            if (StringUtils.isNotEmpty(singleImport)) {
                addImport(singleImport);
            }
        }   
    }

    private void addImport(String singleImport) {
        if (!imports.contains(singleImport))
            imports.add(singleImport);
    }
        
    private void addInnerImports() {
        addImport("org.openl.rules.enumeration");
    }

    private void preprocessIncludeTable(TableSyntaxNode tableSyntaxNode, IGridTable table,
        XlsSheetSourceCodeModule sheetSource) {

        int height = table.getHeight();

        for (int i = 0; i < height; i++) {

            String include = table.getCell(1, i).getStringValue();

            if (StringUtils.isNotBlank(include)) {
                include = include.trim();
                IOpenSourceCodeModule src = null;

                if (include.startsWith("<")) {
                    src = includeSeeker.findInclude(StringTool.openBrackets(include, '<', '>', "")[0]);

                    if (src == null) {
                        registerIncludeError(tableSyntaxNode, table, i, include, null);
                        continue;
                    }
                } else {
                    try {
                        String newURL = PathTool.mergePath(sheetSource.getWorkbookSource().getUri(0), include);
                        src = new URLSourceCodeModule(new URL(newURL));
                    } catch (Throwable t) {
                        registerIncludeError(tableSyntaxNode, table, i, include, t);
                        continue;
                    }
                }

                try {
                    preprocessWorkbook(src);
                } catch (Throwable t) {
                    registerIncludeError(tableSyntaxNode, table, i, include, t);
                    continue;
                }
            }
        }
    }

    private void registerIncludeError(TableSyntaxNode tableSyntaxNode, IGridTable table, int i, String include, Throwable t) {
        SyntaxNodeException se = SyntaxNodeExceptionUtils.createError("Include " + include + " not found", t, null,
            new GridCellSourceCodeModule(table.getSubtable(1, i, 1, 1)));
        addError(se);
        tableSyntaxNode.addError(se);
        OpenLMessagesUtils.addError(se.getMessage());
    }

    private void preprocessOpenlTable(IGridTable table, XlsSheetSourceCodeModule source) {

        String openlName = table.getCell(1, 0).getStringValue();

        setOpenl(new OpenlSyntaxNode(openlName, new GridLocation(table), source));
    }

    private TableSyntaxNode preprocessTable(IGridTable table, XlsSheetSourceCodeModule source)
        throws OpenLCompilationException {

        GridCellSourceCodeModule src = new GridCellSourceCodeModule(table);

        IdentifierNode headerToken = Tokenizer.firstToken(src, " \n\r");
        

        String header = headerToken.getIdentifier();

        String xls_type = getTableHeaders().get(header);

        if (xls_type == null) {
            xls_type = XlsNodeTypes.XLS_OTHER.toString();
        }
        
        HeaderSyntaxNode headerNode = HeaderNodeFactory.getHeaderNode(xls_type, src, headerToken);

        TableSyntaxNode tsn = new TableSyntaxNode(xls_type, new GridLocation(table), source, table, headerNode);

        if (header.equals(IXlsTableNames.ENVIRONMENT_TABLE)) {
            preprocessEnvironmentTable(tsn, source);
        }
        else if (xls_type.equals(XlsNodeTypes.XLS_TABLEPART.toString()))
        {
        	try
        	{
        		preprocessTablePart(table, source);
        	}
        	catch(Throwable t)
        	{
        		SyntaxNodeException sne = SyntaxNodeExceptionUtils.createError(t, tsn);
        		addError(sne);
        		tsn.addError(sne);
                OpenLMessagesUtils.addError(sne.getMessage());
        	}
        }	

        addNode(tsn);

        return tsn;
    }

    TablePartProcessor tablePartProcessor = new TablePartProcessor();
    
    private void preprocessTablePart(IGridTable table,
			XlsSheetSourceCodeModule source) throws OpenLCompilationException {
    	
    	tablePartProcessor.processTablePart(table, source); 
	}

	private void preprocessVocabularyTable(IGridTable table, XlsSheetSourceCodeModule source) {

        String vocabularyStr = table.getCell(1, 0).getStringValue();

        setVocabulary(new IdentifierNode(IXlsTableNames.VOCABULARY_PROPERTY, new GridLocation(table), vocabularyStr,
            source));
    }

    private WorkbookSyntaxNode preprocessWorkbook(IOpenSourceCodeModule source) {

        String uri = source.getUri(0);

        if (preprocessedWorkBooks.contains(uri)) {
            return null;
        }

        preprocessedWorkBooks.add(uri);

        InputStream is = null;
        Workbook workbook = null;

        try {
            is = source.getByteStream();
            workbook = WorkbookFactory.create(is);
        } catch (Exception e) {
            log.error("Error while preprocessing workbook", e);
            
            String message = "Cannot open source file or file is corrupted";
            OpenLRuntimeException error = new OpenLRuntimeException(message);
            OpenLMessagesUtils.addError(error);

            throw error;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Throwable e) {
                log.error("Error trying close input stream:", e);
            }
        }

        XlsWorkbookSourceCodeModule workbookSourceModule = new XlsWorkbookSourceCodeModule(source, workbook);
        int nsheets = workbook.getNumberOfSheets();
        WorksheetSyntaxNode[] sheetNodes = new WorksheetSyntaxNode[nsheets];

        for (int i = 0; i < nsheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            XlsSheetSourceCodeModule sheetSource = preprocessSheet(sheet, workbookSourceModule);
            IGridTable[] tables = getAllGridTables(sheetSource);
            List<TableSyntaxNode> tableNodes = new ArrayList<TableSyntaxNode>();

            for (int j = 0; j < tables.length; j++) {
                
                TableSyntaxNode tsn;
                
                try {
                    tsn = preprocessTable(tables[j], sheetSource);
                    tableNodes.add(tsn);
                } catch (OpenLCompilationException e) {
                    OpenLMessagesUtils.addError(e);
                }
            }

            sheetNodes[i] = new WorksheetSyntaxNode(tableNodes.toArray(new TableSyntaxNode[0]), sheetSource);
        }

        TableSyntaxNode[] mergedNodes = {};
        try {
			List<TablePart> tableParts = tablePartProcessor.mergeAllNodes();
			int n = tableParts.size();
			mergedNodes = new TableSyntaxNode[n];
			for (int i = 0; i < n; i++) {
				mergedNodes[i] = preprocessTable(tableParts.get(i).getTable(),  tableParts.get(i).getSource());

			} 
			
		} catch (OpenLCompilationException e) {
            OpenLMessagesUtils.addError(e);
		}
        
        WorkbookSyntaxNode workbookNode = new WorkbookSyntaxNode(sheetNodes, mergedNodes, workbookSourceModule);
        workbookNodes.add(workbookNode);

        return workbookNode;
    }

    /**
     * Gets all grid tables from the sheet.
     * 
     * @param sheetSource
     * @return
     */
    private IGridTable[] getAllGridTables(XlsSheetSourceCodeModule sheetSource) {

        XlsSheetGridModel xlsGrid = new XlsSheetGridModel(sheetSource);
        IGridTable[] tables = xlsGrid.getTables();

        return tables;
    }

    private XlsSheetSourceCodeModule preprocessSheet(Sheet sheet, XlsWorkbookSourceCodeModule workbookSourceModule) {

        String sheetName = sheet.getSheetName();

        return new XlsSheetSourceCodeModule(sheet, sheetName, workbookSourceModule);
    }

    private void setOpenl(OpenlSyntaxNode openl) {

        if (this.openl == null) {
            this.openl = openl;
        } else {
            if (!this.openl.getOpenlName().equals(openl.getOpenlName())) {
                SyntaxNodeException error = SyntaxNodeExceptionUtils.createError("Only one openl statement is allowed",
                    null, openl);
                OpenLMessagesUtils.addError(error.getMessage());
                addError(error);
            }
        }
    }

    private void setVocabulary(IdentifierNode vocabulary) {

        if (this.vocabulary == null) {
            this.vocabulary = vocabulary;
        } else {
            SyntaxNodeException error = SyntaxNodeExceptionUtils.createError("Only one vocabulary is allowed", null,
                vocabulary);
            OpenLMessagesUtils.addError(error.getMessage());
            addError(error);
        }
    }

}