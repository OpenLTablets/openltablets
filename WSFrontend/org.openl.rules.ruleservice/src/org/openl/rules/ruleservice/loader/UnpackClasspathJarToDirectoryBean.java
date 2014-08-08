package org.openl.rules.ruleservice.loader;

import org.apache.commons.io.FilenameUtils;
import org.openl.rules.ruleservice.core.RuleServiceRuntimeException;
import org.openl.rules.workspace.lw.impl.FolderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Bean to unpack jar with rules.xml to defined folder. This bean is used by
 * FileSystemDataSource. Set depend-on property in bean definition. This class
 * implements InitializingBean.
 *
 * @author Marat Kamalov
 */
public class UnpackClasspathJarToDirectoryBean implements InitializingBean {
    private final Logger log = LoggerFactory.getLogger(UnpackClasspathJarToDirectoryBean.class);

    private final static String RULES_FILE_NAME = "rules.xml";

    private String destinationDirectory;

    private boolean createAndClearDirectory = true;

    /**
     * This bean is used by spring context. DestinationDirectory property must
     * be set in spring configuration. Destination directory should be exist.
     */
    public UnpackClasspathJarToDirectoryBean() {
    }

    /**
     * Returns directory to unpack path.
     *
     * @return destinationDirectory
     */
    public String getDestinationDirectory() {
        return destinationDirectory;
    }

    public void setCreateAndClearDirectory(boolean createAndClearDirectory) {
        this.createAndClearDirectory = createAndClearDirectory;
    }

    public boolean isCreateAndClearDirectory() {
        return createAndClearDirectory;
    }

    /**
     * Sets directory to unpack path.
     *
     * @param destinationDirectory
     */
    public void setDestinationDirectory(String destinationDirectory) {
        if (destinationDirectory == null) {
            throw new IllegalArgumentException("destinationDirectory argument can't be null");
        }
        this.destinationDirectory = destinationDirectory;
    }

    /*
     * private static String getPathJar(Resource resource) throws
     * IllegalStateException, IOException { URL location = resource.getURL();
     * String jarPath = location.getPath(); if (jarPath.lastIndexOf("!") == -1)
     * { return null; } String path = jarPath.substring("file:".length(),
     * jarPath.lastIndexOf("!"));
     * 
     * // Workaround for WebSphere 8.5 path = path.replaceAll("%20", " ");
     * 
     * return path; }
     */

