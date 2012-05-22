package org.openl.rules.tableeditor.renderkit;

import java.util.Collection;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.openl.commons.web.jsf.FacesUtils;
import org.openl.rules.table.IOpenLTable;
import org.openl.rules.table.ui.filters.IGridFilter;
import org.openl.rules.tableeditor.util.Constants;
import org.openl.util.BooleanUtils;

/**
 * @author Andrei Astrouski
 */
public class TableEditor {

    private String id;
    private IOpenLTable table;
    private boolean editable;
    private String mode;
    private String view;
    private boolean showFormulas;
    private boolean collapseProps;
    private IGridFilter[] filters;
    private String beforeSaveAction;
    private String afterSaveAction;
    private String saveFailureAction;
    private String onBeforeSave;
    private String onAfterSave;
    private String onSaveFailure;
    private String excludeScripts;
    private String linkBase;
    private String linkTarget;

    public TableEditor() {
    }

    public TableEditor(FacesContext context, UIComponent component) {
        Map<String, Object> attributes = component.getAttributes();
        id = component.getClientId(context) + Constants.TABLE_EDITOR_PREFIX;
        table = (IOpenLTable) attributes.get(Constants.ATTRIBUTE_TABLE);
        editable = BooleanUtils.toBoolean(attributes.get(Constants.ATTRIBUTE_EDITABLE), true);
        mode = (String) attributes.get(Constants.ATTRIBUTE_MODE);
        view = (String) attributes.get(Constants.ATTRIBUTE_VIEW);
        linkBase = (String) attributes.get(Constants.ATTRIBUTE_LINK_BASE);
        linkTarget = (String) attributes.get(Constants.ATTRIBUTE_LINK_TARGET);
        showFormulas = BooleanUtils.toBoolean(attributes.get(Constants.ATTRIBUTE_SHOW_FORMULAS));
        collapseProps = BooleanUtils.toBoolean(attributes.get(Constants.ATTRIBUTE_COLLAPSE_PROPS));
        castToFilters(component.getAttributes().get(Constants.ATTRIBUTE_FILTERS));
        beforeSaveAction = FacesUtils.getValueExpressionString(component, Constants.ATTRIBUTE_BEFORE_SAVE_ACTION);
        afterSaveAction = FacesUtils.getValueExpressionString(component, Constants.ATTRIBUTE_AFTER_SAVE_ACTION);
        saveFailureAction = FacesUtils.getValueExpressionString(component, Constants.ATTRIBUTE_SAVE_FAILURE_ACTION);
        onBeforeSave = (String) attributes.get(Constants.ATTRIBUTE_ON_BEFORE_SAVE);
        onSaveFailure = (String) attributes.get(Constants.ATTRIBUTE_ON_SAVE_FAILURE);
        onAfterSave = (String) attributes.get(Constants.ATTRIBUTE_ON_AFTER_SAVE);
        excludeScripts = (String) attributes.get(Constants.ATTRIBUTE_EXCLUDE_SCRIPTS);
    }

    private void castToFilters(Object filtersParam) {
        if (filtersParam == null) {
            filters = null;
        } else if (filtersParam instanceof IGridFilter[]) {
            filters = (IGridFilter[]) filtersParam;
        } else if (filtersParam instanceof IGridFilter) {
            filters = new IGridFilter[] { (IGridFilter) filtersParam };
        } else if (filtersParam instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<IGridFilter> collection = (Collection<IGridFilter>) filtersParam;
            filters = collection.toArray(new IGridFilter[collection.size()]);
        } else {
            throw new IllegalArgumentException(String.format("Unsupported type of parameter \"%s\"",
                    Constants.ATTRIBUTE_FILTERS));
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public IOpenLTable getTable() {
        return table;
    }

    public void setTable(IOpenLTable table) {
        this.table = table;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public boolean isShowFormulas() {
        return showFormulas;
    }

    public void setShowFormulas(boolean showFormulas) {
        this.showFormulas = showFormulas;
    }

    public boolean isCollapseProps() {
        return collapseProps;
    }

    public void setCollapseProps(boolean collapseProps) {
        this.collapseProps = collapseProps;
    }

    public IGridFilter[] getFilters() {
        return filters;
    }

    public void setFilters(IGridFilter[] filters) {
        this.filters = filters;
    }

    public String getBeforeSaveAction() {
        return beforeSaveAction;
    }

    public void setBeforeSaveAction(String beforeSaveAction) {
        this.beforeSaveAction = beforeSaveAction;
    }

    public String getAfterSaveAction() {
        return afterSaveAction;
    }

    public void setAfterSaveAction(String afterSaveAction) {
        this.afterSaveAction = afterSaveAction;
    }

    public String getOnBeforeSave() {
        return onBeforeSave;
    }

    public void setOnBeforeSave(String onBeforeSave) {
        this.onBeforeSave = onBeforeSave;
    }

    public String getOnAfterSave() {
        return onAfterSave;
    }

    public void setOnAfterSave(String onAfterSave) {
        this.onAfterSave = onAfterSave;
    }

    public void setExcludeScripts(String excludeScripts) {
        this.excludeScripts = excludeScripts;
    }

    public String getExcludeScripts() {
        return excludeScripts;
    }

    public String getSaveFailureAction() {
        return saveFailureAction;
    }

    public void setSaveFailureAction(String saveFailureAction) {
        this.saveFailureAction = saveFailureAction;
    }

    public String getOnSaveFailure() {
        return onSaveFailure;
    }

    public void setOnSaveFailure(String onSaveFailure) {
        this.onSaveFailure = onSaveFailure;
    }

    public String getLinkBase() {
        return linkBase;
    }

    public void setLinkBase(String linkBase) {
        this.linkBase = linkBase;
    }

    public String getLinkTarget() {
        return linkTarget;
    }

    public void setLinkTarget(String linkTarget) {
        this.linkTarget = linkTarget;
    }

    public boolean isShowLinks() {
        return linkBase != null;
    }

}
