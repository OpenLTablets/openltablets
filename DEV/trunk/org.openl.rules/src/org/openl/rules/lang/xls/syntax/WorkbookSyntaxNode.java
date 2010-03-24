package org.openl.rules.lang.xls.syntax;

import java.util.ArrayList;
import java.util.List;

import org.openl.rules.lang.xls.ITableNodeTypes;
import org.openl.rules.lang.xls.XlsWorkbookSourceCodeModule;
import org.openl.syntax.impl.NaryNode;

public class WorkbookSyntaxNode  extends NaryNode {

    public WorkbookSyntaxNode(WorksheetSyntaxNode[] nodes, XlsWorkbookSourceCodeModule module) {
        super(ITableNodeTypes.XLS_WORKBOOK, null, nodes, module);
    }
    
    public TableSyntaxNode[] getTableSyntaxNodes()
    {
        List<TableSyntaxNode> tnodes = new ArrayList<TableSyntaxNode>();
        
        WorksheetSyntaxNode[] wnodes = getWorksheetSyntaxNodes();
        
        for (int i = 0; i < wnodes.length; i++) {
            TableSyntaxNode[] tt = wnodes[i].getTableSyntaxNodes();
            for (int j = 0; j < tt.length; j++) {
                tnodes.add(tt[j]);
            }
            
        }
        
        return tnodes.toArray(new TableSyntaxNode[0]);
    }
    
    public WorksheetSyntaxNode[] getWorksheetSyntaxNodes()
    {
        return (WorksheetSyntaxNode[])getNodes();
    }
}
