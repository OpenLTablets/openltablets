package org.openl.rules.data;

import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.types.IDynamicObject;
import org.openl.types.IOpenClass;
import org.openl.types.impl.AOpenField;
import org.openl.types.impl.DelegatedDynamicObject;
import org.openl.types.impl.DynamicObject;
import org.openl.vm.IRuntimeEnv;

public class DataOpenField extends AOpenField {

    private ITable table;
    private Object data;
    private ModuleOpenClass declaringClass;

    public DataOpenField(ITable table, TableSyntaxNode tableSyntaxNode, ModuleOpenClass declaringClass) {

        super(table.getDataModel().getName(), table.getDataModel()
            .getType()
            .getAggregateInfo()
            .getIndexedAggregateType(table.getDataModel().getType(), 1));
        
        this.table = table;
        data = table.getDataArray();
        this.declaringClass = declaringClass;
    }
    
    @Override
    public IOpenClass getDeclaringClass() {
    	return declaringClass;
    }

    public ITable getTable() {
        return table;
    }

    public Object getData() {
        return data;
    }

    public void setTable(ITable table) {
        this.table = table;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    public Object get(Object target, IRuntimeEnv env) {

        Object data = ((IDynamicObject) target).getFieldValue(getName());

        if (data == null) {
            data = this.data;

            //if target is spreadsheet result we mustn't set value to spreadsheet cell. Because this result isn't final value of a cell
//            if (target instanceof DynamicObject) {
//                boolean isDynamicObject = !(target instanceof DelegatedDynamicObject) 
//                        || ((DelegatedDynamicObject) target).isAssignableFrom(DynamicObject.class);
//                if (isDynamicObject) {
                    ((IDynamicObject) target).setFieldValue(getName(), data);
//                }
//            }
        }

        return data;
    }

    public void set(Object target, Object value, IRuntimeEnv env) {
        ((IDynamicObject) target).setFieldValue(getName(), value);
    }

}
