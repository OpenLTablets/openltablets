package org.openl.rules.tbasic;
import java.io.File;

import org.junit.Ignore;
import org.openl.rules.TestHelper;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;

@Ignore("Manual test")
public class Test0 {
    public void okRows(File xlsFile, int expectedNumberOfRows) {
        TestHelper<ITestAlgorithm1> testHelper;
        testHelper = new TestHelper<ITestAlgorithm1>(xlsFile, ITestAlgorithm1.class);

        TableSyntaxNode tsn = testHelper.getTableSyntaxNode();
        ITestAlgorithm1 a = testHelper.getInstance();

//        assertEquals(expectedNumberOfRows, a.modification());
        a.modification();
    }

    public Exception catchEx(File xlsFile) {
        Exception result = null;
        try {
            TestHelper<ITestAlgorithm1> testHelper;
            testHelper = new TestHelper<ITestAlgorithm1>(xlsFile, ITestAlgorithm1.class);

            TableSyntaxNode tsn = testHelper.getTableSyntaxNode();
        } catch (Exception e) {
            result = e;
        }

        return result;
    }

    private void _test() {
        okRows(new File("test/rules/Algorithm.xls"), 26);
    }

    public static void main(String[] args) {
        Test0 t = new Test0();
        t._test();
    }
}
