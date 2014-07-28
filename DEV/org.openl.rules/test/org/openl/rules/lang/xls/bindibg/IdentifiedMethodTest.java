package org.openl.rules.lang.xls.bindibg;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import org.openl.meta.DoubleValue;
import org.openl.rules.TestHelper;
import org.openl.rules.context.IRulesRuntimeContext;
import org.openl.rules.context.IRulesRuntimeContextProvider;
import org.openl.rules.table.constraints.Constraint;
import org.openl.rules.table.constraints.RegexpValueConstraint;
import org.openl.rules.table.constraints.UniqueInModuleConstraint;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;

public class IdentifiedMethodTest {

    public interface ITestI extends IRulesRuntimeContextProvider {
        DoubleValue driverRiskScoreOverloadTest(String driverRisk);

        DoubleValue driverRiskScoreNoOverloadTest(String driverRisk);

        DoubleValue driverRiskEarlier(String driverRisk);
    }

    @Test
    public void testCallById() {
        File xlsFile = new File("test/rules/overload/Overload.xls");
        TestHelper<ITestI> testHelper = new TestHelper<ITestI>(xlsFile, ITestI.class);

        ITestI instance = testHelper.getInstance();
        IRulesRuntimeContext context = ((IRulesRuntimeContextProvider) instance).getRuntimeContext();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2003, 5, 15);

        context.setCurrentDate(calendar.getTime());

        DoubleValue res1 = instance.driverRiskScoreOverloadTest("High Risk Driver");
        assertEquals(120.0, res1.doubleValue(), 1e-8);

        calendar.set(2008, 5, 15);
        context.setCurrentDate(calendar.getTime());

        DoubleValue res2 = instance.driverRiskScoreOverloadTest("High Risk Driver");
        assertEquals(100.0, res2.doubleValue(), 1e-8);

        // direct call to particular method avoiding method dispatcher
        calendar.set(2003, 5, 15);

        context.setCurrentDate(calendar.getTime());
        DoubleValue res2_2 = instance.driverRiskEarlier("High Risk Driver");
        assertEquals(120.0, res2_2.doubleValue(), 1e-8);
    }

    @Test
    public void testUniqueInModule() {
        List<Constraint> constraints = TablePropertyDefinitionUtils.getPropertyByName("id").getConstraints().getAll();
        for (Constraint constraint : constraints) {
            if (constraint instanceof UniqueInModuleConstraint) {
                assertTrue(true);
                return;
            }
        }
        assertTrue(false);
    }

    @Test
    public void testIdValuePatterns() {
        List<Constraint> constraints = TablePropertyDefinitionUtils.getPropertyByName("id").getConstraints().getAll();
        Constraint regexpConstraint = null;
        for (Constraint constraint : constraints) {
            if (constraint instanceof RegexpValueConstraint) {
                regexpConstraint = constraint;
                break;
            }
        }
        if (regexpConstraint == null) {
            assertTrue(false);
        } else {
            String regex = ((RegexpValueConstraint)regexpConstraint).getRegexp();
            assertTrue("_name".matches(regex));
            assertTrue("Name".matches(regex));
            assertTrue("name__999".matches(regex));
            assertFalse("9asd".matches(regex));
            assertFalse("name postfix".matches(regex));
            assertFalse("name&9".matches(regex));
        }
    }
}
