package org.openl.rules.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.openl.CompiledOpenClass;
import org.openl.message.OpenLMessages;
import org.openl.rules.lang.xls.ITableNodeTypes;
import org.openl.rules.lang.xls.IXlsTableNames;
import org.openl.rules.lang.xls.XlsWorkbookSourceCodeModule;
import org.openl.rules.lang.xls.binding.XlsMetaInfo;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.lang.xls.syntax.TableSyntaxNodeAdapter;
import org.openl.rules.lang.xls.syntax.WorkbookSyntaxNode;
import org.openl.rules.lang.xls.syntax.XlsModuleSyntaxNode;
import org.openl.rules.project.ModulesCache;
import org.openl.rules.project.instantiation.ReloadType;
import org.openl.rules.project.instantiation.RulesInstantiationStrategy;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.resolving.ResolvingStrategy;
import org.openl.rules.project.resolving.RulesProjectResolver;
import org.openl.rules.search.IOpenLSearch;
import org.openl.rules.search.ISearchTableRow;
import org.openl.rules.search.OpenLAdvancedSearchResult;
import org.openl.rules.search.OpenLAdvancedSearchResultViewer;
import org.openl.rules.search.OpenLBussinessSearchResult;
import org.openl.rules.search.OpenLSavedSearch;
import org.openl.rules.search.OpenLAdvancedSearchResult.TableAndRows;
import org.openl.rules.table.CompositeGrid;
import org.openl.rules.table.IGrid;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.ITable;
import org.openl.rules.table.IWritableGrid;
import org.openl.rules.table.Table;
import org.openl.rules.table.ui.FilteredGrid;
import org.openl.rules.table.ui.RegionGridSelector;
import org.openl.rules.table.ui.filters.ColorGridFilter;
import org.openl.rules.table.ui.filters.IGridFilter;
import org.openl.rules.table.ui.filters.SimpleFormatFilter;
import org.openl.rules.table.xls.XlsSheetGridImporter;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.rules.table.xls.XlsUrlParser;
import org.openl.rules.table.xls.XlsUrlUtils;
import org.openl.rules.tableeditor.model.TableEditorModel;
import org.openl.rules.tableeditor.model.ui.TableModel;
import org.openl.rules.tableeditor.model.ui.TableViewer;
import org.openl.rules.tableeditor.renderkit.HTMLRenderer;
import org.openl.rules.testmethod.TestResult;
import org.openl.rules.testmethod.TestSuiteMethod;
import org.openl.rules.types.OpenMethodDispatcher;
import org.openl.rules.ui.AllTestsRunResult.Test;
import org.openl.rules.ui.tree.OpenMethodsGroupTreeNodeBuilder;
import org.openl.rules.ui.tree.ProjectTreeNode;
import org.openl.rules.ui.tree.TreeBuilder;
import org.openl.rules.ui.tree.TreeCache;
import org.openl.rules.ui.tree.TreeNodeBuilder;
import org.openl.rules.workspace.uw.UserWorkspaceProject;
import org.openl.rules.validation.properties.dimentional.DispatcherTableBuilder;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.impl.IBenchmarkableMethod;
import org.openl.util.Log;
import org.openl.util.RuntimeExceptionWrapper;
import org.openl.util.benchmark.Benchmark;
import org.openl.util.benchmark.BenchmarkInfo;
import org.openl.util.benchmark.BenchmarkUnit;
import org.openl.util.export.IExporter;
import org.openl.util.tree.ITreeElement;
import org.openl.vm.IRuntimeEnv;
import org.openl.vm.SimpleVM;
import org.openl.vm.Tracer;

public class ProjectModel {

    /**
     * Compiled rules with errors. Representation of wrapper.
     */
    private CompiledOpenClass compiledOpenClass;

    private Module moduleInfo;

    private ModulesCache modulesCache = new ModulesCache();

    private ProjectIndexer indexer;

    private WebStudio studio;

    private ColorFilterHolder filterHolder = new ColorFilterHolder();

    private OpenLSavedSearch[] savedSearches;

    private ProjectTreeNode projectRoot = null;

    private TreeCache<String, ITreeElement<?>> idTreeCache = new TreeCache<String, ITreeElement<?>>();

    private TreeCache<String, ProjectTreeNode> uriTreeCache = new TreeCache<String, ProjectTreeNode>();

