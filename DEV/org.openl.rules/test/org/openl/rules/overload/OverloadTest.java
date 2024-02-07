package org.openl.rules.overload;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;

import org.junit.jupiter.api.Test;

import org.openl.rules.TestUtils;
import org.openl.rules.context.IRulesRuntimeContext;
import org.openl.rules.context.IRulesRuntimeContextProvider;

public class OverloadTest {

    @Test
    public void testMethodOverload() {
        ITestI instance = TestUtils.create("test/rules/overload/Overload.xls", ITestI.class);
        IRulesRuntimeContext context = instance.getRuntimeContext();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2003, 5, 15);

        context.setCurrentDate(calendar.getTime());

        Double res1 = instance.driverRiskScoreOverloadTest("High Risk Driver");
        assertEquals(120.0, res1.doubleValue(), 1e-8);

        calendar.set(2008, 5, 15);
        context.setCurrentDate(calendar.getTime());

        Double res2 = instance.driverRiskScoreOverloadTest("High Risk Driver");
        assertEquals(100.0, res2.doubleValue(), 1e-8);

        Double res3 = instance.driverRiskScoreNoOverloadTest("High Risk Driver");
        assertEquals(200.0, res3.doubleValue(), 1e-8);
    }

    public interface ITestI extends IRulesRuntimeContextProvider {
        Double driverRiskScoreOverloadTest(String driverRisk);

        Double driverRiskScoreNoOverloadTest(String driverRisk);
    }
}
