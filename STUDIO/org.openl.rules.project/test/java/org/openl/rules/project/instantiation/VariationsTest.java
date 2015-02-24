package org.openl.rules.project.instantiation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.openl.generated.test.beans.Driver;
import org.openl.generated.test.beans.Policy;
import org.openl.meta.DoubleValue;
import org.openl.rules.calc.SpreadsheetResult;
import org.openl.rules.project.instantiation.variation.VariationInstantiationStrategyEnhancer;
import org.openl.rules.project.instantiation.variation.VariationInstantiationStrategyEnhancerHelper;
import org.openl.rules.project.model.ProjectDescriptor;
import org.openl.rules.project.resolving.RulesProjectResolver;
import org.openl.rules.variation.ArgumentReplacementVariation;
import org.openl.rules.variation.DeepCloningVariation;
import org.openl.rules.variation.JXPathVariation;
import org.openl.rules.variation.NoVariation;
import org.openl.rules.variation.Variation;
import org.openl.rules.variation.VariationDescription;
import org.openl.rules.variation.VariationsFactory;
import org.openl.rules.variation.VariationsFromRules;
import org.openl.rules.variation.VariationsPack;
import org.openl.rules.variation.VariationsResult;

public class VariationsTest {
    private static final String TEST_PROJECT_FOLDER = "test/resources/dependencies/test4/module/dependency-module1";
    public static final String STANDART = "Standard Driver";
    public static final String YOUNG = "Young Driver";
    public static final String SENOIR = "Senior Driver";
    private RulesProjectResolver projectResolver = RulesProjectResolver.loadProjectResolverFromClassPath();;
    private WrapperAdjustingInstantiationStrategy instantiationStrategy;

    @Before
    public void init() throws Exception {
        File tut4Folder = new File(TEST_PROJECT_FOLDER);
        ProjectDescriptor project = projectResolver.isRulesProject(tut4Folder).resolveProject(tut4Folder);
        instantiationStrategy = new WrapperAdjustingInstantiationStrategy(project.getModules().get(0), true, null);
    }

    @Test
    public void testEnhancement() throws Exception {
        // without interface
        VariationInstantiationStrategyEnhancer variationsEnhancer = new VariationInstantiationStrategyEnhancer(instantiationStrategy);
        assertTrue(VariationInstantiationStrategyEnhancerHelper.isDecoratedClass(variationsEnhancer.getInstanceClass()));
        assertFalse(VariationInstantiationStrategyEnhancerHelper.isDecoratedClass(instantiationStrategy.getInstanceClass()));

        // with correct interface
        VariationInstantiationStrategyEnhancer variationsEnhancerWithInterface = new VariationInstantiationStrategyEnhancer(instantiationStrategy);
        variationsEnhancerWithInterface.setServiceClass(EnhancedInterface.class);
        assertTrue(VariationInstantiationStrategyEnhancerHelper.isDecoratedClass(variationsEnhancerWithInterface.getInstanceClass()));
        assertFalse(VariationInstantiationStrategyEnhancerHelper.isDecoratedClass(instantiationStrategy.getInstanceClass()));

        // with wrong interface
        // we use simple api instantiation strategy because wrapper
        // instantiation strategy always has service class that can not be
        // modified
        File folder = new File(new File(TEST_PROJECT_FOLDER, "rules"), "main");
        ProjectDescriptor project = projectResolver.isRulesProject(folder).resolveProject(folder);
        ApiBasedInstantiationStrategy instantiationStrategy = new ApiBasedInstantiationStrategy(project.getModules()
                .get(0), true, null);
        VariationInstantiationStrategyEnhancer variationsEnhancerWithWrongInterface = new VariationInstantiationStrategyEnhancer(instantiationStrategy);
        variationsEnhancerWithWrongInterface.setServiceClass(WrongEnhancedInterface.class);
        try {
            variationsEnhancerWithWrongInterface.instantiate();
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Failed to find corresrponding method in original class for method"));
        }
    }

    @Test
    public void testVariationsFactory() {
        Variation argumentReplacementVariation = VariationsFactory.getVariation("changeArg", 2, ".", new Object());
        assertTrue(argumentReplacementVariation instanceof ArgumentReplacementVariation);
        assertEquals(((ArgumentReplacementVariation) argumentReplacementVariation).getUpdatedArgumentIndex(), 2);
        String path = "drivers[name = 'Sara']/age";
        Variation jxpathVariation = VariationsFactory.getVariation("jxpath", 3, path, new Object());
        assertTrue(jxpathVariation instanceof JXPathVariation);
        assertEquals(((JXPathVariation) jxpathVariation).getPath(), path);
        Variation cloningVariation = VariationsFactory.getVariation("clone", 1, path, new Object(), true);
        assertTrue(((DeepCloningVariation) cloningVariation).getDelegatedVariation() instanceof JXPathVariation);
        assertTrue(cloningVariation instanceof DeepCloningVariation);
        assertTrue(((DeepCloningVariation) cloningVariation).getDelegatedVariation() instanceof JXPathVariation);
    }

