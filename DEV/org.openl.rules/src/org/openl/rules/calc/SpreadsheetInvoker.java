package org.openl.rules.calc;

import org.openl.rules.calc.element.SpreadsheetCell;
import org.openl.rules.method.RulesMethodInvoker;
import org.openl.types.IDynamicObject;
import org.openl.vm.IRuntimeEnv;

/**
 * Invoker for {@link Spreadsheet}.
 *
 * @author DLiauchuk
 */
public class SpreadsheetInvoker extends RulesMethodInvoker<Spreadsheet> {

    protected Object[][] preFetchedResult;

    public SpreadsheetInvoker(Spreadsheet spreadsheet) {
        super(spreadsheet);
        this.preFetchedResult = preFetchResult(spreadsheet);
    }

    public boolean canInvoke() {
        return getInvokableMethod().getResultBuilder() != null;
    }

    public Object invokeSimple(Object target, Object[] params, IRuntimeEnv env) {
        SpreadsheetResultCalculator res = new SpreadsheetResultCalculator(
                getInvokableMethod(), (IDynamicObject) target, params, env,
                preFetchedResult);
        return getInvokableMethod().getResultBuilder().makeResult(res);
    }

    /**
     * Creates a result with constant values that are populated
     *
     * @param spreadsheet
     */
    protected Object[][] preFetchResult(Spreadsheet spreadsheet) {
        SpreadsheetCell[][] cc = spreadsheet.getCells();
        Object[][] res = new Object[cc.length][cc[0].length];

        for (int i = 0; i < cc.length; i++) {
            SpreadsheetCell[] row = cc[i];
            for (int j = 0; j < row.length; j++) {
                SpreadsheetCell cell = row[j];
                switch (cell.getKind()) {
                    case EMPTY:
                        res[i][j] = null;
                        break;
                    case VALUE:
                        res[i][j] = cell.getValue();
                        break;
                    case METHOD:
                        res[i][j] = SpreadsheetResultCalculator.NEED_TO_CALCULATE_VALUE;
                        break;

                }

            }

        }

        return res;

    }
}
