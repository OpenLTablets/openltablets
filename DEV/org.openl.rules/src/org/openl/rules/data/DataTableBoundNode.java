/*
 * Created on Oct 7, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.data;

import org.openl.binding.IBindingContext;
import org.openl.binding.IBoundNode;
import org.openl.binding.IMemberBoundNode;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.exception.OpenLRuntimeException;
import org.openl.rules.lang.xls.binding.ATableBoundNode;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.types.IOpenClass;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 *
 */
public class DataTableBoundNode extends ATableBoundNode implements IMemberBoundNode {

    private DataOpenField field;
    private XlsModuleOpenClass module;
    private ITable table;

    public DataTableBoundNode(TableSyntaxNode tableSyntaxNode, XlsModuleOpenClass module) {
        super(tableSyntaxNode, new IBoundNode[0]);
        
        this.module = module;
    }

    public DataOpenField getField() {
        return field;
    }

    public ITable getTable() {
        return table;
    }

    public IOpenClass getType() {
        return field.getType();
    }

    public void setTable(ITable table) {
        this.table = table;
    }

    public Object evaluateRuntime(IRuntimeEnv env) throws OpenLRuntimeException {
        return null;
    }

    public void addTo(ModuleOpenClass openClass) {
        
        TableSyntaxNode tableSyntaxNode = getTableSyntaxNode();
      
        field = new DataOpenField(table, tableSyntaxNode, openClass);
        openClass.addField(field);
        tableSyntaxNode.setMember(field);
    }

    public void finalizeBind(IBindingContext cxt) throws Exception {
        table.populate(module.getDataBase(), cxt);
    }

    public void removeDebugInformation(IBindingContext cxt) throws Exception {
        field.setTable(null);
    }

}
