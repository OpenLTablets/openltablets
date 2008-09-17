package org.openl.rules.ui.tablewizard;

import javax.faces.model.SelectItem;

/**
 * @author Aliaksandr Antonik.
 */
public class ConditionClause {
    private final TableCondition tableCondition;
    /**
     * If there is condition <i>t.equals(driver.age)</i> then
     * <ul>
     * <li><b><i>t</i></b> - is paramName</li>
     * <li><b><i>.equals</i></b> - is identified by variantId</li>
     * <li><b><i>driver.age</i></b> - is conditionExpression</li>
     * </ul>
     *
     */
    private String paramName;
    private long variantId;
    private String conditionExpression;

    ConditionClause(TableCondition tableCondition) {
        this.tableCondition = tableCondition;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public long getVariantId() {
        return variantId;
    }

    public void setVariantId(long variantId) {
        this.variantId = variantId;
    }

    public String getType() {
        Parameter p = tableCondition.getParameterByName(paramName);
        return p == null ? null : p.getType();
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public SelectItem[] getVariantOptions() {
        SelectItem[] ret = null;

        String type = getType();
        if (type != null)
            ret = ConditionClauseRegistry.getInstance().getItemsByType(type);

        return ret == null ? new SelectItem[0] : ret; 
    }
}
