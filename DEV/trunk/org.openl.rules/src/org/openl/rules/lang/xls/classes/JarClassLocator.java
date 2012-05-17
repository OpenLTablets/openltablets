package org.openl.rules.lang.xls.classes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Finds a classes in a given jar
 * 
 * @author NSamatov
 */
public class JarClassLocator implements ClassLocator {
    private final List<LocatorExceptionHandler> handlers;

    public JarClassLocator() {
        this(new ArrayList<LocatorExceptionHandler>());
    }

    public JarClassLocator(List<? extends LocatorExceptionHandler> handlers) {
        this.handlers = new ArrayList<LocatorExceptionHandler>(handlers);
    }

    /**
     * Add exception handler
     * 
     * @param handler exception handler
     */
    public void addExceptionHandler(LocatorExceptionHandler handler) {
        handlers.add(handler);
    }

    /**
     * Find all classes in a given path. If a class cannot be loaded, it is
     * skipped (in our case we don't need such classes).
     * 
     * @param pathURL path to the jar
     * @param packageName The package name for classes found inside the path
     * @param classLoader a ClassLoader that is used to load a classes
     * @return Found classes
     */
    @Override
    public Collection<Class<?>> getClasses(URL pathURL, String packageName, ClassLoader classLoader) {
        String jarPath = pathURL.getFile().split("!")[0];
        URL jar;
        try {
            jar = new URL(jarPath);
        } catch (MalformedURLException e) {
            for (LocatorExceptionHandler handler : handlers) {
                handler.handleURLParseException(e);
            }
            return Collections.emptySet();
        }

        Set<Class<?>> classes = new HashSet<Class<?>>();
        ZipInputStream zip = null;
        try {
            zip = new ZipInputStream(jar.openStream());
            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace(".class", "").replace('/', '.');
                    if (className.startsWith(packageName) && !className.contains("$")) {
                        try {
                            classes.add(Class.forName(className, true, classLoader));
                        } catch (Throwable t) {
                            for (LocatorExceptionHandler handler : handlers) {
                                handler.handleClassInstatiateException(t);
                            }
                            continue;
                        }
                    }
                }
            }
        } catch (IOException e) {
            for (LocatorExceptionHandler handler : handlers) {
                handler.handleIOException(e);
            }
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    for (LocatorExceptionHandler handler : handlers) {
                        handler.handleIOException(e);
                    }
                }
            }
        }
        return classes;
    }

}
