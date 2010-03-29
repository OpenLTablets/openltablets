package org.openl.rules.calc;

import org.openl.OpenL;
import org.openl.binding.IBindingContext;
import org.openl.binding.IMemberBoundNode;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.rules.lang.xls.IXlsTableNames;
import org.openl.rules.lang.xls.binding.AMethodBasedNode;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.ILogicalTable;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenMethodHeader;

public class SSheetBoundNode extends AMethodBasedNode implements IMemberBoundNode, IXlsTableNames {

    public SSheetBoundNode(TableSyntaxNode tsn, OpenL openl, IOpenMethodHeader header, ModuleOpenClass module) {
        super(tsn, openl, header, module);
    }

    @Override
    protected IOpenMethod createMethodShell() {
        return Spreadsheet.createSpreadsheet(header, this);
    }

    public void finalizeBind(IBindingContext cxt) throws Exception {
        SpreadsheetBuilder builder = new SpreadsheetBuilder(cxt, getSpreadsheet(), getTableSyntaxNode());

        ILogicalTable tableBody = getTableSyntaxNode().getTableBody();
        if (tableBody == null) {
            throw SyntaxNodeExceptionUtils.createError(
                    "Table has no body! Try to merge header cell horizontally to identify table.", 
                    getTableSyntaxNode());
        }
        int h = tableBody.getLogicalHeight();
        int w = tableBody.getLogicalWidth();
        if (h < 2 || w < 2) {
            throw SyntaxNodeExceptionUtils.createError(String.format(
                "Spreadsheet must have at least 2x2 cells! Actual size %dx%d.", w, h), getTableSyntaxNode());
        }

        getTableSyntaxNode().getSubTables().put(VIEW_BUSINESS, tableBody);
        builder.build(tableBody);
    }

    public Spreadsheet getSpreadsheet() {
        return (Spreadsheet) method;
    }

}
