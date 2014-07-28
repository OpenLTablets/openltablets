package org.openl.rules.ui.tablewizard;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.faces.validator.ValidatorException;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.constraints.NotBlank;
import org.openl.base.INamedThing;
import org.openl.commons.web.jsf.FacesUtils;
import org.openl.meta.BigDecimalValue;
import org.openl.meta.BigIntegerValue;
import org.openl.meta.ByteValue;
import org.openl.meta.DoubleValue;
import org.openl.meta.FloatValue;
import org.openl.meta.IntValue;
import org.openl.meta.LongValue;
import org.openl.meta.ShortValue;
import org.openl.rules.domaintree.DomainTree;
import org.openl.rules.helpers.DoubleRange;
import org.openl.rules.helpers.IntRange;
import org.openl.rules.lang.xls.XlsSheetSourceCodeModule;
import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;
import org.openl.rules.table.properties.inherit.InheritanceLevel;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.rules.table.xls.builder.CreateTableException;
import org.openl.rules.table.xls.builder.DataTableBuilder;
import org.openl.rules.table.xls.builder.SimpleRulesTableBuilder;
import org.openl.rules.table.xls.builder.TableBuilder;
import org.openl.rules.ui.tablewizard.util.CellStyleManager;
import org.openl.rules.ui.tablewizard.util.JSONHolder;
import org.openl.types.IOpenClass;
import org.openl.types.impl.DomainOpenClass;
import org.openl.types.impl.OpenClassDelegator;
import org.richfaces.json.JSONException;

public class SimpleRulesCreationWizard extends TableCreationWizard {
    private final Log log = LogFactory.getLog(SimpleRulesCreationWizard.class);

    @NotBlank(message = "Can not be empty")
    @Pattern(regexp = "([a-zA-Z_][a-zA-Z_0-9]*)?", message = "Invalid name")
    private String tableName;
    private SelectItem[] domainTypes;
    private String jsonTable;
    private JSONHolder table;
    private String restoreTable;
    private final String TABLE_TYPE = "xls.dt";

    private String restoreTableFunction = "tableModel.restoreTableFromJSONString";

    private List<DomainTypeHolder> typesList;

    private String returnValueType;

    private List<TypeNamePair> parameters = new ArrayList<TypeNamePair>();

    @Override
    protected void onCancel() {
        reset();
    }

    @Override
    protected void onStart() {
        reset();

        initDomainType();
        initWorkbooks();
    }

    private void initDomainType() {
        List<IOpenClass> types = new ArrayList<IOpenClass>(WizardUtils.getProjectOpenClass().getTypes().values());
        Collection<IOpenClass> importedClasses = WizardUtils.getImportedClasses();
        types.addAll(importedClasses);

        List<String> datatypes = new ArrayList<String>(types.size());
        datatypes.add("");
        for (IOpenClass datatype : types) {
            if (Modifier.isFinal(datatype.getInstanceClass().getModifiers())) {
                // cannot inherit from final class
                continue;
            }

            if (!(datatype instanceof DomainOpenClass)) {
                datatypes.add(datatype.getDisplayName(INamedThing.SHORT));
            }
        }

        Collection<String> allClasses = DomainTree.buildTree(WizardUtils.getProjectOpenClass()).getAllClasses();
        for (IOpenClass type : importedClasses) {
            if (type instanceof OpenClassDelegator) {
                allClasses.add(type.getName());
            } else {
                allClasses.add(type.getDisplayName(INamedThing.SHORT));
            }
        }

        domainTypes = FacesUtils.createSelectItems(allClasses);

        Collection<IOpenClass> classTypes =  DomainTree.buildTree(WizardUtils.getProjectOpenClass()).getAllOpenClasses();
        this.typesList = new ArrayList<DomainTypeHolder>();

        for (IOpenClass oc : classTypes) {
            if (oc instanceof OpenClassDelegator) {
                typesList.add(new DomainTypeHolder(oc.getName(), oc, false));
            } else {
                typesList.add(new DomainTypeHolder(oc.getDisplayName(INamedThing.SHORT), oc, false));
            }
        }
    }

    public int getColumnSize() {
        int size = 0;
        size += this.parameters.size();
        size ++;

        return size;
    }

    public List<SelectItem> getPropertyList() {
        List<SelectItem> propertyNames = new ArrayList<SelectItem>();
        TablePropertyDefinition[] propDefinitions = TablePropertyDefinitionUtils
                .getDefaultDefinitionsForTable(TABLE_TYPE, InheritanceLevel.TABLE, true);

        SelectItem selectItem = new SelectItem("");
        selectItem.setLabel("");
        propertyNames.add(selectItem);

        Map<String, Set<TablePropertyDefinition>> propGroups = TablePropertyDefinitionUtils
                .groupProperties(propDefinitions);
        for (String groupName : propGroups.keySet()) {
            List<SelectItem> items = new ArrayList<SelectItem>();

            for (TablePropertyDefinition propDefinition : propGroups.get(groupName)) {
                String propName = propDefinition.getName();
                if (propDefinition.getDeprecation() == null) {
                    items.add(new SelectItem(propName, propDefinition.getDisplayName()));
                }
            }

            if (!items.isEmpty()) {
                SelectItemGroup itemGroup = new SelectItemGroup(groupName);
                itemGroup.setSelectItems(items.toArray(new SelectItem[items.size()]));
                propertyNames.add(itemGroup);
            }
        }

        return propertyNames;
    }

