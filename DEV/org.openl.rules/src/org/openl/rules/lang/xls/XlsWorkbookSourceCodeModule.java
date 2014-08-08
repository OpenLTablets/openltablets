package org.openl.rules.lang.xls;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.openl.rules.indexer.IDocumentType;
import org.openl.rules.indexer.IIndexElement;
import org.openl.rules.lang.xls.load.WorkbookLoader;
import org.openl.rules.lang.xls.load.WorkbookLoaders;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.source.impl.ASourceCodeModule;
import org.openl.source.impl.FileSourceCodeModule;
import org.openl.source.impl.SourceCodeModuleDelegator;
import org.openl.util.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class XlsWorkbookSourceCodeModule extends SourceCodeModuleDelegator implements IIndexElement {

    private final Logger log = LoggerFactory.getLogger(XlsWorkbookSourceCodeModule.class);

    /**
     * Delegates modification checking to parent
     */
    public final ModificationChecker DEFAULT_MODIDFICATION_CHECKER = new ModificationChecker() {
        @Override
        public boolean isModified() {
            return XlsWorkbookSourceCodeModule.super.isModified();
        }
    };

    private WorkbookLoader workbookLoader;

    private Set<Short> wbColors = new TreeSet<Short>();

    private Collection<XlsWorkbookListener> listeners = new ArrayList<XlsWorkbookListener>();

    private ModificationChecker modificationChecker = DEFAULT_MODIDFICATION_CHECKER;

    public XlsWorkbookSourceCodeModule(IOpenSourceCodeModule src) {
        this(src, WorkbookLoaders.getWorkbookLoader(src));
    }

    public XlsWorkbookSourceCodeModule(IOpenSourceCodeModule src, WorkbookLoader workbookLoader) {
        super(src);
        this.workbookLoader = workbookLoader;
        if (getWorkbook() instanceof HSSFWorkbook) {
            initWorkbookColors();
        }
    }

    private void initWorkbookColors() {
        Workbook workbook = getWorkbook();
        short numStyles = workbook.getNumCellStyles();
        for (short i = 0; i < numStyles; i++) {
            CellStyle cellStyle = workbook.getCellStyleAt(i);

            wbColors.add(cellStyle.getFillForegroundColor());
            wbColors.add(cellStyle.getFillBackgroundColor());
            wbColors.add(cellStyle.getTopBorderColor());
            wbColors.add(cellStyle.getBottomBorderColor());
            wbColors.add(cellStyle.getLeftBorderColor());
            wbColors.add(cellStyle.getRightBorderColor());
        }

        short numFonts = workbook.getNumberOfFonts();
        for (short i = 0; i < numFonts; i++) {
            Font font = workbook.getFontAt(i);
            wbColors.add(font.getColor());
        }
    }

    public void addListener(XlsWorkbookListener listener) {
        listeners.add(listener);
    }

    public Collection<XlsWorkbookListener> getListeners() {
        return listeners;
    }

    public String getCategory() {
        return IDocumentType.WORKBOOK.getCategory();
    }

    public String getDisplayName() {
        String uri = StringTool.decodeURL(src.getUri(0));
        return FilenameUtils.getName(uri);
    }

    public String getIndexedText() {
        return getDisplayName();
    }

    public String getType() {
        return IDocumentType.WORKBOOK.getType();
    }

    public String getUri() {
        return src.getUri(0);
    }

    public Workbook getWorkbook() {
        return workbookLoader.getWorkbook();
    }

    public WorkbookLoader getWorkbookLoader() {
        return workbookLoader;
    }

    /**
     * Synch object for file accessing. It is necessary to prevent getting
     * isModified info before save operation will be finished.
     */
    private final Object fileAccessLock = new Object();

    public File getSourceFile() {
        synchronized (fileAccessLock) {
            File sourceFile = null;
            if (src instanceof FileSourceCodeModule) {
                sourceFile = ((FileSourceCodeModule) src).getFile();
            } else {
                try {
                    URI uri = new URI(getUri());
                    sourceFile = new File(uri);
                } catch (URISyntaxException me) {
                    log.warn("Can not get source file");
                }
            }
            return sourceFile;
        }
    }

    public void save() throws IOException {
        File sourceFile = getSourceFile();
        String fileName = sourceFile.getCanonicalPath();
        synchronized (fileAccessLock) {
            saveAs(fileName);
            resetModified();
        }
    }

    public ModificationChecker getModificationChecker() {
        return modificationChecker;
    }

    public void setModificationChecker(ModificationChecker modificationChecker) {
        this.modificationChecker = modificationChecker;
    }

    @Override
    public boolean isModified() {
        synchronized (fileAccessLock) {
            return modificationChecker.isModified();
        }
    }

    public void resetModified() {
        synchronized (fileAccessLock) {
            if (getSource() instanceof ASourceCodeModule) {
                ((ASourceCodeModule) getSource()).resetModified();
            }
        }
    }

    public void saveAs(String fileName) throws IOException {
        for (XlsWorkbookListener wl : listeners) {
            wl.beforeSave(this);
        }

        OutputStream fileOut = new DeferredCreateFileOutputStream(fileName);
        getWorkbook().write(fileOut);
        fileOut.close();

        for (XlsWorkbookListener wl : listeners) {
            wl.afterSave(this);
        }
    }

    public IOpenSourceCodeModule getSource() {
        return src;
    }

    public Set<Short> getWorkbookColors() {
        return wbColors;
    }

    /**
     * Interface that provides modification checking
     *
     * @author NSamatov
     */
    public static interface ModificationChecker {
        /**
         * Returns a modification status
         *
         * @return true if a workbook is modified
         */
        boolean isModified();
    }

    /**
     * Avoids rewriting the file before actual write operation is occurred.
     * For example if OutOfMemoryError is thrown before actual write operation begins, the file should not be corrupted.
     *
     * @author NSamatov
     */
    private static final class DeferredCreateFileOutputStream extends ProxyOutputStream {
        private final String fileName;

        /**
         * Create deferred output stream.
         *
         * @param fileName the system-dependent file name
         * @throws FileNotFoundException if the file exists but is a directory
         *                               rather than a regular file, does not exist but cannot be
         *                               created, or cannot be opened for any other reason.
         */
        private DeferredCreateFileOutputStream(String fileName) throws FileNotFoundException {
            super(null);
            this.fileName = fileName;
            throwExceptionIfNotWritable(fileName);
        }

        /**
         * Check that file is writable. File should not be rewritten in this
         * method.
         *
         * @param fileName the checking file
         * @throws FileNotFoundException if the file exists but is a directory
         *                               rather than a regular file, does not exist but cannot be
         *                               created, or cannot be opened for any other reason.
         */
        private void throwExceptionIfNotWritable(String fileName) throws FileNotFoundException {
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(fileName, true);
            } finally {
                IOUtils.closeQuietly(os);
            }
        }

        @Override
        protected void beforeWrite(int n) throws IOException {
            if (out == null) {
                out = new FileOutputStream(fileName);
            }
        }

        @Override
        public void flush() throws IOException {
            if (out != null) {
                super.flush();
            }
        }

        @Override
        public void close() throws IOException {
            if (out != null) {
                super.close();
            }
        }
    }
}
