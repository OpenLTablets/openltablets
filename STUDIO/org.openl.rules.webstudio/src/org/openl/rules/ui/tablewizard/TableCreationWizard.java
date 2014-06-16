package org.openl.rules.ui.tablewizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.validator.constraints.NotBlank;
import org.openl.rules.lang.xls.XlsSheetSourceCodeModule;
import org.openl.rules.lang.xls.XlsWorkbookSourceCodeModule;
import org.openl.rules.lang.xls.load.SimpleSheetLoader;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.lang.xls.syntax.TableUtils;
import org.openl.rules.lang.xls.syntax.WorkbookSyntaxNode;
import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;
import org.openl.rules.table.properties.def.TablePropertyDefinition.SystemValuePolicy;
import org.openl.rules.ui.BaseWizard;
import org.openl.rules.ui.ProjectModel;
import org.openl.rules.ui.WebStudio;
import org.openl.rules.webstudio.properties.SystemValuesManager;
import org.openl.rules.webstudio.web.util.WebStudioUtils;

/**
 * @author Aliaksandr Antonik.
 * 
 * TODO Rename Workbook and Worksheet to Module and Category correspondently
 */
public abstract class TableCreationWizard extends BaseWizard {

    private final Log log = LogFactory.getLog(TableCreationWizard.class);

    private static final String SHEET_EXSISTING = "existing";
    private static final String SHEET_NEW = "new";
    private String workbook;
    private Integer worksheetIndex;
    private Map<String, XlsWorkbookSourceCodeModule> workbooks;
    private boolean newWorksheet;
    private boolean wizardFinised;

    @NotBlank(message="Can not be empty")
    private String newWorksheetName;

    /** New table identifier */
    private String newTableId;

    private Set<XlsWorkbookSourceCodeModule> modifiedWorkbooks = new HashSet<XlsWorkbookSourceCodeModule>();

    protected XlsSheetSourceCodeModule getDestinationSheet() {
        XlsSheetSourceCodeModule sourceCodeModule;
        XlsWorkbookSourceCodeModule module = workbooks.get(workbook);
        if (newWorksheet) {
            Sheet sheet = module.getWorkbook().createSheet(getNewWorksheetName());
            sourceCodeModule = new XlsSheetSourceCodeModule(new SimpleSheetLoader(sheet), module);
        } else {
            Sheet sheet = module.getWorkbook().getSheetAt(getWorksheetIndex());
            sourceCodeModule = new XlsSheetSourceCodeModule(new SimpleSheetLoader(sheet), module);
        }
        return sourceCodeModule;
    }

    public String getNewWorksheet() {
        return newWorksheet ? SHEET_NEW : SHEET_EXSISTING;
    }

    public void setNewWorksheet(String value) {
        newWorksheet = SHEET_NEW.equals(value);
    }

    public String getNewWorksheetName() {
        return newWorksheetName;
    }

    public void setNewWorksheetName(String newWorksheetName) {
        this.newWorksheetName = newWorksheetName;
    }

    public String getWorkbook() {
        return workbook;
    }

    public void setWorkbook(String workbook) {
        this.workbook = workbook;
    }

    public String getModuleName() {
        return FilenameUtils.getBaseName(workbook);
    }

    public void setWorksheetIndex(Integer worksheetIndex) {
        this.worksheetIndex = worksheetIndex;
    }

    public List<SelectItem> getWorkbooks() {
        List<SelectItem> items = new ArrayList<SelectItem>(workbooks.size());
        for (String wbURI : workbooks.keySet()) {
            items.add(new SelectItem(wbURI, FilenameUtils.getBaseName(wbURI)));
        }

        return items;
    }

    public Integer getWorksheetIndex() {
        return worksheetIndex;
    }

    public String getWorksheetName() {
        Workbook currentWorkbook = workbooks.get(workbook).getWorkbook();
        return currentWorkbook.getSheetName(worksheetIndex);
    }

    public List<SelectItem> getWorksheets() {
        if (workbook == null || workbooks == null) {
            return Collections.emptyList();
        }

        XlsWorkbookSourceCodeModule currentSheet = workbooks.get(workbook);
        if (currentSheet == null) {
            return Collections.emptyList();
        }

        Workbook workbook = currentSheet.getWorkbook();
        List<SelectItem> items = new ArrayList<SelectItem>(workbook.getNumberOfSheets());
        for (int i = 0; i < workbook.getNumberOfSheets(); ++i) {
            items.add(new SelectItem(i, workbook.getSheetName(i)));
        }

        Collections.sort(items, new Comparator<SelectItem>() {
            @Override
            public int compare(SelectItem item1, SelectItem item2) {
                return item1.getLabel().compareToIgnoreCase(item2.getLabel());
            }
        });

        return items;
    }

