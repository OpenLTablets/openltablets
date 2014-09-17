package org.openl.rules.webstudio.web.tableeditor;

import static org.openl.rules.security.AccessManager.isGranted;
import static org.openl.rules.security.DefaultPrivileges.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openl.commons.web.jsf.FacesUtils;
import org.openl.message.OpenLMessage;
import org.openl.message.OpenLMessagesUtils;
import org.openl.message.OpenLWarnMessage;
import org.openl.message.Severity;
import org.openl.rules.lang.xls.IXlsTableNames;
import org.openl.rules.lang.xls.XlsNodeTypes;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.service.TableServiceImpl;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.IOpenLTable;
import org.openl.rules.table.properties.ITableProperties;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.rules.tableeditor.model.TableEditorModel;
import org.openl.rules.testmethod.ParameterWithValueDeclaration;
import org.openl.rules.testmethod.TestDescription;
import org.openl.rules.testmethod.TestSuite;
import org.openl.rules.testmethod.TestSuiteMethod;
import org.openl.rules.testmethod.TestUtils;
import org.openl.rules.ui.ProjectHelper;
import org.openl.rules.ui.ProjectModel;
import org.openl.rules.ui.RecentlyVisitedTables;
import org.openl.rules.ui.WebStudio;
import org.openl.rules.validation.properties.dimentional.DispatcherTablesBuilder;
import org.openl.rules.webstudio.web.test.TestDescriptionWithPreview;
import org.openl.rules.webstudio.web.util.Constants;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IOpenMethod;

/**
 * Request scope managed bean for Table page.
 */
@ManagedBean
@ViewScoped
public class TableBean {

    private IOpenMethod method;

    // Test in current table (only for test tables)
    private TestDescription[] runnableTestMethods = {}; //test units
    // All checks and tests for current table (including tests with no cases, run methods).
    private IOpenMethod[] allTests = {};
    private IOpenMethod[] tests = {};

    private List<IOpenLTable> targetTables;

    private String uri;
    private String id;
    private IOpenLTable table;
    private boolean editable;
    private boolean canBeOpenInExel;
    private boolean copyable;

    private List<OpenLMessage> errors;
    private List<OpenLMessage> warnings;
    // Errors + Warnings
    private List<OpenLMessage> problems;

    public TableBean() {
        id = FacesUtils.getRequestParameter(Constants.REQUEST_PARAM_ID);

        WebStudio studio = WebStudioUtils.getWebStudio();
        final ProjectModel model = studio.getModel();

        table = model.getTableById(id);

        // TODO: There is should be a method to get the table by the ID without using URI which is used to generate the ID.
        if (table == null) {
            table = model.getTable(studio.getTableUri());
        }

        if (table != null) {
            id = table.getId();
            uri = table.getUri();
            // Save URI because some actions don't provide table ID
            studio.setTableUri(uri);

            method = model.getMethod(uri);

            editable = model.isEditableTable(uri) && !isDispatcherValidationNode();
            canBeOpenInExel = model.isEditable() && !isDispatcherValidationNode();
            copyable = editable && table.isCanContainProperties()
                    && !XlsNodeTypes.XLS_DATATYPE.toString().equals(table.getType())
                    && isGranted(PRIVILEGE_CREATE_TABLES);

            String tableType = table.getType();
            if (tableType.equals(XlsNodeTypes.XLS_TEST_METHOD.toString())
                    || tableType.equals(XlsNodeTypes.XLS_RUN_METHOD.toString())) {
                targetTables = model.getTargetTables(uri);
            }

            initProblems();
            initTests(model);

            // Save last visited table
            model.getRecentlyVisitedTables().setLastVisitedTable(table);
            // Check the save table parameter
            boolean saveTable = FacesUtils.getRequestParameterMap().get("saveTable") == null ? true :
                    Boolean.valueOf(FacesUtils.getRequestParameterMap().get("saveTable"));
            if (saveTable) {
                storeTable();
            }
        }
    }

    private void storeTable() {
        ProjectModel model = WebStudioUtils.getProjectModel();
        RecentlyVisitedTables recentlyVisitedTables = model.getRecentlyVisitedTables();
        recentlyVisitedTables.add(table);
    }

    private void initTests(final ProjectModel model) {
        initRunnableTestMethods();

        allTests = model.getTestAndRunMethods(uri);
        tests = model.getTestMethods(uri);
    }

