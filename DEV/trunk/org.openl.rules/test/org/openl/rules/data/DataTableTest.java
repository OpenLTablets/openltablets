package org.openl.rules.data;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.openl.rules.BaseOpenlBuilder;
import org.openl.rules.data.binding.DataTableBoundNode.DataOpenField;

import org.openl.rules.lang.xls.syntax.TableSyntaxNode;

/**
 * 
 * @author DLiauchuk
 *
 */
public class DataTableTest extends BaseOpenlBuilder{
    
    private String __src = "test/rules/Tutorial_2_Test.xls";
    
    @Test
    public void testSimpleStringArray() {
        String tableName = "Data String simpleStringArray";
        TableSyntaxNode[] tsns = getTableSyntaxNodes(__src);        
        TableSyntaxNode resultTsn = findTable(tableName, tsns);
        if (resultTsn != null) {
            DataOpenField member = (DataOpenField)resultTsn.getMember();
            assertNotNull(member);
            String[] stringData = (String[])member.getTable().getDataArray();
            assertTrue(stringData.length == 5);
            List<String> dataList = new ArrayList<String>();
            for (String data : stringData) {
                dataList.add(data);
            }                
            assertTrue(dataList.contains("StringValue1"));
            assertTrue(dataList.contains("StringValue2"));
            assertTrue(dataList.contains("StringValue3"));
            assertTrue(dataList.contains("StringValue4"));
            assertTrue(dataList.contains("StringValue5"));  
        } else {
            fail();
        }
    }    
    
    @Test
    public void testTypeWithArrayColumns() {
        String tableName = "Data TypeWithArray testTypeWithArrayColumns";
        TableSyntaxNode[] tsns = getTableSyntaxNodes(__src);
        TableSyntaxNode resultTsn = findTable(tableName, tsns);
        if (resultTsn != null) {
            DataOpenField member = (DataOpenField)resultTsn.getMember();
            assertNotNull(member);
            TypeWithArray[] typeWitharray = (TypeWithArray[])member.getTable().getDataArray();
            assertTrue(typeWitharray[0].getIntArray().length == 4);
            List<Integer> dataList = new ArrayList<Integer>();
            for (int i=0; i< typeWitharray[0].getIntArray().length; i++) {                    
                dataList.add(Integer.valueOf(typeWitharray[0].getIntArray()[i]));
            }                                
            assertTrue(dataList.contains(111));
            assertTrue(dataList.contains(23));
            assertTrue(dataList.contains(5));
            assertTrue(dataList.contains(67));  
        } else {        
            fail();
        }
    }
    
    @Test
    public void testTypeWithArrayRows() {
        String tableName = "Data TypeWithArray testTypeWithArrayRows";
        TableSyntaxNode[] tsns = getTableSyntaxNodes(__src);        
        TableSyntaxNode resultTsn = findTable(tableName, tsns);
        if (resultTsn != null) {
            DataOpenField member = (DataOpenField)resultTsn.getMember();
            assertNotNull(member);
            TypeWithArray[] typeWitharray = (TypeWithArray[])member.getTable().getDataArray();
            assertTrue(typeWitharray[0].getIntArray().length == 5);
            List<Integer> dataList = new ArrayList<Integer>();
            for (int i=0; i< typeWitharray[0].getIntArray().length; i++) {                    
                dataList.add(Integer.valueOf(typeWitharray[0].getIntArray()[i]));
            }                                
            assertTrue(dataList.contains(12));
            assertTrue(dataList.contains(13));
            assertTrue(dataList.contains(14));
            assertTrue(dataList.contains(15));
            assertTrue(dataList.contains(16));  
        } else {
            fail();
        }
    }
    
    @Test
    public void testTypeWithArrayRowsOneElement() {
        String tableName = "Data TypeWithArray testTypeWithArrayRowsOneElement";
        TableSyntaxNode[] tsns = getTableSyntaxNodes(__src);        
        TableSyntaxNode resultTsn = findTable(tableName, tsns);
        if (resultTsn != null) {
            DataOpenField member = (DataOpenField)resultTsn.getMember();
            assertNotNull(member);
            TypeWithArray[] typeWitharray = (TypeWithArray[])member.getTable().getDataArray();
            assertTrue(typeWitharray[0].getIntArray().length == 1);
            List<Integer> dataList = new ArrayList<Integer>();
            for (int i=0; i< typeWitharray[0].getIntArray().length; i++) {                    
                dataList.add(Integer.valueOf(typeWitharray[0].getIntArray()[i]));
            }                                
            assertTrue(dataList.contains(12));  
        } else {
            fail();
        }   
    }
    
