package org.openl.rules.lang.xls.syntax;

import java.util.List;

import org.openl.message.OpenLMessage;
import org.openl.message.OpenLMessagesUtils;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.ITable;
import org.openl.rules.table.properties.ITableProperties;
import org.openl.syntax.exception.SyntaxNodeException;

public class TableSyntaxNodeAdapter implements ITable {

    private TableSyntaxNode tsn;

    public TableSyntaxNodeAdapter(TableSyntaxNode tsn) {
        if (tsn == null) {
            throw new IllegalArgumentException("TableSyntaxNode is null");
        }
        this.tsn = tsn;
    }

    public IGridTable getGridTable() {
        return tsn.getTable().getGridTable();
    }

    public IGridTable getGridTable(String view) {
        if (view != null) {
            ILogicalTable gtx = tsn.getSubTables().get(view);
            if (gtx != null) {
                return gtx.getGridTable();
            }
        }
        return getGridTable();
    }

    public ITableProperties getProperties() {
        return tsn.getTableProperties();
    }

    public String getType() {
        return tsn.getType();
    }

    public List<OpenLMessage> getMessages() {
        SyntaxNodeException[] errors = tsn.getErrors();
        return OpenLMessagesUtils.newMessages(errors);
    }

}
