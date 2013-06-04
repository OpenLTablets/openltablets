package template;

import org.openl.rules.runtime.RulesEngineFactory;

import java.io.File;

/**
 * This class shows how to execute OpenL Tablets methods using Java wrapper.
 * Looks really simple...
 */
public class Main {

    public static void main(String[] args) {
        File xlsFile = new File("src/main/resources/rules/TemplateRules.xls");

        RulesEngineFactory<Wrapper> engineFactory = new RulesEngineFactory<Wrapper>(xlsFile, Wrapper.class);

        Wrapper instance = engineFactory.newEngineInstance();
        instance.hello1(10);
    }
}
