package org.openl.rules.project.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProjectDescriptor {
    private static final Log LOG = LogFactory.getLog(ProjectDescriptor.class);
    private String id;
    private String name;
    private String comment;
    private File projectFolder;
    private List<Module> modules;
    private List<PathEntry> classpath;
    private ClassLoader classLoader;

    public File getProjectFolder() {
        return projectFolder;
    }

    public void setProjectFolder(File projectRoot) {
        this.projectFolder = projectRoot;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    public List<PathEntry> getClasspath() {
        return classpath;
    }

    public void setClasspath(List<PathEntry> classpath) {
        this.classpath = classpath;
    }

    public Module getModuleByClassName(String className) {
        if (modules != null && className != null) {
            for (Module module : modules) {
                if (className.equals(module.getClassname())) {
                    return module;
                }
            }
        }
        return null;
    }

    /**
     * @param reload Boolean flag that indicates whether classloader must be
     *            reloaded or used existing.
     * @return ClassLoader for this project.
     */
    @SuppressWarnings("deprecation")
    public ClassLoader getClassLoader(boolean reload) {
        if (classLoader == null || reload) {
            URL[] urls = new URL[classpath.size()];

            int i = 0;

            for (PathEntry entry : classpath) {
                File f = new File(entry.getPath());

                try {
                    if (f.isAbsolute()) {
                        urls[i] = f.toURL();
                    } else {
                        urls[i] = new File(projectFolder, entry.getPath()).toURL();
                    }
                    i++;
                } catch (MalformedURLException e) {
                    LOG.error("Bad URL in classpath \"" + entry.getPath() + "\"");
                }
            }
            classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
        }
        return classLoader;
    }
}
