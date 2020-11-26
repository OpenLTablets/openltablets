package org.openl.codegen.tools;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openl.codegen.FileCodeGen;
import org.openl.codegen.ICodeGenAdaptor;
import org.openl.codegen.JavaCodeGen;
import org.openl.codegen.tools.generator.SourceGenerator;
import org.openl.codegen.tools.loader.IContextPropertyDefinitionLoader;
import org.openl.codegen.tools.loader.ITablePropertyDefinitionLoader;
import org.openl.codegen.tools.loader.ITablesPriorityLoader;
import org.openl.codegen.tools.type.ContextPropertyDefinitionWrappers;
import org.openl.codegen.tools.type.TablePriorityRuleWrappers;
import org.openl.codegen.tools.type.TablePropertyDefinitionWrapper;
import org.openl.codegen.tools.type.TablePropertyDefinitionWrappers;
import org.openl.codegen.tools.type.TablePropertyValidatorsWrapper;
import org.openl.codegen.tools.type.TablePropertyValidatorsWrappers;
import org.openl.rules.context.DefaultRulesRuntimeContext;
import org.openl.rules.context.IRulesRuntimeContext;
import org.openl.rules.context.properties.ContextPropertyDefinition;
import org.openl.rules.runtime.RulesEngineFactory;
import org.openl.rules.table.properties.ITableProperties;
import org.openl.rules.table.properties.TableProperties;
import org.openl.rules.table.properties.def.DefaultPropertyDefinitions;
import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.types.impl.DefaultPropertiesContextMatcher;
import org.openl.rules.types.impl.DefaultPropertiesIntersectionFinder;
import org.openl.rules.types.impl.DefaultTablePropertiesSorter;
import org.openl.rules.types.impl.MatchingOpenMethodDispatcher;
import org.openl.xls.RulesCompileContext;

public class GenRulesCode {

    private static final String TMP_FILE = null;

    private TablePropertyDefinition[] tablePropertyDefinitions;
    private ContextPropertyDefinition[] contextPropertyDefinitions;
    private String[] tablePriorityRules;

    private TablePropertyDefinitionWrappers tablePropertyDefinitionWrappers;
    private TablePropertyValidatorsWrappers tablePropertyValidatorsWrappers;
    private ContextPropertyDefinitionWrappers contextPropertyDefinitionWrappers;
    private TablePriorityRuleWrappers tablePriorityRuleWrappers;

    public static void main(String[] args) throws Exception {
        new GenRulesCode().run();
    }

    private static String getClassSourcePathInRulesModule(Class<?> clazz) {
        return CodeGenConstants.RULES_SOURCE_LOCATION + clazz.getName().replace('.', '/') + ".java";
    }

    public void run() throws Exception {

        System.out.println("Generating Rules Code...");

        loadDefinitions();

        generateRulesCompileContextCode();
        generateIRulesRuntimeContextCode();
        generateDefaultRulesRuntimeContextCode();
        generateDefaultPropertyDefinitionsCode();

        generateITablePropertiesCode();
        generateDefaultTableProperties();
        generateDefaultTablePropertiesSorter();

        generateDefaultPropertiesContextMatcherCode();
        generateDefaultPropertiesIntersectionFinderCode();
        generateMatchingOpenMethodDispatcherCode();

        System.out.println("Generating Activiti Integration Code...");
        generateIRulesRuntimeContextUtils();
    }

    private void generateDefaultPropertyDefinitionsCode() throws IOException {

        String sourceFilePath = getClassSourcePathInRulesModule(DefaultPropertyDefinitions.class);

        FileCodeGen fileGen = new FileCodeGen(sourceFilePath, TMP_FILE);
        fileGen.processFile(new ICodeGenAdaptor() {

            @Override
            public void processInsertTag(String line, StringBuilder sb) {

                JavaCodeGen jcgen = new JavaCodeGen();

                jcgen.setGenLevel(JavaCodeGen.METHOD_BODY_LEVEL);
                jcgen.genInitializeBeanArray(CodeGenConstants.DEFINITIONS_ARRAY_NAME,
                    tablePropertyDefinitions,
                    TablePropertyDefinition.class,
                    null,
                    sb);
            }

            @Override
            public void processEndInsertTag(String line, StringBuilder sb) {
            }
        });
    }

