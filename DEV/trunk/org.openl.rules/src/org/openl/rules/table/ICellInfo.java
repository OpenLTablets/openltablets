package org.openl.rules.table;

import org.openl.rules.table.ui.ICellFont;
import org.openl.rules.table.ui.ICellStyle;

public interface ICellInfo {

    ICellStyle getCellStyle();

    int getColumn();

    ICellFont getFont();

    int getRow();

    /**
     * 
     * @return IGridRegion if the cell is part of the merged cell, null othrwise
     */
    IGridRegion getSurroundingRegion();

    /**
     * 
     * @return true if column is part of the merged region and is in it's
     *         top-left-most corner
     */
    boolean isTopLeft();

    /**
     * @return true if cell contains formula.
     */
    boolean hasFormula();

}
