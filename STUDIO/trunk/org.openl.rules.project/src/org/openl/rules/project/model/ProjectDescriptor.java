package org.openl.rules.project.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.rules.convertor.String2DataConvertorFactory;
import org.openl.types.java.JavaOpenClass;

public class ProjectDescriptor {
	private final Log log = LogFactory.getLog(ProjectDescriptor.class);
	private String id;
	private String name;
	private String comment;
	private File projectFolder;
	private List<Module> modules;
	private List<PathEntry> classpath;
	private ClassLoader classLoader;
	private Configuration configuration;

	public Configuration getConfiguration() {
		if (configuration == null) {
			this.configuration = new Configuration();
		}
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

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
	 * @param reload
	 *            Boolean flag that indicates whether classloader must be
	 *            reloaded or used existing.
	 * @return ClassLoader for this project.
	 * @deprecated Must be removed to separate class. Project descriptor is just
	 *             description of project and must be simple java bean.
	 */
	public ClassLoader getClassLoader(boolean reload) {
		if (classLoader == null || reload) {
			unregisterClassloader(classLoader);
			URL[] urls = new URL[0];
			// temporary commented. as we extends the strategies classloaders
			// with this URLS.
			// it is done to ensure that wrapper class will be loaded with
			// strategy classloader.
			// URL[] urls = getClassPathUrls();
			classLoader = new URLClassLoader(urls, this.getClass()
					.getClassLoader());
		}
		return classLoader;
	}

	/**
	 * @deprecated Must be removed to separate class. Project descriptor is just
	 *             description of project and must be simple java bean.
	 * 
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public URL[] getClassPathUrls() {
		if (classpath == null) {
			return new URL[] {};
		}
		URL[] urls = new URL[classpath.size()];

		int i = 0;

		for (PathEntry entry : classpath) {
			File f = new File(entry.getPath());

			try {
				if (f.isAbsolute()) {
					urls[i] = f.toURI().toURL();
				} else {
					urls[i] = new File(projectFolder, entry.getPath()).toURI()
							.toURL();
				}
				i++;
			} catch (MalformedURLException e) {
				log.error("Bad URL in classpath \"" + entry.getPath() + "\"");
			}
		}
		return urls;
	}

	/**
	 * Class loader of current project have to be unregistered if it is not in
	 * use to prevent memory leaks.
	 * 
	 * @param classLoader
	 *            ClassLoader to unregister.
	 * 
	 * @deprecated Must be removed to separate class. Project descriptor is just
	 *             description of project and must be simple java bean.
	 * 
	 */
	private void unregisterClassloader(ClassLoader classLoader) {
		if (classLoader != null) {
			JavaOpenClass.resetClassloader(classLoader);
			LogFactory.release(classLoader);
			String2DataConvertorFactory.unregisterClassLoader(classLoader);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		unregisterClassloader(classLoader);
		super.finalize();
	}
}
