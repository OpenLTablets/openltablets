package org.openl.rules.project.resolving;

import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.lang.StringUtils;
import org.openl.classloader.ClassLoaderCloserFactory;
import org.openl.rules.project.model.ProjectDescriptor;

public final class PropertiesFileNameProcessorBuilder {
    public PropertiesFileNameProcessorBuilder() {
    }

    PropertiesFileNameProcessor processor;
    
    public PropertiesFileNameProcessor build(ProjectDescriptor projectDescriptor) throws InvalidFileNameProcessorException {
        if (processor != null){
            throw new IllegalStateException("Processor has already built! Use a new builder!");
        }
        if (!StringUtils.isBlank(projectDescriptor.getPropertiesFileNameProcessor())) {
            processor = buildCustomProcessor(projectDescriptor);
        } else {
            processor = buildDefaultProcessor(projectDescriptor);
        }
        return processor;
    }

    public PropertiesFileNameProcessor buildCustomProcessor(ProjectDescriptor projectDescriptor) throws InvalidFileNameProcessorException {
        if (processor != null){
            throw new IllegalStateException("Processor has already built! Use a new builder!");
        }
        ClassLoader classLoader = getCustomClassLoader(projectDescriptor);
        try {
            Class<?> clazz = classLoader.loadClass(projectDescriptor.getPropertiesFileNameProcessor());
            processor = (PropertiesFileNameProcessor) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            String message = "Properties file name processor class '" + projectDescriptor.getPropertiesFileNameProcessor() + "' wasn't found!";
            throw new InvalidFileNameProcessorException(message, e);
        } catch (Exception e) {
            String message = "Failed to instantiate default properties file name processor! Class should have default constructor and implement org.openl.rules.project.resolving.PropertiesFileNameProcessor interface!";
            throw new InvalidFileNameProcessorException(message, e);
        }
        return processor;
    }

    public PropertiesFileNameProcessor buildDefaultProcessor(ProjectDescriptor projectDescriptor) throws InvalidFileNameProcessorException {
        if (processor != null){
            throw new IllegalStateException("Processor has already built! Use a new builder!");
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> clazz = classLoader.loadClass("org.openl.rules.project.resolving.DefaultPropertiesFileNameProcessor");
            return (PropertiesFileNameProcessor) clazz.newInstance();
        } catch (Exception e) {
            // Should not occur in normal situation.
            throw new InvalidFileNameProcessorException(e);
        }
    }

    public void destroy() {
        if (classLoader != null){
            ClassLoaderCloserFactory.getClassLoaderCloser().close(classLoader);
        }
    }

    URLClassLoader classLoader;
    
    protected ClassLoader getCustomClassLoader(ProjectDescriptor projectDescriptor) {
        URL[] urls = projectDescriptor.getClassPathUrls();
        classLoader = new URLClassLoader(urls, PropertiesFileNameProcessorBuilder.class.getClassLoader());
        return classLoader;
    }
}
