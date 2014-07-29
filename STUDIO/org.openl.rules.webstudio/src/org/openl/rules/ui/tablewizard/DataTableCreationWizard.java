package org.openl.rules.ui.tablewizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.application.FacesMessage;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.model.SelectItem;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.openl.base.INamedThing;
import org.openl.commons.web.jsf.FacesUtils;
import org.openl.rules.domaintree.DomainTree;
import org.openl.rules.lang.xls.XlsNodeTypes;
import org.openl.rules.lang.xls.XlsSheetSourceCodeModule;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.rules.table.xls.builder.CreateTableException;
import org.openl.rules.table.xls.builder.DataTableBuilder;
import org.openl.rules.table.xls.builder.DataTableField;
import org.openl.rules.table.xls.builder.DataTablePredefinedTypeVariable;
import org.openl.rules.table.xls.builder.DataTableUserDefinedTypeField;
import org.openl.rules.table.xls.builder.TableBuilder;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.impl.OpenClassDelegator;
import org.openl.util.Log;

public class DataTableCreationWizard extends TableCreationWizard {
    @NotBlank(message = "Can not be empty")
    private String tableType;

    @NotBlank(message = "Can not be empty")
    @Pattern(regexp = "([a-zA-Z_][a-zA-Z_0-9]*)?", message = "Invalid name")
    private String tableName;
    private IOpenClass tableOpenClass;
    private DataTableTree tree = new DataTableTree();

    private DomainTree domainTree;
    private SelectItem[] domainTypes;
    private HtmlDataTable parametersTable;
    private List<TableSyntaxNode> allDataTables;
    private Collection<IOpenClass> importedClasses;

    public DataTableCreationWizard() {
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String technicalName) {
        this.tableName = technicalName;
    }

    public DataTableTree getTree() {
        return tree;
    }

    public SelectItem[] getDomainTypes() {
        return domainTypes;
    }

    public HtmlDataTable getParametersTable() {
        return parametersTable;
    }

    public void setParametersTable(HtmlDataTable parametersTable) {
        this.parametersTable = parametersTable;
    }

    @Override
    public String getName() {
        return "newDataTable";
    }

    @Override
    protected void onStart() {
        reset();

        domainTree = DomainTree.buildTree(WizardUtils.getProjectOpenClass());
        importedClasses = WizardUtils.getImportedClasses();
        
        Collection<String> allClasses = domainTree.getAllClasses();

        Iterator<String> classIterator = allClasses.iterator();
        
        String typeName, className;

        while (classIterator.hasNext()) {
            className = classIterator.next();
            for (IOpenClass dataType : domainTree.getAllOpenClasses()) {
                if (dataType instanceof OpenClassDelegator) {
                    typeName = dataType.getName();
                    if (typeName.equals(className)) {
                        classIterator.remove();
                    }
                }
            }
        }

        for (IOpenClass type : importedClasses) {
            if (!(type instanceof OpenClassDelegator)) {
                allClasses.add(type.getDisplayName(INamedThing.SHORT));
            }
        }

        domainTypes = FacesUtils.createSelectItems(allClasses);

        allDataTables = new ArrayList<TableSyntaxNode>();
        for (TableSyntaxNode tbl : WizardUtils.getTableSyntaxNodes()) {
            if (XlsNodeTypes.XLS_DATA.toString().equals(tbl.getType())) {
                allDataTables.add(tbl);
            }
        }
    }

    @Override
    protected void onCancel() {
    }

    @Override
    protected void onStepFirstVisit(int step) {
        if (Page.DESTINATION == Page.valueOf(step)) {
            initWorkbooks();
        }
    }

    @Override
    public String next() {
        // validate current page before switching to next one
        if (Page.COLUMNS_CONFIGURATION == Page.valueOf(getStep())) {
            updateNodesForeignKey(tree.getRoot());
            
            if (!isColumnConfigValid())
                return null;
        }

        String s = super.next();

        // Switched to a next page
        Log.debug("Go to page {0}", getStep());

        if (Page.COLUMNS_CONFIGURATION == Page.valueOf(getStep())) {
            initTree();
        }

        return s;
    }

