package org.openl.rules.lang.xls.load;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openl.exception.OpenLRuntimeException;
import org.openl.message.OpenLMessagesUtils;
import org.openl.source.IOpenSourceCodeModule;

import java.io.InputStream;

// Package scope util class
final class WorkbookLoadUtils {
    private WorkbookLoadUtils() {
    }

    static Workbook loadWorkbook(IOpenSourceCodeModule fileSource) {
        Log log = LogFactory.getLog(LazyWorkbookLoader.class);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Loading workbook '%s'...", fileSource.getUri(0)));
        }

        InputStream is = null;
        Workbook workbook;
        try {
            is = fileSource.getByteStream();
            workbook = WorkbookFactory.create(is);
        } catch (Exception e) {
            log.error("Error while preprocessing workbook", e);

            String message = "Can't open source file or file is corrupted: " +
                    ExceptionUtils.getRootCause(e).getMessage();
            OpenLRuntimeException error = new OpenLRuntimeException(message, e);
            OpenLMessagesUtils.addError(error);

            throw error;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Throwable e) {
                log.error("Error trying close input stream:", e);
            }
        }

        return workbook;
    }
}
