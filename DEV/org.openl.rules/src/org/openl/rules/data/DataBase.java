/*
 * Created on Oct 23, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.data;

import java.util.HashMap;
import java.util.Map;

import org.openl.rules.OpenlToolAdaptor;
import org.openl.rules.lang.xls.binding.DuplicatedTableException;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.ILogicalTable;

/**
 * @author snshor
 * 
 */
public class DataBase implements IDataBase {

    private Map<String, ITable> tables = new HashMap<String, ITable>();

    public synchronized ITable getTable(String name) {
        return tables.get(name);
    }

    public ITable addNewTable(String tableName, TableSyntaxNode tsn) throws DuplicatedTableException {

        ITable table = getTable(tableName);

        if (table != null) {
            throw new DuplicatedTableException(tableName, table.getTableSyntaxNode(), tsn);
        }

        table = makeNewTable(tableName, tsn);
        tables.put(tableName, table);

        return table;
    }

    protected ITable makeNewTable(String tableName, TableSyntaxNode tsn)
    {
    	return new Table(tableName, tsn);
    }
    

    public void preLoadTable(ITable table,
            ITableModel dataModel,
            ILogicalTable dataWithTitles,
            OpenlToolAdaptor openlAdapter) throws Exception {

        table.setModel(dataModel);
        table.setData(dataWithTitles);
        table.preLoad(openlAdapter);
    }

}
