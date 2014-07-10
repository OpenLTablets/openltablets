/*
 * Created on Oct 23, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.data;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

import org.openl.OpenL;
import org.openl.meta.StringValue;
import org.openl.rules.OpenlToolAdaptor;
import org.openl.rules.binding.RuleRowHelper;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.LogicalTableHelper;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.impl.IdentifierNode;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 * 
 */
public class ColumnDescriptor {

    private IOpenField field;
    private StringValue displayValue;
    private OpenL openl;
    private boolean valuesAnArray = false;
    
    /**
     * Flag indicating that current column descriptor is a constructor.<br>
     * See {@link DataTableBindHelper#CONSTRUCTOR_FIELD}. 
     */
    private boolean constructor = false;    

    private Map<String, Integer> uniqueIndex = null;
    private Map<String, Integer> formattedUniqueIndex = null;
    private IdentifierNode[] fieldChainTokens;

    public ColumnDescriptor(IOpenField field, StringValue displayValue, OpenL openl, boolean constructor, IdentifierNode[] fieldChainTokens) {
        this.field = field;
        this.displayValue = displayValue;
        this.openl = openl;
        this.constructor = constructor;
        this.fieldChainTokens = fieldChainTokens;
        if (field != null)
            this.valuesAnArray = isValuesAnArray(field.getType());
    }

    protected IRuntimeEnv getRuntimeEnv() {
        return openl.getVm().getRuntimeEnv();
    }

    /**
     * Checks if type values are represented as array of elements.
     * 
     * @param paramType Parameter type.
     * @return
     */
    protected static boolean isValuesAnArray(IOpenClass paramType) {
        return paramType.getAggregateInfo().isAggregate(paramType);
    }

    protected IOpenField getField() {
        return field;
    }

    public Object getColumnValue(Object target) {
        return field == null ? target : field.get(target, getRuntimeEnv());
    }

    public String getDisplayName() {
        return displayValue.getValue();
    }

    /**
     * Method is using to load data. Is used when data table is represents
     * <b>AS</b> a constructor (see {@link #isConstructor()}).
     * @throws SyntaxNodeException 
     */
    public Object getLiteral(IOpenClass paramType, ILogicalTable valuesTable, OpenlToolAdaptor ota) throws SyntaxNodeException  {
        Object resultLiteral = null;
        boolean valuesAnArray = isValuesAnArray(paramType);

        if (valuesAnArray) {
            paramType = paramType.getAggregateInfo().getComponentType(paramType);
        }

        valuesTable = LogicalTableHelper.make1ColumnTable(valuesTable);

        if (!valuesAnArray) {
            resultLiteral = RuleRowHelper.loadSingleParam(paramType,
                field == null ? RuleRowHelper.CONSTRUCTOR : field.getName(),
                null,
                valuesTable,
                ota);
        }

        return resultLiteral;
    }

    public String getName() {
        return field == null ? "this" : field.getName();
    }

    public IOpenClass getType() {
        return field.getType();
    }

    public synchronized Map<String, Integer> getUniqueIndex(ITable table, int idx) throws SyntaxNodeException {
        if (uniqueIndex == null) {
            uniqueIndex = table.makeUniqueIndex(idx);
        }
        return uniqueIndex;
    }

    public synchronized Map<String, Integer> getFormattedUniqueIndex(ITable table, int idx) throws SyntaxNodeException {
        if (formattedUniqueIndex == null) {
            formattedUniqueIndex = table.makeFormattedUniqueIndex(idx);
        }
        return formattedUniqueIndex;
    }

    public boolean isConstructor() {        
        return constructor;
    }

    public IdentifierNode[] getFieldChainTokens() {
        return fieldChainTokens;
    }

    /**
     * Method is using to load data. Is used when data table is represents as
     * <b>NOT</b> a constructor (see {@link #isConstructor()}). Support loading
     * single value, array of values.
     * @throws SyntaxNodeException 
     */
    public void populateLiteral(Object literal, ILogicalTable valuesTable, OpenlToolAdaptor toolAdapter) throws SyntaxNodeException {        
        if (field != null) {
            IOpenClass paramType = field.getType();

            if (valuesAnArray) {
                paramType = paramType.getAggregateInfo().getComponentType(paramType);
            }

            valuesTable = LogicalTableHelper.make1ColumnTable(valuesTable);

            if (!valuesAnArray) {
                Object res = RuleRowHelper.loadSingleParam(paramType, field.getName(), null, valuesTable, toolAdapter);

                if (res != null) {
                    field.set(literal, res, getRuntimeEnv());
                }
            } else {
                Object arrayValues = getArrayValues(valuesTable, toolAdapter, paramType);
                field.set(literal, arrayValues, getRuntimeEnv());
            }
        } else {
            /**
             * field == null, in this case don`t do anything. The appropriate information why it is null would have been
             * processed during prepDaring column descriptor. 
             * See {@link DataTableBindHelper#makeDescriptors(IBindingContext bindingContext, ITable table, IOpenClass type,
             * OpenL openl, ILogicalTable descriptorRows, ILogicalTable dataWithTitleRows, boolean hasForeignKeysRow,
             * boolean hasColumnTytleRow)}
             */
        }
    }

    public boolean isReference() {
        return false;
    }

    private Object getArrayValues(ILogicalTable valuesTable, OpenlToolAdaptor ota, IOpenClass paramType)
        throws SyntaxNodeException {

        if (valuesTable.getHeight() == 1 && valuesTable.getWidth() == 1) {
            return RuleRowHelper.loadCommaSeparatedParam(paramType, field.getName(), null, valuesTable.getRow(0), ota);
        }

        if (valuesTable.getHeight() != 1) {
            valuesTable.transpose();
        }

        return loadMultiRowArray(valuesTable, ota, paramType);
    }

    private Object loadMultiRowArray(ILogicalTable logicalTable, OpenlToolAdaptor openlAdaptor, IOpenClass paramType)
        throws SyntaxNodeException {

        // get height of table without empty cells at the end
        //
        int valuesTableHeight = RuleRowHelper.calculateHeight(logicalTable);/*logicalTable.getHeight();*/
        ArrayList<Object> values = new ArrayList<Object>(valuesTableHeight);

        for (int i = 0; i < valuesTableHeight; i++) {

            Object res = RuleRowHelper.loadSingleParam(paramType,
                field.getName(),
                null,
                logicalTable.getRow(i),
                openlAdaptor);

            // Change request: null value cells should be loaded into array as a
            // null value elements.
            //
            if (res == null) {
                res = paramType.nullObject();
            }
            
            values.add(res);
        }

        Object arrayValues = paramType.getAggregateInfo().makeIndexedAggregate(paramType, new int[] { values.size() });

        for (int i = 0; i < values.size(); i++) {
            Array.set(arrayValues, i, values.get(i));
        }

        return arrayValues;
    }

}
