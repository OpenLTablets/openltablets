package org.openl.rules.webstudio.web.repository.upload;

import com.thoughtworks.xstream.XStreamException;
import org.openl.rules.project.model.ProjectDescriptor;
import org.openl.rules.webstudio.web.repository.project.ProjectFile;
import org.openl.rules.webstudio.web.repository.upload.zip.ProjectDescriptorFinder;
import org.openl.rules.webstudio.web.repository.upload.zip.ZipWalker;
import org.openl.rules.workspace.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class ZipProjectDescriptorExtractor {
    private ZipProjectDescriptorExtractor() {
    }

    public static ProjectDescriptor getProjectDescriptorOrNull(ProjectFile uploadedFile, PathFilter zipFilter) {
        try {
            return getProjectDescriptorOrThrow(uploadedFile, zipFilter);
        } catch (Exception e) {
            final Logger log = LoggerFactory.getLogger(ZipProjectDescriptorExtractor.class);
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static ProjectDescriptor getProjectDescriptorOrThrow(ProjectFile uploadedFile, PathFilter zipFilter) throws IOException, XStreamException {
        ZipWalker zipWalker = new ZipWalker(uploadedFile, zipFilter);
        ProjectDescriptorFinder finder = new ProjectDescriptorFinder();
        zipWalker.iterateEntries(finder);
        return finder.getProjectDescriptor();
    }

}
