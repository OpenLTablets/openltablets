package org.openl.rules.dt;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openl.exception.OpenLCompilationException;
import org.openl.rules.RulesCommons;
import org.openl.rules.binding.RuleRowHelper;
import org.openl.rules.helpers.CharRange;
import org.openl.rules.helpers.DoubleRange;
import org.openl.rules.helpers.INumberRange;
import org.openl.rules.helpers.IntRange;
import org.openl.rules.lang.xls.IXlsTableNames;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.CompositeGrid;
import org.openl.rules.table.GridRegion;
import org.openl.rules.table.GridTable;
import org.openl.rules.table.ICell;
import org.openl.rules.table.IGrid;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.IWritableGrid;
import org.openl.rules.table.LogicalTableHelper;
import org.openl.rules.table.xls.XlsSheetGridHelper;

public class DecisionTableHelper {
    
    /**
     * Check if table is vertical.<br>
     * Vertical table is when conditions are represented from left to right, table is reading from top to bottom.</br> 
     * Example of vertical table:
     * 
     * <table cellspacing="2">
     * <tr>
     * <td align="center" bgcolor="#ccffff"><b>Rule</b></td>
     * <td align="center" bgcolor="#ccffff"><b>C1</b></td>
     * <td align="center" bgcolor="#ccffff"><b>C2</b></td>
     * </tr>
     * <tr>
     * <td align="center" bgcolor="#ccffff"></td>
     * <td align="center" bgcolor="#ccffff">paramLocal1==paramInc</td>
     * <td align="center" bgcolor="#ccffff">paramLocal2==paramInc</td>
     * </tr>
     * <tr>
     * <td align="center" bgcolor="#ccffff"></td>
     * <td align="center" bgcolor="#ccffff">String paramLocal1</td>
     * <td align="center" bgcolor="#ccffff">String paramLocal2</td>
     * </tr>
     * <tr>
     * <td align="center" bgcolor="#8FCB52">Rule</td>
     * <td align="center" bgcolor="#ffff99">Local Param 1</td>
     * <td align="center" bgcolor="#ffff99">Local Param 2</td>
     * </tr>
     * <tr>
     * <td align="center" bgcolor="#8FCB52">Rule1</td>
     * <td align="center" bgcolor="#ffff99">value11</td>
     * <td align="center" bgcolor="#ffff99">value21</td>
     * </tr>
     * <tr>
     * <td align="center" bgcolor="#8FCB52">Rule2</td>
     * <td align="center" bgcolor="#ffff99">value12</td>
     * <td align="center" bgcolor="#ffff99">value22</td>
     * </tr>
     * <tr>
     * <td align="center" bgcolor="#8FCB52">Rule3</td>
     * <td align="center" bgcolor="#ffff99">value13</td>
     * <td align="center" bgcolor="#ffff99">value23</td>
     * </tr>
     * </table>
     * 
     * @param table     
     * @return <code>TRUE</code> if table is vertical.
     */
    public static boolean looksLikeVertical(ILogicalTable table) {

        if (table.getWidth() <= IDecisionTableConstants.SERVICE_COLUMNS_NUMBER) {
            return true;
        }

        if (table.getHeight() <= IDecisionTableConstants.SERVICE_COLUMNS_NUMBER) {
            return false;
        }

        int cnt1 = countConditionsAndActions(table);
        int cnt2 = countConditionsAndActions(table.transpose());

        if (cnt1 != cnt2) {
            return cnt1 > cnt2;
        }

        return table.getWidth() <= IDecisionTableConstants.SERVICE_COLUMNS_NUMBER;
    }
    
    public static boolean isValidConditionHeader(String s) {
        return s.length() >= 2 && s.charAt(0) == DecisionTableColumnHeaders.CONDITION.getHeaderKey().charAt(0) 
            && Character.isDigit(s.charAt(1));
    }

    public static boolean isValidActionHeader(String s) {
        return s.length() >= 2 && s.charAt(0) == DecisionTableColumnHeaders.ACTION.getHeaderKey().charAt(0) 
            && Character.isDigit(s.charAt(1));
    }

    public static boolean isValidRetHeader(String s) {
        return s.length() >= 3 && s.startsWith(DecisionTableColumnHeaders.RETURN.getHeaderKey())
            && (s.length() == 3 || Character.isDigit(s.charAt(3)));
    }

    public static boolean isValidRuleHeader(String s) {
        return s.equals(DecisionTableColumnHeaders.RULE.getHeaderKey());
    }

