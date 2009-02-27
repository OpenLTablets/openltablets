package org.openl.rules.tbasic;

import java.io.File;

import org.junit.Test;
import org.openl.rules.TestUtils;

public class TestSet extends Test0 {
    @Test
    public void test1() {
        Exception ex = catchEx(new File("test/rules/tbasic1/SET_F1.xls"));
        TestUtils.assertEx(ex, "Operation must not have Condition value");
    }

    @Test
    public void test2() {
        Exception ex = catchEx(new File("test/rules/tbasic1/SET_F2.xls"));
        TestUtils.assertEx(ex, "org.openl.syntax.SyntaxErrorException:");
    }

    @Test
    public void test3() {
        Exception ex = catchEx(new File("test/rules/tbasic1/SET_F3.xls"));
        TestUtils.assertEx(ex, "Operation must have Action value");
   }

    @Test
    public void test4() {
        Exception ex = catchEx(new File("test/rules/tbasic1/SET_F4.xls"));
        TestUtils.assertEx(ex, "Operation can not be multiline, i.e. can not have nested operations");
    }

    @Test
    public void test5() {
        okRows(new File("test/rules/tbasic1/SET_P1.xls"), 0);
    }
}