    public ProjectModel(WebStudio studio) {
        this.studio = studio;
    }

    public static TableModel buildModel(IGridTable gt, IGridFilter[] filters) {
        IGrid htmlGrid = gt.getGrid();
        if (!(htmlGrid instanceof FilteredGrid)) {
            int N = 1;
            IGridFilter[] f1 = new IGridFilter[filters == null ? N : filters.length + N];
            f1[0] = new SimpleFormatFilter();
            // f1[1] = new SimpleHtmlFilter();
            for (int i = N; i < f1.length; i++) {
                f1[i] = filters[i - N];
            }

            htmlGrid = new FilteredGrid(gt.getGrid(), f1);

        }

        return new TableViewer(htmlGrid, gt.getRegion()).buildModel(gt);
    }

    public UserWorkspaceProject getProject() {
        return studio.getCurrentProject();
    }

    public BenchmarkInfo benchmarkElement(String elementUri, final String testName, String testID,
            final String testDescr, int ms) throws Exception {

        BenchmarkUnit bu = null;

        if (StringUtils.isBlank(testName)) {
            IOpenMethod m = getMethod(elementUri);
            return benchmarkMethod(m, ms);
        }
        final AllTestsRunResult atr = getRunMethods(elementUri);

        final int tid = Integer.parseInt(testID);

        final IRuntimeEnv env = new SimpleVM().getRuntimeEnv();
        final Object target = compiledOpenClass.getOpenClassWithErrors().newInstance(env);

        bu = new BenchmarkUnit() {

            @Override
            public String getName() {
                return testDescr;
            }

            @Override
            public void run() throws Exception {
                throw new RuntimeException();
            }

            @Override
            public void runNtimes(int times) throws Exception {
                try {
                    atr.run(testName, tid, target, env, times);
                } catch (Throwable t) {
                    Log.error("Error during Method run: ", t);
                    throw RuntimeExceptionWrapper.wrap(t);
                }
            }

            @Override
            public String[] unitName() {
                return new String[] { testName + ":" + tid };
            }

        };

        BenchmarkUnit[] buu = { bu };
        return new Benchmark(buu).runUnit(bu, ms, false);

    }

