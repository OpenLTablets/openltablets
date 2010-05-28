package org.openl.rules.ui.tablewizard;

import static org.openl.rules.ui.tablewizard.WizardUtils.getMetaInfo;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;

import org.openl.rules.domaintree.DomainTree;
import org.openl.rules.lang.xls.XlsSheetSourceCodeModule;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.rules.table.xls.builder.CreateTableException;
import org.openl.rules.table.xls.builder.DatatypeTableBuilder;
import org.openl.rules.webtools.jsf.FacesUtils;
import org.richfaces.component.html.HtmlDataTable;

/**
 * @author Andrei Astrouski
 */
public class DatatypeTableCreationWizard extends WizardBase {

    @NotEmpty(message="Table name can not be empty")
    @Pattern(regexp="([a-zA-Z_][a-zA-Z_0-9]*)?", message="Invalid table name")
    private String tableName;

    @Valid
    private List<TypeNamePair> parameters = new ArrayList<TypeNamePair>();

    private DomainTree domainTree;
    private SelectItem[] domainTypes;

    private HtmlDataTable parametersTable;

    public DatatypeTableCreationWizard() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<TypeNamePair> getParameters() {
        return parameters;
    }

    public void setParameters(List<TypeNamePair> parameters) {
        this.parameters = parameters;
    }

    public DomainTree getDomainTree() {
        return domainTree;
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
        return "newDatatypeTable";
    }

    @Override
    protected void onStart() {
        reset();

        domainTree = DomainTree.buildTree(getMetaInfo());
        domainTypes = FacesUtils.createSelectItems(domainTree.getAllClasses(true));

        addParameter();
    }

    @Override
    protected void onCancel() {
        reset();
    }

    protected String buildTable(XlsSheetSourceCodeModule sourceCodeModule) throws CreateTableException {
        XlsSheetGridModel gridModel = new XlsSheetGridModel(sourceCodeModule);
        DatatypeTableBuilder builder = new DatatypeTableBuilder(gridModel);

        builder.beginTable(parameters.size() + 1); // params + header

        builder.writeHeader(tableName);

        for (TypeNamePair parameter : parameters) {
            String paramType = parameter.getType();
            if (parameter.isIterable()) {
                paramType += "[]";
            }
            builder.writeParameter(paramType, parameter.getName());
        }

        String uri = gridModel.getRangeUri(builder.getTableRegion());

        builder.endTable();

        return uri;
    }

    @Override
    protected void onStepFirstVisit(int step) {
        switch (step) {
            case 3:
                initWorkbooks();
                break;
        }
    }

    public void addParameter() {
        parameters.add(new TypeNamePair());
    }

    public void removeParameter() {
        TypeNamePair parameter = (TypeNamePair) parametersTable.getRowData();
        parameters.remove(parameter);
    }

    @Override
    protected void reset() {
        tableName = null;
        parameters = new ArrayList<TypeNamePair>();

        domainTree = null;
        domainTypes = null;

        super.reset();
    }

    @Override
    protected void onFinish() throws Exception {
        XlsSheetSourceCodeModule sheetSourceModule = getDestinationSheet();
        String newTableUri = buildTable(sheetSourceModule);
        setNewTableUri(newTableUri);
        getModifiedWorkbooks().add(sheetSourceModule.getWorkbookSource());
        super.onFinish();
    }

}
