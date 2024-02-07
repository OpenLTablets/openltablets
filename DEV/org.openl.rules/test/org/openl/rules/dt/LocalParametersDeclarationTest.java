package org.openl.rules.dt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openl.rules.runtime.RulesEngineFactory;

public class LocalParametersDeclarationTest {

    public interface ILocalParametersDeclarationTest {

        String test1(int age);

        String test2(String ageType);

        String test3(String ageType);

        String test4(boolean hadTraining);

        String test5(int age);

        String test6(int age);

        void test7(int age);

        void test8(int age);

        String test9(int age);

        int test10(int age);

        Double test11(int age);

        void test12(int age);

    }

    private static final String SRC = "test/rules/dt/LocalParametersDeclarationTest.xls";

    private ILocalParametersDeclarationTest instance;

    @BeforeEach
    public void setUp() throws Exception {
        RulesEngineFactory<ILocalParametersDeclarationTest> engineFactory = new RulesEngineFactory<>(SRC,
                ILocalParametersDeclarationTest.class);

        instance = engineFactory.newEngineInstance();
    }

    @Test
    public void testParamsDeclarationWithDifferentTypes() {
        String result = instance.test1(10);
        assertEquals("Not Eligible", result);

        result = instance.test1(40);
        assertEquals("Eligible", result);
    }

    @Test
    public void testAnonymousParamUsing() {

        String result = instance.test2("Young Driver");
        assertEquals("Not Eligible", result);

        result = instance.test2("Senior Driver");
        assertEquals("Not Eligible", result);

        result = instance.test2("Just Driver");
        assertEquals("Eligible", result);
    }

    @Test
    public void testWithoutParamNameDeclaration() {

        String result = instance.test3("Young Driver");
        assertEquals("Not Eligible", result);

        result = instance.test3("Senior Driver");
        assertEquals("Not Eligible", result);

        result = instance.test3("Just Driver");
        assertEquals("Eligible", result);
    }

    @Test
    public void testWithoutParamDeclaration1() {

        String result = instance.test4(false);
        assertEquals("Not Eligible", result);

        result = instance.test4(true);
        assertEquals("Eligible", result);
    }

    @Test
    public void testFullParamsDeclaration() {
        String result = instance.test5(10);
        assertEquals("Not Eligible", result);

        result = instance.test5(40);
        assertEquals("Eligible", result);
    }

    @Test
    public void testReturnParamsDeclaration() {
        String result = instance.test6(10);
        assertEquals("Not Eligible", result);

        result = instance.test6(40);
        assertEquals("Eligible", result);
    }

    @Test
    public void testActionParamsDeclaration() {
        instance.test7(10);
        instance.test7(40);
    }

    @Test
    public void testEmptyActionParamsDeclaration() {
        instance.test8(10);
        instance.test8(40);
    }

    @Test
    public void testSimplifiedReturnParamsDeclaration1() {

        String result = instance.test9(10);
        assertEquals("1", result);

        result = instance.test9(40);
        assertEquals("2", result);
    }

    @Test
    public void testSimplifiedReturnParamsDeclaration2() {

        int result = instance.test10(10);
        assertEquals(1, result);

        result = instance.test10(40);
        assertEquals(2, result);
    }

    @Test
    public void testSimplifiedReturnParamsDeclaration3() {

        Double result = instance.test11(10);
        assertEquals(1.0, result);

        result = instance.test11(40);
        assertEquals(2.0, result);
    }

    @Test
    public void testSimplifiedReturnParamsDeclaration4() {

        instance.test12(10);
        instance.test12(40);
    }

}
