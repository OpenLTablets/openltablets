package com.exigen.rules.openl.integrator;

import java.net.URL;

import org.openl.OpenL;
import org.openl.rules.dt.DTRule;
import org.openl.rules.dt.DTRuleQuery;
import org.openl.syntax.impl.FileSourceCodeModule;
import org.openl.types.IOpenClass;

import junit.framework.TestCase;

public class RulesLoadingTest extends TestCase {

    public void testSelectRules() {

        RulePrinter rulePrinter = new RulePrinter();

        URL url = this.getClass().getClassLoader().getResource("com/exigen/rules/openl/integrator/TestRule.xls");

        IOpenClass ioc = OpenL.getInstance("org.openl.xls")
                .compileModule(new FileSourceCodeModule(url.getPath(), null));

        rulePrinter.printRule(ioc, "authority", 1);

        DTRule[] rules = DTRuleQuery.selectRulesWithParam(ioc, null, "Client Age", null);

        for (int i = 0; i < rules.length; i++) {
            rulePrinter.printDTRule(rules[i]);
            System.out.println("<p/>");
        }
    }
}