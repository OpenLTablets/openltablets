package org.openl.rules.webstudio.web.repository.project;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ResourceUtils;

/**
 * @author nsamatov.
 */
public class PredefinedTemplatesResolver extends TemplatesResolver {
    private final Log log = LogFactory.getLog(PredefinedTemplatesResolver.class);

    private static final String TEMPLATES_PATH = "org.openl.rules.demo.";

    @Override
    protected List<String> resolveCategories() {
        return Arrays.asList("templates", "examples", "tutorials");
    }

    protected List<String> resolveTemplates(String category) {
        List<String> templateNames = new ArrayList<String>();

        try {
            for (Resource resource : getFolderResources(TEMPLATES_PATH + category + "/*")) {
                if (!ResourceUtils.isFileURL(resource.getURL())) {
                    // JAR file
                    // In most of cases protocol is "jar", but in case of IBM
                    // WebSphere protocol is "wsjar"
                    String templateUrl = URLDecoder.decode(resource.getURL().getPath(), "UTF8");
                    String[] templateParsed = templateUrl.split("/");
                    templateNames.add(templateParsed[templateParsed.length - 1]);
                } else {
                    // File System
                    templateNames.add(resource.getFilename());
                }
            }

        } catch (Exception e) {
            log.error("Failed to get project templates", e);
        }
        return templateNames;
    }

    public ProjectFile[] getProjectFiles(String category, String templateName) {
        String url = TEMPLATES_PATH + category + "/" + templateName;

        List<ProjectFile> templateFiles = new ArrayList<ProjectFile>();
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

        try {
            Resource[] templates = resourceResolver.getResources(url + "/*");
            if (templates.length == 0) {
                resourceResolver = new EncodedJarPathResourcePatternResolver();
                templates = resourceResolver.getResources(url + "/*");
            }
            for (Resource resource : templates) {
                templateFiles.add(new ProjectFile(resource.getFilename(), resource.getInputStream()));
            }
        } catch (Exception e) {
            log.error("Failed to get project template: " + url, e);
        }

        return templateFiles.isEmpty() ? new ProjectFile[0]
                : templateFiles.toArray(new ProjectFile[templateFiles.size()]);
    }

    private Resource[] getFolderResources(String folderPattern) throws IOException {
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        // JAR file
        Resource[] resources = resourceResolver.getResources(folderPattern + "/");
        if (resources.length == 0) {
            // File System
            resources = resourceResolver.getResources(folderPattern);
        }
        return resources;
    }

}