    private void generateDefaultTableProperties() throws IOException {

        Map<String, Object> variables = new HashMap<>();
        variables.put("tool", new VelocityTool());
        variables.put("tablePropertyDefinitions", tablePropertyDefinitions);

        String sourceFilePath = getClassSourcePathInRulesModule(TableProperties.class);
        processSourceCode(sourceFilePath, "DefaultTableProperties-properties.vm", variables);
    }

    private void generateITablePropertiesCode() throws IOException {

        Map<String, Object> variables = new HashMap<>();
        variables.put("tool", new VelocityTool());
        variables.put("tablePropertyDefinitions", tablePropertyDefinitions);

        String sourceFilePath = getClassSourcePathInRulesModule(ITableProperties.class);
        processSourceCode(sourceFilePath, "ITableProperties-properties.vm", variables);
    }

    private void generateDefaultRulesRuntimeContextCode() throws IOException {

        Map<String, Object> variables = new HashMap<>();
        variables.put("tool", new VelocityTool());
        variables.put("contextPropertyDefinitions", contextPropertyDefinitions);

        String sourceFilePath = getClassSourcePathInRulesModule(DefaultRulesRuntimeContext.class);

        processSourceCode(sourceFilePath, "DefaultRulesContext-properties.vm", variables);

    }

    private void generateIRulesRuntimeContextCode() throws IOException {

        Map<String, Object> variables = new HashMap<>();
        variables.put("tool", new VelocityTool());
        variables.put("contextPropertyDefinitions", contextPropertyDefinitions);

        String sourceFilePath = getClassSourcePathInRulesModule(IRulesRuntimeContext.class);
        processSourceCode(sourceFilePath, "IRulesContext-properties.vm", variables);
    }

    private void generateIRulesRuntimeContextUtils() throws IOException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("tool", new VelocityTool());
        variables.put("contextPropertyDefinitions", contextPropertyDefinitions);

