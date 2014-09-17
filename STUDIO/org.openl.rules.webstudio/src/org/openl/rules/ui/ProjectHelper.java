/**
 * Created Mar 24, 2007
 */
package org.openl.rules.ui;

import java.util.ArrayList;
import java.util.List;

import org.openl.base.INamedThing;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.testmethod.TestSuite;
import org.openl.rules.testmethod.TestSuiteMethod;
import org.openl.rules.types.OpenMethodDispatcher;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.impl.MethodDelegator;

/**
 * @author snshor
 *
 */
public final class ProjectHelper {

    private static final String TEST_CASES = "test cases";
    private static final String NO = "No";
    private static final String RUNS = "runs";

    private ProjectHelper() {
    }

    public static TestSuiteMethod[] allTesters(IOpenClass openClass) {
        List<TestSuiteMethod> res = new ArrayList<TestSuiteMethod>();
        for (IOpenMethod tester : openClass.getMethods()) {
            if (isTester(tester)) {
                res.add((TestSuiteMethod) tester);
            }
            if (tester instanceof OpenMethodDispatcher && isTester(((OpenMethodDispatcher) tester).getTargetMethod())) {
                res.add((TestSuiteMethod) ((OpenMethodDispatcher) tester).getTargetMethod());
            }
        }

        TestSuiteMethod[] testSuiteMethods = new TestSuiteMethod[res.size()];
        return res.toArray(testSuiteMethods);
    }

    public static boolean isMethodRunnedBy(IOpenMethod tested, IOpenMethod runner) {
        if (!(runner instanceof TestSuiteMethod)) {
            return false;
        }
        if (runner == tested) {
            return true;
        }
        IOpenMethod toTest = ((TestSuiteMethod) runner).getTestedMethod();
        return toTest == tested && ((TestSuiteMethod) runner).isRunmethod();
    }

    public static boolean isMethodTestedBy(IOpenMethod tested, IOpenMethod tester) {
        return isTester(tester) && isTestForMethod(tester, tested);
    }

    public static boolean isMethodHasParams(IOpenMethod m) {
        IOpenClass[] par = m.getSignature().getParameterTypes();
        return par.length > 0;
    }

    public static boolean isTestable(IOpenMethod m) {
        return testers(m).length > 0;
    }

    /**
     * Checks if the tester is instance of {@link TestSuiteMethod}, if it has
     * any parameters for testing(see
     * {@link TestSuiteMethod#isRunmethodTestable()}) and if there is no errors
     * in it.
     * 
     * @param tester instance of method that is considered to be a test.
     * @return true if tester is valid {@link TestSuiteMethod}.
     */
    public static boolean isTester(IOpenMethod tester) {
        return (tester instanceof TestSuiteMethod) && !((TestSuiteMethod) tester).isRunmethod() && ((TestSuiteMethod) tester).isRunmethodTestable() && noErrors((TestSuiteMethod) tester);
    }

    /**
     * Checks if test method doesn`t contain any error.
     * 
     * @param testMethod test method
     * @return true if there is no errors in the test method.
     */
    public static boolean noErrors(TestSuiteMethod testMethod) {
        return testMethod.getSyntaxNode().getErrors() == null || testMethod.getSyntaxNode().getErrors().length == 0;
    }

    public static IOpenMethod[] runners(IOpenMethod tested) {

        List<IOpenMethod> res = new ArrayList<IOpenMethod>();
        for (IOpenMethod runner : tested.getDeclaringClass().getMethods()) {
            if (isMethodRunnedBy(tested, runner)) {
                res.add(runner);
            }

        }

        return res.toArray(new IOpenMethod[res.size()]);
    }

    /**
     * Get tests for tested method that have filled rules rows data for testing
     * its functionality. Run methods and tests with empty test cases are not
     * being processed. If you need to get all test methods, including run
     * methods and empty ones, use
     * {@link #isTestForMethod(IOpenMethod, IOpenMethod)}.
     */
    public static IOpenMethod[] testers(IOpenMethod tested) {

        List<IOpenMethod> res = new ArrayList<IOpenMethod>();
        for (IOpenMethod tester : tested.getDeclaringClass().getMethods()) {
            if (isMethodTestedBy(tested, tester)) {
                res.add(tester);
            }

        }

        return res.toArray(new IOpenMethod[res.size()]);
    }

    /**
     * If tester is an instance of {@link TestSuiteMethod} and tested method
     * object in tester is equal to tested we consider tester is test for tested
     * method.
     */
    public static boolean isTestForMethod(IOpenMethod tester, IOpenMethod tested) {
        if (!(tester instanceof TestSuiteMethod)) {
            return false;
        }
        IOpenMethod toTest = ((TestSuiteMethod) tester).getTestedMethod();
        if (toTest == tested) {
            return true;
        }
        if (toTest instanceof OpenMethodDispatcher) {
            if (((OpenMethodDispatcher) toTest).getCandidates().contains(tested)) {
                return true;
            }
        }
        if (tested instanceof MethodDelegator) {
            return isTestForMethod(tester, tested.getMethod());
        }
        if (tested instanceof OpenMethodDispatcher) {
            return isTestForMethod(tester, ((OpenMethodDispatcher) tested).getTargetMethod());
        }
        return false;
    }

    public static String createTestName(IOpenMethod testMethod) {
        String name = getTestName(testMethod);
        String info = getTestInfo(testMethod);
        return String.format("%s (%s)", name, info);
    }

    public static String getTestName(IOpenMethod testMethod) {
        IMemberMetaInfo mi = testMethod.getInfo();
        TableSyntaxNode tnode = (TableSyntaxNode) mi.getSyntaxNode();
        return TableSyntaxNodeUtils.getTableDisplayValue(tnode)[INamedThing.SHORT];
    }

    public static String getTestInfo(IOpenMethod testMethod) {
        String info = null;

        if (testMethod instanceof TestSuiteMethod) {
            TestSuiteMethod testSuite = (TestSuiteMethod) testMethod;
            if (testSuite.isRunmethod()) {
                if (testSuite.nUnitRuns() < 1) {
                    info = formatTestInfo(NO, RUNS);
                } else {
                    info = formatTestInfo(testSuite.nUnitRuns(), RUNS);
                }
            } else {
                if (testSuite.getNumberOfTests() < 1) {
                    info = formatTestInfo(NO, TEST_CASES);
                } else {
                    info = formatTestInfo(testSuite.getNumberOfTests(), TEST_CASES);
                }
            }
        }

        return info;
    }

    public static String getTestInfo(TestSuite testSuite) {
        String info = null;
        TestSuiteMethod testSuiteMethod = testSuite.getTestSuiteMethod();
        if (testSuiteMethod != null) {
            if (testSuiteMethod.isRunmethod()) {
                if (testSuite.getNumberOfTests() < 1) {
                    info = formatTestInfo(NO, RUNS);
                } else {
                    info = formatTestInfo(testSuite.getNumberOfTests(), RUNS);
                }
            } else {
                if (testSuite.getNumberOfTests() < 1) {
                    info = formatTestInfo(NO, TEST_CASES);
                } else {
                    info = formatTestInfo(testSuite.getNumberOfTests(), TEST_CASES);
                }
            }
        }

        return info;
    }

    private static String formatTestInfo(Object param1, Object param2) {
        return String.format("%s %s", param1, param2);
    }

}
