package org.openl.rules.data;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import org.openl.binding.IBindingContext;
import org.openl.binding.impl.BindHelper;
import org.openl.exception.OpenLCompilationException;
import org.openl.exception.OpenLRuntimeException;
import org.openl.rules.OpenlToolAdaptor;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.types.IOpenClass;
import org.openl.util.BiMap;

public class Table implements ITable {

    protected ILogicalTable logicalTable;
    protected ITableModel dataModel;

    protected String tableName;
    protected TableSyntaxNode tableSyntaxNode;

    protected Object dataArray;

    protected BiMap<Integer, Object> rowIndexMap = new BiMap<Integer, Object>();
    protected BiMap<Integer, String> primaryIndexMap = new BiMap<Integer, String>();

    public Table(ITableModel dataModel, ILogicalTable data) {
        this.dataModel = dataModel;
        this.logicalTable = data;
    }

    public Table(String tableName, TableSyntaxNode tsn) {
        this.tableName = tableName;
        this.tableSyntaxNode = tsn;
    }

    public void setData(ILogicalTable dataWithHeader) {
        logicalTable = dataWithHeader;
    }

    public void setModel(ITableModel dataModel) {
        this.dataModel = dataModel;
    }

    public String getColumnDisplay(int n) {
        return dataModel.getDescriptor()[n].getDisplayName();
    }

