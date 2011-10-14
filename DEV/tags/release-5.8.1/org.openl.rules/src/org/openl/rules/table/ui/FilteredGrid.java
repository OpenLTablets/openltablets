/**
 * Created Feb 27, 2007
 */
package org.openl.rules.table.ui;

import org.openl.rules.table.FormattedCell;
import org.openl.rules.table.ICell;
import org.openl.rules.table.IGrid;
import org.openl.rules.table.AGrid;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.ui.filters.IGridFilter;

/**
 * @author snshor
 *
 */
public class FilteredGrid extends AGrid {

    private IGridFilter[] formatFilters;
    
    private IGrid delegate;

    public FilteredGrid(IGrid delegate, IGridFilter[] formatFilters) {     
        this.delegate = delegate;
        this.formatFilters = formatFilters.clone();
    }

    private void formatCell(FormattedCell fcell, int col, int row) {
        if (formatFilters != null) {
            for (int i = 0; i < formatFilters.length; i++) {
                if (formatFilters[i].getGridSelector() == null
                        || formatFilters[i].getGridSelector().selectCoords(col, row)) {
                    try {
                        // Side effect of method call is setting object value of the cell
                        formatFilters[i].filterFormat(fcell);
                    }
                    catch (IllegalArgumentException e){
                        //Ignore if failed to format
                    }
                }
            }
        }
    }

    public ICell getCell(int column, int row) {
        if (isEmpty(column, row)) {
            delegate.getCell(column, row);
        }

        return getFormattedCell(column, row);
    }

    public synchronized FormattedCell getFormattedCell(int col, int row) {
        FormattedCell cellToFormat = new FormattedCell(delegate.getCell(col, row));

        formatCell(cellToFormat, col, row);

        return cellToFormat;
    }

    public int getColumnWidth(int col) {
        return delegate.getColumnWidth(col);
    }

    public int getMaxColumnIndex(int row) {        
        return delegate.getMaxColumnIndex(row);
    }

    public int getMaxRowIndex() {
        return delegate.getMaxRowIndex();
    }

    public IGridRegion getMergedRegion(int i) {
        return delegate.getMergedRegion(i);
    }

    public int getMinColumnIndex(int row) {
        return delegate.getMaxColumnIndex(row);
    }
    
    public int getMinRowIndex() {
        return delegate.getMinRowIndex();
    }

    public int getNumberOfMergedRegions() {
        return delegate.getNumberOfMergedRegions();
    }

    public String getRangeUri(int colStart, int rowStart, int colEnd, int rowEnd) {
        return delegate.getRangeUri(colStart, rowStart, colEnd, rowEnd);
    }

    public IGridRegion getRegionStartingAt(int colFrom, int rowFrom) {
        return delegate.getRegionStartingAt(colFrom, rowFrom);
    }

    public String getUri() {
        return delegate.getUri();
    }

    public boolean isEmpty(int col, int row) {
        return delegate.isEmpty(col, row);
    }

    public boolean isPartOfTheMergedRegion(int col, int row) {
        return delegate.isPartOfTheMergedRegion(col, row);
    }

    public IGridRegion getRegionContaining(int column, int row) {
        return delegate.getRegionContaining(column, row);
    }

}
