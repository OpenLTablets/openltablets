package org.openl.rules.lang.xls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EventListener;
import java.util.Collection;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openl.IOpenSourceCodeModule;
import org.openl.rules.indexer.IDocumentType;
import org.openl.rules.indexer.IIndexElement;
import org.openl.syntax.impl.FileSourceCodeModule;
import org.openl.syntax.impl.SourceCodeModuleDelegator;
import org.openl.util.Log;
import org.openl.util.RuntimeExceptionWrapper;

public class XlsWorkbookSourceCodeModule extends SourceCodeModuleDelegator implements IIndexElement {

    public static interface WorkbookListener extends EventListener {
        void beforeSave(XlsWorkbookSourceCodeModule xwscm);
    }

	Workbook workbook;

    private Collection<WorkbookListener> listeners = new ArrayList<WorkbookListener>();

    public XlsWorkbookSourceCodeModule(IOpenSourceCodeModule src) {
        this(src, false);
    }
    
	/**
	 * @deprecated ignores preserveNodes
	 * @param src
	 * @param preserveNodes (ignored for a sake of POI 3.5)
	 */
    public XlsWorkbookSourceCodeModule(IOpenSourceCodeModule src, boolean preserveNodes) {
        super(src);

        InputStream is = null;
        try {
            is = src.getByteStream();
//			POIFSFileSystem fs = new POIFSFileSystem(is);
//	
//			workbook = new HSSFWorkbook(fs, preserveNodes);
            workbook = WorkbookFactory.create(is);
        } catch (Throwable t) {
            throw RuntimeExceptionWrapper.wrap(t);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }

            } catch (Throwable e) {
                Log.error("Error trying close input stream:", e);
            }
        }
    }

    public XlsWorkbookSourceCodeModule(IOpenSourceCodeModule src, Workbook workbook) {
        super(src);
        this.workbook = workbook;
    }

    public void addListener(WorkbookListener listener) {
        listeners.add(listener);
    }

    public String getCategory() {
        return IDocumentType.WORKBOOK.getCategory();
    }

    public String getDisplayName() {
        String uri = src.getUri(0);

        try {
            URL url = new URL(uri);
            String file = url.getFile();
            int index = file.lastIndexOf('/');

            return index < 0 ? file : file.substring(index + 1);

        } catch (MalformedURLException e) {
            throw RuntimeExceptionWrapper.wrap(e);
        }

    }

    public String getIndexedText() {
        return getDisplayName();
    }

    // public IIndexElement getParent()
    // {
    // return null;
    // }
    public String getType() {
        return IDocumentType.WORKBOOK.getType();
    }

    public String getUri() {
        return src.getUri(0);
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void save() throws IOException {
        if (src instanceof FileSourceCodeModule) {
            String fname = ((FileSourceCodeModule) src).getFile().getCanonicalPath();
            saveAs(fname);
        } else {

            try {
                File f = new File(new URI(src.getUri(0)));
                String fname = f.getCanonicalPath();
                saveAs(fname);
            } catch (URISyntaxException me) {
                throw new IOException("The xls source is not file based, can not save");
            }
        }
    }

    public void saveAs(String fileName) throws IOException {
        for (WorkbookListener wl : listeners) {
            wl.beforeSave(this);
        }
        FileOutputStream fileOut = new FileOutputStream(fileName);
        workbook.write(fileOut);
        fileOut.close();
    }

}