    private void initRunnableTestMethods() {
        if (method instanceof TestSuiteMethod) {
            runnableTestMethods = ((TestSuiteMethod) method).getTests();
        }
    }

    private void initProblems() {
        initErrors();
        initWarnings();

        problems = new ArrayList<OpenLMessage>();
        problems.addAll(errors);
        problems.addAll(warnings);
    }

    private void initErrors() {
        List<OpenLMessage> messages = table.getMessages();
        errors = OpenLMessagesUtils.filterMessagesBySeverity(messages, Severity.ERROR);
    }

    private void initWarnings() {
        warnings = new ArrayList<OpenLMessage>();
        
        if (targetTables != null) {
            for (IOpenLTable targetTable : targetTables) {
                if (targetTable.getMessages().size() > 0) {
                    warnings.add(new OpenLMessage("Tested rules have errors", StringUtils.EMPTY, Severity.WARN));
                    // one warning is enough.
                    break;
                }
            }
        }

        ProjectModel model = WebStudioUtils.getProjectModel();

        List<OpenLMessage> messages = model.getModuleMessages();

        List<OpenLMessage> warningMessages = OpenLMessagesUtils.filterMessagesBySeverity(messages, Severity.WARN);
        for (OpenLMessage message : warningMessages) {
            if (message instanceof OpenLWarnMessage) {//there can be simple OpenLMessages with severity WARN
                OpenLWarnMessage warning = (OpenLWarnMessage) message;
                ISyntaxNode syntaxNode = warning.getSource();
                if (syntaxNode instanceof TableSyntaxNode
                        && ((TableSyntaxNode) syntaxNode).getUri().equals(table.getUri())) {
                    warnings.add(warning);
                }
            }
        }
    }

    public String getTableName (IOpenLTable table) {
        String[] dimensionProps = TablePropertyDefinitionUtils.getDimensionalTablePropertiesNames();
        ITableProperties tableProps = table.getProperties();
        StringBuilder dimensionBuilder = new StringBuilder();
        String tableName = table.getDisplayName();
        if (tableProps != null) {
            for (String dimensionProp : dimensionProps) {
                String propValue = tableProps.getPropertyValueAsString(dimensionProp);

                if (propValue != null && !propValue.isEmpty()) {
                    dimensionBuilder.append(dimensionBuilder.length() == 0 ? "" : ", ").append(dimensionProp).append(" = ").append(propValue);
                }
            }
        }
        if (dimensionBuilder.length() > 0) {
            return tableName +" ["+ dimensionBuilder.toString() +"]";
        } else {
            return tableName;
        }
    }

    public final boolean isDispatcherValidationNode() {
        return table != null && table.getName().startsWith(DispatcherTablesBuilder.DEFAULT_DISPATCHER_TABLE_NAME);
    }

    public String getMode() {
        return getCanEdit() ? FacesUtils.getRequestParameter("mode") : null;
    }

    public IOpenLTable getTable() {
        return table;
    }

    public List<OpenLMessage> getErrors() {
        return errors;
    }

    public List<OpenLMessage> getWarnings() {
        return warnings;
    }

    public List<OpenLMessage> getProblems() {
        return problems;
    }

    /**
     * Return test cases for current table.
     * 
     * @return array of tests for current table. 
     */
    public TestDescription[] getTests() {
        return runnableTestMethods;
    }

    public ParameterWithValueDeclaration[] getTestCaseParams(TestDescription testCase) {
        ParameterWithValueDeclaration[] params;
        if (testCase != null) {
            ParameterWithValueDeclaration[] contextParams = TestUtils.getContextParams(
                    new TestSuite((TestSuiteMethod) method), testCase);
            ParameterWithValueDeclaration[] inputParams = new TestDescriptionWithPreview(testCase).getExecutionParams();

            params = new ParameterWithValueDeclaration[contextParams.length + inputParams.length];
            int n = 0;
            for (ParameterWithValueDeclaration contextParam : contextParams) {
                params[n++] = contextParam;
            }
            for (ParameterWithValueDeclaration inputParam : inputParams) {
                params[n++] = inputParam;
            }
        } else {
            params = new ParameterWithValueDeclaration[0];
        }
        return params;
    }

    public String getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<IOpenLTable> getTargetTables() {
        return targetTables;
    }

