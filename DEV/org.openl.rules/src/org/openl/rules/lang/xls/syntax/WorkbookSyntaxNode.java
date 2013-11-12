package org.openl.rules.lang.xls.syntax;

import java.util.ArrayList;
import java.util.List;

import org.openl.rules.lang.xls.XlsNodeTypes;
import org.openl.rules.lang.xls.XlsWorkbookSourceCodeModule;
import org.openl.syntax.impl.NaryNode;

public class WorkbookSyntaxNode extends NaryNode {

    private TableSyntaxNode[] mergedTableParts;

	public WorkbookSyntaxNode(WorksheetSyntaxNode[] nodes, TableSyntaxNode[] mergedTableParts, XlsWorkbookSourceCodeModule module) {
        super(XlsNodeTypes.XLS_WORKBOOK.toString(), null, nodes, module);
        this.mergedTableParts = mergedTableParts;
    }
    
    public TableSyntaxNode[] getTableSyntaxNodes() {
        List<TableSyntaxNode> tnodes = new ArrayList<TableSyntaxNode>();               
        WorksheetSyntaxNode[] sheetNodes = getWorksheetSyntaxNodes();
        
        for (WorksheetSyntaxNode sheetNode :  sheetNodes) {
            TableSyntaxNode[] tableSyntaxNodes = sheetNode.getTableSyntaxNodes();
            for (TableSyntaxNode tsn : tableSyntaxNodes) {
                tnodes.add(tsn);
            }
        }        
        
        for (TableSyntaxNode tnode : mergedTableParts) {
			tnodes.add(tnode);
		}
        return tnodes.toArray(new TableSyntaxNode[0]);
    }
    
    public XlsWorkbookSourceCodeModule getWorkbookSourceCodeModule() {
        return (XlsWorkbookSourceCodeModule) getModule();
    }
    
    public WorksheetSyntaxNode[] getWorksheetSyntaxNodes() {
        return (WorksheetSyntaxNode[])getNodes();
    }
}
