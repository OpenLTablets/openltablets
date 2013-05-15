package org.openl.codegen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Generates file in outFileLocation by inserting code into predefined places in
 * input file inFileLocation. Insertion places are defined by INSERT_TAG. The
 * insertion logic is handled by ICodeGenAdaptor, there could be multiple
 * INSERT_TAGS in the code, calling class can redefine INSERT_TAG value
 * 
 * @author snshor Created Jul 27, 2009
 * 
 */

public class FileCodeGen {

    public static final String DEFAULT_INSERT_TAG = "<<< INSERT";
    public static final String DEFAULT_END_INSERT_TAG = "<<< END INSERT";

    private String inFileLocation;
    private String outFileLocation;
    private String insertTag;

    public FileCodeGen(String inFileLocation, String outFileLocation) {
        this.inFileLocation = inFileLocation;
        this.outFileLocation = outFileLocation == null ? inFileLocation : outFileLocation;
        if (inFileLocation.equals(this.outFileLocation)){
            System.out.println("Processing " + inFileLocation);
        }else{
            System.out.println("Processing " + inFileLocation + " into " + this.outFileLocation);
        }
        this.insertTag = insertTag == null ? DEFAULT_INSERT_TAG : insertTag;

    }

    public String getEndInsertTag(String line) {
        return DEFAULT_END_INSERT_TAG;
    }

    public void processFile(ICodeGenAdaptor cga) throws IOException {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder(10000);
        IOException ex = null;
        try {
            br = new BufferedReader(new FileReader(inFileLocation));

            String line = null;

            Deque<String> endInsert = new LinkedList<String>();

            while ((line = br.readLine()) != null) {

                if (line.contains(insertTag)) {
                    sb.append(line).append('\n');
                    cga.processInsertTag(line, sb);
                    endInsert.push(getEndInsertTag(line));
                }

                boolean skipTillEnd = endInsert.size() > 0;

                if (skipTillEnd) {
                    String endTag = endInsert.peek();
                    if (line.contains(endTag)) {
                        cga.processEndInsertTag(line, sb);
                        sb.append(line.trim()).append('\n');
                        endInsert.pop();
                    }
                    continue;
                }
                sb.append(line).append('\n');

            }

            if (endInsert.size() > 0) {
                throw new IllegalStateException("Not processed " + endInsert);
            }
        } catch (IOException e) {
            ex = e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    if (ex == null) {
                        throw e;
                    }
                    throw ex;
                }
            }
        }

        BufferedWriter bw = null;
        ex = null;
        try {
            bw = new BufferedWriter(new FileWriter(outFileLocation));
            bw.write(sb.toString());
        } catch (IOException e) {
            ex = e;
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    if (ex == null) {
                        throw e;
                    }
                    throw ex;
                }
            }
        }
    }
}
