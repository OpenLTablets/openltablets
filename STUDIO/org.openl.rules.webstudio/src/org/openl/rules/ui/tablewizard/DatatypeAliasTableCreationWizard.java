package org.openl.rules.ui.tablewizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.faces.component.html.HtmlDataTable;
import javax.faces.model.SelectItem;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import org.openl.commons.web.jsf.FacesUtils;
import org.openl.rules.domaintree.DomainTree;
import org.openl.rules.lang.xls.XlsSheetSourceCodeModule;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.rules.table.xls.builder.CreateTableException;
import org.openl.rules.table.xls.builder.DatatypeAliasTableBuilder;
import org.openl.rules.table.xls.builder.TableBuilder;
import org.springframework.util.CollectionUtils;

/**
 * @author Andrei Astrouski
 */
public class DatatypeAliasTableCreationWizard extends TableCreationWizard {

    @NotBlank(message="Can not be empty")
    @Pattern(regexp = "([a-zA-Z_][a-zA-Z_0-9]*)?", message = INVALID_NAME_MESSAGE)
    private String technicalName;

    private String aliasType;

    @Valid
    private List<AliasValue> values = new ArrayList<AliasValue>();

    private DomainTree domainTree;
    private SelectItem[] domainTypes;

    private HtmlDataTable valuesTable;

    public DatatypeAliasTableCreationWizard() {
    }

    public String getTechnicalName() {
        return technicalName;
    }

    public void setTechnicalName(String technicalName) {
        this.technicalName = technicalName;
    }

    public String getAliasType() {
        return aliasType;
    }

    public void setAliasType(String aliasType) {
        this.aliasType = aliasType;
    }

    public List<AliasValue> getValues() {
        return values;
    }

    public void setValues(List<AliasValue> values) {
        this.values = values;
    }

    public DomainTree getDomainTree() {
        return domainTree;
    }

    public SelectItem[] getDomainTypes() {
        return domainTypes;
    }

    public HtmlDataTable getValuesTable() {
        return valuesTable;
    }

    public void setValuesTable(HtmlDataTable valuesTable) {
        this.valuesTable = valuesTable;
    }

    @Override
    public String getName() {
        return "newDatatypeAliasTable";
    }

    @Override
    protected void onStart() {
        reset();

        domainTree = DomainTree.buildTree(WizardUtils.getProjectOpenClass(), false);
        Collection<String> allClasses = domainTree.getAllClasses();
        domainTypes = FacesUtils.createSelectItems(allClasses);
        
        if (!CollectionUtils.isEmpty(allClasses) && CollectionUtils.contains(allClasses.iterator(), "String")) {
            setAliasType("String");
        }

        addValue();
    }

    @Override
    protected void onCancel() {
        reset();
    }

    protected String buildTable(XlsSheetSourceCodeModule sourceCodeModule) throws CreateTableException {
        XlsSheetGridModel gridModel = new XlsSheetGridModel(sourceCodeModule);
        DatatypeAliasTableBuilder builder = new DatatypeAliasTableBuilder(gridModel);

        Map<String, Object> properties = buildProperties();

        int width = DatatypeAliasTableBuilder.MIN_WIDTH;
        if (!properties.isEmpty()) {
            width = TableBuilder.PROPERTIES_MIN_WIDTH;
        }
        int height = TableBuilder.HEADER_HEIGHT + properties.size() + values.size();

        builder.beginTable(width, height);

        builder.writeHeader(technicalName, aliasType);
        builder.writeProperties(properties, null);

        for (AliasValue value : values) {
            builder.writeValue(value.getValue());
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

    public void addValue() {
        values.add(new AliasValue());
    }

    public void removeValue(AliasValue value) {
        values.remove(value);
    }

    @Override
    protected void reset() {
        technicalName = null;
        values = new ArrayList<AliasValue>();

        domainTree = null;
        domainTypes = null;

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

}