    public BenchmarkInfo benchmarkMethod(final IOpenMethod m, int ms) throws Exception {
        final IRuntimeEnv env = new SimpleVM().getRuntimeEnv();
        final Object target = compiledOpenClass.getOpenClassWithErrors().newInstance(env);

        ClassLoader currentContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(moduleInfo.getProject().getClassLoader(false));

            final Object[] params = {};

            // Object res = null;
            BenchmarkUnit bu = null;

            try {

                if (m instanceof IBenchmarkableMethod) {
                    final IBenchmarkableMethod bm = (IBenchmarkableMethod) m;
                    bu = new BenchmarkUnit() {
                        @Override
                        public String getName() {
                            return bm.getBenchmarkName();
                        }

                        @Override
                        public int nUnitRuns() {
                            return bm.nUnitRuns();
                        }

                        @Override
                        protected void run() throws Exception {
                            throw new RuntimeException();
                        }

                        @Override
                        public void runNtimes(int times) throws Exception {
                            bm.invokeBenchmark(target, params, env, times);
                        }

                        @Override
                        public String[] unitName() {
                            return bm.unitName();
                        }

                    };

                } else {
                    bu = new BenchmarkUnit() {

                        @Override
                        public String getName() {
                            return m.getName();
                        }

                        @Override
                        protected void run() throws Exception {
                            m.invoke(target, params, env);
                        }

                    };

                }

                BenchmarkUnit[] buu = { bu };
                return new Benchmark(buu).runUnit(bu, ms, false);

            } catch (Throwable t) {
                Log.error("Run Error:", t);
                return new BenchmarkInfo(t, bu, bu.getName());
            }
        } finally {
            Thread.currentThread().setContextClassLoader(currentContextClassLoader);
        }

    }

    private Object convertTestResult(Object res) {
        if (res == null) {
            return null;
        }
        Class<?> clazz = res.getClass();
        if (!(clazz == TestResult.class)) {
            return res;
        }
        TestResult tr = (TestResult) res;
        Object[] ores = new Object[tr.getNumberOfTests()];

        for (int i = 0; i < ores.length; i++) {
            ores[i] = tr.getResult(i);
        }

        return ores;
    }

    public TableSyntaxNode findAnyTableNodeByLocation(XlsUrlParser p1) {        
        TableSyntaxNode[] nodes = getTableSyntaxNodes();

        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].getType().equals(ITableNodeTypes.XLS_DT)
                    && XlsUrlUtils.intersectsByLocation(p1, nodes[i].getTable().getGridTable().getUri())) {
                return nodes[i];
            }
        }

        return null;
    }

    public TableSyntaxNode findNode(String url) {
        XlsUrlParser parsedUrl = new XlsUrlParser();
        parsedUrl.parse(url);

        if (parsedUrl.range == null) {
            return null;
        }

        return findNode(parsedUrl);
    }

    public String findTableUri(String partialUri) {
        TableSyntaxNode tableSyntaxNode = findNode(partialUri);

        if (tableSyntaxNode != null) {
            return tableSyntaxNode.getUri();
        }

        return null;
    }

    public TableSyntaxNode findNode(XlsUrlParser p1) {        
        TableSyntaxNode[] nodes = getTableSyntaxNodes();

        for (int i = 0; i < nodes.length; i++) {
            if (XlsUrlUtils.intersects(p1, nodes[i].getTable().getGridTable().getUri())) {
                return nodes[i];
            }
        }

        return null;
    }

    public List<TableSyntaxNode> getAllValidatedNodes() {
        if (compiledOpenClass == null) {
            return Collections.emptyList();
        }
        
        TableSyntaxNode[] nodes = getTableSyntaxNodes();

        List<TableSyntaxNode> list = new ArrayList<TableSyntaxNode>();

        for (int i = 0; i < nodes.length; i++) {
            TableSyntaxNode tsn = nodes[i];

            if (tsn.getType() == ITableNodeTypes.XLS_DT) {
                if (tsn.getErrors() == null) {
                    if (tsn.getTableProperties() != null) {
                        if ("on".equals(tsn.getTableProperties().getValidateDT())) {
                            list.add(tsn);
                        }
                    }
                }
            }
        }
        return list;
    }

    public String getTreeNodeId(ITreeElement<?> treeNode) {
        return idTreeCache.getKey(treeNode);
    }

    public String getTreeNodeId(String uri) {
        ProjectTreeNode node = uriTreeCache.getNode(uri);
        String nodeId = idTreeCache.getKey(node);
        return nodeId;
    }

    public ProjectTreeNode getTreeNodeById(String id) {
        return (ProjectTreeNode) idTreeCache.getNode(id);
    }

    public ProjectTreeNode getTreeNodeByUri(String uri) {
        return uriTreeCache.getNode(uri);
    }

    public ColorFilterHolder getFilterHolder() {
        return filterHolder;
    }

    /**
     * @return Returns the indexer.
     */
    public ProjectIndexer getIndexer() {
        return indexer;
    }

    public IOpenMethod getMethod(String elementUri) {
        TableSyntaxNode tsn = getNode(elementUri);
        if (tsn == null) {
            return null;
        }

        return getMethod(tsn);
    }

    public List<IOpenMethod> getTargetMethods(String testOrRunUri) {
        List<IOpenMethod> targetMethods = new ArrayList<IOpenMethod>();
        IOpenMethod testMethod = getMethod(testOrRunUri);

        if (testMethod instanceof TestSuiteMethod) {
            IOpenMethod targetMethod = ((TestSuiteMethod) testMethod).getTestedMethod();

            // Overloaded methods
            if (targetMethod instanceof OpenMethodDispatcher) {
                List<IOpenMethod> overloadedMethods = ((OpenMethodDispatcher) targetMethod).getCandidates();
                targetMethods.addAll(overloadedMethods);
            } else {
                targetMethods.add(targetMethod);
            }
        }

        return targetMethods;
    }

    public List<ITable> getTargetTables(String testOrRunUri) {
        List<ITable> targetTables = new ArrayList<ITable>();
        List<IOpenMethod> targetMethods = getTargetMethods(testOrRunUri);

        for (IOpenMethod targetMethod : targetMethods) {
            if (targetMethod != null) {
                TableSyntaxNode tsn = (TableSyntaxNode) targetMethod.getInfo().getSyntaxNode();
                ITable targetTable = new TableSyntaxNodeAdapter(tsn);
                targetTables.add(targetTable);
            }
        }

        return targetTables;
    }

    public IOpenMethod getMethod(TableSyntaxNode tsn) {

        IOpenClass openClass = compiledOpenClass.getOpenClassWithErrors();

        for (IOpenMethod method : openClass.getMethods()) {
            IOpenMethod resolvedMethod = null;

            if (method instanceof OpenMethodDispatcher) {
                resolvedMethod = resolveMethodDispatcher((OpenMethodDispatcher) method, tsn);
            } else {
                resolvedMethod = resolveMethod(method, tsn);
            }

            if (resolvedMethod != null) {
                return resolvedMethod;
            }
        }

        return null;
    }

    private IOpenMethod resolveMethodDispatcher(OpenMethodDispatcher method, TableSyntaxNode syntaxNode) {

        List<IOpenMethod> candidates = method.getCandidates();

        for (IOpenMethod candidate : candidates) {

            IOpenMethod resolvedMethod = resolveMethod(candidate, syntaxNode);

            if (resolvedMethod != null) {
                return method;
            }
        }

        return null;
    }

    private IOpenMethod resolveMethod(IOpenMethod method, TableSyntaxNode syntaxNode) {

        if (isInstanceOfTable(method, syntaxNode)) {
            return method;
        }

        return null;
    }

    /**
     * Checks that {@link IOpenMethod} object is instance that represents the
     * given {@TableSyntaxNode} object. Actually,
     * {@link IOpenMethod} object must have the same syntax node as given one.
     * If given method is instance of {@link OpenMethodDispatcher}
     * <code>false</code> value will be returned.
     * 
     * @param method method to check
     * @param syntaxNode syntax node
     * @return <code>true</code> if {@link IOpenMethod} object represents the
     *         given table syntax node; <code>false</code> - otherwise
     */
    private boolean isInstanceOfTable(IOpenMethod method, TableSyntaxNode syntaxNode) {

        IMemberMetaInfo metaInfo = method.getInfo();

        return (metaInfo != null && metaInfo.getSyntaxNode() == syntaxNode);
    }

    public TableSyntaxNode getNode(String elementUri) {
        TableSyntaxNode tsn = null;
        if (elementUri != null) {
            ProjectTreeNode pte = getTreeNodeByUri(elementUri);
            if (pte != null) {
                tsn = (TableSyntaxNode) pte.getObject();
            }
            if (tsn == null) {
                tsn = findNode(elementUri);
            }
        }
        return tsn;
    }

    public synchronized ITreeElement<?> getProjectTree() {
        if (projectRoot == null) {
            buildProjectTree();
        }
        return projectRoot;
    }

    public AllTestsRunResult getRunMethods(String elementUri) {
        IOpenMethod m = getMethod(elementUri);

        if (m == null) {
            return null;
        }

        IOpenMethod[] runners = ProjectHelper.runners(m);
        String[] names = new String[runners.length];

        for (int i = 0; i < runners.length; i++) {
            names[i] = ProjectHelper.createTestName(runners[i]);
        }

        return new AllTestsRunResult(runners, names);

    }

    public OpenLSavedSearch[] getSavedSearches() {
        if (savedSearches == null && isReady()) {
            TableSyntaxNode[] nodes = getTableSyntaxNodes();

            List<OpenLSavedSearch> savedSearches = new ArrayList<OpenLSavedSearch>();

            for (TableSyntaxNode node : nodes) {
                if (node.getType().equals(ITableNodeTypes.XLS_PERSISTENT)) {
                    String code = node.getHeader().getModule().getCode();
                    if ((IXlsTableNames.PERSISTENCE_TABLE + " " + OpenLSavedSearch.class.getName()).equals(code)) {
                        OpenLSavedSearch savedSearch = new OpenLSavedSearch().restore(new XlsSheetGridImporter(
                                (XlsSheetGridModel) node.getTable().getGridTable().getGrid(), node));
                        savedSearches.add(savedSearch);
                    }
                }
            }

            this.savedSearches = savedSearches.toArray(new OpenLSavedSearch[savedSearches.size()]);
        }
        return savedSearches;
    }

    public List<ITable> getAdvancedSearchResults(Object searchResult) {
        List<ITable> searchResults = new ArrayList<ITable>();

        if (searchResult instanceof OpenLAdvancedSearchResult) {
            TableAndRows[] tr = ((OpenLAdvancedSearchResult) searchResult).getFoundTableAndRows();
            OpenLAdvancedSearchResultViewer searchViewer = new OpenLAdvancedSearchResultViewer();
            for (int i = 0; i < tr.length; i++) {
                ISearchTableRow[] rows = tr[i].getRows();
                if (rows.length > 0) {
                    TableSyntaxNode tsn = tr[i].getTsn();
                    String tableUri = tsn.getUri();

                    CompositeGrid cg = searchViewer.makeGrid(rows);
                    IGridTable gridTable = cg != null ? cg.asGridTable() : null;

                    Table newTable = new Table();
                    newTable.setGridTable(gridTable);
                    newTable.setUri(tableUri);
                    newTable.setProperties(tsn.getTableProperties());

                    searchResults.add(newTable);
                }
            }
        }

        return searchResults;
    }

    public List<ITable> getBussinessSearchResults(Object searchResult) {
        List<ITable> searchResults = new ArrayList<ITable>();

        if (searchResult instanceof OpenLBussinessSearchResult) {
            List<TableSyntaxNode> foundTables = ((OpenLBussinessSearchResult) searchResult).getFoundTables();
            for (TableSyntaxNode foundTable : foundTables) {
                searchResults.add(new TableSyntaxNodeAdapter(foundTable));
            }
        }

        return searchResults;
    }

    public WebStudio getStudio() {
        return studio;
    }

    public ITable getTable(String elementUri) {
        TableSyntaxNode tsn = getNode(elementUri);
        if (tsn != null) {
            return new TableSyntaxNodeAdapter(tsn);
        }
        return null;
    }

    public IGridTable getGridTable(String elementUri) {
        TableSyntaxNode tsn = getNode(elementUri);
        return tsn == null ? null : tsn.getTable().getGridTable();
    }

    public String getTableView(String view) {
        return view == null ? studio.getMode().getTableMode() : view;
    }

    public AllTestsRunResult getTestsRunner(IOpenMethod[] testMethods) {
        String[] names = new String[testMethods.length];

        for (int i = 0; i < testMethods.length; i++) {
            
            names[i] = ProjectHelper.createTestName(testMethods[i]);
        }

        return new AllTestsRunResult(testMethods, names);
    }
    
    /**
     * Get runnable tests for tested method by uri.
     * Runnable tests - tests with  filled rules rows data for testing its functionality.
     * If you need to get all test methods, including and empty ones, use {@link #getAllTestMethods(String).
     * 
     * @param elementUri
     * @return
     */
    public AllTestsRunResult getTestMethods(String elementUri) {
        IOpenMethod[] testMethods = null;

        IOpenMethod method = getMethod(elementUri);
        if (method != null) {
            if (ProjectHelper.isTester(method)) {
                testMethods = new IOpenMethod[] {method};
            } else {
                testMethods = ProjectHelper.testers(method);
            }
            if (ArrayUtils.isNotEmpty(testMethods)) {
                return getTestsRunner(testMethods);
            }
        }
        return null;
    }
    
    /**
     * Gets all test methods for method by uri.
     * 
     * @param elementUri
     * @return all test methods, including tests with test cases, runs with filled runs, tests without cases(empty),
     * runs without any parameters and tests without cases and runs.
     */
    public AllTestsRunResult getAllTestMethods(String elementUri) {
        IOpenMethod[] testMethods = null;

        IOpenMethod method = getMethod(elementUri);
        if (method != null) {
            testMethods = ProjectHelper.allTesters(method);
            if (ArrayUtils.isNotEmpty(testMethods)) {
                return getTestsRunner(testMethods);
            }
        }
        return null;
    }

    public AllTestsRunResult getAllTestMethods() {
        IOpenMethod[] testMethods = ProjectHelper.allTesters(
                compiledOpenClass.getOpenClassWithErrors());

        return getTestsRunner(testMethods);
    }

    // TODO Refactor
    private WorkbookSyntaxNode[] getWorkbookNodes() {
        if (compiledOpenClass != null) {
            XlsModuleSyntaxNode xlsModuleNode = ((XlsMetaInfo) compiledOpenClass.getOpenClassWithErrors().getMetaInfo())
                .getXlsModuleNode();
            WorkbookSyntaxNode[] workbookNodes = xlsModuleNode.getWorkbookSyntaxNodes();
            return workbookNodes;
        }

        return null;
    }

    public boolean isSourceModified() {
        WorkbookSyntaxNode[] workbookNodes = getWorkbookNodes();
        if (workbookNodes != null) {
            for (WorkbookSyntaxNode node : workbookNodes) {
                if (node.getWorkbookSourceCodeModule().isModified()) {
                    return true;
                }
            }
        }
        return false;
    }

    public CompiledOpenClass getCompiledOpenClass() {
        return compiledOpenClass;
    }

    /**
     * @return Returns the wrapperInfo.
     */
    public Module getModuleInfo() {
        return moduleInfo;
    }

    public XlsModuleSyntaxNode getXlsModuleNode() {
        XlsMetaInfo xmi = (XlsMetaInfo) compiledOpenClass.getOpenClassWithErrors().getMetaInfo();
        XlsModuleSyntaxNode xsn = xmi.getXlsModuleNode();
        return xsn;
    }

    /**
     * Returns if current project is read only.
     * 
     * @return <code>true</code> if project is read only.
     */
    public boolean isEditable() {
        UserWorkspaceProject project = getProject();

        if (project != null) {
            return project.isCheckedOut() || project.isLocalOnly();
        }

        return false;
    }

    public boolean isReady() {
        return compiledOpenClass != null;
    }

    public boolean isRunnable(String elementUri) {
        IOpenMethod m = getMethod(elementUri);
        if (m == null) {
            return false;
        }

        return ProjectHelper.isRunnable(m);
    }

    public boolean isTestable(String elementUri) {
        IOpenMethod m = getMethod(elementUri);
        if (m == null) {
            return false;
        }

        return ProjectHelper.isTestable(m);
    }

    public boolean isTestable(TableSyntaxNode tsn) {
        IOpenMethod m = getMethod(tsn);
        if (m == null) {
            return false;
        }

        return ProjectHelper.isTestable(m);
    }

    public synchronized void buildProjectTree() {
        if (compiledOpenClass == null) {
            return;
        }

        ProjectTreeNode root = makeProjectTreeRoot();

        TableSyntaxNode[] tableSyntaxNodes = getTableSyntaxNodes();
        
        OverloadedMethodsDictionary methodNodesDictionary = makeMethodNodesDictionary(tableSyntaxNodes);

        TreeBuilder<Object> treeBuilder = new TreeBuilder<Object>();

        TreeNodeBuilder<Object>[] treeSorters = studio.getMode().getBuilders();

        // Find all group sorters defined for current subtree.
        // Group sorter should have additional information for grouping
        // nodes by method signature.
        // author: Alexey Gamanovich
        //
        for (TreeNodeBuilder<?> treeSorter : treeSorters) {

            if (treeSorter instanceof OpenMethodsGroupTreeNodeBuilder) {
                // Set to sorter information about open methods.
                // author: Alexey Gamanovich
                //
                OpenMethodsGroupTreeNodeBuilder tableTreeNodeBuilder = (OpenMethodsGroupTreeNodeBuilder) treeSorter;
                tableTreeNodeBuilder.setOpenMethodGroupsDictionary(methodNodesDictionary);
            }
        }

        HashSet<TableSyntaxNode> nodesWithErrors = new HashSet<TableSyntaxNode>();

        boolean treeEnlarged = false;

        for (int i = 0; i < tableSyntaxNodes.length; i++) {                
            if (studio.getMode().select(tableSyntaxNodes[i])) {

                treeBuilder.addToNode(root, tableSyntaxNodes[i], treeSorters);
                treeEnlarged = true;

            } else if (tableSyntaxNodes[i].getErrors() != null
                    && !DispatcherTableBuilder.isDispatcherTable(tableSyntaxNodes[i])) {

                treeBuilder.addToNode(root, tableSyntaxNodes[i], treeSorters);
                nodesWithErrors.add(tableSyntaxNodes[i]);
            }
        }

        if (!treeEnlarged) {
            // No selection have been made (usually in a business mode)
            for (int i = 0; i < tableSyntaxNodes.length; i++) {                    
                    if (!ITableNodeTypes.XLS_OTHER.equals(tableSyntaxNodes[i].getType())
                            && !nodesWithErrors.contains(tableSyntaxNodes[i])) {
                        
                        treeBuilder.addToNode(root, tableSyntaxNodes[i], treeSorters);
                    }
            }
        }

        projectRoot = root;
        uriTreeCache.clear();
        idTreeCache.clear();
        cacheTree(projectRoot);
    }

    private TableSyntaxNode[] getTableSyntaxNodes() {
        XlsModuleSyntaxNode moduleSyntaxNode = getXlsModuleNode();
        TableSyntaxNode[] tableSyntaxNodes = moduleSyntaxNode.getXlsTableSyntaxNodes();
        return tableSyntaxNodes;
    }

    private void cacheTree(String key, ProjectTreeNode treeNode) {
        int childNumber = 0;
        for (Iterator<?> iterator = treeNode.getChildren(); iterator.hasNext();) {
            ProjectTreeNode child = (ProjectTreeNode) iterator.next();
            if (child.getType().startsWith(IProjectTypes.PT_TABLE + ".")) {
                ProjectTreeNode ptr = (ProjectTreeNode) child;
                uriTreeCache.put(ptr.getUri(), ptr);
            }
            String childKey = (StringUtils.isNotBlank(key) ? key + ":" : "") + (childNumber + 1);
            idTreeCache.put(childKey, child);
            childNumber++;
            cacheTree(childKey, child);
        }
    }

    private void cacheTree(ProjectTreeNode treeNode) {
        cacheTree(null, treeNode);
    }

    private OverloadedMethodsDictionary makeMethodNodesDictionary(TableSyntaxNode[] tableSyntaxNodes) {

        // Create open methods dictionary that organizes
        // open methods in groups using their meta info.
        // Dictionary contains required information what will be used to create
        // groups of methods in tree.
        // author: Alexey Gamanovich
        //
        List<TableSyntaxNode> executableNodes = getAllExecutableTables(tableSyntaxNodes);
        OverloadedMethodsDictionary methodNodesDictionary = new OverloadedMethodsDictionary();
        methodNodesDictionary.addAll(executableNodes);

        return methodNodesDictionary;
    }

    private ProjectTreeNode makeProjectTreeRoot() {

        String name = studio.getMode().getDisplayName(moduleInfo);

        return new ProjectTreeNode(new String[] { name, name, name }, "root", null, null, 0, null);
    }

    private List<TableSyntaxNode> getAllExecutableTables(TableSyntaxNode[] nodes) {
        List<TableSyntaxNode> executableNodes = new ArrayList<TableSyntaxNode>();
        for (TableSyntaxNode node : nodes) {
            if (node.getMember() instanceof IOpenMethod) {
                executableNodes.add(node);
            }
        }
        return executableNodes;
    }

    public void redraw() throws Exception {
        projectRoot = null;
    }
    
    public void reset(ReloadType reloadType) throws Exception {
        if (moduleInfo != null) {
            setModuleInfo(moduleInfo, reloadType);
        }

        savedSearches = null;
        projectRoot = null;
    }

    public AllTestsRunResult runAllTests(String elementUri) {

        AllTestsRunResult atr = getTestMethods(elementUri);
        if (atr == null) {
            atr = getAllTestMethods();
        }

        Test[] ttm = atr.getTests();

        TestResult[] ttr = new TestResult[ttm.length];
        for (int i = 0; i < ttr.length; i++) {
            ttr[i] = (TestResult) runMethod(ttm[i].method);
        }

        atr.setResults(ttr);
        return atr;
    }

    public Object runElement(String elementUri, String testName, String testID) {
        if (testName == null) {
            IOpenMethod m = getMethod(elementUri);
            return convertTestResult(runMethod(m));
        }

        AllTestsRunResult atr = getRunMethods(elementUri);

        int tid = Integer.parseInt(testID);

        IRuntimeEnv env = new SimpleVM().getRuntimeEnv();
        Object target = compiledOpenClass.getOpenClassWithErrors().newInstance(env);
        try {
            Object res = atr.run(testName, tid, target, env, 1);
            return res;
        } catch (Throwable t) {
            Log.error("Error during Method run: ", t);
            return t;
        }

    }

    public Object runMethod(IOpenMethod m) {
        IRuntimeEnv env = new SimpleVM().getRuntimeEnv();
        Object target = compiledOpenClass.getOpenClassWithErrors().newInstance(env);

        ClassLoader currentContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(moduleInfo.getProject().getClassLoader(false));

            Object res = null;

            try {
                res = m.invoke(target, new Object[] {}, env);

            } catch (Throwable t) {
                Log.error("Run Error:", t);
                return t;
            }
            return res;
        } finally {
            Thread.currentThread().setContextClassLoader(currentContextClassLoader);
        }
    }

    public Object runSearch(IOpenLSearch searchBean) {
        XlsModuleSyntaxNode xsn = getXlsModuleNode();

        return searchBean.search(xsn);
    }

    public void saveSearch(OpenLSavedSearch search) throws Exception {
        XlsWorkbookSourceCodeModule module = getWorkbookNodes()[0].getWorkbookSourceCodeModule();
        if (module != null) {
            IExporter iExporter = IWritableGrid.Tool.createExporter(module);
            iExporter.persist(search);
            module.save();
            reset(ReloadType.RELOAD);
        }
    }

    public void setProjectTree(ProjectTreeNode projectRoot) {
        this.projectRoot = projectRoot;
    }

    public void setModuleInfo(Module moduleInfo) throws Exception {
        setModuleInfo(moduleInfo, ReloadType.NO);
    }

    public void setModuleInfo(Module moduleInfo, ReloadType reloadType) throws Exception {
        if (this.moduleInfo == moduleInfo && reloadType == ReloadType.NO) {
            return;
        }
        
        File projectFolder = moduleInfo.getProject().getProjectFolder();
        if(reloadType == ReloadType.FORCED){
            RulesProjectResolver projectResolver = studio.getProjectResolver();
            ResolvingStrategy resolvingStrategy = projectResolver.isRulesProject(projectFolder);
            this.moduleInfo = resolvingStrategy.resolveProject(projectFolder).getModuleByClassName(moduleInfo.getClassname());
        }else{
            this.moduleInfo = moduleInfo;
        }

        indexer = new ProjectIndexer(projectFolder.getAbsolutePath());

        // Clear project messages (errors, warnings)
        OpenLMessages.getCurrentInstance().clear();

        compiledOpenClass = null;
        projectRoot = null;
        savedSearches = null;
        
        RulesInstantiationStrategy instantiationStrategy = modulesCache.getInstantiationStrategy(moduleInfo);
        
        try {
            compiledOpenClass = instantiationStrategy.compile(reloadType);
        } catch (Throwable t) {
            Log.error("Problem Loading OpenLWrapper", t);
        }
    }

    public String showTableWithSelection(String url, String view) {
        TableSyntaxNode tsn = findNode(url);
        if (tsn == null) {
            return "NOT FOUND";
        }

        XlsUrlParser p1 = new XlsUrlParser();
        p1.parse(url);

        IGridRegion region = XlsSheetGridModel.makeRegion(p1.range);

        if (view == null) {
            view = IXlsTableNames.VIEW_BUSINESS;
        }
        ILogicalTable gtx = tsn.getSubTables().get(view);
        IGridTable gt = tsn.getTable().getGridTable();
        if (gtx != null) {
            gt = gtx.getGridTable();
        }

        TableModel tableModel = buildModel(gt, new IGridFilter[] { new ColorGridFilter(new RegionGridSelector(region,
                true), filterHolder.makeFilter()) });
        // FIXME: should formulas be displayed?
        return new HTMLRenderer.TableRenderer(tableModel).render(false);
    }

    public Tracer traceElement(String elementUri) {
        IOpenMethod m = getMethod(elementUri);
        return traceMethod(m);
    }

    public Tracer traceMethod(IOpenMethod m) {
        Tracer t = new Tracer();
        Tracer.setTracer(t);

        ClassLoader currentContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(moduleInfo.getProject().getClassLoader(false));
            try {
                IRuntimeEnv env = new SimpleVM().getRuntimeEnv();
                Object target = compiledOpenClass.getOpenClassWithErrors().newInstance(env);

                m.invoke(target, new Object[] {}, env);
            } finally {
                Tracer.setTracer(null);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(currentContextClassLoader);
        }
        return t;
    }

    public TableEditorModel getTableEditorModel(String tableUri) {
        ITable table = getTable(tableUri);
        String tableView = getTableView(null);
        TableEditorModel tableModel = new TableEditorModel(table, tableView, false);
        return tableModel;
    }

    @Deprecated
    public static String showTable(IGridTable gt, boolean showgrid) {
        return showTable(gt, (IGridFilter[]) null, showgrid);
    }

    @Deprecated
    public static String showTable(IGridTable gt, IGridFilter[] filters, boolean showgrid) {
        TableModel model = buildModel(gt, filters);
        return TableViewer.showTable(model, showgrid);
    }

}
