package org.openl.rules.datatype.binding;

import org.apache.commons.lang.StringUtils;
import org.openl.OpenL;
import org.openl.binding.IBindingContext;
import org.openl.exception.OpenLCompilationException;
import org.openl.rules.lang.xls.XlsBinder;
import org.openl.rules.lang.xls.XlsNodeTypes;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.syntax.impl.IdentifierNode;
import static org.openl.rules.datatype.binding.DatatypeTableBoundNode.*;

import java.util.*;

/**
 * In the Datatype TableSyntaxNode find the dependent types.
 * There are 2 types of dependencies:
 * 1) inheritance dependency (TypeA extends TypeB)
 * 2) dependency in field declaration (TypeB fieldB)
 *
 * @author Denis Levchuk
 */
public class DependentTypesExtractor {

    public Set<String> extract(TableSyntaxNode node, IBindingContext cxt) {
        ILogicalTable dataPart = DatatypeHelper.getNormalizedDataPartTable(
                node.getTable(),
                OpenL.getInstance(XlsBinder.DEFAULT_OPENL_NAME),
                cxt);

        int tableHeight = 0;

        if (dataPart != null) {
            tableHeight = dataPart.getHeight();
        }

        Set<String> dependencies = new LinkedHashSet<String>();

        // TODO: put this functionality to the current class
        //
        String parentType = getParentDatatypeName(node);
        if (StringUtils.isNotBlank(parentType)) {
            dependencies.add(parentType);
        }

        for (int i = 0; i < tableHeight; i++) {
            ILogicalTable row = dataPart.getRow(i);

            if (canProcessRow(row, cxt)) {
                String typeName = getType(row, cxt);
                if (StringUtils.isNotBlank(typeName)) {
                    dependencies.add(typeName);
                }
            }

        }
        return dependencies;
    }

    private String getParentDatatypeName(TableSyntaxNode tsn) {

        if (XlsNodeTypes.XLS_DATATYPE.equals(tsn.getNodeType())) {
            IOpenSourceCodeModule src = tsn.getHeader().getModule();

            IdentifierNode[] parsedHeader = new IdentifierNode[0];
            try {
                parsedHeader = DatatypeHelper.tokenizeHeader(src);
            } catch (OpenLCompilationException e) {
                // Suppress the exception
                // This exception has already been processed when parsing the table header
                //
            }

            if (parsedHeader.length == 4) {
                return parsedHeader[DatatypeNodeBinder.PARENT_TYPE_INDEX].getIdentifier();
            } else {
                return null;
            }
        }

        return null;
    }

    private String getType(ILogicalTable row, IBindingContext cxt) {
        // Get the cell that has index 0. This cell contains the Type name
        //
        GridCellSourceCodeModule type = getCellSource(row, cxt, 0);
        IdentifierNode[] idn = new IdentifierNode[0];
        try {
            idn = getIdentifierNode(type);
        } catch (OpenLCompilationException e) {
            // Suppress the exception
            //
        }
        if (idn.length == 1) {
            // Return the Type name
            //
            return idn[0].getIdentifier();
        }
        // Alias Datatype don't have Type name
        //
        return null;
    }
}
