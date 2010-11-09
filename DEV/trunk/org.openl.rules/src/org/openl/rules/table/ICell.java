package org.openl.rules.table;

import java.util.Date;

import org.openl.rules.lang.xls.types.CellMetaInfo;
import org.openl.rules.table.ui.ICellFont;
import org.openl.rules.table.ui.ICellStyle;
import org.openl.rules.table.xls.IncorrectFormulaException;

public interface ICell {

    String ERROR_VALUE = "#ERROR";

    int getRow();

    int getColumn();

    /**
     * @return Absolute row index inside the sheet.
     */
    int getAbsoluteRow();

    /**
     * @return Absolute column index inside the sheet.
     */
    int getAbsoluteColumn();
    
    /**
     * @return Absolute region of cell inside the sheet.
     */
    IGridRegion getAbsoluteRegion();

    int getWidth();

    int getHeight();

    ICellStyle getStyle();
    
    /**
     * @throws IncorrectFormulaException  <br> Be careful!! When trying to evaluate
     *  an incorrect formula, throws exception.
     */
    Object getObjectValue();

    String getStringValue();
    
    // TODO: move this method to ICellStyle
    ICellFont getFont();
    
    /**
     * 
     * @return grid region, if cell belongs to any merged region. In other cases <code>null</code>.
     */
    IGridRegion getRegion();

    String getFormula();

    int getType();

    String getUri();
    
    
    // used for optimized access
    
    /**
     *  @return true if the cell has ability to provide fast access to the native value(cached)
     *  If cell has not such an ability, the native methods should not be used
     */
    
    boolean hasNativeType();
    
    
    /**
     * 
     * @return IGrid.CELL_TYPE... constant, in case of CELL_TYPE_FORMULA returns cached value type
     */
    int getNativeType();
    
    double getNativeNumber();
    boolean getNativeBoolean();

    Date getNativeDate();

    CellMetaInfo getMetaInfo();

    ICellComment getComment();

}