    public static boolean isValidCommentHeader(String s) {
        return s.startsWith(RulesCommons.COMMENT_SYMBOLS.toString());
    }

    public static boolean isActionHeader(String s) {
        return isValidActionHeader(s) || isValidRetHeader(s);
    }

    public static boolean isConditionHeader(String s) {
        return isValidConditionHeader(s) || isValidHConditionHeader(s);
    }

    public static int countConditionsAndActions(ILogicalTable table) {

        int width = table.getWidth();
        int count = 0;

        for (int i = 0; i < width; i++) {

            String value = table.getColumn(i).getSource().getCell(0, 0).getStringValue();

            if (value != null) {
                value = value.toUpperCase();
                count += isValidConditionHeader(value) || isActionHeader(value) ? 1 : 0;
            }
        }

        return count;
    }
    
    /**
     * Checks if given table contain any horizontal condition header.
     * 
     * @param table
     * @return true if there is is any horizontal condition header in the table.
     */
    public static boolean hasHConditions(ILogicalTable table) {
        int width = table.getWidth();

        for (int i = 0; i < width; i++) {

            String value = table.getColumn(i).getSource().getCell(0, 0).getStringValue();

            if (value != null) {
                value = value.toUpperCase();

                if (isValidHConditionHeader(value)) {
                    return true;
                }
            }
        }

        return false;
    }
    
    public static boolean isValidHConditionHeader(String headerStr) {
        return headerStr.startsWith(
            DecisionTableColumnHeaders.HORIZONTAL_CONDITION.getHeaderKey()) && headerStr.length() > 2 && 
            Character.isDigit(headerStr.charAt(2));
    }
    
    /**
     * Creates virtual headers for condition and return columns to load simple
     * Decision Table as an usual Decision Table
     * 
     * @param decisionTable method description for simple Decision Table.
     * @param originalTable The original body of simple Decision Table.
     * @param numberOfHcondition
     * @return prepared usual Decision Table.
     */
    public static ILogicalTable preprocessSimpleDecisionTable(DecisionTable decisionTable, ILogicalTable originalTable,
            int numberOfHcondition) throws OpenLCompilationException {
        IWritableGrid virtualGrid = createVirtualGrid();
        writeVirtualHeadersForSimpleDecisionTable(virtualGrid, originalTable, decisionTable, numberOfHcondition);
        
        //If the new table header size bigger than the size of the old table we use the new table size
        int sizeOfVirtualGridTable = virtualGrid.getMaxColumnIndex(0) < originalTable.getSource().getWidth() ?
                originalTable.getSource().getWidth() - 1 : virtualGrid.getMaxColumnIndex(0) - 1;
        GridTable virtualGridTable = 
            new GridTable(0, 0, IDecisionTableConstants.SIMPLE_DT_HEADERS_HEIGHT - 1, 
                    sizeOfVirtualGridTable/*originalTable.getSource().getWidth() - 1*/, virtualGrid);
    
        IGrid grid = new CompositeGrid(new IGridTable[] { virtualGridTable, originalTable.getSource() }, true);
        
        //If the new table header size bigger than the size of the old table we use the new table size
        int sizeofGrid = virtualGridTable.getWidth() < originalTable.getSource().getWidth() ?
                originalTable.getSource().getWidth() - 1 : virtualGridTable.getWidth() - 1;
                
        return LogicalTableHelper.logicalTable(new GridTable(0, 0, originalTable.getSource().getHeight()
                + IDecisionTableConstants.SIMPLE_DT_HEADERS_HEIGHT - 1, 
                sizeofGrid /*originalTable.getSource().getWidth() - 1*/, grid));
    }
    
    private static void writeVirtualHeadersForSimpleDecisionTable(IWritableGrid grid, ILogicalTable originalTable,
            DecisionTable decisionTable, int numberOfHcondition) throws OpenLCompilationException {
        int columnsForConditions = 0;
        
        // number of physical columns for conditions(including merged)
        //
        columnsForConditions = writeConditions(grid, originalTable, decisionTable, numberOfHcondition);
        
        // write return
        //
        writeReturn(grid, originalTable, decisionTable, columnsForConditions, numberOfHcondition > 0 ? true : false);
    }

