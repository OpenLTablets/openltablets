package org.openl.rules.webtools.indexer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Test;
import org.openl.rules.indexer.Index;
import org.openl.rules.indexer.IndexQuery;
import org.openl.rules.indexer.IndexQueryParser;
import org.openl.rules.indexer.Index.TokenBucket;

public class FileIndexerTest {
    
    private String testFile = "test/rules/Tutorial_4_Test.xls";
    
    @Test
    public void testIsFilesChanged() {
        FileIndexer fileInd = new FileIndexer();
        assertTrue(fileInd.isFilesChanged(null, null));
        
        File file = new File(testFile);
        long[] times = new long[]{file.lastModified()};
        assertTrue(fileInd.isFilesChanged(null, times));
        
        String[] fileMas = null;
        try {
             fileMas = new String[]{file.getCanonicalPath()};            
        } catch (IOException e) {            
            e.printStackTrace();
        }
        assertTrue(fileInd.isFilesChanged(fileMas, times));
        
        fileInd.setFiles(fileMas);
        assertFalse(fileInd.isFilesChanged(fileMas, times));
        
        try {
            boolean res = fileInd.isFilesChanged(null, null);
            fail();
        } catch(NullPointerException ex) {
            assertTrue(true);
        }
    }
    
    @Test
    public void testMakeIndex() {
        FileIndexer fileInd = new FileIndexer();
        assertTrue(fileInd.makeIndex().getFirstCharMap().isEmpty());
        
        File file = new File(testFile);
        String[] fileMas = null;
        try {
             fileMas = new String[]{file.getCanonicalPath()};            
        } catch (IOException e) {            
            e.printStackTrace();
        }
        fileInd.setFiles(fileMas);
        assertFalse(fileInd.makeIndex().getFirstCharMap().isEmpty());
        assertEquals(30, fileInd.makeIndex().getFirstCharMap().size());
    }
    
    @Test
    public void testGetLetters() {
        FileIndexer fileInd = new FileIndexer();        
        
        File file = new File(testFile);
        String[] fileMas = null;
        try {
             fileMas = new String[]{file.getCanonicalPath()};            
        } catch (IOException e) {            
            e.printStackTrace();
        }
        fileInd.setFiles(fileMas);
        String[] letters = fileInd.getLetters();
        assertTrue(22 == letters.length);
        assertEquals("A", letters[0]);
        assertEquals("Y", letters[21]);        
    }
    
    @Test
    public void testGetBuckets() {
        FileIndexer fileInd = new FileIndexer();        
        
        File file = new File(testFile);
        String[] fileMas = null;
        try {
             fileMas = new String[]{file.getCanonicalPath()};            
        } catch (IOException e) {            
            e.printStackTrace();
        }
        fileInd.setFiles(fileMas);
        TokenBucket[] buckets = fileInd.getBuckets("A");
        
        for(TokenBucket buck : buckets) {
            System.out.println("111"+buck.displayValue());
        }
        assertTrue(25 == buckets.length);        
    }
    
    @Test
    public void testIndexQuery() {
        FileIndexer fileInd = new FileIndexer();        
        
        File file = new File(testFile);
        String[] fileMas = null;
        try {
             fileMas = new String[]{file.getCanonicalPath()};            
        } catch (IOException e) {            
            e.printStackTrace();
        }
        fileInd.setFiles(fileMas);
        Index index = fileInd.makeIndex();
        assertFalse(index.getFirstCharMap().isEmpty());
        assertEquals(30, index.getFirstCharMap().size());
        
        IndexQuery indexQuery = IndexQueryParser.parse("Gender");
        TreeSet ts = indexQuery.execute(index);        
    }
}
