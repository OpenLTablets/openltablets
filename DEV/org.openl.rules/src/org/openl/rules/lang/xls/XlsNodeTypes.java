package org.openl.rules.lang.xls;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// TODO: implement common for all node types interface, e.g. INodeTypes. Place
// it to the core and rewrite ISyntaxNode#getType to returning INodeTypes.
//
public enum XlsNodeTypes {

    WORKBOOK("Workbook"),
    WORKSHEET("Worksheet"),
    TABLE("Table"),
    CELL("Cell"),

    XLS_MODULE("xls.module"),
    XLS_WORKBOOK("xls.workbook"),
    XLS_WORKSHEET("xls.worksheet"),

    // executable tables
    XLS_DT("xls.dt"),
    XLS_SPREADSHEET("xls.spreadsheet"),
    XLS_TBASIC("xls.tbasic"),
    XLS_COLUMN_MATCH("xls.columnmatch"),
    XLS_METHOD("xls.method"),
    XLS_TEST_METHOD("xls.test.method"),
    XLS_RUN_METHOD("xls.run.method"),

    XLS_DATA("xls.data"),
    XLS_DATATYPE("xls.datatype"),
    XLS_OPENL("xls.openl"),
    XLS_ENVIRONMENT("xls.environment"),
    XLS_TABLEPART("xls.tablepart"),
    XLS_OTHER("xls.other"),
    XLS_PROPERTIES("xls.properties"),
    XLS_CONDITIONS("xls.conditions"),
    XLS_ACTIONS("xls.actions"),
    XLS_RETURNS("xls.returns"),

    XLS_CONSTANTS("xls.constants");

    private final String value;

    XlsNodeTypes(String name) {
        this.value = name;
    }

    @Override
    public String toString() {
        return value;
    }

    private static final Map<String, XlsNodeTypes> CACHE;

    static {
        XlsNodeTypes[] tmp = XlsNodeTypes.values();
        Map<String, XlsNodeTypes> cache = new HashMap<>(tmp.length);
        for (XlsNodeTypes xlsNodeType : tmp) {
            cache.put(xlsNodeType.value, xlsNodeType);
        }
        CACHE = Collections.unmodifiableMap(cache);
    }

    // Temporary method.
    // Should be removed when TableSyntaxNode will be switched from String node type
    // to XlsNodeTypes
    //
    public static XlsNodeTypes getEnumByValue(String value) {
        return CACHE.get(value);
    }
}