    public DomainTypeHolder getReturnValue() {
        return getTypedParameterByName(this.returnValueType);
    }

    private DomainTypeHolder getTypedParameterByName(String name) {
        DomainTypeHolder dth = null;

        for (DomainTypeHolder type :  typesList) {
            if (type.name.equals(name)) {
                dth = type.clone();
                break;
            }
        }

        if (dth == null) {
            dth = new DomainTypeHolder(name, "STRING", false);
        }

        return dth;
    }

    public List<DomainTypeHolder> getTypedParameters() {
        List<DomainTypeHolder> typedParameters = new ArrayList<DomainTypeHolder>();

        for (TypeNamePair tnp : this.parameters) {
            DomainTypeHolder gth = getTypedParameterByName(tnp.getType());
            gth.setTypeName(tnp.getName());
            gth.setIterable(tnp.isIterable());

            typedParameters.add(gth);
        }

        return typedParameters;
    }

    @Override
    protected void onFinish() throws Exception {
        XlsSheetSourceCodeModule sheetSourceModule = getDestinationSheet();
        String newTableUri = buildTable(sheetSourceModule);
        setNewTableId(newTableUri);
        getModifiedWorkbooks().add(sheetSourceModule.getWorkbookSource());
        super.onFinish();
    }

    protected String buildTable(XlsSheetSourceCodeModule sourceCodeModule) throws CreateTableException {
        XlsSheetGridModel gridModel = new XlsSheetGridModel(sourceCodeModule);
        SimpleRulesTableBuilder builder = new SimpleRulesTableBuilder(gridModel);

        CellStyleManager styleManager = new CellStyleManager(gridModel, table);

        Map<String, Object> properties = buildProperties();
        properties.putAll(table.getProperties());

        int width = DataTableBuilder.MIN_WIDTH;
        if (!properties.isEmpty()) {
            width = TableBuilder.PROPERTIES_MIN_WIDTH;
        }

        List<List<Map<String, Object>>> rows = table.getDataRows(styleManager);
        width = Math.max(table.getFieldsCount(), width);
        int height = TableBuilder.HEADER_HEIGHT + properties.size() + rows.size();

        builder.beginTable(width, height);
        builder.writeHeader(table.getHeaderStr(), styleManager.getHeaderStyle());

        builder.writeProperties(properties, styleManager.getPropertyStyles());

        for(List<Map<String, Object>> row : rows) {
            builder.writeTableBodyRow(row);
        }

        String uri = gridModel.getRangeUri(builder.getTableRegion());

        builder.endTable();

        return uri;
    }

    public void addParameter() {
        parameters.add(new TypeNamePair());
    }

    public void removeParameter(TypeNamePair parameter) {
        parameters.remove(parameter);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public SelectItem[] getDomainTypes() {
        return domainTypes;
    }

    public void setDomainTypes(SelectItem[] domainTypes) {
        this.domainTypes = domainTypes;
    }

    public String getReturnValueType() {
        return returnValueType;
    }

    public void setReturnValueType(String returnValueType) {
        this.returnValueType = returnValueType;
    }

    public List<TypeNamePair> getParameters() {
        return parameters;
    }

    public void setParameters(List<TypeNamePair> parameters) {
        this.parameters = parameters;
    }

    public List<DomainTypeHolder> getTypesList() {
        return typesList;
    }

    public void setTypesList(List<DomainTypeHolder> typesList) {
        this.typesList = typesList;
    }
    
    public class DomainTypeHolder {
        private String name;
        private String type;
        private boolean iterable = false;
        private String typeName;
        
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }

        DomainTypeHolder(String name, IOpenClass openClass, boolean iterable) {
            this.name = name;
            this.iterable = iterable;

            if (openClass != null) {
                if(openClass.isArray()) {
                    this.type = "ARRAY";
                } else if (openClass.toString().equals(Date.class.getCanonicalName())) {
                    this.type = "DATE";
                } else if (openClass.toString().equals(boolean.class.getCanonicalName()) ||
                        openClass.toString().equals(Boolean.class.getCanonicalName())) {
                    this.type = "BOOLEAN";
                } else if (openClass.toString().equals(BigInteger.class.getCanonicalName()) ||
                        openClass.toString().equals(BigIntegerValue.class.getCanonicalName()) ||
                        openClass.toString().equals(ByteValue.class.getCanonicalName()) ||
                        openClass.toString().equals(LongValue.class.getCanonicalName()) ||
                        openClass.toString().equals(ShortValue.class.getCanonicalName()) ||
                        openClass.toString().equals(byte.class.getCanonicalName()) ||
                        openClass.toString().equals(long.class.getCanonicalName()) ||
                        openClass.toString().equals(short.class.getCanonicalName()) ||
                        openClass.toString().equals(int.class.getCanonicalName()) ||
                        openClass.toString().equals(IntValue.class.getCanonicalName())) {
                    this.type = "INT";
                } else if (openClass.toString().equals(BigDecimal.class.getCanonicalName()) ||
                        openClass.toString().equals(BigDecimalValue.class.getCanonicalName()) ||
                        openClass.toString().equals(DoubleValue.class.getCanonicalName()) ||
                        openClass.toString().equals(Double.class.getCanonicalName()) ||
                        openClass.toString().equals(FloatValue.class.getCanonicalName()) ||
                        openClass.toString().equals(double.class.getCanonicalName()) ||
                        openClass.toString().equals(float.class.getCanonicalName())) {
                    this.type = "FLOAT";
                } else if (openClass.toString().equals(IntRange.class.getCanonicalName()) ||
                     openClass.toString().equals(DoubleRange.class.getCanonicalName())) {
                    this.type = "RANGE";
                } else {
                    this.type = "STRING";
                }
            }
        }

        DomainTypeHolder(String name, String type, boolean iterable) {
            this.name = name;
            this.iterable = iterable;
            this.type = type;
        }

        public DomainTypeHolder clone(){
            return new DomainTypeHolder(this.name, this.type, this.iterable);
        }

        public DomainTypeHolder(TypeNamePair tnp) {
            // TODO Auto-generated constructor stub
        }

        public String getTypeName() {
            return typeName;
        }
        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
        public boolean isIterable() {
            return iterable;
        }
        public void setIterable(boolean iterable) {
            this.iterable = iterable;
        }
    }

