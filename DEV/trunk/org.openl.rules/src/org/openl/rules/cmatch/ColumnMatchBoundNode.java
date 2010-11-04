package org.openl.rules.cmatch;

import org.openl.OpenL;
import org.openl.binding.BindingDependencies;
import org.openl.binding.IBindingContext;
import org.openl.binding.IMemberBoundNode;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.rules.lang.xls.IXlsTableNames;
import org.openl.rules.lang.xls.binding.AMethodBasedNode;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.ILogicalTable;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenMethodHeader;

public class ColumnMatchBoundNode extends AMethodBasedNode implements IMemberBoundNode {
    private final IOpenSourceCodeModule nameOfAlgorithm;

    public ColumnMatchBoundNode(TableSyntaxNode tsn, OpenL openl, IOpenMethodHeader header, ModuleOpenClass module,
            IOpenSourceCodeModule nameOfAlgorithm) {
        super(tsn, openl, header, module);

        this.nameOfAlgorithm = nameOfAlgorithm;
    }

    @Override
    protected IOpenMethod createMethodShell() {
        return new ColumnMatch(getHeader(), this);
    }

    public void finalizeBind(IBindingContext cxt) throws Exception {
        ColumnMatchBuilder builder = new ColumnMatchBuilder(cxt, getColumnMatch(), getTableSyntaxNode());
        ILogicalTable tableBody = getTableSyntaxNode().getTableBody();
        builder.build(tableBody);
        getTableSyntaxNode().getSubTables().put(IXlsTableNames.VIEW_BUSINESS, tableBody.getRows(1));
        if (cxt.isExecutionMode()) {
            getColumnMatch().setBoundNode(null);
        }
    }

    public IOpenSourceCodeModule getAlgorithm() {
        return nameOfAlgorithm;
    }

    public ColumnMatch getColumnMatch() {
        return (ColumnMatch) getMethod();
    }
    
    @Override
    public void updateDependency(BindingDependencies dependencies) {
        //seems column match can`t call other methods in its body.
    }
}
