package org.openl.rules.calc.trace;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openl.base.INamedThing;
import org.openl.meta.DoubleValue;
import org.openl.rules.calc.Spreadsheet;
import org.openl.rules.calc.element.SpreadsheetCell;

public class SpreadsheetTracerLeafTest {

    @Test
    public void getDisplayNameTest() {
        SpreadsheetTracerLeaf leafNode = new SpreadsheetTracerLeaf(createNodeMock(), createCellMock());

        leafNode.setValue(null);
        assertEquals("$Value$Vehicle_Premiums = null", leafNode.getDisplayName(INamedThing.LONG));

        leafNode.setValue(0.95);
        assertEquals("$Value$Vehicle_Premiums = 0.95", leafNode.getDisplayName(INamedThing.LONG));

        leafNode.setValue(new DoubleValue(0.95));
        assertEquals("$Value$Vehicle_Premiums = 0.95", leafNode.getDisplayName(INamedThing.LONG));

        leafNode.setValue(new DoubleValue[] { new DoubleValue(0.95) });
        assertEquals("$Value$Vehicle_Premiums = {0.95}", leafNode.getDisplayName(INamedThing.LONG));

        leafNode.setValue(new DoubleValue[] { new DoubleValue(0.95), new DoubleValue(0.55) });
        assertEquals("$Value$Vehicle_Premiums = {0.95,0.55}", leafNode.getDisplayName(INamedThing.LONG));

        leafNode.setValue(new double[][] { { 0.95, 0.55 }, { 1.95, 1.55 } });
        assertEquals("$Value$Vehicle_Premiums = {{0.95,0.55},{1.95,1.55}}", leafNode.getDisplayName(INamedThing.LONG));
    }

    protected SpreadsheetCell createCellMock() {
        SpreadsheetCell cell = mock(SpreadsheetCell.class);
        when(cell.getColumnIndex()).thenReturn(0);
        when(cell.getRowIndex()).thenReturn(0);
        return cell;
    }

    protected SpreadsheetTraceObject createNodeMock() {
        Spreadsheet spreadsheet = mock(Spreadsheet.class);
        when(spreadsheet.getColumnNames()).thenReturn(new String[] { "Value" });
        when(spreadsheet.getRowNames()).thenReturn(new String[] { "Vehicle_Premiums" });

        SpreadsheetTraceObject node = mock(SpreadsheetTraceObject.class);
        when(node.getSpreadsheet()).thenReturn(spreadsheet);
        return node;
    }

}