    @Test
    public void testCommaSeparated() {
        String tableName = "Data TypeWithArray testCommaSeparated";
        TableSyntaxNode[] tsns = getTableSyntaxNodes(__src);        
        TableSyntaxNode resultTsn = findTable(tableName, tsns);
        if (resultTsn != null) {
            DataOpenField member = (DataOpenField)resultTsn.getMember();
            assertNotNull(member);
            TypeWithArray[] typeWitharray = (TypeWithArray[])member.getTable().getDataArray();
            assertTrue(typeWitharray[0].getIntArray().length == 5);
            List<Integer> dataList = new ArrayList<Integer>();
            for (int i=0; i< typeWitharray[0].getIntArray().length; i++) {                    
                dataList.add(Integer.valueOf(typeWitharray[0].getIntArray()[i]));
            }                                
            assertTrue(dataList.contains(1));
            assertTrue(dataList.contains(56));
            assertTrue(dataList.contains(78));
            assertTrue(dataList.contains(45));
            assertTrue(dataList.contains(99));  
        } else {
            fail();
        }  
    }
    
    @Test
    public void testStringArray() {
        String tableName = "Data TypeWithArray testStringArray";
        TableSyntaxNode[] tsns = getTableSyntaxNodes(__src);        
        TableSyntaxNode resultTsn = findTable(tableName, tsns);
        if (resultTsn != null) {
            DataOpenField member = (DataOpenField)resultTsn.getMember();
            assertNotNull(member);
            TypeWithArray[] typeWitharray = (TypeWithArray[])member.getTable().getDataArray();
            assertTrue(typeWitharray[0].getStringArray().length == 2);
            List<String> dataList = new ArrayList<String>();
            for (String token : typeWitharray[0].getStringArray()) {                    
                dataList.add(token);
            }                                
            assertTrue(dataList.contains("Hello Denis! My name is vova."));
            assertTrue(dataList.contains("Yeah you are right."));  
        } else {
            fail();
        }
    }
    
    @Test
    public void testStringArrayWithEscaper() {
        String tableName = "Data TypeWithArray testStringArrayWithEscaper";
        TableSyntaxNode[] tsns = getTableSyntaxNodes(__src);        
        TableSyntaxNode resultTsn = findTable(tableName, tsns);
        if (resultTsn != null) {
            DataOpenField member = (DataOpenField)resultTsn.getMember();
            assertNotNull(member);
            TypeWithArray[] typeWitharray = (TypeWithArray[])member.getTable().getDataArray();
            assertTrue(typeWitharray[0].getStringArray().length == 4);
            List<String> dataList = new ArrayList<String>();
            for (String token : typeWitharray[0].getStringArray()) {                    
                dataList.add(token);
            }                                
            assertTrue(dataList.contains("One"));
            assertTrue(dataList.contains("two"));
            assertTrue(dataList.contains("three,continue this"));
            assertTrue(dataList.contains("four"));  
        } else {
            fail();
        }
    }
    
    @Test
    public void testClass() {
        String tableName = "Data TypeWithArray testClassLoading";
        TableSyntaxNode[] tsns = getTableSyntaxNodes(__src);        
        TableSyntaxNode resultTsn = findTable(tableName, tsns);
        if (resultTsn != null) {
            DataOpenField member = (DataOpenField)resultTsn.getMember();
            assertNotNull(member);
            TypeWithArray[] typeWitharray = (TypeWithArray[])member.getTable().getDataArray();
            assertTrue(typeWitharray[0].getStringArray().length == 4);
            List<String> dataList = new ArrayList<String>();
            for (String token : typeWitharray[0].getStringArray()) {                    
                dataList.add(token);
            }                                
            assertTrue(dataList.contains("One"));
            assertTrue(dataList.contains("two"));
            assertTrue(dataList.contains("three,continue this"));
            assertTrue(dataList.contains("four"));  
        } else {
            fail();
        }
    }

}