        String sourceFilePath = CodeGenConstants.ACTIVITI_SOURCE_LOCATION + CodeGenConstants.ACTIVITI_IRULESRUNTIMECONTEXTUTILS_PACKAGE_PATH + CodeGenConstants.ACTIVITI_IRULESRUNTIMECONTEXTUTILS_CLASSNAME + ".java";
        processSourceCode(sourceFilePath, "IRulesRuntimeContextUtils-properties.vm", variables);
    }

    private void generateRulesCompileContextCode() throws IOException {

        List<TablePropertyValidatorsWrapper> propertyValidatorsWrappers = tablePropertyValidatorsWrappers.asList();

        Map<String, Object> variables = new HashMap<>();
        variables.put("tool", new VelocityTool());
        variables.put("validatorsDefinitions", propertyValidatorsWrappers);

        String sourceFilePath = getClassSourcePathInRulesModule(RulesCompileContext.class);
        processSourceCode(sourceFilePath, "RulesCompileContext-validators.vm", variables);
    }

    private void generateMatchingOpenMethodDispatcherCode() throws IOException {

        Map<String, Object> variables = new HashMap<>();

        List<TablePropertyDefinitionWrapper> dimensionalTablePropertyDefinitions = tablePropertyDefinitionWrappers
            .getDimensionalPropertiesWithContextVar();
        variables.put("tool", new VelocityTool());
        variables.put("tablePropertyDefinitions", dimensionalTablePropertyDefinitions);

        String sourceFilePath = getClassSourcePathInRulesModule(MatchingOpenMethodDispatcher.class);
        processSourceCode(sourceFilePath, "MatchingOpenMethodDispatcher.vm", variables);
    }

    private void generateDefaultPropertiesContextMatcherCode() throws IOException {
        Map<String, Object> variables = new HashMap<>();

        List<TablePropertyDefinitionWrapper> dimensionalTablePropertyDefinitions = tablePropertyDefinitionWrappers
            .getDimensionalPropertiesWithMatchExpression();
        variables.put("tool", new VelocityTool());
        variables.put("tablePropertyDefinitions", dimensionalTablePropertyDefinitions);
        variables.put("contextPropertyDefinitionWrappers", contextPropertyDefinitionWrappers);

        String sourceFilePath = getClassSourcePathInRulesModule(DefaultPropertiesContextMatcher.class);
        processSourceCode(sourceFilePath, "DefaultPropertiesContextMatcher-constraints.vm", variables);
    }

    private void generateDefaultPropertiesIntersectionFinderCode() throws IOException {

        Map<String, Object> variables = new HashMap<>();

        List<TablePropertyDefinitionWrapper> dimensionalTablePropertyDefinitions = tablePropertyDefinitionWrappers
            .getGapOverlapDimensionalProperties();
        variables.put("tool", new VelocityTool());
        variables.put("tablePropertyDefinitions", dimensionalTablePropertyDefinitions);

        String sourceFilePath = getClassSourcePathInRulesModule(DefaultPropertiesIntersectionFinder.class);
        processSourceCode(sourceFilePath, "DefaultPropertiesIntersectionFinder.vm", variables);
    }

    private void generateDefaultTablePropertiesSorter() throws IOException {

        Map<String, Object> variables = new HashMap<>();

        variables.put("tool", new VelocityTool());
        variables.put("priorityRuleWrappers", tablePriorityRuleWrappers);

        String sourceFilePath = getClassSourcePathInRulesModule(DefaultTablePropertiesSorter.class);
        processSourceCode(sourceFilePath, "DefaultTablePropertiesSorter-constraints.vm", variables);
    }

    protected void processSourceCode(String sourceFilePath,
            final String templateName,
            final Map<String, Object> variables) throws IOException {

        FileCodeGen fileGen = new FileCodeGen(sourceFilePath, TMP_FILE);
        fileGen.processFile(new ICodeGenAdaptor() {

            @Override
            public void processInsertTag(String line, StringBuilder sb) {

                try {
                    String codeSnippet = SourceGenerator.getInstance().generateSource(templateName, variables);
                    sb.append(codeSnippet);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void processEndInsertTag(String line, StringBuilder sb) {
            }
        });
    }

    private void loadDefinitions() {
        tablePropertyDefinitions = loadTablePropertyDefinitions();
        tablePropertyDefinitionWrappers = new TablePropertyDefinitionWrappers(tablePropertyDefinitions);
        tablePropertyValidatorsWrappers = new TablePropertyValidatorsWrappers(tablePropertyDefinitions);

        contextPropertyDefinitions = loadContextPropertyDefinitions();
        contextPropertyDefinitionWrappers = new ContextPropertyDefinitionWrappers(contextPropertyDefinitions);
        tablePriorityRules = loadTablePriorityRules();
        tablePriorityRuleWrappers = new TablePriorityRuleWrappers(tablePriorityRules);
    }

    private TablePropertyDefinition[] loadTablePropertyDefinitions() {

        RulesEngineFactory<ITablePropertyDefinitionLoader> engineFactory = new RulesEngineFactory<>(
            CodeGenConstants.DEFINITIONS_XLS,
            ITablePropertyDefinitionLoader.class);

        return engineFactory.newEngineInstance().getDefinitions();
    }

    private ContextPropertyDefinition[] loadContextPropertyDefinitions() {

        RulesEngineFactory<IContextPropertyDefinitionLoader> engineFactory = new RulesEngineFactory<>(
            CodeGenConstants.DEFINITIONS_XLS,
            IContextPropertyDefinitionLoader.class);

        return engineFactory.newEngineInstance().getContextDefinitions();
    }

    private String[] loadTablePriorityRules() {
        RulesEngineFactory<ITablesPriorityLoader> engineFactory = new RulesEngineFactory<>(
            CodeGenConstants.DEFINITIONS_XLS,
            ITablesPriorityLoader.class);

        return engineFactory.newEngineInstance().getTablesPriorityRules();
    }

}