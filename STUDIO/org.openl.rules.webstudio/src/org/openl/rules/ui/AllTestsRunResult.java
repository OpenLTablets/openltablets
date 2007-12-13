package org.openl.rules.ui;

import org.openl.rules.testmethod.TestMethodHelper.TestMethodTestAll;
import org.openl.rules.testmethod.TestResult;

import org.openl.types.IOpenMethod;

import org.openl.vm.IRuntimeEnv;

import java.util.Arrays;
import java.util.Comparator;


/**
 * DOCUMENT ME!
 *
 * @author Stanislav Shor
 */
public class AllTestsRunResult {
    Test[] tests;

    public AllTestsRunResult(IOpenMethod[] methods, String[] names) {
        tests = new Test[methods.length];
        for (int i = 0; i < names.length; i++) {
            tests[i] = new Test();
            tests[i].method = (TestMethodTestAll) methods[i];
            tests[i].testName = names[i];
        }
    }

    public int numberOfFailedTests() {
        int cnt = 0;
        for (int i = 0; i < tests.length; i++) {
            cnt += ((tests[i].result.getNumberOfFailures() > 0) ? 1 : 0);
        }

        return cnt;
    }

    public int totalNumberOfTestUnits() {
        int cnt = 0;
        for (int i = 0; i < tests.length; i++) {
            cnt += tests[i].result.getNumberOfTests();
        }
        return cnt;
    }

    public int totalNumberOfFailures() {
        int cnt = 0;
        for (int i = 0; i < tests.length; i++) {
            cnt += tests[i].result.getNumberOfFailures();
        }
        return cnt;
    }

    public Test[] getTests() {
        Comparator<Test> c = new Comparator<Test>() {
                public int compare(Test t1, Test t2) {
                    if ((t2.result != null) && (t1.result != null)) {
                        int cmp = t2.result
                                .getNumberOfFailures() - t1.result.getNumberOfFailures();
                        if (cmp != 0) {
                            return cmp;
                        }
                    }

                    return t1.testName.compareTo(t2.testName);
                }
            };
        Arrays.sort(tests, c);

        return this.tests;
    }

    public void setTests(Test[] tests) {
        this.tests = tests;
    }

    /**
     * DOCUMENT ME!
     *
     * @param ttr
     */
    public void setResults(TestResult[] ttr) {
        for (int i = 0; i < ttr.length; i++) {
            tests[i].result = ttr[i];
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param testName
     * @param tid
     * @param target DOCUMENT ME!
     * @param env DOCUMENT ME!
     * @param ntimes DOCUMENT ME!
     *
     * @return
     */
    public Object run(String testName, int tid, Object target, IRuntimeEnv env, int ntimes) {
        Test test = findTest(testName);
        return test.run(tid, target, env, ntimes);
    }

    /**
     * DOCUMENT ME!
     *
     * @param testName
     *
     * @return
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    private Test findTest(String testName) {
        for (int i = 0; i < tests.length; i++) {
            if (tests[i].testName.equals(testName)) {
                return tests[i];
            }
        }

        throw new RuntimeException("Test " + testName + " not found");
    }

    public static class Test {
        TestMethodTestAll method;
        TestResult result;
        String testName;

        public int ntests() {
            return method.getNumberOfTests();
        }

        public String getTestDescription(int i) {
            return method.getTestDescriptions()[i];
        }

        public IOpenMethod getMethod() {
            return this.method;
        }

        public TestResult getResult() {
            return this.result;
        }

        public String getTestName() {
            return this.testName;
        }

        /**
         * DOCUMENT ME!
         *
         * @param tid
         * @param target
         * @param env
         * @param ntimes DOCUMENT ME!
         *
         * @return
         */
        public Object run(int tid, Object target, IRuntimeEnv env, int ntimes) {
            return method.run(tid, target, env, ntimes);
        }
    }
}