    @Override
    protected void reset() {
        tableName = null;
        tableOpenClass = null;
        tree.setRoot(null);

        domainTree = null;
        domainTypes = null;
        
        importedClasses = null;
        
        super.reset();
    }

    @Override
    protected void onFinish() throws Exception {
        XlsSheetSourceCodeModule sheetSourceModule = getDestinationSheet();
        String newTableUri = buildTable(sheetSourceModule);
        setNewTableId(newTableUri);
        getModifiedWorkbooks().add(sheetSourceModule.getWorkbookSource());
        super.onFinish();
    }

    /**
     * Get a foreign key table variants for type "typeName". That types are
     * searched in current project.
     * 
     * @param typeName type of a table
     * @return possible foreign key table array
     */
    public SelectItem[] getForeignKeyTables(String typeName) {
        List<String> tableNames = new ArrayList<String>();

        IOpenClass to = getUserDefinedType(typeName);

        for (TableSyntaxNode tbl : allDataTables) {
            if (tbl.getMember() == null) {
                continue;
            }
            
            IOpenClass from = tbl.getMember().getType().getComponentClass();

            if (typeName.equals(from.getDisplayName(INamedThing.SHORT))) {
                tableNames.add(tbl.getMember().getName());
            } else if (to != null) {
                if (to.getInstanceClass().isAssignableFrom(from.getInstanceClass())) {
                    tableNames.add(tbl.getMember().getName());
                }
            }
        }
        
        Collections.sort(tableNames);

        return FacesUtils.createSelectItems(tableNames);
    }

    /**
     * Check if there is a foreign key table variants for type "typeName". That types are
     * searched in current project.
     * 
     * @param typeName type of a table
     * @return true if there is found a foreign key table variants for type "typeName"
     */
    public boolean hasForeignKeyTables(String typeName) {
        return getForeignKeyTables(typeName).length > 0;
    }
    
    /**
     * Get foreign key table columns for the type "typeName". Just a fields of it's type.
     * 
     * @param typeName type of a table
     * @return columns of a table
     */
    public SelectItem[] getTableColumns(String typeName) {
        List<String> columns = new ArrayList<String>();
        columns.add("");

        IOpenClass type = getUserDefinedType(typeName);
        if (type != null) {
            for (Entry<String, IOpenField> fieldEntry : type.getFields().entrySet()) {
                IOpenField field = fieldEntry.getValue();
                if (!field.isConst() && field.isWritable()) {
                    columns.add(fieldEntry.getKey());
                }
            }
        }

        return FacesUtils.createSelectItems(columns);
    }

    protected String buildTable(XlsSheetSourceCodeModule sourceCodeModule) throws CreateTableException {
        XlsSheetGridModel gridModel = new XlsSheetGridModel(sourceCodeModule);

        DataTableBuilder builder = new DataTableBuilder(gridModel);

        Map<String, Object> properties = buildProperties();

        int width = DataTableBuilder.MIN_WIDTH;
        if (!properties.isEmpty()) {
            width = TableBuilder.PROPERTIES_MIN_WIDTH;
        }

        width = Math.max(getFieldsCount(tree.getRoot().getValue()), width);

        int height = TableBuilder.HEADER_HEIGHT + properties.size() + 1;

        builder.beginTable(width, height);

        builder.writeHeader(tableType, tableName);
        builder.writeProperties(properties, null);

        builder.writeFieldNames(tree.getRoot().getValue().getAggregatedFields());

        String uri = gridModel.getRangeUri(builder.getTableRegion());

        builder.endTable();

        return uri;
    }

    /**
     * Count recursively a fields count that will be present in a result xls
     * file
     * 
     * @param rootNode root node of a data table type
     * @return total fields count including child ones
     */
    private int getFieldsCount(DataTableField rootNode) {
        int count = 0;

        for (DataTableField childNode : rootNode.getAggregatedFields()) {
            if (childNode.isFillChildren()) {
                count += getFieldsCount(childNode);
            } else {
                count++;
            }
        }

        return count;
    }

