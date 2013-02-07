package org.openl.rules.tableeditor.model;

/**
 * @author snshor
 *
 * Provides interface for table editor creator. An implementation can differ in
 * different scenarios It allows to separate an actual implementation from the
 * caller, for example, the call to makeCodeEditor() may return simple
 * multi-line editor for now
 */

public interface ICellEditorFactory {
    /**
     * Creates combobox editor with choices in String array
     *
     * @param choices
     * @return
     */
    ICellEditor makeComboboxEditor(String[] choices);

    ICellEditor makeComboboxEditor(String[] choices, String[] displayValues);

    ICellEditor makeMultiSelectEditor(String[] choices);

    ICellEditor makeMultiSelectEditor(String[] choices, String[] displayValues);

    /**
     * Creates numeric editor with min, max bounds.
     *
     * @param min
     * @param max
     * @return
     */
    ICellEditor makeNumericEditor(Number min, Number max);

    ICellEditor makeNumericEditor();

    ICellEditor makeMultilineEditor();

    ICellEditor makeTextEditor();

    ICellEditor makeFormulaEditor();

    ICellEditor makeDateEditor();

    ICellEditor makeBooleanEditor();

    ICellEditor makeArrayEditor();

    ICellEditor makeArrayEditor(String separator, String entryEditor);
    
    ICellEditor makeNumberRangeEditor(String entryEditor);

}
