package org.openl.rules.structure;

import org.openl.OpenL;
import org.openl.binding.IBindingContext;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.IMemberBoundNode;
import org.openl.engine.OpenLManager;
import org.openl.rules.lang.xls.IXlsTableNames;
import org.openl.rules.lang.xls.binding.AXlsTableBinder;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.method.binding.MethodTableBoundNode;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.source.impl.SubTextSourceCodeModule;
import org.openl.types.IOpenMethodHeader;

public class StructureTableNodeBinder extends AXlsTableBinder implements IXlsTableNames {
    @Override
    public IMemberBoundNode preBind(TableSyntaxNode tsn, OpenL openl, IBindingContext cxt, XlsModuleOpenClass module) {

        IGridTable table = tsn.getTable().getGridTable();

        IOpenSourceCodeModule src = new GridCellSourceCodeModule(table);

        IOpenMethodHeader header = OpenLManager.makeMethodHeader(openl, new SubTextSourceCodeModule(src, tsn.getHeader()
                .getHeaderToken().getIdentifier().length()), (IBindingContextDelegator) cxt);

        return new MethodTableBoundNode(tsn, openl, header, module);
    }

}