    public int getColumnIndex(String columnName) {

        ColumnDescriptor[] descriptors = dataModel.getDescriptor();

        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i] == null) {
                continue;
            }
            if (descriptors[i].getName().equals(columnName)) {
                return i;
            }
        }

        return -1;
    }

    public String getColumnName(int n) {
        return dataModel.getDescriptor()[n].getName();
    }

    public IOpenClass getColumnType(int n) {

        ColumnDescriptor descriptor = dataModel.getDescriptor()[n];

        if (!descriptor.isConstructor()) {
            return descriptor.getType();
        }

        return null;
    }

    public Object getData(int row) {
        return Array.get(dataArray, row);
    }

    public Object getDataArray() {
        return dataArray;
    }

    public ITableModel getDataModel() {
        return dataModel;
    }

    public IGridTable getHeaderTable() {
        return logicalTable.getRow(0).getSource();
    }

    public String getName() {
        return tableName;
    }

    public int getNumberOfColumns() {
        return dataModel.getDescriptor().length;
    }

    public int getNumberOfRows() {
        return logicalTable.getHeight() - 1;
    }

    public String getPrimaryIndexKey(int row) {
        return primaryIndexMap.get(row);
    }

    public int getRowIndex(Object target) {
        return rowIndexMap.getKey(target);
    }

    public IGridTable getRowTable(int row) {
        return logicalTable.getRow(row + 1).getSource();
    }

    public int getSize() {
        return Array.getLength(dataArray);
    }

    public TableSyntaxNode getTableSyntaxNode() {
        return tableSyntaxNode;
    }

    public Map<String, Integer> getUniqueIndex(int columnIndex) throws SyntaxNodeException {

        ColumnDescriptor descriptor = dataModel.getDescriptor()[columnIndex];

        return descriptor.getUniqueIndex(this, columnIndex);
    }

    public Map<String, Integer> getFormattedUniqueIndex(int columnIndex) throws SyntaxNodeException {

        ColumnDescriptor descriptor = dataModel.getDescriptor()[columnIndex];

        return descriptor.getFormattedUniqueIndex(this, columnIndex);
    }

    public Object getValue(int col, int row) {

        Object rowObject = Array.get(getDataArray(), row);
        Object colObject = dataModel.getDescriptor()[col].getColumnValue(rowObject);

        return colObject;
    }

    public Map<String, Integer> makeUniqueIndex(int colIdx) throws SyntaxNodeException {

        Map<String, Integer> index = new HashMap<String, Integer>();

        int rows = logicalTable.getHeight();

        for (int i = 1; i < rows; i++) {

            IGridTable gridTable = logicalTable.getSubtable(colIdx, i, 1, 1).getSource();
            String key = gridTable.getCell(0, 0).getStringValue();

            if (key == null) {
                throw SyntaxNodeExceptionUtils.createError("Empty key in an unique index",
                    new GridCellSourceCodeModule(gridTable));
            }

            key = key.trim();

            if (index.containsKey(key)) {
                throw SyntaxNodeExceptionUtils.createError("Duplicated key in an unique index: " + key,
                    new GridCellSourceCodeModule(gridTable));
            }

            index.put(key, i - 1);
        }

        return index;
    }

    public Map<String, Integer> makeFormattedUniqueIndex(int colIdx) throws SyntaxNodeException {

        Map<String, Integer> index = new HashMap<String, Integer>();

        int rows = logicalTable.getHeight();

        for (int i = 1; i < rows; i++) {

            IGridTable gridTable = logicalTable.getSubtable(colIdx, i, 1, 1).getSource();
            String key = gridTable.getCell(0, 0).getFormattedValue();

            if (key == null) {
                throw SyntaxNodeExceptionUtils.createError("Empty key in an unique index",
                    new GridCellSourceCodeModule(gridTable));
            }

            key = key.trim();

            if (index.containsKey(key)) {
                throw SyntaxNodeExceptionUtils.createError("Duplicated key in an unique index: " + key,
                    new GridCellSourceCodeModule(gridTable));
            }

            index.put(key, i - 1);
        }

        return index;
    }

    public void populate(IDataBase dataBase, IBindingContext bindingContext) throws Exception {

        int rows = logicalTable.getHeight();
        int columns = logicalTable.getWidth();

        int startRow = 1;

        for (int i = startRow; i < rows; i++) {

            Object target = Array.get(dataArray, i - startRow);

            for (int j = 0; j < columns; j++) {

                ColumnDescriptor descriptor = dataModel.getDescriptor()[j];

                if (descriptor != null && (descriptor instanceof ForeignKeyColumnDescriptor)) {
                    ForeignKeyColumnDescriptor fkDescriptor = (ForeignKeyColumnDescriptor) descriptor;

                    if (fkDescriptor.isReference()) {

                        if (descriptor.isConstructor()) {
                            target = fkDescriptor.getLiteralByForeignKey(dataModel.getType(),
                                logicalTable.getSubtable(j, i, 1, 1),
                                dataBase,
                                bindingContext);
                        } else {
                            fkDescriptor.populateLiteralByForeignKey(target,
                                logicalTable.getSubtable(j, i, 1, 1),
                                dataBase,
                                bindingContext);
                        }
                    }
                }
            }
        }
    }

    public void preLoad(OpenlToolAdaptor openlAdapter) throws Exception {

        int rows = logicalTable.getHeight();
        int startRow = getStartRowForData();

        dataArray = Array.newInstance(dataModel.getInstanceClass(), rows - startRow);        

        for (int rowNum = startRow; rowNum < rows; rowNum++) {
            processRow(openlAdapter, startRow, rowNum);
        }
    }

    protected void processRow(OpenlToolAdaptor openlAdapter, int startRow, int rowNum) throws OpenLCompilationException 
        {
        
        boolean constructor = isConstructor();
        Object literal = null;

        int rowIndex = rowNum - startRow;

        if (!constructor) {
            literal = dataModel.newInstance();
            if (literal == null) {
                String errorMessage = String.format("Can`t create instance of %s", dataModel.getName());
                throw new OpenLCompilationException(errorMessage);
            }
            addToRowIndex(rowIndex, literal);
        }

        int columns = logicalTable.getWidth();

        for (int columnNum = 0; columnNum < columns; columnNum++) {
            literal = processColumn(openlAdapter, constructor, rowNum, literal, columnNum);
        }

        if (literal == null) {
            literal = dataModel.getType().nullObject();
        }

        Array.set(dataArray, rowNum - startRow, literal);
    }

    protected Object processColumn(OpenlToolAdaptor openlAdapter, boolean constructor, int rowNum, Object literal,
            int columnNum) throws SyntaxNodeException {
        
        ColumnDescriptor columnDescriptor = dataModel.getDescriptor()[columnNum];

        
        if (columnDescriptor != null && !columnDescriptor.isReference()) {
            if (constructor) {
                literal = columnDescriptor.getLiteral(dataModel.getType(), logicalTable.getSubtable(columnNum,
                    rowNum,
                    1,
                    1), openlAdapter);
            } else {
            	
                try {
					columnDescriptor.populateLiteral(literal,
					    logicalTable.getSubtable(columnNum, rowNum, 1, 1),
					    openlAdapter);
		        } catch (SyntaxNodeException ex) {
		        	tableSyntaxNode.addError(ex);
		        	BindHelper.processError(ex);
		        }
			}
        }

        return literal;
    }

    public synchronized void setPrimaryIndexKey(int row, String value) {
        Integer oldRow = primaryIndexMap.getKey(value);
        if (oldRow != null) {
            throw new OpenLRuntimeException("Duplicated key: " + value + "in rows " + oldRow + "," + row);
        }
        primaryIndexMap.put(row, value);
    }

    public Object findObject(int columnIndex, String skey, IBindingContext cxt) throws SyntaxNodeException {

        Map<String, Integer> index = getUniqueIndex(columnIndex);

        Integer found = index.get(skey);

        if (found == null) {
            return null;
        }

        return Array.get(dataArray, found);
    }

    private void addToRowIndex(int rowIndex, Object target) {
        rowIndexMap.put(rowIndex, target);
    }

    /**
     * @return Start row for data rows from Data_With_Titles rows. It depends on
     *         if table has or no column title row.
     * @see {@link DataNodeBinder#getDataWithTitleRows}
     */
    protected int getStartRowForData() {

        if (dataModel.hasColumnTitleRow()) {
            return 1;
        }

        return 0;
    }

    private boolean isConstructor() {

        for (int i = 0; i < dataModel.getDescriptor().length; i++) {

            ColumnDescriptor columnDescriptor = dataModel.getDescriptor()[i];

            if (columnDescriptor != null && columnDescriptor.isConstructor()) {
                return true;
            }
        }

        return false;
    }
}