    private void initTree() {
        tableOpenClass = getUserDefinedType(tableType);

        if (tableOpenClass == null || tableOpenClass.isSimple()) {
            tree.setRoot(new DataTableTreeNode(new DataTablePredefinedTypeVariable(tableType), true));
        } else {
            DataTableField field = new DataTableUserDefinedTypeField(tableOpenClass, tableType,
                    new DataTableUserDefinedTypeField.PredefinedTypeChecker() {
                        @Override
                        public boolean isPredefined(IOpenClass type) {
                            return getUserDefinedType(type.getDisplayName(INamedThing.SHORT)) == null;
                        }
                    });

            tree.setRoot(new DataTableTreeNode(field, true));
        }
    }

    /**
     * Get user-defined type from opened project by it's name 
     * 
     * @param type type name
     * @return OpenClass for given type or null if type is not user-defined
     */
    private IOpenClass getUserDefinedType(String type) {
        for (IOpenClass dataType : WizardUtils.getProjectOpenClass().getTypes().values()) {
            if (dataType.getDisplayName(INamedThing.SHORT).equals(type)) {
                return dataType;
            }
        }
        
        for (IOpenClass dataType : importedClasses) {
            if (dataType.getDisplayName(INamedThing.SHORT).equals(type)) {
                return dataType;
            }
        }

        return null;
    }

    /**
     * Validate column configuration page.
     * If it is not valid, validation error messages will be added to a page
     * 
     * @return true if it valid
     */
    private boolean isColumnConfigValid() {
        String clientId = tree.getCurrentTreeNode().getClientId();
        DataTableTreeNode node = tree.getRoot();
        Iterator<Object> it = node.getChildrenKeysIterator();

        boolean correct = true;

        while (it.hasNext()) {
            DataTableTreeNode child = (DataTableTreeNode) node.getChild(it.next());
            if (!isColumnNodeCorrect(child, clientId + ":")) {
                correct = false;
            }
        }
        return correct;
    }

    private boolean isColumnNodeCorrect(DataTableTreeNode node, String prefix) {
        if (!node.isLeaf()) {
            Iterator<Object> it = node.getChildrenKeysIterator();
            boolean correct = true;

            while (it.hasNext()) {
                DataTableTreeNode child = (DataTableTreeNode) node.getChild(it.next());
                if (!isColumnNodeCorrect(child, prefix + node.getName() + ".")) {
                    correct = false;
                }
            }

            return correct;
        } else {
            if (StringUtils.isBlank(node.getForeignKeyTable())) {
                String clientId = prefix + node.getName();

                if (!node.isComplex() && node.isEditForeignKey()) {
                    clientId += ":simpleForeignKeyTable";
                    FacesUtils.addMessage(clientId, "Validation Error: ", "Fill foreign key or uncheck the checkbox",
                            FacesMessage.SEVERITY_ERROR);
                    return false;
                }

                return true;
            }

            return true;
        }
    }
    
    private void updateNodesForeignKey(DataTableTreeNode node) {
        if (!node.isEditForeignKey()) {
            node.setForeignKeyTable(null);
            node.setForeignKeyColumn(null);
        }
        
        if (!node.isLeaf()) {
            Iterator<Object> it = node.getChildrenKeysIterator();

            while (it.hasNext()) {
                updateNodesForeignKey((DataTableTreeNode) node.getChild(it.next()));
            }
            
        }
    }

    /**
     * Enumeration with the wizard's pages
     * 
     * @author NSamatov
     * 
     */
    private static enum Page {
        NO_SUCH_PAGE(-1),
        SELECT_WIZARD_TYPE(0),
        DATA_TABLE_TYPE(1),
        COLUMNS_CONFIGURATION(2),
        DESTINATION(3);

        public static Page valueOf(int pageNum) {
            for (Page page : values()) {
                if (page.pageNum == pageNum)
                    return page;
            }

            Log.warn("There is no pageNum {0}", pageNum);

            return NO_SUCH_PAGE;
        }

        private final int pageNum;

        private Page(int pageNum) {
            this.pageNum = pageNum;
        }
    }

}
