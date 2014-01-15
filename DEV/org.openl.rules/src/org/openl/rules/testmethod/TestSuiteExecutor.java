package org.openl.rules.testmethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.engine.OpenLSystemProperties;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class TestSuiteExecutor {
    private static final int QUEUE_SIZE = 2000;
    public static final int DEFAULT_THREAD_COUNT = 4;

    private static volatile TestSuiteExecutor instance;
    private static Map<String, Object> externalParameters;

    private final ThreadPoolExecutor executor;
    private final int threadCount;

    /**
     * Set parameters needed to configure TestSuiteExecutor singleton.
     * Should be invoked during application configuration step for example when application starts.
     * If not used, default values will be applied.
     * If invoked after instance was created, {@link #shutDown()} should be invoked to reinstantiate it.
     *
     * @param externalParameters parameters needed to configure TestSuiteExecutor singleton
     */
    public static void setExternalParameters(Map<String, Object> externalParameters) {
        TestSuiteExecutor.externalParameters = externalParameters;
    }

    /**
     * Get TestSuiteExecutor singleton instance.
     * Use {@link #setExternalParameters(java.util.Map)} to configure TestSuiteExecutor instance.
     *
     * @return TestSuiteExecutor singleton
     */
    public static TestSuiteExecutor getInstance() {
        if (instance == null) {
            synchronized (TestSuiteExecutor.class) {
                if (instance == null) {
                    instance = new TestSuiteExecutor(externalParameters);
                }
            }
        }

        return instance;
    }

    /**
     * Shut down current executor instance. Attempts to stop all actively executing tasks.
     */
    public static void shutDown() {
        synchronized (TestSuiteExecutor.class) {
            if (TestSuiteExecutor.instance != null) {
                TestSuiteExecutor.instance.executor.shutdownNow();
                TestSuiteExecutor.instance = null;
            }
        }
    }

    /**
     * Create TestSuiteExecutor instance. If externalParameters is null, default values will be used to configure
     * the object.
     *
     * @param externalParameters parameters needed to configure TestSuiteExecutor singleton
     */
    private TestSuiteExecutor(Map<String, Object> externalParameters) {
        int testRunThreadCount;
        ThreadPoolExecutor threadPoolExecutor;
        try {
            testRunThreadCount = OpenLSystemProperties.getTestRunThreadCount(externalParameters);
            threadPoolExecutor = new ThreadPoolExecutor(testRunThreadCount,
                    testRunThreadCount,
                    1l,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<Runnable>(QUEUE_SIZE),
                    new ThreadPoolExecutor.CallerRunsPolicy());
        } catch (Exception e) {
            Log log = LogFactory.getLog(TestSuiteExecutor.class);
            if (log.isErrorEnabled()) {
                log.error("Exception while configuring ThreadPoolExecutor. Default thread count will be used.", e);
            }
            testRunThreadCount = DEFAULT_THREAD_COUNT;
            threadPoolExecutor = new ThreadPoolExecutor(testRunThreadCount,
                    testRunThreadCount,
                    1l,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<Runnable>(QUEUE_SIZE),
                    new ThreadPoolExecutor.CallerRunsPolicy());
        }

        threadPoolExecutor.allowCoreThreadTimeOut(true);

        threadCount = testRunThreadCount;
        executor = threadPoolExecutor;
    }

    public Executor getExecutor() {
        return executor;
    }

    public int getThreadCount() {
        return threadCount;
    }
}
