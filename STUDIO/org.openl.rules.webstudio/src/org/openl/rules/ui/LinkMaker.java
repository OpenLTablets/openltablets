package org.openl.rules.ui;

import org.openl.meta.explanation.ExplanationNumberValue;
import org.openl.rules.table.FormattedCell;
import org.openl.rules.table.ui.filters.AGridFilter;

/**
 * @author Yury Molchan
 */
class LinkMaker extends AGridFilter {
    static LinkMaker INSTANCE = new LinkMaker();

    public FormattedCell filterFormat(FormattedCell cell) {
        Object value = cell.getObjectValue();
        if (value instanceof ExplanationNumberValue<?>) {
            int rootID = Explanator.getCurrent().getUniqueId((ExplanationNumberValue<?>) value);
            String url = "javascript: explain(\'?rootID=" + rootID + "')";
            cell.setFormattedValue("<a href=\"" + url + "\">" + cell.getFormattedValue() + "</a>");
        }
        return cell;
    }
}