    @Test
    public void testVariationsFromRules() throws Exception {
        File folder = new File(new File(TEST_PROJECT_FOLDER, "rules"), "main");
        ProjectDescriptor project = projectResolver.isRulesProject(folder).resolveProject(folder);
        ApiBasedInstantiationStrategy instantiationStrategy = new ApiBasedInstantiationStrategy(project.getModules()
                .get(0), true, null);
        VariationInstantiationStrategyEnhancer variationsEnhancer = new VariationInstantiationStrategyEnhancer(instantiationStrategy);
        variationsEnhancer.setServiceClass(EnhancedInterface2.class);
        EnhancedInterface2 instance = (EnhancedInterface2) variationsEnhancer.instantiate();
        Policy policy = instance.getPolicyProfile1()[0];
        VariationsPack variations = new VariationsPack(
                new JXPathVariation("young", 0, "drivers[name = 'Sara']/age", 17));
        VariationsResult<SpreadsheetResult> resultsPolicies = instance.processPolicy(policy, variations);
        int variationsCount = policy.getDrivers().length + 2/*
                                                             * original
                                                             * calculation
                                                             * +young
                                                             */;
        assertEquals(variationsCount, resultsPolicies.getAllProcessedVariationIDs().length);
        assertTrue(resultsPolicies.getVariationFailures().isEmpty());
        assertEquals(new DoubleValue(1390),
                resultsPolicies.getResultForVariation("young").getFieldValue("$Value$Premium"));
        assertEquals(new DoubleValue(990),
                resultsPolicies.getResultForVariation("variaitionForDriver2").getFieldValue("$Value$Premium"));
        assertEquals(new DoubleValue(1290), resultsPolicies.getResultForVariation("variaitionForDriver1")
                .getFieldValue("$Value$Premium"));
        assertEquals(new DoubleValue(1090), resultsPolicies.getResultForVariation(NoVariation.ORIGINAL_CALCULATION)
                .getFieldValue("$Value$Premium"));
        for (String id : resultsPolicies.getCalculatedVariationIDs()) {
            System.out.println(id + " : " + resultsPolicies.getResultForVariation(id).getFieldValue("$Value$Premium"));
        }
    }

    @Test
    public void testJXPathVariation() throws Exception {
        VariationInstantiationStrategyEnhancer variationsEnhancer = new VariationInstantiationStrategyEnhancer(instantiationStrategy);
        variationsEnhancer.setServiceClass(EnhancedInterface.class);
        EnhancedInterface instance = (EnhancedInterface) variationsEnhancer.instantiate();
        Driver[] drivers = instance.getDriverProfiles1();
        VariationsResult<String> resultsDrivers = instance.driverAgeType(drivers[0], new VariationsPack(
                new JXPathVariation("young", 0, "age", 18), new JXPathVariation("senior", 0, "age", 71)));
        assertTrue(resultsDrivers.getVariationFailures().isEmpty());
        assertEquals(resultsDrivers.getResultForVariation("young"), YOUNG);
        assertEquals(resultsDrivers.getResultForVariation("senior"), SENOIR);
        assertEquals(resultsDrivers.getResultForVariation(NoVariation.ORIGINAL_CALCULATION), STANDART);
        Policy[] policies = instance.getPolicyProfile1();
        VariationsResult<SpreadsheetResult> resultsPolicies = instance.processPolicy(policies[0], new VariationsPack(
                new JXPathVariation("young", 0, "drivers[name = 'Sara']/age", 17), new JXPathVariation("senior", 0,
                        "drivers[name = 'Sara']/age", 88)));
        assertTrue(resultsPolicies.getVariationFailures().isEmpty());
        assertEquals(resultsPolicies.getResultForVariation("young").getFieldValue("$Value$Premium"), new DoubleValue(
                1390));
        assertEquals(resultsPolicies.getResultForVariation("senior").getFieldValue("$Value$Premium"), new DoubleValue(
                1290));
        assertEquals(
                resultsPolicies.getResultForVariation(NoVariation.ORIGINAL_CALCULATION)
                        .getFieldValue("$Value$Premium"), new DoubleValue(1090));
    }

    @Test
    public void testArgumentReplacementVariation() throws Exception {
        VariationInstantiationStrategyEnhancer variationsEnhancer = new VariationInstantiationStrategyEnhancer(instantiationStrategy);
        variationsEnhancer.setServiceClass(EnhancedInterface.class);
        EnhancedInterface instance = (EnhancedInterface) variationsEnhancer.instantiate();
        Driver[] drivers = instance.getDriverProfiles1();
        VariationsResult<String> variationsResult = instance.driverAgeType(drivers[0], new VariationsPack(
                new ArgumentReplacementVariation("young", 0, drivers[1])));
        assertTrue(variationsResult.getVariationFailures().isEmpty());
        assertEquals(variationsResult.getResultForVariation("young"), YOUNG);
        assertEquals(variationsResult.getResultForVariation(NoVariation.ORIGINAL_CALCULATION), STANDART);
    }

    public static interface EnhancedInterface {
        String driverAgeType(Driver driver);

        VariationsResult<String> driverAgeType(Driver driver, VariationsPack variations);

        SpreadsheetResult processPolicy(Policy policy);

        VariationsResult<SpreadsheetResult> processPolicy(Policy policy, VariationsPack variations);

        Driver[] getDriverProfiles1();

        Policy[] getPolicyProfile1();
    }

    public static interface EnhancedInterface2 {
        String driverAgeType(Driver driver);

        VariationsResult<String> driverAgeType(Driver driver, VariationsPack variations);

        SpreadsheetResult processPolicy(Policy policy);

        @VariationsFromRules(ruleName = "processPolicyVariations")
        VariationsResult<SpreadsheetResult> processPolicy(Policy policy, VariationsPack variations);

        VariationDescription[] processPolicyVariations(Policy policy);

        Driver[] getDriverProfiles1();

        Policy[] getPolicyProfile1();
    }

    // without original methods.
    public static interface WrongEnhancedInterface {
        VariationsResult<String> driverAgeType(Driver driver, VariationsPack variations);

        VariationsResult<SpreadsheetResult> processPolicy(Policy policy, VariationsPack variations);

        Driver[] getDriverProfiles1();

        Policy[] getPolicyProfile1();
    }
}
