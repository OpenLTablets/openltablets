package org.openl.rules.webstudio.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.openl.commons.web.jsf.FacesUtils;
import org.openl.commons.web.util.WebTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ExportModule {

    public static void writeOutContent(final HttpServletResponse res, final File content, final String filename) {
        if (content == null) {
            return;
        }
        FileInputStream input = null;
        try {
            res.setHeader("Pragma", "no-cache");
            res.setDateHeader("Expires", 0);
            res.setContentType("application/" + FilenameUtils.getExtension(filename));
            WebTool.setContentDisposition(res, filename);

            input = new FileInputStream(content);
            IOUtils.copy(input, res.getOutputStream());
        } catch (final IOException e) {
            String msg = "Failed to write content of '" + content.getAbsolutePath() + "' into response!";
            final Logger log = LoggerFactory.getLogger(ExportModule.class);
            log.error(msg, e);
            FacesUtils.addErrorMessage(msg, e.getMessage());
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
}
