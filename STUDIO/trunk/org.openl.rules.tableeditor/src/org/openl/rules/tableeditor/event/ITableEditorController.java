package org.openl.rules.tableeditor.event;

/**
 * This interface contains methods that TableEditor javascript object expects to
 * be present in a bean it communicates with.
 *
 */
public interface ITableEditorController {

    String load() throws Exception;

    String getCellType() throws Exception;

    String insertRowBefore() throws Exception;

    String insertColBefore() throws Exception;

    String removeRow() throws Exception;

    String removeCol() throws Exception;

    String setCellValue() throws Exception;

    String setProp() throws Exception;

    String setAlign() throws Exception;

    String setIndent() throws Exception;

    String saveTable() throws Exception;

    String undo() throws Exception;

    String redo() throws Exception;

}