    public String getJsonTable() {
        return jsonTable;
    }

    public void setJsonTable(String jsonTable) {
        this.jsonTable = jsonTable;

        try {
            this.table = new JSONHolder(jsonTable);
            this.restoreTable = getTableInitFunction(jsonTable);
        } catch (JSONException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private String getTableInitFunction(String jsonStr) {
        return this.restoreTableFunction + "("+jsonStr+")";
    }

    public String getRestoreTable() {
        return restoreTable;
    }

    public void setRestoreTable(String restoreTable) {
        this.restoreTable = restoreTable;
    }

    @Override
    protected void reset() {
        jsonTable = null;
        table = null;
        restoreTable = null;

        super.reset();
    }

    /**
     * Validation for properties name
     */
    public void validatePropsName(FacesContext context, UIComponent toValidate, Object value) {
        String name = ((String) value);
        FacesMessage message = new FacesMessage();
        ValidatorException validEx;
        int paramId = this.getParamId(toValidate.getClientId());

        if (this.containsRemoveLink(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap())) {
            return;
        }

        checkParameterName(name);

        try {
            int i = 0;
            for (TypeNamePair param : parameters) {
                if (paramId != i && param.getName() != null && param.getName().equals(name)){
                    message.setDetail("Parameter with such name already exists");
                    validEx = new ValidatorException(message);
                    throw validEx;
                }

                i++;
            }

        } catch (Exception e) {
            throw new ValidatorException(message);
        }
    }

    /**
     * Validation for table name
     */
    public void validateTableName(FacesContext context, UIComponent toValidate, Object value) {
        String name = ((String) value);
        FacesMessage message = new FacesMessage();
        ValidatorException validEx = null;
        int paramId = this.getParamId(toValidate.getClientId());

        if (this.containsRemoveLink(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap())) {
            return;
        }

        checkParameterName(name);
    }

    public void pNameListener(ValueChangeEvent valueChangeEvent) {
        int paramId = this.getParamId(valueChangeEvent.getComponent().getClientId());

        this.parameters.get(paramId).setName(valueChangeEvent.getNewValue().toString()); 
    }

    public void pTypeListener(ValueChangeEvent valueChangeEvent) {
        int paramId = this.getParamId(valueChangeEvent.getComponent().getClientId());

        this.parameters.get(paramId).setType(valueChangeEvent.getNewValue().toString()); 
    }

    public void pIterableListener(ValueChangeEvent valueChangeEvent) {
        int paramId = this.getParamId(valueChangeEvent.getComponent().getClientId());

        this.parameters.get(paramId).setIterable(Boolean.getBoolean(valueChangeEvent.getNewValue().toString())); 
    }

    private int getParamId(String componentId) {
        if (componentId != null) {
            String[] elements = componentId.split(":");
            
            if (elements.length > 3) {
                return Integer.parseInt(elements[2]);
            }
        }

        return 0;
    }

    public boolean containsRemoveLink(Map<String, String> params) {
        if (params == null) {
            return false;
        }

        for (String param : params.keySet()) {
            if (param.endsWith("removeLink")) {
                return true;
            }
        }
        return false;
    }

    private void checkParameterName(String name) {
        String regex = "([a-zA-Z_][a-zA-Z_0-9]*)?";
        FacesMessage message = new FacesMessage();
        ValidatorException validEx;

        if (StringUtils.isEmpty(name)) {
            message.setDetail("Can not be empty");
            validEx = new ValidatorException(message);
            throw validEx;
        }

        if (!name.matches(regex)) {
            message.setDetail("Invalid name");
            validEx = new ValidatorException(message);
            throw validEx;
        }
    }
}
