package org.openl.rules.webstudio.web;

import org.apache.commons.lang3.StringUtils;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.util.ASelector;

class TableHeaderSelector extends ASelector<TableSyntaxNode> {

    private final String value;

    public TableHeaderSelector(String value) {
        this.value = value;
    }

    @Override
    public boolean select(TableSyntaxNode node) {
        if (StringUtils.isBlank(value)) {
            return true;
        }

        String header = node.getHeaderLineValue().getValue();

        return StringUtils.containsIgnoreCase(header, value);
    }

}
