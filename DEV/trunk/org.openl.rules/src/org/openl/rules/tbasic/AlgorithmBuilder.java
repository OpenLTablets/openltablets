package org.openl.rules.tbasic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openl.binding.IBindingContext;
import org.openl.domain.EnumDomain;
import org.openl.meta.StringValue;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.lang.xls.types.CellMetaInfo;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.IWritableGrid;
import org.openl.rules.table.ui.ICellStyle;
import org.openl.rules.tbasic.compile.AlgorithmCompiler;
import org.openl.syntax.impl.SyntaxError;
import org.openl.types.impl.DomainOpenClass;
import org.openl.types.java.JavaOpenClass;

public class AlgorithmBuilder {

    // Section Description Operation Condition Action Before After
    private static class AlgorithmColumn {
        private String id;
        private int columnIndex;

        private AlgorithmColumn(String id, int columnIndex) {
            this.id = id;
            this.columnIndex = columnIndex;
        }
    }

    private static final String OPERATION = "Operation";
    // FIXME: eliminate static string array and replace it with rules based data
    // type
    // (if possible...)
    public static String[] algorithmOperations = new String[] { "SET", "VAR", "IF", "ELSE", "WHILE",
    /* "FOR EACH", */
    "SUB", "FUNCTION", "END IF", "END WHILE", "END FOR EACH", "END SUB", "END FUNCTION", "GOTO", "BREAK", "CONTINUE",
            "RETURN" };
    private final IBindingContext bindingContext;

    private final Algorithm algorithm;

    private final TableSyntaxNode tsn;

    private Map<String, AlgorithmColumn> columns;

    public AlgorithmBuilder(IBindingContext ctx, Algorithm algorithm, TableSyntaxNode tsn) {
        bindingContext = ctx;
        this.algorithm = algorithm;
        this.tsn = tsn;
    }

    /**
     * Sets CellMetaInfo for operation cell. Thus, editor can use special
     * controller to validate/limit user input.
     *
     * @param c
     * @param r
     * @param grid
     */
    private void bindMetaInfo(IGridTable grid, int c, int r) {
        CellMetaInfo meta = new CellMetaInfo(CellMetaInfo.Type.DT_CA_CODE, null, new DomainOpenClass("operation",
                JavaOpenClass.STRING, new EnumDomain<String>(algorithmOperations), null));
        IWritableGrid wgrid = IWritableGrid.Tool.getWritableGrid(grid);
        wgrid.setCellMetaInfo(IGridRegion.Tool.getAbsoluteColumn(grid.getRegion(), c), IGridRegion.Tool.getAbsoluteRow(
                grid.getRegion(), r), meta);
    }

    public void build(ILogicalTable tableBody) throws Exception {
        if (tableBody.getLogicalHeight() <= 2) {
            throw new SyntaxError(tsn, "Unsufficient rows. Must be more than 2!", null);
        }

        prepareColumns(tableBody);

        // parse data, row=2..*
        List<AlgorithmRow> algorithmRows = buildRows(tableBody);

        RowParser rowParser = new RowParser(algorithmRows, AlgorithmTableParserManager.instance()
                .getAlgorithmSpecification());

        List<AlgorithmTreeNode> parsedNodes = rowParser.parse();

        AlgorithmCompiler compiler = new AlgorithmCompiler(bindingContext, algorithm.getHeader(), parsedNodes);
        compiler.compile(algorithm);
    }

    private List<AlgorithmRow> buildRows(ILogicalTable tableBody) throws SyntaxError {
        List<AlgorithmRow> result = new ArrayList<AlgorithmRow>();

        IGridTable grid = tableBody.rows(2).getGridTable();
        for (int r = 0; r < grid.getLogicalHeight(); r++) {

            AlgorithmRow aRow = new AlgorithmRow();

            // set sequential number of the row in table
            aRow.setRowNumber(r + 1);

            ILogicalTable rowTable = grid.getLogicalRow(r);
            aRow.setGridRegion(rowTable.getGridTable().getRegion());

            // parse data row
            for (AlgorithmColumn column : columns.values()) {
                int c = column.columnIndex;

                ILogicalTable valueTable = rowTable.getLogicalColumn(c);
                aRow.setValueGridRegion(column.id, valueTable.getGridTable().getRegion());

                String value = grid.getCell(c, r).getStringValue();
                String uri = grid.getUri(c, r);

                if (value == null) {
                    value = "";
                }

                StringValue sv = new StringValue(value, "cell" + r + "_" + c, null, uri);

                setRowField(aRow, column.id, sv);
                if (OPERATION.equalsIgnoreCase(column.id)) {
                    ICellStyle cellStyle = grid.getCell(c, r).getStyle();
                    int i = (cellStyle == null) ? 0 : cellStyle.getIdent();
                    aRow.setOperationLevel(i);
                    bindMetaInfo(grid, c, r);
                }
            }

            result.add(aRow);
        }

        return result;
    }

    private void prepareColumns(ILogicalTable tableBody) throws SyntaxError {
        columns = new HashMap<String, AlgorithmColumn>();

        ILogicalTable ids = tableBody.getLogicalRow(0);

        // parse ids, row=0
        for (int c = 0; c < ids.getLogicalWidth(); c++) {
            String id = safeId(ids.getGridTable().getCell(c, 0).getStringValue());
            if (id.length() == 0) {
                // ignore column with NO ID
                continue;
            }

            if (columns.get(id) != null) {
                // duplicate ids
                throw new SyntaxError(tsn, "Duplicate column '" + id + "'!", null);
            }

            columns.put(id, new AlgorithmColumn(id, c));
        }
    }

    private String safeId(String s) {
        String id = "";
        if (s != null) {
            id = s.trim().toLowerCase();
        }
        return id;
    }

    private void setRowField(AlgorithmRow row, String column, StringValue sv) throws SyntaxError {
        if ("label".equalsIgnoreCase(column)) {
            row.setLabel(sv);
        } else if ("description".equalsIgnoreCase(column)) {
            row.setDescription(sv);
        } else if ("operation".equalsIgnoreCase(column)) {
            row.setOperation(sv);
        } else if ("condition".equalsIgnoreCase(column)) {
            row.setCondition(sv);
        } else if ("action".equalsIgnoreCase(column)) {
            row.setAction(sv);
        } else if ("before".equalsIgnoreCase(column)) {
            row.setBefore(sv);
        } else if ("after".equalsIgnoreCase(column)) {
            row.setAfter(sv);
        } else {
            throw new SyntaxError(tsn, "Invalid column id '" + column + "'!", null);
        }
    }
}
