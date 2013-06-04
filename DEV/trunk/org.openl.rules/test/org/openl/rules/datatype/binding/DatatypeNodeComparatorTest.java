package org.openl.rules.datatype.binding;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openl.exception.OpenLCompilationException;
import org.openl.rules.BaseOpenlBuilderHelper;
import org.openl.rules.lang.xls.XlsNodeTypes;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.syntax.impl.IdentifierNode;

/**
 * @author PUdalau
 */
public class DatatypeNodeComparatorTest extends BaseOpenlBuilderHelper {
    private static final String SRC = "test/rules/DatatypeInheritanceTest.xls";

    public DatatypeNodeComparatorTest() {
        super(SRC);
    }

    private TableSyntaxNode findTableSyntaxNodeByDatatypeName(String datatypeName) {
        for (TableSyntaxNode tsn : getTableSyntaxNodes()) {
            if (XlsNodeTypes.XLS_DATATYPE.equals(tsn.getNodeType())) {
                ILogicalTable table = tsn.getTable();
                IOpenSourceCodeModule src = new GridCellSourceCodeModule(table.getSource());
                
                try {
                    IdentifierNode[] parsedHeader = DatatypeHelper.tokenizeHeader(src);
                    if (parsedHeader[DatatypeNodeBinder.TYPE_INDEX].getIdentifier().equals(datatypeName)) {
                        return tsn;
                    }
                } catch (OpenLCompilationException e) {
                }
            }
        }
        return null;
    }

    @Test
    public void testComparing() throws OpenLCompilationException {

        TableSyntaxNode parent = findTableSyntaxNodeByDatatypeName("ParentType");
        TableSyntaxNode child = findTableSyntaxNodeByDatatypeName("ChildType");
        TableSyntaxNode secondLevelChild = findTableSyntaxNodeByDatatypeName("SecondLevelChildType");
        TableSyntaxNode warnChild = findTableSyntaxNodeByDatatypeName("WarnChild");
        TableSyntaxNode errorChild = findTableSyntaxNodeByDatatypeName("ErrorChild");
        
        Map<String, TableSyntaxNode> map = new HashMap<String, TableSyntaxNode>();
        map.put("ParentType", parent);
        map.put("ChildType", child);
        map.put("SecondLevelChildType", secondLevelChild);
        map.put("WarnChild", warnChild);
        map.put("ErrorChild", errorChild);
        
        int parentLevel = DatatypeHelper.getInheritanceLevel(map, parent);
        int childLevel = DatatypeHelper.getInheritanceLevel(map, child);
        int secondLevelChildLevel = DatatypeHelper.getInheritanceLevel(map, secondLevelChild);
        int warnChildLevel = DatatypeHelper.getInheritanceLevel(map, warnChild);
        int errorChildLevel = DatatypeHelper.getInheritanceLevel(map, errorChild);
        
        assertTrue(parentLevel - secondLevelChildLevel < 0);
        assertTrue(childLevel - errorChildLevel == 0);
        assertTrue(parentLevel - warnChildLevel < 0);
        assertTrue(childLevel - secondLevelChildLevel < 0);
        assertTrue(secondLevelChildLevel - errorChildLevel > 0);
    }

}
