package org.openl.rules.ui.tablewizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.openl.rules.lang.xls.ITableNodeTypes;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import static org.openl.rules.ui.tablewizard.WizardUtils.getMetaInfo;

import org.openl.rules.table.properties.DefaultPropertyDefinitions;
import org.openl.rules.table.properties.TablePropertyDefinition;
import org.openl.rules.table.properties.TablePropertyDefinition.SystemValuePolicy;
import org.openl.rules.table.xls.builder.CreateTableException;
import org.openl.rules.table.xls.builder.TestTableBuilder;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.rules.webstudio.properties.SystemValuesManager;

/**
 * @author Aliaksandr Antonik.
 */
public class TestTableCreationWizard extends WizardBase {
    private SelectItem[] tableItems;
    private int selectedTable;    
    private String technicalName;

    public String getTechnicalName() {
        return technicalName;
    }

    public void setTechnicalName(String technicalName) {
        this.technicalName = technicalName;
    }
    
    private String getDefaultTechnicalName() {
        TableSyntaxNode node = getSelectedNode();
        String defaultName = TestTableBuilder.getDefaultTechnicalName(node);
        return defaultName;
    }
    
    /**
     * 
     * @return <code>TableSyntaxNode</code> from model, by the 
     * technical name of the table we have selected. 
     */
    private TableSyntaxNode getSelectedNode() {
        TableSyntaxNode[] nodes = getSyntaxNodes();
        if (selectedTable < 0 || selectedTable >= nodes.length) {
            throw new IllegalStateException("not table is selected");
        }

        TableSyntaxNode node = nodes[selectedTable];
        return node;
    }

    
    private void doSave() throws CreateTableException, IOException {
        TableSyntaxNode node = getSelectedNode();
        
        String header = TestTableBuilder.getHeader(node, technicalName);
        
        Map<String, String> params = TestTableBuilder.getParams(node);

        TestTableBuilder builder = new TestTableBuilder(new XlsSheetGridModel(getDestinationSheet()));
        builder.beginTable(params.size() + 1, 4);
        builder.writeHeader(header, null);
        
        builder.writeProperties(getSystemProperties(), null);
        
        builder.writeParams(params, null);
        builder.endTable();
        builder.save();
    }

    private Map<String, Object> getSystemProperties() {        
        Map<String, Object> result = new HashMap<String, Object>();
            List<TablePropertyDefinition> systemPropDefinitions = DefaultPropertyDefinitions
                    .getSystemProperties();
            for (TablePropertyDefinition systemPropDef : systemPropDefinitions) {
                if (systemPropDef.getSystemValuePolicy().equals(SystemValuePolicy.IF_BLANK_ONLY)) {
                    Object systemValue = SystemValuesManager.instance().
                        getSystemValue(systemPropDef.getSystemValueDescriptor());
                    if (systemValue != null) {
                        result.put(systemPropDef.getName(), systemValue);                        
                    }
                }
            }
        
        return result;
    }

    public SelectItem[] getDecisionTables() {
        return tableItems;
    }

    @Override
    public String getName() {
        return "newTestTable";
    }

    public int getSelectedTable() {
        return selectedTable;
    }

    private TableSyntaxNode[] getSyntaxNodes() {
        return getMetaInfo().getXlsModuleNode().getXlsTableSyntaxNodesWithoutErrors();
    }

    @Override
    protected void onFinish(boolean cancelled) {
        tableItems = null;
        reset();
    }

    @Override
    protected void onStart() {
        selectedTable = 0;

        TableSyntaxNode[] syntaxNodes = getSyntaxNodes();
        List<SelectItem> result = new ArrayList<SelectItem>();

        for (int i = 0; i < syntaxNodes.length; i++) {
            TableSyntaxNode node = syntaxNodes[i];
            if (ITableNodeTypes.XLS_DT.equals(node.getType())) {
                result.add(new SelectItem(i, node.getMember().getName()));
            }
        }

        tableItems = result.toArray(new SelectItem[result.size()]);
        Arrays.sort(tableItems, new Comparator<SelectItem>() {
            public int compare(SelectItem o1, SelectItem o2) {
                return (o1.getValue().toString()).compareTo(o2.getValue().toString());
            }
        });
    }

    @Override
    protected void onStepFirstVisit(int step) {
        if (step == 2) {
            initWorkbooks();
        }
    }

    public String save() {
        try {
            doSave();
            return "done";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not create table: ", e.getMessage()));
        }
        return null;
    }

    public void setSelectedTable(int selectedTable) {
        this.selectedTable = selectedTable;
        this.technicalName = getDefaultTechnicalName();
    }
}
