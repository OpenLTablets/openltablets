package org.openl.rules.webstudio.web.trace;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openl.meta.DoubleValue;
import org.openl.rules.calc.Spreadsheet;
import org.openl.rules.calc.element.SpreadsheetCell;
import org.openl.rules.webstudio.web.trace.node.SimpleTracerObject;
import org.openl.rules.webstudio.web.trace.node.TracedObjectFactory;

public class TraceFormatterTest {

    @Test
    public void getDisplayNameTest() {
        Spreadsheet spreadsheet = createNodeMock();
        SimpleTracerObject spreadsheetTraceObject = TracedObjectFactory
            .getTracedObject(spreadsheet, null, null, null, null);
        SpreadsheetCell cell = createCellMock();
        SimpleTracerObject leafNode = TracedObjectFactory.getTracedObject(null, cell, null, null, null);
        spreadsheetTraceObject.addChild(leafNode);

        leafNode.setResult(null);
        assertEquals("$Value$Vehicle_Premiums = null", TraceFormatter.getDisplayName(leafNode, false));

        leafNode.setResult(0.95);
        assertEquals("$Value$Vehicle_Premiums = 0.95", TraceFormatter.getDisplayName(leafNode, false));

        leafNode.setResult(1.0E-5);
        assertEquals("$Value$Vehicle_Premiums = 0.00001", TraceFormatter.getDisplayName(leafNode, false));
        assertEquals("$Value$Vehicle_Premiums = 0.00001", TraceFormatter.getDisplayName(leafNode, true));

        leafNode.setResult(0.1 + 0.2);
        assertEquals("$Value$Vehicle_Premiums = 0.30000000000000004", TraceFormatter.getDisplayName(leafNode, false));
        assertEquals("$Value$Vehicle_Premiums = 0.3", TraceFormatter.getDisplayName(leafNode, true));

        leafNode.setResult(new DoubleValue(0.95));
        assertEquals("$Value$Vehicle_Premiums = 0.95", TraceFormatter.getDisplayName(leafNode, false));

        leafNode.setResult(new DoubleValue[] { new DoubleValue(0.95) });
        assertEquals("$Value$Vehicle_Premiums = {0.95}", TraceFormatter.getDisplayName(leafNode, false));

        leafNode.setResult(new DoubleValue[] { new DoubleValue(0.95), new DoubleValue(1.0E-5) , null});
        assertEquals("$Value$Vehicle_Premiums = {0.95,0.00001,null}", TraceFormatter.getDisplayName(leafNode, false));

        leafNode.setResult(new double[][] { { 0.95, 0.55 }, { 1.95, 1.55 } });
        assertEquals("$Value$Vehicle_Premiums = {{0.95,0.55},{1.95,1.55}}", TraceFormatter.getDisplayName(leafNode, false));

        leafNode.setResult(new double[][] { { 1.0E-5, 0.1 + 0.2 }, { 0.1 + 0.2, 1.0E-5 } });
        assertEquals("$Value$Vehicle_Premiums = {{0.00001,0.30000000000000004},{0.30000000000000004,0.00001}}", TraceFormatter.getDisplayName(leafNode, false));
        assertEquals("$Value$Vehicle_Premiums = {{0.00001,0.3},{0.3,0.00001}}", TraceFormatter.getDisplayName(leafNode, true));
    }

    protected SpreadsheetCell createCellMock() {
        SpreadsheetCell cell = mock(SpreadsheetCell.class);
        when(cell.getColumnIndex()).thenReturn(0);
        when(cell.getRowIndex()).thenReturn(0);
        return cell;
    }

    protected Spreadsheet createNodeMock() {
        Spreadsheet spreadsheet = mock(Spreadsheet.class);
        when(spreadsheet.getColumnNames()).thenReturn(new String[] { "Value" });
        when(spreadsheet.getRowNames()).thenReturn(new String[] { "Vehicle_Premiums" });
        return spreadsheet;
    }

}