    private static void writeReturn(IWritableGrid grid, ILogicalTable originalTable, DecisionTable decisionTable,
            int columnsForConditions, boolean isLookupTable) throws OpenLCompilationException {
        // if the physical number of columns for conditions is equals or more than whole width of the table,
        // means there is no return column.
        //
        /*
        if (columnsForConditions >= originalTable.getWidth()) {
            throw new OpenLCompilationException("Wrong table structure: There is no column for return values");
        }
        */
        // write return column
        //
        grid.setCellValue(columnsForConditions, 0, (DecisionTableColumnHeaders.RETURN.getHeaderKey() + "1").intern());
        
        if (!isLookupTable && !(originalTable.getWidth() <= getNumberOfConditions(decisionTable))) {
            int mergedColumnsCounts = mergedColumnsCounts = originalTable.getColumnWidth(getNumberOfConditions(decisionTable));
            
            if (mergedColumnsCounts > 1) {
                for (int row = 0; row < IDecisionTableConstants.SIMPLE_DT_HEADERS_HEIGHT; row++) {
                    grid.addMergedRegion(
                        new GridRegion(row, columnsForConditions, row, columnsForConditions + mergedColumnsCounts - 1));
                }
            }
        }
    }

    private static int writeConditions(IWritableGrid grid, ILogicalTable originalTable, DecisionTable decisionTable,
            int numberOfHcondition) throws OpenLCompilationException {
        int numberOfConditions = getNumberOfConditions(decisionTable);
        int column = 0;
        int vColumnCounter = 0;
        
        for (int i = 0; i < numberOfConditions; i++) {
            if (column > originalTable.getWidth()) {
                String message = "Wrong table structure: Columns count is less than parameters count";
                throw new OpenLCompilationException(message);
            }
            // write headers
            //
            boolean isThatVCondition = i < numberOfConditions - numberOfHcondition;
            
            if (isThatVCondition) {
                vColumnCounter++;
                // write simple condition
                //
                grid.setCellValue(column, 0, (DecisionTableColumnHeaders.CONDITION.getHeaderKey() + (i + 1)).intern());
            } else {
                // write horizontal condition
                //
                grid.setCellValue(column, 0, (DecisionTableColumnHeaders.HORIZONTAL_CONDITION.getHeaderKey() + (i + 1)).intern());
            }
            
            grid.setCellValue(column, 1, decisionTable.getSignature().getParameterName(i));
            
            //Set type of condition values(for Ranges and Array)
            grid.setCellValue(column, 2,
                    checkTypeOfValues(originalTable, column, decisionTable.getSignature().getParameterType(i).getName(), isThatVCondition, vColumnCounter) );
            
            //merge columns
            int mergedColumnsCounts = originalTable.getColumnWidth(i);
            
            if (mergedColumnsCounts > 1) {
                for (int row = 0; row < IDecisionTableConstants.SIMPLE_DT_HEADERS_HEIGHT; row++) {
                    grid.addMergedRegion(new GridRegion(row, column, row, column + mergedColumnsCounts - 1));
                }
            }
            
            column += mergedColumnsCounts;
        }
        return column;
    }
    
