package org.openl.rules.project.resolving;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import org.openl.engine.OpenLCompileManager;
import org.openl.rules.project.ProjectDescriptorManager;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.model.ProjectDescriptor;
import org.openl.rules.project.model.validation.ValidationException;
import org.openl.rules.table.properties.ITableProperties;
import org.openl.rules.table.properties.PropertiesLoader;
import org.openl.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectDescriptorBasedResolvingStrategy implements ResolvingStrategy {

    public static final String PROJECT_DESCRIPTOR_FILE_NAME = "rules.xml";
    private static final Logger LOG = LoggerFactory.getLogger(ProjectDescriptorBasedResolvingStrategy.class);

    @Override
    public boolean isRulesProject(File folder) {
        File descriptorFile = new File(folder, PROJECT_DESCRIPTOR_FILE_NAME);
        if (descriptorFile.exists()) {
            LOG.debug("Project in {} folder has been resolved as Project descriptor based project.", folder.getPath());
            return true;
        } else {
            LOG.debug(
                "Project descriptor based strategy has failed to resolve project folder {}: there is no file {} in folder.",
                folder.getPath(),
                PROJECT_DESCRIPTOR_FILE_NAME);
            return false;
        }
    }

    @Override
    public ProjectDescriptor resolveProject(File folder) throws ProjectResolvingException {
        File descriptorFile = new File(folder, PROJECT_DESCRIPTOR_FILE_NAME);
        ProjectDescriptorManager descriptorManager = new ProjectDescriptorManager();
        Set<String> globalErrorMessages = new LinkedHashSet<>();
        PropertiesFileNameProcessorBuilder propertiesFileNameProcessorBuilder = new PropertiesFileNameProcessorBuilder();
        try {
            ProjectDescriptor projectDescriptor = descriptorManager.readDescriptor(descriptorFile);
            PropertiesFileNameProcessor processor = null;
            if (StringUtils.isNotBlank(projectDescriptor.getPropertiesFileNameProcessor())) {
                try {
                    processor = propertiesFileNameProcessorBuilder.buildCustomProcessor(projectDescriptor);
                } catch (InvalidFileNameProcessorException e1) {
                    String message = e1.getMessage();
                    LOG.warn(message);
                    globalErrorMessages.add(message);
                }
            } else {
                if (StringUtils.isNotBlank(projectDescriptor.getPropertiesFileNamePattern())) {
                    processor = propertiesFileNameProcessorBuilder.buildDefaultProcessor(projectDescriptor);
                }
            }
            if (processor != null) {
                Set<String> globalWarnMessages = new LinkedHashSet<>();
                if (processor instanceof CWPropertyFileNameProcessor) {
                    globalWarnMessages.add("CWPropertyFileNameProcessor is deprecated. 'CW' keyword support for 'state' property was moved to the default property processor. Delete declaration of this class from rules.xml");
                }
                for (Module module : projectDescriptor.getModules()) {
                    Set<String> moduleErrorMessages = new HashSet<>(globalErrorMessages);
                    Set<String> moduleWarnMessages = new HashSet<>(globalWarnMessages);
                    Map<String, Object> params = new HashMap<>();
                    try {
                        ITableProperties tableProperties = processor.process(module,
                            projectDescriptor.getPropertiesFileNamePattern());
                        params.put(PropertiesLoader.EXTERNAL_MODULE_PROPERTIES_KEY, tableProperties);
                    } catch (NoMatchFileNameException e) {
                        String moduleFileName = FilenameExtractorUtil.extractFileNameFromModule(module);
                        String defaultMessage;
                        if (projectDescriptor.getPropertiesFileNamePattern() != null) {
                            defaultMessage = "Module file name '" + moduleFileName + "' does not match file name pattern! File name pattern is: " + projectDescriptor
                                .getPropertiesFileNamePattern();
                        } else {
                            defaultMessage = "Module file name '" + moduleFileName + "' does not match file name pattern.";
                        }

                        if (e.getMessage() == null) {
                            moduleWarnMessages.add(defaultMessage);
                        } else {
                            if (!(processor instanceof DefaultPropertiesFileNameProcessor)) {
                                moduleWarnMessages.add(
                                    "Module file name '" + moduleFileName + "' does not match to file name pattern! " + e
                                        .getMessage());
                            } else {
                                moduleWarnMessages.add(e.getMessage());
                            }
                        }
                    } catch (InvalidFileNamePatternException e) {
                        if (e.getMessage() == null) {
                            moduleErrorMessages.add("Wrong file name pattern.");
                        } else {
                            if (!(processor instanceof DefaultPropertiesFileNameProcessor)) {
                                moduleErrorMessages.add("Wrong file name pattern! " + e.getMessage());
                            } else {
                                moduleErrorMessages.add(e.getMessage());
                            }
                        }
                    } catch (Exception | LinkageError e) {
                        LOG.warn("Failed to load custom file name processor.", e);
                        moduleErrorMessages.add("Failed to load custom file name processor.");
                    }
                    params.put(OpenLCompileManager.ADDITIONAL_ERROR_MESSAGES_KEY, moduleErrorMessages);
                    params.put(OpenLCompileManager.ADDITIONAL_WARN_MESSAGES_KEY, moduleWarnMessages);
                    module.setProperties(params);
                }
            }
            return projectDescriptor;
        } catch (ValidationException ex) {
            throw new ProjectResolvingException(
                "Project descriptor is wrong. Please, verify '" + PROJECT_DESCRIPTOR_FILE_NAME + "' file format.",
                ex);
        } catch (FileNotFoundException e) {
            throw new ProjectResolvingException(
                "Project descriptor is not found! Project must contain '" + PROJECT_DESCRIPTOR_FILE_NAME + "' file.",
                e);
        } catch (Exception e) {
            throw new ProjectResolvingException("Failed to read project descriptor.", e);
        } finally {
            propertiesFileNameProcessorBuilder.destroy();
        }
    }
}
