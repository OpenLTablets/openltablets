package org.openl.rules.ui.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.apache.commons.lang3.StringUtils;
import org.openl.rules.search.OpenLBussinessSearch;
import org.openl.rules.table.properties.def.DefaultPropertyDefinitions;
import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.tableeditor.renderkit.TableProperty;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;

/**
 * Bean to handle business search, has session scope.
 * @author DLiauchuk
 *
 */
@ManagedBean(name="bussinessSearch")
@RequestScoped
@Deprecated
public class BussinesSearchPropertyBean {

    private List<TableProperty> propForSearch = new ArrayList<TableProperty>();
    private List<TableProperty> filledPropsForSearch = new ArrayList<TableProperty>();
    private String tableContain;
    private final OpenLBussinessSearch search = new OpenLBussinessSearch();    

    public String getTableContain() {
        return tableContain;
    }

    public void setTableContain(String tableContain) {
        this.tableContain = tableContain;        
    }
    
    public List<TableProperty> getPropForSearch() {
        return propForSearch;
    }

    public void setPropForSearch(List<TableProperty> propForSearch) {
        this.propForSearch = propForSearch;
    }
    
    /**
     * During the construction, we take all the properties from DefaultPropertyDefinitions 
     * that must be included in business search 
     */
    public BussinesSearchPropertyBean() {
        TablePropertyDefinition[] propDefinitions = DefaultPropertyDefinitions.getDefaultDefinitions();
        for (TablePropertyDefinition propDefinition : propDefinitions) {
            //if(propDefinition.isBusinessSearch()) {
                Class<?> propertyType = propDefinition.getType() == null ? null : propDefinition.getType()
                        .getInstanceClass();
                propForSearch.add(
                        new TableProperty.TablePropertyBuilder(propDefinition.getName(), propertyType)
                            .displayName(propDefinition.getDisplayName()).group(propDefinition.getGroup())
                            .format(propDefinition.getFormat()).constraints(propDefinition.getConstraints())
                            .build());
            //}
        }
        //busSearchResBean = new BussinessSearchResultBean(propForSearch);
    }

    /**
     * Initialize the conditions for business search
     */
    public void initBusSearchCond() {
        if(isAnyPropertyFilled()) {
            Map<String, Object> mapforSearch = search.getBusSearchCondit().getPropToSearch();  
            mapforSearch.clear();
            for(TableProperty prop : filledPropsForSearch) {
                mapforSearch.put(prop.getName(), prop.getValue());
            }
            search.getBusSearchCondit().setTablesContains(searchTableContains());
        }
    }
    
    public boolean isReady() {
        return WebStudioUtils.isStudioReady();
    }
    
    /**
     * Returns <code>true</code> if any property on UI was filled for search.
     * If none returns <code>false</code>. Put all filled properties to new list. 
     * 
     * @return <code>true</code> if any property on UI was filled for search.
     * If none returns <code>false</code>.
     */
    public boolean isAnyPropertyFilled() {
        filledPropsForSearch.clear();
        boolean result = false;
        for(TableProperty prop : propForSearch) {
            if (prop.getValue() != null && StringUtils.isNotEmpty(prop.getDisplayValue())){
                filledPropsForSearch.add(prop);
                result = true;
            }
        }
        return result;
    }

    /**
     * Get all the tables, that contain string from tableConsist field.
     * 
     * @return Massive of tables that suit to table contains field 
     */
    public TableSyntaxNode[] searchTableContains() {
        String[][] values;
        TableSyntaxNode[] result = null;
        List<TableSyntaxNode> resultNodes = new ArrayList<TableSyntaxNode>();
        if (!tableContain.isEmpty()) {
            values = WebStudioUtils.getWebStudio().getModel().getIndexer().getResultsForQuery(tableContain, 200, null);
            if (values.length > 0) {
                for (String[] value : values) {
                    String uri = value[0];
                    if (uri.contains(".xls")) {
                        resultNodes.add(WebStudioUtils.getWebStudio().getModel().getNode(uri));
                    }
                }
            }
            result = resultNodes.toArray(new TableSyntaxNode[resultNodes.size()]);
        } 
        return result;
    } 

    public OpenLBussinessSearch getSearch() {
        return search;
    }

}
