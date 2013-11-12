package org.openl.rules.dt.element;

import org.openl.rules.dt.IDecisionTableConstants;
import org.openl.rules.table.ILogicalTable;

public class RuleRow {
    
    private int row;
    private ILogicalTable table;

    public RuleRow(int row, ILogicalTable table) {
        this.row = row;
        this.table = table;
    }

    public String getRuleName(int col) {
        return getValueCell(col).getSource().getCell(0, 0).getStringValue();
    }

    private ILogicalTable getValueCell(int col) {
        return table.getSubtable(col + IDecisionTableConstants.SERVICE_COLUMNS_NUMBER, row, 1, 1);
    }

}