    private static void unpack(File jarFile, String destDir) throws IOException {
        File newProjectDir = new File(destDir, FilenameUtils.getBaseName(jarFile.getCanonicalPath()));
        newProjectDir.mkdirs();

        JarFile jar = null;
        try {
            jar = new JarFile(jarFile);

            Enumeration<JarEntry> e = jar.entries();
            while (e.hasMoreElements()) {
                JarEntry file = e.nextElement();
                File f = new File(newProjectDir, file.getName());
                if (file.isDirectory()) {
                    f.mkdir();
                    continue;
                }

                InputStream is = jar.getInputStream(file);
                InputStream bufferedInputStream = new BufferedInputStream(is);

                FileOutputStream fos = new FileOutputStream(f);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                int data;
                while ((data = bufferedInputStream.read()) != -1) {
                    bos.write(data);
                }
                bos.close();
                bufferedInputStream.close();
            }
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException e) {

                }
            }
        }
    }

    private static boolean checkOrCreateFolder(File location) {
        if (location.exists()) {
            return true;
        } else {
            return location.mkdirs();
        }
    }

    private void extractJarForJboss(URL resourceURL, File desFile) throws IOException,
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException,
            ClassNotFoundException {
        // This reflection implementation for JBoss vfs
        URLConnection conn = resourceURL.openConnection();
        Object content = conn.getContent();
        Class<?> clazz = content.getClass();
        if ("org.jboss.vfs.VirtualFile".equals(clazz.getName())) {
            String urlString = resourceURL.toString();
            urlString = urlString.substring(0, urlString.lastIndexOf(".jar") + 4);
            Object jarFile = new URL(urlString).openConnection().getContent();
            java.lang.reflect.Method getChildrenMethod = clazz.getMethod("getChildren");
            List<?> children = (List<?>) getChildrenMethod.invoke(jarFile);
            if (!children.isEmpty()) {
                Method getNameMethod = clazz.getMethod("getName");
                String name = (String) getNameMethod.invoke(jarFile);
                File newProjectDir = new File(desFile, FilenameUtils.getBaseName(name));
                Class<?> VFSUtilsClazz = Thread.currentThread()
                        .getContextClassLoader()
                        .loadClass("org.jboss.vfs.VFSUtils");
                java.lang.reflect.Method recursiveCopyMethod = VFSUtilsClazz.getMethod("recursiveCopy",
                        clazz,
                        File.class);
                newProjectDir.mkdirs();
                for (Object child : children) {
                    recursiveCopyMethod.invoke(VFSUtilsClazz, child, newProjectDir);
                }
            } else {
                throw new RuleServiceRuntimeException("Protocol VFS supported only for JBoss VFS. URL content should be org.jboss.vfs.VirtualFile!");
            }
        } else {
            throw new RuleServiceRuntimeException("Protocol VFS supported only for JBoss VFS. URL content should be org.jboss.vfs.VirtualFile!");
        }
    }

    public void afterPropertiesSet() throws IOException {
        String destDirectory = getDestinationDirectory();
        if (destDirectory == null) {
            throw new IllegalStateException("Distination directory is null. Please, check bean configuration.");
        }

        File desFile = new File(destDirectory);

        if (!isCreateAndClearDirectory()) {
            if (!desFile.exists()) {
                throw new IOException("Destination folder does not exist. Path: " + destDirectory);
            }

            if (!desFile.isDirectory()) {
                throw new IOException("Destination path isn't a directory on file system. Path: " + destDirectory);
            }
        } else {
            if (checkOrCreateFolder(desFile)) {
                log.info("Destination folder is already exist. Path: {}", destDirectory);
            } else {
                log.info("Destination folder was created. Path: {}", destDirectory);
            }
        }

        PathMatchingResourcePatternResolver prpr = new PathMatchingResourcePatternResolver();
        Resource[] resources = prpr.getResources(PathMatchingResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + RULES_FILE_NAME);
        if (!FolderHelper.clearFolder(new File(destDirectory))) {
            log.warn("Failed on a folder clear. Path: \"{}\"", destDirectory);
        }
        for (Resource rulesXmlResource : resources) {
            File file = null;
            try {
                final URL resourceURL = rulesXmlResource.getURL();
                if ("jar".equals(resourceURL.getProtocol()) || "wsjar".equals(resourceURL.getProtocol())) {
                    URL jarUrl = org.springframework.util.ResourceUtils.extractJarFileURL(resourceURL);
                    file = org.springframework.util.ResourceUtils.getFile(jarUrl);
                } else if ("vfs".equals(rulesXmlResource.getURL().getProtocol())) {
                    // This reflection implementation for JBoss vfs
                    extractJarForJboss(resourceURL, desFile);
                    log.info("Unpacking \"{}\" into \"{}\" was completed", resourceURL, destDirectory);
                    continue;
                } else {
                    throw new RuleServiceRuntimeException("Protocol for URL doesn't supported! URL: " + resourceURL.toString());
                }
            } catch (Exception e) {
                log.error("Invalid resource!", e);
                throw new IOException("Invalid resource", e);
            }
            if (!file.exists()) {
                throw new IOException("File not found. File: " + file.getAbsolutePath());
            }

            unpack(file, destDirectory);

            log.info("Unpacking \"{}\" into \"{}\" was completed", file.getAbsolutePath(), destDirectory);
        }
    }
}
