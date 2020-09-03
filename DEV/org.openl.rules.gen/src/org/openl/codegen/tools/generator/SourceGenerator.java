package org.openl.codegen.tools.generator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public final class SourceGenerator {

    private static final String VELOCITY_PROPERTIES = "velocity.properties";

    private static SourceGenerator instance;

    private VelocityGenerator generator;

    public static SourceGenerator getInstance() throws Exception {

        if (instance == null) {
            instance = new SourceGenerator();
        }

        return instance;
    }

    private SourceGenerator() throws Exception {
        init();
    }

    private void init() throws IOException {

        Properties properties = loadVelocityProperties();

        generator = VelocityGenerator.getInstance(properties);
    }

    private static Properties loadVelocityProperties() throws IOException {
        try (FileInputStream is = new FileInputStream(new File(VELOCITY_PROPERTIES))) {
            Properties properties = new Properties();
            properties.load(is);
            return properties;
        }
    }

    public void generateSource(String sourceFilePath,
            String templateName,
            Map<String, Object> variables) throws Exception {

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(sourceFilePath),
            StandardCharsets.UTF_8)) {
            String codeSnippet = generateSource(templateName, variables);
            writer.write(codeSnippet);
        }
    }

    public String generateSource(String templateName, Map<String, Object> variables) {
        return generator.generate(templateName, variables);
    }
}
