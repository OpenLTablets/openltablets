package org.openl.rules.dt;

import org.openl.rules.table.IGridTable;
import org.openl.rules.table.CoordinatesTransformer;
import org.openl.rules.table.Point;

/**
 * Transforms coordinates for table with two dimensions(table that has vertical
 * and horizontal conditions).
 * 
 * @author PUdalau
 */
public class TwoDimensionDecisionTableTranformer implements CoordinatesTransformer {
    private static final int CONDITION_HEADERS_HEIGHT = 4;
    private static final int HCONDITION_HEADERS_HEIGHT = 3;
    private int conditionsWidth;
    private int hConditionsCount;
    private int lookupValuesTableHeight;
    private int lookupValuesTableWidth;
    private int retTableWidth;

    private int DT_HEADER_HEIGHT;
    
    /**
     * @param conditionsCount Vertical conditions count.
     * @param hConditionsCount Horizontal conditions count.
     * @param lookupValuesTableHeight Height of "values subtable" == vertical
     *            condition values height.
     * @param lookupValuesTableWidth Width of "values subtable" == vertical
     *            condition values width.
     */
    public TwoDimensionDecisionTableTranformer(int conditionsCount, int hConditionsCount, int lookupValuesTableHeight,
            int lookupValuesTableWidth, int retTableWidth) {
        this.conditionsWidth = conditionsCount;
        this.hConditionsCount = hConditionsCount;
        this.lookupValuesTableHeight = lookupValuesTableHeight;
        this.lookupValuesTableWidth = lookupValuesTableWidth;
        this.retTableWidth = retTableWidth;
        this.DT_HEADER_HEIGHT = CONDITION_HEADERS_HEIGHT+(hConditionsCount-1);
    }

    /**
     * @param entireTable The entire table with two dimensions(WITHOUT a
     *            header).
     * @param lookupValuesTable The "values subtable"
     */
    public TwoDimensionDecisionTableTranformer(IGridTable entireTable, IGridTable lookupValuesTable, IGridTable retTable) {
        lookupValuesTableHeight = lookupValuesTable.getGridHeight();
        lookupValuesTableWidth = lookupValuesTable.getGridWidth();
        this.conditionsWidth = entireTable.getGridWidth() - lookupValuesTableWidth;
        this.hConditionsCount = entireTable.getGridHeight() - lookupValuesTableHeight - HCONDITION_HEADERS_HEIGHT;
        this.retTableWidth = retTable.getGridWidth();
        this.DT_HEADER_HEIGHT = CONDITION_HEADERS_HEIGHT+(hConditionsCount-1);
    }

    public Point calculateCoordinates(int column, int row) {
        if (row < DT_HEADER_HEIGHT) {
            return getCoordinatesFromConditionHeaders(column, row);
        }
        if (column < conditionsWidth) {
            return getCoordinatesFromConditionValues(column, row);
        }
        if (column < conditionsWidth + hConditionsCount) {
            return getCoordinatesFromHConditionValues(column, row);
        }
        return getCoordinatesFromLookupValues(column, row);
    }

    private Point getCoordinatesFromConditionHeaders(int column, int row) {
        return new Point(column, row);
    }

    private Point getCoordinatesFromConditionValues(int column, int row) {
        int conditionValueIndex = (row - DT_HEADER_HEIGHT) % lookupValuesTableHeight;
        return new Point(column, DT_HEADER_HEIGHT + conditionValueIndex);
    }

    private Point getCoordinatesFromHConditionValues(int column, int row) {
        int hConditionIndex = column - conditionsWidth;
        int hConditionValueIndex = (row - DT_HEADER_HEIGHT) / lookupValuesTableHeight * retTableWidth;
        return new Point(conditionsWidth + hConditionValueIndex , HCONDITION_HEADERS_HEIGHT + hConditionIndex);
    }

    private Point getCoordinatesFromLookupValues(int column, int row) {
        int conditionValueIndex = (row - DT_HEADER_HEIGHT) % lookupValuesTableHeight;
        int hConditionValueIndex = (row - DT_HEADER_HEIGHT) / lookupValuesTableHeight* retTableWidth;
        return new Point(conditionsWidth + hConditionValueIndex + (column - conditionsWidth - hConditionsCount), HCONDITION_HEADERS_HEIGHT + hConditionsCount
                + conditionValueIndex);
    }

    public int getHeight() {
        return DT_HEADER_HEIGHT + lookupValuesTableWidth / retTableWidth * lookupValuesTableHeight;
    }

    public int getWidth() {
        return conditionsWidth + hConditionsCount + retTableWidth;
    }
}
