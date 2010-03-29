package org.openl.rules.cmatch;

import org.openl.OpenL;
import org.openl.binding.IBindingContext;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.IMemberBoundNode;
import org.openl.engine.OpenLManager;
import org.openl.rules.lang.xls.binding.AExecutableNodeBinder;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.source.impl.SubTextSourceCodeModule;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.types.impl.OpenMethodHeader;

public class ColumnMatchNodeBinder extends AExecutableNodeBinder {
    private SubTextSourceCodeModule nameOfAlgorithm;
    private SubTextSourceCodeModule cutNameOfAlgorithm(TableSyntaxNode tsn, IOpenSourceCodeModule src,
            int headerTokenLength) throws SyntaxNodeException {
        String s = src.getCode();

        // parse '<ALGORITHM>' if it exists
        int p2 = s.indexOf('>');
        if (p2 < 0) {
            return null;
        }

        int p1 = s.indexOf('<');
        for (int i = headerTokenLength; i < p1; i++) {
            if (s.charAt(i) != ' ') {
                // illegal character detected
                p1 = -1;
                break;
            }
        }

        if (p1 < 0 || p1 > p2) {
            throw SyntaxNodeExceptionUtils.createError("Illegal header format!", null, tsn);
        }

        return new SubTextSourceCodeModule(src, p1 + 1, p2);
    }

    @Override
    protected OpenMethodHeader createHeader(TableSyntaxNode tsn, OpenL openl, IBindingContext cxt) throws Exception {
        IGridTable table = tsn.getTable().getGridTable();

        IOpenSourceCodeModule src = new GridCellSourceCodeModule(table);

        int headerTokenLength = tsn.getHeader().getHeaderToken().getIdentifier().length();

        nameOfAlgorithm = cutNameOfAlgorithm(tsn, src, headerTokenLength);
        if (nameOfAlgorithm != null) {
            String name = nameOfAlgorithm.getCode();
            // TODO
            // headerTokenLength = name.getEndPosition() + 1;
            headerTokenLength = nameOfAlgorithm.getStartPosition() + name.length() + 1;
        }

        SubTextSourceCodeModule codeModule = new SubTextSourceCodeModule(src, headerTokenLength);
        OpenMethodHeader header = (OpenMethodHeader) OpenLManager.makeMethodHeader(openl, codeModule,
                (IBindingContextDelegator) cxt);
        return header;
    }

    @Override
    protected IMemberBoundNode createNode(TableSyntaxNode tsn, OpenL openl, OpenMethodHeader header,
            XlsModuleOpenClass module) {
        return new ColumnMatchBoundNode(tsn, openl, header, module, nameOfAlgorithm);
    }
}
