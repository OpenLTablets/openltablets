package org.openl.message;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.openl.exception.OpenLException;
import org.openl.exception.OpenLExceptionUtils;
import org.openl.main.SourceCodeURLConstants;
import org.openl.main.SourceCodeURLTool;

/**
 * Class defines error OpenL message abstraction. <code>OpenLErrorMessage</code> encapsulates {@link IOpenLError} object
 * as source of message.
 * 
 */
public class OpenLErrorMessage extends OpenLMessage {

    private OpenLException error;

    public OpenLErrorMessage(String summary) {
        super(summary, Severity.ERROR);
    }

    public OpenLErrorMessage(OpenLException error) {
        super(OpenLExceptionUtils.getOpenLExceptionMessage(error), Severity.ERROR);
        if (error == null) {
            throw new NullPointerException();
        }
        this.error = error;
    }

    public OpenLException getError() {
        return error;
    }

    @Override
    public String toString() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.println(super.toString());
        
        if (getError() != null) {
            String url = SourceCodeURLTool.makeSourceLocationURL(getError().getLocation(),
                getError().getSourceModule());

            if (!StringUtils.isEmpty(url)) {
                printWriter.print(SourceCodeURLConstants.AT_PREFIX + url);
            }
        }
        
        printWriter.close();

        return stringWriter.toString();
    }

    @Override
    public String getSourceLocation() {
        return SourceCodeURLTool.makeSourceLocationURL(error.getLocation(), error.getSourceModule());
    }

}
