package org.openl.rules.dt;

import junit.framework.TestCase;

import org.openl.rules.runtime.RuleEngineFactory;
import org.openl.runtime.EngineFactory;

public class LocalParametersDeclarationTest extends TestCase {

    public interface ILocalParametersDeclarationTest {

        String test1(int age);

        String test2(String ageType);

        String test3(String ageType);

        String test4(boolean hadTraining);

        String test5(int age);

        String test6(int age);

        void test7(int age);

        void test8(int age);
    }

    private static String src = "test/rules/dt/LocalParametersDeclarationTest.xls";

    private ILocalParametersDeclarationTest test;

    @Override
    protected void setUp() throws Exception {
        EngineFactory<ILocalParametersDeclarationTest> engineFactory = new EngineFactory<ILocalParametersDeclarationTest>(RuleEngineFactory.RULE_OPENL_NAME,
            src,
            ILocalParametersDeclarationTest.class);

        test = engineFactory.makeInstance();
    }

    public void testParamsDeclarationWithDifferentTypes() {
        String result = test.test1(10);
        assertEquals("Not Eligible", result);

        result = test.test1(40);
        assertEquals("Eligible", result);
    }

    public void testAnonymousParamUsing() {

        String result = test.test2("Young Driver");
        assertEquals("Not Eligible", result);

        result = test.test2("Senior Driver");
        assertEquals("Not Eligible", result);

        result = test.test2("Just Driver");
        assertEquals("Eligible", result);
    }

    public void testWithoutParamNameDeclaration() {

        String result = test.test3("Young Driver");
        assertEquals("Not Eligible", result);

        result = test.test3("Senior Driver");
        assertEquals("Not Eligible", result);

        result = test.test3("Just Driver");
        assertEquals("Eligible", result);
    }

    public void testWithoutParamDeclaration() {

        String result = test.test4(false);
        assertEquals("Not Eligible", result);

        result = test.test4(true);
        assertEquals("Eligible", result);
    }

    public void testFullParamsDeclaration() {
        String result = test.test5(10);
        assertEquals("Not Eligible", result);

        result = test.test5(40);
        assertEquals("Eligible", result);
    }

    public void testReturnParamsDeclaration() {
        String result = test.test6(10);
        assertEquals("Not Eligible", result);

        result = test.test6(40);
        assertEquals("Eligible", result);
    }

    public void testActionParamsDeclaration() {
        test.test7(10);
        test.test7(40);
    }

    public void testEmptyActionParamsDeclaration() {
        test.test8(10);
        test.test8(40);
    }

}
