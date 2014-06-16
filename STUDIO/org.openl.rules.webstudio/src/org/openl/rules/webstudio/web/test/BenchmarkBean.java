package org.openl.rules.webstudio.web.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.openl.rules.lang.xls.syntax.TableUtils;
import org.openl.rules.testmethod.TestSuite;
import org.openl.rules.ui.BenchmarkInfoView;
import org.openl.rules.ui.ProjectHelper;
import org.openl.rules.ui.WebStudio;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.types.IOpenMethod;
import org.openl.util.benchmark.BenchmarkInfo;
import org.openl.util.benchmark.BenchmarkOrder;

@ManagedBean
@SessionScoped
public class BenchmarkBean {
    private WebStudio studio;
    private ArrayList<BenchmarkInfoView> benchmarkResults = new ArrayList<BenchmarkInfoView>();
    private BenchmarkInfo[] comparedBenchmarks = new BenchmarkInfo[0];
    private BenchmarkOrder[] benchmarkOrders;

    private boolean isTestForOverallTestSuiteMethod(TestSuite testSuite) {
        return testSuite.getTestSuiteMethod() != null && testSuite.getNumberOfTests() == testSuite.getTestSuiteMethod()
                .getNumberOfTests();
    }

    public void addLastBenchmark() {
        studio = WebStudioUtils.getWebStudio();
        TestSuite testSuite = studio.getModel().popLastTest();
        String testSuiteUri = testSuite.getUri();
        IOpenMethod table = studio.getModel().getMethod(testSuiteUri);
        String tableId = TableUtils.makeTableId(testSuiteUri);
        String testName = ProjectHelper.getTestName(table);
        String testInfo = ProjectHelper.getTestInfo(table);
        if (isTestForOverallTestSuiteMethod(testSuite)) {
            try {
                BenchmarkInfo buLast = studio.getModel().benchmarkTestsSuite(testSuite, 3000);
                BenchmarkInfoView biv = new BenchmarkInfoView(buLast, tableId, testName, testInfo);
                studio.addBenchmark(biv);
                benchmarkResults.add(biv);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        } else {
            for (int i = 0; i < testSuite.getNumberOfTests(); i++) {
                try {
                    BenchmarkInfo buLast = studio.getModel().benchmarkSingleTest(testSuite, i, 3000);
                    BenchmarkInfoView biv = new BenchmarkInfoView(buLast, tableId, testName, testInfo,
                            testSuite.getTest(i).getExecutionParams());
                    studio.addBenchmark(biv);
                    benchmarkResults.add(biv);
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }

            }
        }
        comparedBenchmarks = new BenchmarkInfo[0];
    }

    public String compare() {
        compareBenchmarks();
        return null;
    }

    private void compareBenchmarks() {
        BenchmarkInfoView[] benchmarks = studio.getBenchmarks();
        comparedBenchmarks = new BenchmarkInfo[benchmarks.length];
        for (int i = 0; i < benchmarks.length; ++i) {
            if (benchmarks[i].isSelected()) {
                comparedBenchmarks[i] = benchmarks[i].getBenchmarkInfo();
            }
        }
        benchmarkOrders = BenchmarkInfo.order(comparedBenchmarks);
    }

    public String delete() {
        deleteBenchmark();
        return null;
    }

    private void deleteBenchmark() {
        BenchmarkInfoView[] benchmarks = studio.getBenchmarks();
        for (int i = benchmarks.length - 1; i >= 0; i--) {
            if (benchmarks[i].isSelected()) {
                studio.removeBenchmark(i);
                benchmarkResults.remove(i);
            }
        }
        comparedBenchmarks = new BenchmarkInfo[0];
    }

    public int getBenchmarkCount() {
        return benchmarkResults.size();
    }

    public List<BenchmarkInfoView> getBenchmarks() {
        studio = WebStudioUtils.getWebStudio();
        if (studio.getModel().hasTestSuitesToRun()) {
            addLastBenchmark();
        }
        BenchmarkInfoView[] benchmarks = studio.getBenchmarks();
        benchmarkResults.clear();
        Collections.addAll(benchmarkResults, benchmarks);
        return benchmarkResults;
    }

    public boolean isAnyBencmarkSelected() {
        for (BenchmarkInfoView bi : benchmarkResults) {
            if (bi.isSelected()) {
                return true;
            }
        }
        return false;
    }

    public boolean getAllBencmarkSelected() {
        if (benchmarkResults.isEmpty()) {
            return false;
        }
        for (BenchmarkInfoView bi : benchmarkResults) {
            if (!bi.isSelected()) {
                return false;
            }
        }
        return true;
    }

    public void setAllBencmarkSelected(boolean allBencmarkSelected) {
        for (BenchmarkInfoView bi : benchmarkResults) {
            bi.setSelected(allBencmarkSelected);
        }
    }

    public List<BenchmarkInfo> getComparedBenchmarks() {
        List<BenchmarkInfo> benchmarks = new ArrayList<BenchmarkInfo>();
        for (BenchmarkInfo comparedBenchmark : comparedBenchmarks) {
            if (comparedBenchmark != null) {
                benchmarks.add(comparedBenchmark);
            }
        }
        return benchmarks;
    }

    public int getComparedOrder(BenchmarkInfo bi) {
        return benchmarkOrders[getBenchmarkResultIndex(bi)].getOrder();
    }

    public String getComparedRatio(BenchmarkInfo bi) {
        double ratio = benchmarkOrders[getBenchmarkResultIndex(bi)].getRatio();
        return BenchmarkInfo.printDouble(ratio, 2);
    }

    public int getI(BenchmarkInfoView bi) {
        return benchmarkResults.indexOf(bi) + 1;
    }

    public String getStyleForOrder(BenchmarkInfo bi) {
        switch (benchmarkOrders[getBenchmarkResultIndex(bi)].getOrder()) {
            case 1:
                return "color: red; font-size: large;";
            case 2:
                return "color: green; font-size: medium;";
            case 3:
                return "color: blue;font-size: medium;";
        }
        return "color: black;";
    }

    public int getBenchmarkResultIndex(BenchmarkInfo bi) {
        int index = -1;
        for (int i = 0; i < benchmarkResults.size(); i++) {
            BenchmarkInfoView biv = benchmarkResults.get(i);
            if (biv.getBenchmarkInfo().equals(bi)) {
                return i;
            }
        }
        return index;
    }

}
