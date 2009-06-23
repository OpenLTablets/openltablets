package org.openl.rules.liveexcel;

import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.openl.rules.liveexcel.formula.ParsedDeclaredFunction;
import org.openl.rules.liveexcel.usermodel.LiveExcelWorkbook;
import org.openl.types.IOpenMethodHeader;
import org.openl.types.impl.AMethod;
import org.openl.vm.IRuntimeEnv;

public class LiveExcelMethod extends AMethod {
	
	private ParsedDeclaredFunction declaredFunction;
	
	private LiveExcelWorkbook workbook;

	public LiveExcelMethod(IOpenMethodHeader header,ParsedDeclaredFunction declaredFunction, LiveExcelWorkbook workbook) {
		super(header);
		this.declaredFunction = declaredFunction;
		this.workbook = workbook;
	}

	public Object invoke(Object target, Object[] params, IRuntimeEnv env) {
	    LiveExcelEvaluator evaluator = new LiveExcelEvaluator(workbook, workbook.getEvaluationContext());
	    ValueEval evaluate = evaluator.evaluateServiceModelUDF(declaredFunction.getDeclFuncName(), params);
		if (evaluate instanceof NumberEval) {
			return ((NumberEval)evaluate).getNumberValue();
		} else if (evaluate instanceof StringEval) {
			return ((StringEval)evaluate).getStringValue();
		} else if (evaluate instanceof BoolEval) {
			return ((BoolEval)evaluate).getBooleanValue();
		}
		return null;
	}

}