    /**
     * Check type of condition values. If condition values are complex(Range, Array) 
     * than types of complex values will be returned 
     * 
     * @param originalTable The original body of simple Decision Table.
     * @param column The number of a condition 
     * @param typeName The type name of an input parameter
     * @param isThatVCondition If condition is vertical value = true
     * @param vColumnCounter Counter of vertical conditions. Needed for calculating 
     * position of horizontal condition
     * @return type of condition values
     */
    private static String checkTypeOfValues(ILogicalTable originalTable, int column, String typeName,
            boolean isThatVCondition, int vColumnCounter) {
        final List<String> intType = Arrays.asList("Int","Long","int","long","IntValue","java.lang.Integer");
        final List<String> doubleType = Arrays.asList("Double","Float","double","float","DoubleValue","java.lang.Double");
        ILogicalTable decisionValues;
        int width = 0;
        
        if (isThatVCondition) {
            decisionValues = originalTable.getColumn(column);
            width = decisionValues.getHeight();
        } else {
            //The first cell of SimpleLookupTable merge rows and column of vertical and horizontal conditions
            ICell mergedCell = originalTable.getSource().getCell(0, 0);
            int numOfHRow = column - vColumnCounter;
            
            decisionValues = LogicalTableHelper.logicalTable(originalTable.getSource().getRow(numOfHRow));
            width = decisionValues.getWidth();
        }
        
        int mergedColumnsCounts = originalTable.getColumnWidth(column);
        boolean isMerged = mergedColumnsCounts > 1 ? true : false;
        
        //if the name row is merged than we have Array
        if (isMerged) {
            return typeName+"[]";
        }
        
        for (int valueNum = 1; valueNum < width; valueNum++) {
            ILogicalTable cellValue;
            
            if (isThatVCondition) {
                cellValue = decisionValues.getRow(valueNum);
            } else {
                cellValue = decisionValues.getColumn(valueNum);
            }
            
            
            if (cellValue.getSource().getCell(0, 0).getStringValue() == null) {
                continue;
            }
            
            if (RuleRowHelper.isCommaSeparatedArray(cellValue)) {
                return typeName+"[]";
            } else if (isRangeValue(cellValue.getSource().getCell(0, 0).getStringValue())) {
                INumberRange range = null;
                
                /**try to create range by values**/
                if (intType.contains(typeName)) {
                    try {
                        range = new IntRange(cellValue.getSource().getCell(0, 0).getStringValue());
                        
                        /**Return name of a class without a package prefix**/
                        return range.getClass().getSimpleName();
                    } catch(Exception e) {
                       continue;
                    }
                } else if (doubleType.contains(typeName)) {
                    try {
                        range = new DoubleRange(cellValue.getSource().getCell(0, 0).getStringValue());
                        
                        /**Return name of a class without a package prefix**/
                        return range.getClass().getSimpleName();
                    } catch(Exception e) {
                        continue;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Check range values
     * 
     * @param cellValue The string value of the cell
     * @return boolean
     */
    private static boolean isRangeValue(String cellValue) {
        String valuePattern = "-?\\$?[0-9]+\\.?[0-9]*[KMB]?";// -$67M 
        Pattern p = Pattern.compile("(\\s*"+valuePattern+"\\s(\\.\\.|\\.\\.\\.)\\s"+valuePattern+"\\s*)|" +// -1 .. 13K
                                    "(\\s*[<>]{1}[=\\s]*"+valuePattern+"\\s*)|" +//<-$4K
                                    "(\\s*"+valuePattern+"\\+\\s*)|" +//"0.67B+
                                    "(\\s*(\\[|\\()"+valuePattern+"\\s*(;|\\.\\.)\\s*"+valuePattern+"(\\]|\\))\\s*)|" +//[6.000; $12)
                                    "(\\s*"+valuePattern+" and more\\s*)|" +
                                    "\\s*more than "+valuePattern+"\\s*");
        Matcher m = p.matcher(cellValue);

        return m.matches();
    }

    private static int getNumberOfConditions(DecisionTable decisionTable) {
        // number of conditions is counted by the number of income parameters
        //
        return decisionTable.getSignature().getNumberOfParameters();        
    }
    /**
     * Creates not-existing virtual grid.
     * 
     * @return virtual {@link IWritableGrid}.
     */
    public static IWritableGrid createVirtualGrid() {
        return XlsSheetGridHelper.createVirtualGrid(getPOIHSSFSheet());
    }
    
    /**
     * @deprecated 26.12.2011
     * As always creates workbooks for excel before 2007
     */
    @Deprecated    
    public static IWritableGrid createVirtualGrid(String gridName, String poiSheetName) {
        return XlsSheetGridHelper.createVirtualGrid(getPOIHSSFSheet(poiSheetName), gridName);
    }
    
    public static IWritableGrid createVirtualGrid(String gridName, String poiSheetName, int numberOfColumns) {
        return XlsSheetGridHelper.createVirtualGrid(getPOISheet(poiSheetName, numberOfColumns), gridName);
    }

    private static Sheet getPOISheet(String poiSheetName, int numberOfColumns) {
        Sheet sheet = null;
        
        if (numberOfColumns > 256) {
             sheet = getPOIXSSFSheet(poiSheetName);
        } else {
            // Pre-2007 excel sheets had a limitation of 256 columns.
            sheet = getPOIHSSFSheet(poiSheetName);
        }
        return sheet;
    }
    
    private static Sheet getPOIHSSFSheet() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        return workbook.createSheet();
    }
    
    private static Sheet getPOIHSSFSheet(String poiSheetName) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        return workbook.createSheet(poiSheetName);
    }
    
    private static Sheet getPOIXSSFSheet(String poiSheetName) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        return workbook.createSheet(poiSheetName);
    }
 
    public static boolean isSimpleDecisionTable(TableSyntaxNode tableSyntaxNode) {
        String dtType = tableSyntaxNode.getHeader().getHeaderToken().getIdentifier();
        
        if (IXlsTableNames.SIMPLE_DECISION_TABLE.equals(dtType)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSimpleLookupTable(TableSyntaxNode tableSyntaxNode) {
        String dtType = tableSyntaxNode.getHeader().getHeaderToken().getIdentifier();
        
        if (IXlsTableNames.SIMPLE_DECISION_LOOKUP.equals(dtType)) {
            return true;
        } else {
            return false;
        }
    }
}
