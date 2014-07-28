package org.openl.rules.webstudio.web.trace;

import java.io.IOException;
import java.io.Writer;

import javax.activation.MimetypesFileTypeMap;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.commons.web.jsf.FacesUtils;
import org.openl.commons.web.util.WebTool;
import org.openl.rules.ui.ProjectModel;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.vm.trace.DefaultTracePrinter;
import org.openl.vm.trace.TraceFormatter;
import org.openl.vm.trace.TraceFormatterFactory;
import org.openl.vm.trace.TracePrinter;
import org.openl.vm.trace.Tracer;

/**
 * Request scope managed bean for Trace into File functionality.
 */
@ManagedBean
@RequestScoped
public class TraceIntoFileBean {

    private final Log log = LogFactory.getLog(TraceIntoFileBean.class);

    public static final String EXTENSION_SEPARATOR = ".";

    /**
     * Output file name without extension. By default 'trace'.
     */
    private String fileBaseName = "trace";

    /**
     * Output file format.
     */
    private String fileFormat = TraceFormatterFactory.FORMAT_TEXT;

    public String traceIntoFile() {
        Tracer tracer = trace();

        TracePrinter tracePrinter = getTracePrinter(fileFormat);

        HttpServletResponse response = (HttpServletResponse) FacesUtils.getResponse();
        initResponse(response, getFileName());

        Writer writer = null;

        try {
            writer = response.getWriter();
            tracePrinter.print(tracer, writer);
            writer.close();
        } catch (IOException e) {
            log.error("Error when printing trace", e);
        } finally {
            IOUtils.closeQuietly(writer);
        }

        FacesUtils.getFacesContext().responseComplete();

        return null;
    }

    private Tracer trace() {
        ProjectModel model = WebStudioUtils.getProjectModel();
        return model.traceElement(model.popLastTest());
    }

    private TracePrinter getTracePrinter(String fileFormat) {
        DefaultTracePrinter tracePrinter = new DefaultTracePrinter();

        TraceFormatter traceFormatter = new TraceFormatterFactory().getTraceFormatter(fileFormat);
        tracePrinter.setFormatter(traceFormatter);

        return tracePrinter;
    }

    private void initResponse(HttpServletResponse response, String outputFileName) {
        WebTool.setContentDisposition(response, outputFileName);

        String contentType = new MimetypesFileTypeMap().getContentType(outputFileName);
        response.setContentType(contentType);
    }

    public String getFileBaseName() {
        return fileBaseName;
    }

    public void setFileBaseName(String fileBaseName) {
        this.fileBaseName = fileBaseName;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String getFileName() {
        StringBuilder result = new StringBuilder();

        result.append(fileBaseName);

        if (StringUtils.isNotBlank(fileFormat)) {
            result.append(EXTENSION_SEPARATOR).append(fileFormat);
        }

        return result.toString();
    }

}
