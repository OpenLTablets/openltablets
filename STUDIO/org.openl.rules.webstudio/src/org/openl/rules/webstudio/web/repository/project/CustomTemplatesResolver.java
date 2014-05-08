package org.openl.rules.webstudio.web.repository.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author nsamatov.
 */
public class CustomTemplatesResolver extends TemplatesResolver {
    public static final String PROJECT_TEMPLATES_FOLDER = "project-templates";

    private final Log log = LogFactory.getLog(CustomTemplatesResolver.class);
    private final String TEMPLATES_PATH = new File(System.getProperty("webstudio.home"), PROJECT_TEMPLATES_FOLDER).getPath();
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(new TemplateResourceLoader());

    @Override
    protected List<String> resolveCategories() {
        return getFolders(TEMPLATES_PATH + "/*");
    }

    @Override
    protected List<String> resolveTemplates(String category) {
        return getFolders(TEMPLATES_PATH + "/" + category + "/*");
    }

    @Override
    public ProjectFile[] getProjectFiles(String category, String templateName) {
        String url = TEMPLATES_PATH + "/" + category + "/" + templateName;

        List<ProjectFile> templateFiles = getProjectFilesRecursively(url, "");

        return templateFiles.toArray(new ProjectFile[templateFiles.size()]);
    }

    private List<ProjectFile> getProjectFilesRecursively(String baseUrl, final String folder) {
        List<ProjectFile> templateFiles = new ArrayList<ProjectFile>();

        try {
            String locationPattern = folder.isEmpty() ? baseUrl + "/*" : baseUrl + "/" + folder + "/*";
            Resource[] resources = resourcePatternResolver.getResources(locationPattern);
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                String relativePath = folder.isEmpty() ? filename : folder + "/" + filename;
                if (resource.getFile().isDirectory()) {
                    templateFiles.addAll(getProjectFilesRecursively(baseUrl, relativePath));
                } else {
                    templateFiles.add(new ProjectFile(relativePath, resource.getInputStream()));
                }
            }
        } catch (Exception e) {
            log.error("Failed to get project template: " + baseUrl, e);
        }
        return templateFiles;
    }

    private List<String> getFolders(String folderPattern) {
        List<String> folderNames = new ArrayList<String>();
        try {
            for (Resource folder : resourcePatternResolver.getResources(folderPattern)) {
                if (folder.getFile().isDirectory()) {
                    folderNames.add(folder.getFilename());
                }
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }

        return folderNames;
    }

    private static class TemplateResourceLoader implements ResourceLoader {
        @Override
        public Resource getResource(String location) {
            return new FileSystemResource(location);
        }

        @Override
        public ClassLoader getClassLoader() {
            return getClass().getClassLoader();
        }
    }
}