    protected void initWorkbooks() {
        workbooks = new HashMap<String, XlsWorkbookSourceCodeModule>();

        WorkbookSyntaxNode[] syntaxNodes = WizardUtils.getWorkbookNodes();
        for (WorkbookSyntaxNode node : syntaxNodes) {
            XlsWorkbookSourceCodeModule module = node.getWorkbookSourceCodeModule();
            workbooks.put(module.getDisplayName(), module);
        }

        if (workbooks.size() > 0) {
            workbook = workbooks.keySet().iterator().next();
        }
    }

    public String getNewTableId() {
        return newTableId;
    }

    public void setNewTableId(String newTableId) {
        this.newTableId = TableUtils.makeTableId(newTableId);
    }

    public Set<XlsWorkbookSourceCodeModule> getModifiedWorkbooks() {
        return modifiedWorkbooks;
    }

    protected void reset() {
        worksheetIndex = 0;
        workbooks = null;
        newWorksheet = false;
        wizardFinised = false;
        newWorksheetName = StringUtils.EMPTY;
        getModifiedWorkbooks().clear();
    }

    protected void doSave() throws Exception {
        for (XlsWorkbookSourceCodeModule workbook : modifiedWorkbooks){
            workbook.save();
        }
    }

    @Override
    public String finish() throws Exception {
        boolean success = false;
        try {
            if (!wizardFinised) {
                onFinish();
                wizardFinised = true;
            }
            doSave();
            success = true;
        } catch (Exception e) {
            log.error("Could not save table: ", e);
            throw e;
        }
        if (success) {
            resetStudio();
            reset(); // After wizard is finished - no need to store references to tables etc: it will be a memory leak.
        }
        return null;
    }

    private void resetStudio() {
        final WebStudio studio = WebStudioUtils.getWebStudio();
        studio.rebuildModel();
    }

    /**
     * Validation for technical name
     */
    public void validateTechnicalName(FacesContext context, UIComponent toValidate, Object value) {
        FacesMessage message = new FacesMessage();
        ValidatorException validEx = null;

        try {
            String name = ((String) value).toUpperCase();

            if (!this.checkNames(name)) {
                message.setDetail("Table with such name already exists");
                validEx = new ValidatorException(message);
                throw validEx;
            }

        } catch (Exception e) {
            throw new ValidatorException(message);
        }
    }

    private boolean checkNames(String techName) {
        WebStudio studio = WebStudioUtils.getWebStudio();
        ProjectModel model = studio.getModel();

        for (TableSyntaxNode node : model.getAllTableNodes().values()) {
            try {
                if (node.getMember().getName().equalsIgnoreCase(techName)) {
                    return false;
                }

            } catch(Exception e){
            }
        }

        return true;
    }

    protected Map<String, Object> buildSystemProperties() {
        String userMode = WebStudioUtils.getWebStudio().getSystemConfigManager().getStringProperty("user.mode");
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        List<TablePropertyDefinition> systemPropDefinitions = TablePropertyDefinitionUtils.getSystemProperties();
        for (TablePropertyDefinition systemPropDef : systemPropDefinitions) {
            String systemValueDescriptor = systemPropDef.getSystemValueDescriptor();
            if (userMode.equals("single") && systemValueDescriptor.equals(SystemValuesManager.CURRENT_USER_DESCRIPTOR)) {
                continue;
            }
            if (systemPropDef.getSystemValuePolicy().equals(SystemValuePolicy.IF_BLANK_ONLY)) {
                Object systemValue = SystemValuesManager.getInstance().getSystemValue(systemValueDescriptor);
                if (systemValue != null) {
                    result.put(systemPropDef.getName(), systemValue);
                }
            }
        }

        return result;
    }

    protected Map<String, Object> buildProperties() {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();

        // Put system properties.
        if (WebStudioUtils.getWebStudio().isUpdateSystemProperties()) {
            Map<String, Object> systemProperties = buildSystemProperties();
            properties.putAll(systemProperties);
        }

        return properties;
    }

}
