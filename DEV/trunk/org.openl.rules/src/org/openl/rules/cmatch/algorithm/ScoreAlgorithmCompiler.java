package org.openl.rules.cmatch.algorithm;

import java.util.LinkedList;
import java.util.List;

import org.openl.rules.cmatch.ColumnMatch;
import org.openl.rules.cmatch.MatchNode;
import org.openl.rules.cmatch.SubValue;
import org.openl.rules.cmatch.TableRow;
import org.openl.rules.convertor.IString2DataConvertor;
import org.openl.rules.convertor.String2DataConvertorFactory;
import org.openl.source.impl.StringSourceCodeModule;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.types.IOpenClass;

public class ScoreAlgorithmCompiler extends MatchAlgorithmCompiler {
    public static final String WEIGHT = "weight";

    public static final String ROW_SCORE = "Score";

    public static final int ROW_SCORE_IDX = 0;

    protected static final List<ColumnDefinition> SCORE_COLUMN_DEFINITION = new LinkedList<ColumnDefinition>();
    private static final ScoreAlgorithmExecutor SCORE_EXECUTOR = new ScoreAlgorithmExecutor();

    static {
        SCORE_COLUMN_DEFINITION.addAll(MATCH_COLUMN_DEFINITION);
        SCORE_COLUMN_DEFINITION.add(new ColumnDefinition(WEIGHT, false));
    }

    @Override
    protected void assignExecutor(ColumnMatch columnMatch) {
        columnMatch.setAlgorithmExecutor(SCORE_EXECUTOR);
    }

    @Override
    protected MatchNode buildTree(List<TableRow> rows, MatchNode[] nodes) throws SyntaxNodeException {
        MatchNode rootNode = new MatchNode(-1);

        for (int i = getSpecialRowCount(); i < rows.size(); i++) {
            MatchNode node = nodes[i];
            TableRow row = rows.get(i);
            SubValue nameSV = row.get(NAMES)[0];
            int indent = nameSV.getIndent();

            if (indent == 0) {
                rootNode.add(node);
            } else {
                String msg = "Sub node are prohibited here!";
                throw SyntaxNodeExceptionUtils.createError(msg, nameSV.getStringValue().asSourceCodeModule());
            }
        }

        return rootNode;
    }

    @Override
    protected void checkSpecialRows(ColumnMatch columnMatch) throws SyntaxNodeException {
        List<TableRow> rows = columnMatch.getRows();
        checkRowName(rows.get(ROW_SCORE_IDX), ROW_SCORE);
    }

    @Override
    protected List<ColumnDefinition> getColumnDefinition() {
        return SCORE_COLUMN_DEFINITION;
    }

    @Override
    protected int getSpecialRowCount() {
        return 1; // score
    }

    @Override
    protected void parseSpecialRows(ColumnMatch columnMatch) throws SyntaxNodeException {
        super.parseSpecialRows(columnMatch);

        IOpenClass retType = columnMatch.getHeader().getType();
        Class<?> retClass = retType.getInstanceClass();
        if (retClass != int.class && retClass != Integer.class) {
            String msg = "Score algorithm supports int or Integer return type only!";
            // String uri =
            // columnMatch.getTableSyntaxNode().getTableBody().getOriginalGridTable().getUri(0,
            // 0);
            String uri = columnMatch.getSourceUrl();
            throw SyntaxNodeExceptionUtils.createError(msg, new StringSourceCodeModule(null, uri));
        }

        int retValuesCount = columnMatch.getReturnValues().length;

        // score
        TableRow scoreRow = columnMatch.getRows().get(ROW_SCORE_IDX);
        SubValue operationSV = scoreRow.get(OPERATION)[0];
        if (!"".equals(operationSV.getString())) {
            String msg = "Column " + OPERATION + " of special row " + ROW_SCORE + " must be empty!";
            throw SyntaxNodeExceptionUtils.createError(msg, operationSV.getStringValue().asSourceCodeModule());
        }

        // score(s)
        Object[] objScores = parseValues(scoreRow, Integer.class);
        int[] scores = new int[retValuesCount];
        for (int i = 0; i < retValuesCount; i++) {
            scores[i] = (Integer) objScores[i];
        }
        columnMatch.setColumnScores(scores);
        bindMetaInfo(columnMatch, "Scores", scoreRow.get(VALUES), objScores);
    }

    @Override
    protected MatchNode[] prepareNodes(ColumnMatch columnMatch, ArgumentsHelper argumentsHelper, int retValuesCount)
            throws SyntaxNodeException {
        MatchNode[] nodes = super.prepareNodes(columnMatch, argumentsHelper, retValuesCount);

        List<TableRow> rows = columnMatch.getRows();

        // parse weight(s) of each row
        IString2DataConvertor convertor = String2DataConvertorFactory.getConvertor(Integer.class);
        for (int i = getSpecialRowCount(); i < rows.size(); i++) {
            TableRow row = rows.get(i);
            SubValue weightSV = row.get(WEIGHT)[0];

            Integer rowWeight = (Integer) convertor.parse(weightSV.getString(), null, null);
            nodes[i].setWeight(rowWeight);
        }

        return nodes;
    }

    /**
     * Overrides to do nothing.
     *
     * @see #buildTree
     */
    @Override
    protected void validateTree(MatchNode rootNode, List<TableRow> rows, MatchNode[] nodes) throws SyntaxNodeException {
        // DO NOTHING!!!
    }
}
