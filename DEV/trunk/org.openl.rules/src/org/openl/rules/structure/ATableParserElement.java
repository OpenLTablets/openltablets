package org.openl.rules.structure;

import org.openl.rules.table.ILogicalTable;
import org.openl.util.ISelector;

public abstract class ATableParserElement implements ITableParserElement {

    String name;

    ISelector<ILogicalTable> selector;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ILogicalTable parse(ILogicalTable unparsedTable, ITableObject tobj) {
        if (selector != null && !selector.select(unparsedTable))
            return unparsedTable;
        return parseInternal(unparsedTable, tobj);
    }

    protected abstract ILogicalTable parseInternal(ILogicalTable unparsedTable, ITableObject tobj);

}
