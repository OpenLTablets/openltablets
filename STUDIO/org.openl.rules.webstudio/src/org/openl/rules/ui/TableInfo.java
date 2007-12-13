package org.openl.rules.ui;

import org.openl.rules.table.IGridTable;
import org.openl.rules.webtools.WebTool;
import org.openl.rules.webtools.indexer.FileIndexer;


/**
 * DOCUMENT ME!
 *
 * @author Stanislav Shor
 */
public class TableInfo {
    IGridTable table;
    String displayName;
    boolean runnable;
    String uri;

    public TableInfo(IGridTable table, String displayName, boolean runnable) {
        this.table = table;
        this.displayName = displayName;
        this.runnable = runnable;
    }

    public TableInfo(IGridTable table, String displayName, boolean runnable, String uri) {
        this.table = table;
        this.displayName = displayName;
        this.runnable = runnable;
        this.uri = uri;
    }

    public String getUrl() {
        return WebTool.makeXlsOrDocUrl((uri == null) ? table.getUri() : uri);
    }

    public IGridTable getTable() {
        return this.table;
    }

    public String getUri() {
        return table.getUri();
    }

    public String getText() {
        return FileIndexer.showElementHeader(getUri());
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean isRunnable() {
        return this.runnable;
    }
}
