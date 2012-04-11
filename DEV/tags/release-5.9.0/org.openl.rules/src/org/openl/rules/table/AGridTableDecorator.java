package org.openl.rules.table;

/**
 * @author snshor
 */
public abstract class AGridTableDecorator extends AGridTable {

    protected IGridTable table;

    public AGridTableDecorator(IGridTable table) {
        this.table = table;
    }

    public IGrid getGrid() {
        return table.getGrid();
    }

    /**
     * @return Original table which includes this delegated table
     */
    public IGridTable getOriginalGridTable() {
        return table;
    }

}
