/**
 * Created Mar 1, 2007
 */
package org.openl.rules.table.ui;

public abstract class AGridFilter implements IGridFilter {
    IGridSelector selector;

    public AGridFilter() {
    }

    public AGridFilter(IGridSelector selector) {
        this.selector = selector;
    }

    public IGridSelector getGridSelector() {
        return selector;
    }

    public Object parse(String value) {
        throw new UnsupportedOperationException("This format does not parse");
    }
}