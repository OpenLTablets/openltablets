/*
 * Created on Oct 23, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.lang.xls.binding;

import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.syntax.exception.SyntaxNodeException;

/**
 * @author snshor
 */
public class DuplicatedTableException extends SyntaxNodeException {

    private static final long serialVersionUID = -6269440215951548170L;

    private TableSyntaxNode existingTable;

    public DuplicatedTableException(String tableName, TableSyntaxNode existingTable, TableSyntaxNode duplicatedTable) {
        super("The table already exists: " + tableName, null, duplicatedTable);
        this.existingTable = existingTable;
    }

    public TableSyntaxNode getDuplicatedTable() {
        return (TableSyntaxNode) getSyntaxNode();
    }

    public TableSyntaxNode getExistingTable() {
        return existingTable;
    }

}
