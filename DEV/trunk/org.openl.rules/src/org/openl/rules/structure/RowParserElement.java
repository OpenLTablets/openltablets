package org.openl.rules.structure;

import org.openl.rules.table.ILogicalTable;

public class RowParserElement extends ATableParserElement {

    protected ILogicalTable parseInternal(ILogicalTable unparsedTable, ITableObject tobj) {
        ILogicalTable row = unparsedTable.getLogicalRow(0);

        tobj.addParsedTable(name, row);
        return unparsedTable.rows(1);
    }

}
