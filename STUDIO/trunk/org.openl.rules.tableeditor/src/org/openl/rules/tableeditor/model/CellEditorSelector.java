package org.openl.rules.tableeditor.model;

import java.util.Date;

import org.apache.commons.lang.ClassUtils;
import org.openl.domain.EnumDomain;
import org.openl.domain.IDomain;
import org.openl.rules.lang.xls.types.CellMetaInfo;
import org.openl.rules.table.ICell;
import org.openl.util.EnumUtils;
import org.openl.util.NumberUtils;
import org.openl.types.IOpenClass;

// TODO Reimplement
public class CellEditorSelector {

    private ICellEditorFactory factory = new CellEditorFactory();

    public ICellEditor selectEditor(ICell cell) {
        if (cell != null && cell.getFormula() != null) {
            return factory.makeFormulaEditor();
        }
        CellMetaInfo cellMetaInfo = cell.getMetaInfo();
        ICellEditor editor = selectEditor(cellMetaInfo);
        return editor == null ? defaultEditor(cell) : editor;
    }

    private ICellEditor selectEditor(CellMetaInfo meta) {
        ICellEditor result = null;
        IOpenClass dataType = meta == null ? null : meta.getDataType();
        if (dataType != null) {
            IDomain<?> domain = dataType.getDomain();
            Class<?> instanceClass = dataType.getInstanceClass();

            if (domain instanceof EnumDomain) {
                Object[] allObjects = ((EnumDomain<?>) domain).getEnum().getAllObjects();
                
                if (allObjects instanceof String[]) {
                    if (meta.isMultiValue()) {
                        return factory.makeMultiSelectEditor((String[]) allObjects);
                    } else {
                        return factory.makeComboboxEditor((String[]) allObjects);
                    }
                }
            }
            
            // Numeric
            if (ClassUtils.isAssignable(instanceClass, Number.class, true)) {
                if (domain == null) {
                    if (!meta.isMultiValue()) {
                        Number minValue = NumberUtils.getMinValue(instanceClass);
                        Number maxValue = NumberUtils.getMaxValue(instanceClass);
                        result = factory.makeNumericEditor(minValue, maxValue);
                    } else {
                        // Numeric Array
                        return factory.makeArrayEditor(",", ICellEditor.CE_NUMERIC);
                    }
                }

            // Date
            } else if (instanceClass == Date.class) {
                result = factory.makeDateEditor();

            // Boolean
            } else if (ClassUtils.isAssignable(instanceClass, Boolean.class, true)) {
                result = factory.makeBooleanEditor();

            // Enum
            } else if (instanceClass.isEnum()) {
                String[] values = EnumUtils.getNames(instanceClass);
                String[] displayValues = EnumUtils.getValues(instanceClass);

                if (meta.isMultiValue()) {
                    result = factory.makeMultiSelectEditor(values, displayValues);
                } else {
                    result = factory.makeComboboxEditor(values, displayValues);
                }

            }

        }

        return result;
    }

    private ICellEditor defaultEditor(ICell cell) {
        final String cellValue = cell.getStringValue();
        return cellValue != null && cellValue.indexOf('\n') >= 0 ? factory.makeMultilineEditor()
                : factory.makeTextEditor();
    }

}