    /**
     * 
     * @return true if it is possible to create tests for current table.
     */
    public boolean isCanCreateTest() {
        return table != null && table.isExecutable() && isEditable() && isGranted(PRIVILEGE_CREATE_TABLES);
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isCopyable() {
        return copyable;
    }

    public boolean isTablePart() {
        return WebStudioUtils.getProjectModel().isTablePart(uri);
    }

    public boolean isHasErrors() {
        return CollectionUtils.isNotEmpty(errors);
    }

    public boolean isHasWarnings() {
        return CollectionUtils.isNotEmpty(warnings);
    }

    public boolean isHasProblems() {
        return isHasErrors() || isHasWarnings();
    }

    /**
     * Checks if there are runnable tests for current table.
     * 
     * @return true if there are runnable tests for current table.
     */
    public boolean isTestable() {
        return runnableTestMethods.length > 0;
    }
    
    /**
     * Checks if there are tests, including tests with test cases, runs with filled runs, tests without cases(empty),
     * runs without any parameters and tests without cases and runs.
     */
    public boolean isHasAnyTests() {
        return ArrayUtils.isNotEmpty(allTests);
    }

    public boolean isHasTests() {
        return ArrayUtils.isNotEmpty(tests);
    }

    /**
     * Gets all tests for current table.
     */
    public IOpenMethod[] getAllTests() {
        return allTests;
    }
    
    public String getTestName(Object testMethod){
        return ProjectHelper.createTestName((IOpenMethod) testMethod);
    }

    public String removeTable() throws Throwable {
        try {
            final WebStudio studio = WebStudioUtils.getWebStudio();
            IGridTable gridTable = table.getGridTable(IXlsTableNames.VIEW_DEVELOPER);

            new TableServiceImpl().removeTable(gridTable);
            XlsSheetGridModel sheetModel = (XlsSheetGridModel) gridTable.getGrid();
            sheetModel.getSheetSource().getWorkbookSource().save();
            studio.rebuildModel();
            RecentlyVisitedTables visitedTables = studio.getModel().getRecentlyVisitedTables();
            visitedTables.remove(table);
        } catch (Exception e) {
            throw e.getCause();
        }
        return null;
    }

    public boolean beforeSaveAction() {
        String editorId = FacesUtils.getRequestParameter(
                org.openl.rules.tableeditor.util.Constants.REQUEST_PARAM_EDITOR_ID);

        Map<?, ?> editorModelMap = (Map<?, ?>) FacesUtils.getSessionParam(
                org.openl.rules.tableeditor.util.Constants.TABLE_EDITOR_MODEL_NAME);

        TableEditorModel editorModel = (TableEditorModel) editorModelMap.get(editorId);

        if (WebStudioUtils.getWebStudio().isUpdateSystemProperties()) {
            return EditHelper.updateSystemProperties(table, editorModel,
                    WebStudioUtils.getWebStudio().getSystemConfigManager().getStringProperty("user.mode"));
        }
        return true;
    }

    public void afterSaveAction(String newId) {
        final WebStudio studio = WebStudioUtils.getWebStudio();
        //studio.setTableUri(newUri);
        studio.rebuildModel();
    }

    public void setShowFormulas() {
        final WebStudio studio = WebStudioUtils.getWebStudio();
        studio.setShowFormulas(!studio.isShowFormulas());
    }

    public void setCollapseProperties() {
        final WebStudio studio = WebStudioUtils.getWebStudio();
        studio.setCollapseProperties(!studio.isCollapseProperties());
    }

    public boolean getCanEdit() {
        return isEditable() && isGranted(PRIVILEGE_EDIT_TABLES);
    }

    public boolean isCanOpenInExel() {
        return canBeOpenInExel;
    }

    public boolean getCanRemove() {
        return isEditable() && isGranted(PRIVILEGE_REMOVE_TABLES);
    }

    public boolean getCanRun() {
        return isGranted(PRIVILEGE_RUN);
    }

    public boolean getCanTrace() {
        return isGranted(PRIVILEGE_TRACE);
    }

    public boolean getCanBenchmark() {
        return isGranted(PRIVILEGE_BENCHMARK);
    }

    public Integer getRowIndex() {
        if (runnableTestMethods.length > 0 && !runnableTestMethods[0].hasId()) {
            return table.getGridTable().getHeight() - runnableTestMethods.length + 1;
        }
        return null;
    }
}
