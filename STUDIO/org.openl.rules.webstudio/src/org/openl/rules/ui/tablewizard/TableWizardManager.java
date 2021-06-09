package org.openl.rules.ui.tablewizard;

import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

/**
 * @author Aliaksandr Antonik.
 */
@Service("tableCreatorWizardManager")
@SessionScope
public class TableWizardManager extends BaseTableWizardManager {

    enum TableType {
        DATATYPE,
        DATATYPE_ALIAS,
        DATA,
        TEST,
        TEST_DIRECT,
        PROPERTY,
        SIMPLERULES
    }

    private TableType tableType = TableType.DATATYPE;

    @Override
    public String cancel() {
        if (wizard != null) {
            wizard.cancel();
        }
        return null;
    }

    public String getTableType() {
        return tableType.name();
    }

    public void setTableType(String tableType) {
        try {
            this.tableType = TableType.valueOf(tableType);
        } catch (IllegalArgumentException e) {
            this.tableType = TableType.DATATYPE;
        }
    }

    @Override
    public String start() {
        tableType = TableType.DATATYPE;
        return "wizardSelect";
    }

    @Override
    public String startWizard() {
        reload();
        switch (tableType) {
            case DATATYPE:
                wizard = new DatatypeTableCreationWizard();
                wizard.setStepsCount(3);
                break;
            case DATATYPE_ALIAS:
                wizard = new DatatypeAliasTableCreationWizard();
                wizard.setStepsCount(3);
                break;
            case DATA:
                wizard = new DataTableCreationWizard();
                wizard.setStepsCount(3);
                break;
            case TEST:
                wizard = new TestTableCreationWizard();
                wizard.setStepsCount(3);
                break;
            case TEST_DIRECT:
                wizard = new TestTableCreationWizardDirect(getTable());
                wizard.setStepsCount(2);
                break;
            case PROPERTY:
                wizard = new PropertyTableCreationWizard();
                wizard.setStepsCount(3);
                break;
            case SIMPLERULES:
                wizard = new SimpleRulesCreationWizard();
                wizard.setStepsCount(3);
                break;
            default:
                return null;
        }

        try {
            String next = wizard.start();
            wizard.next();
            return next;
        } catch (Exception e) {
            // Process the error situation on start.
            WebStudioUtils.addErrorMessage("Cannot create wizard for this kind of table.", e.getMessage());
            return StringUtils.EMPTY;
        }
    }

}
