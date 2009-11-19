/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.MissingArgEval;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
// ZS
import org.apache.poi.ss.formula.ArrayEval;
// end changes ZS

/**
 * Implementation for the Excel function INDEX
 * <p>
 *
 * Syntax : <br/>
 *  INDEX ( reference, row_num[, column_num [, area_num]])</br>
 *  INDEX ( array, row_num[, column_num])
 *    <table border="0" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 *      <tr><th>reference</th><td>typically an area reference, possibly a union of areas</td></tr>
 *      <tr><th>array</th><td>a literal array value (currently not supported)</td></tr>
 *      <tr><th>row_num</th><td>selects the row within the array or area reference</td></tr>
 *      <tr><th>column_num</th><td>selects column within the array or area reference. default is 1</td></tr>
 *      <tr><th>area_num</th><td>used when reference is a union of areas</td></tr>
 *    </table>
 * </p>
 *
 * @author Josh Micich
 * @author zsulkins(ZS) - array support
 */
// ZS 
public final class Index implements FunctionWithArraySupport, ArrayMode {

	public ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {
		return evaluateX(args, srcCellRow, srcCellCol, false);
	}

	
	protected  ValueEval evaluateX(ValueEval[] args, int srcCellRow, int srcCellCol, boolean supportRowCol) {
// end changes ZS	
		int nArgs = args.length;
		if(nArgs < 2) {
			// too few arguments
			return ErrorEval.VALUE_INVALID;
		}
		ValueEval firstArg = args[0];
		if (firstArg instanceof RefEval) {
			// convert to area ref for simpler code in getValueFromArea()
			firstArg = ((RefEval)firstArg).offset(0, 0, 0, 0);
		}
		// !! changed ZS
		if (firstArg instanceof ArrayEval){
			firstArg = ((ArrayEval)firstArg).arrayAsArea();
			supportRowCol = true;
		}
		
		// end change
		
		if(!(firstArg instanceof AreaEval)) {

			// else the other variation of this function takes an array as the first argument
			// it seems like interface 'ArrayEval' does not even exist yet
			throw new RuntimeException("Incomplete code - cannot handle first arg of type ("
					+ firstArg.getClass().getName() + ")");
		}
		AreaEval reference = (AreaEval) firstArg;

		int rowIx = 0;
		int columnIx = 0;
		boolean colArgWasPassed = false;
		try {
			switch(nArgs) {
				case 4:
					throw new RuntimeException("Incomplete code" +
							" - don't know how to support the 'area_num' parameter yet)");
					// Excel expression might look like this "INDEX( (A1:B4, C3:D6, D2:E5 ), 1, 2, 3)
					// In this example, the 3rd area would be used i.e. D2:E5, and the overall result would be E2
					// Token array might be encoded like this: MemAreaPtg, AreaPtg, AreaPtg, UnionPtg, UnionPtg, ParenthesesPtg
					// The formula parser doesn't seem to support this yet. Not sure if the evaluator does either

				case 3:
					columnIx = resolveIndexArg(args[2], srcCellRow, srcCellCol);
					colArgWasPassed = true;
				case 2:
					rowIx = resolveIndexArg(args[1], srcCellRow, srcCellCol);
					break;
				default:
					// too many arguments
					return ErrorEval.VALUE_INVALID;
			}
			// ZS
			// From POI:			return getValueFromArea(reference, rowIx, columnIx, colArgWasPassed, srcCellRow, srcCellCol);
			return getValueFromArea(reference, rowIx, columnIx, colArgWasPassed, srcCellRow, srcCellCol, supportRowCol);
			// end changes ZS
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	/**
	 * @param colArgWasPassed <code>false</code> if the INDEX argument list had just 2 items
	 *            (exactly 1 comma).  If anything is passed for the <tt>column_num</tt> argument
	 *            (including {@link BlankEval} or {@link MissingArgEval}) this parameter will be
	 *            <code>true</code>.  This parameter is needed because error codes are slightly
	 *            different when only 2 args are passed.
	 */
//	ZS 
	private static ValueEval getValueFromArea(AreaEval ae, int pRowIx, int pColumnIx,
			boolean colArgWasPassed, int srcRowIx, int srcColIx, boolean supportRowColum) throws EvaluationException {
//	end changes ZS		
		boolean rowArgWasEmpty = pRowIx == 0;
		boolean colArgWasEmpty = pColumnIx == 0;
		int rowIx;
		int columnIx;

		// !!changed ZS
		// implementation of this function isn't support all features of the Excel
		// here I'm adding only support for return of entire row or columm
		if ( supportRowColum && ( (rowArgWasEmpty && !ae.isRow() && pColumnIx<=ae.getWidth() && !colArgWasEmpty) ||
			 (colArgWasEmpty && !ae.isColumn() && pRowIx<=ae.getHeight() && !rowArgWasEmpty))
			){
			// return row or column
			ValueEval[][] result = null;
			if (rowArgWasEmpty ){ // entire column
				result = new ValueEval[ae.getHeight()][1];
				for( int r=0; r<ae.getHeight(); r++ ){
					result[r][0] = ae.getRelativeValue(r, pColumnIx-1);
				}
			}
			else { //entire row
				result = new ValueEval[1][ae.getWidth()];
				for (int c=0; c<ae.getWidth(); c++){
					result[0][c] = ae.getRelativeValue(	pRowIx-1,c);
				}
			}
			return (new ArrayEval(result));
		}
		// end change
		
		
		// when the area ref is a single row or a single column,
		// there are special rules for conversion of rowIx and columnIx
		if (ae.isRow()) {
			if (ae.isColumn()) {
				// single cell ref
				rowIx = rowArgWasEmpty ? 0 : pRowIx-1;
				columnIx = colArgWasEmpty ? 0 : pColumnIx-1;
			} else {
				if (colArgWasPassed) {
					rowIx = rowArgWasEmpty ? 0 : pRowIx-1;
					columnIx = pColumnIx-1;
				} else {
					// special case - row arg seems to get used as the column index
					rowIx = 0;
					// transfer both the index value and the empty flag from 'row' to 'column':
					columnIx = pRowIx-1;
					colArgWasEmpty = rowArgWasEmpty;
				}
			}
		} else if (ae.isColumn()) {
			if (rowArgWasEmpty) {
				rowIx = srcRowIx - ae.getFirstRow();
			} else {
				rowIx = pRowIx-1;
			}
			if (colArgWasEmpty) {
				columnIx = 0;
			} else {
				columnIx = colArgWasEmpty ? 0 : pColumnIx-1;
			}
		} else {
			// ae is an area (not single row or column)
			if (!colArgWasPassed) {
				// always an error with 2-D area refs
				// Note - the type of error changes if the pRowArg is negative
				throw new EvaluationException(pRowIx < 0 ? ErrorEval.VALUE_INVALID : ErrorEval.REF_INVALID);
			}
			// Normal case - area ref is 2-D, and both index args were provided
			// if either arg is missing (or blank) the logic is similar to OperandResolver.getSingleValue()
			if (rowArgWasEmpty) {
				rowIx = srcRowIx - ae.getFirstRow();
			} else {
				rowIx = pRowIx-1;
			}
			if (colArgWasEmpty) {
				columnIx = srcColIx - ae.getFirstColumn();
			} else {
				columnIx = pColumnIx-1;
			}
		}

		int width = ae.getWidth();
		int height = ae.getHeight();
		// Slightly irregular logic for bounds checking errors
		if (!rowArgWasEmpty && rowIx >= height || !colArgWasEmpty && columnIx >= width) {
			// high bounds check fail gives #REF! if arg was explicitly passed
			throw new EvaluationException(ErrorEval.REF_INVALID);
		}
		if (rowIx < 0 || columnIx < 0 || rowIx >= height || columnIx >= width) {
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}
		return ae.getRelativeValue(rowIx, columnIx);
	}


	/**
	 * @param arg a 1-based index.
	 * @return the resolved 1-based index. Zero if the arg was missing or blank
	 * @throws EvaluationException if the arg is an error value evaluates to a negative numeric value
	 */
	private static int resolveIndexArg(ValueEval arg, int srcCellRow, int srcCellCol) throws EvaluationException {

		ValueEval ev = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol);
		if (ev == MissingArgEval.instance) {
			return 0;
		}
		if (ev == BlankEval.INSTANCE) {
			return 0;
		}
		int result = OperandResolver.coerceValueToInt(ev);
		if (result < 0) {
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}
		return result;
	}
// ZS
	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.record.formula.functions.ArrayMode#evaluateInArrayFormula(org.apache.poi.hssf.record.formula.eval.ValueEval[], int, short)
	 */
	public ValueEval evaluateInArrayFormula(ValueEval[] args, int srcCellRow, int srcCellCol) {
		// in array formula index(reference,row,0) and index(reference,0,col) should return entire row/column
		return evaluateX(args, srcCellRow, srcCellCol, true);
	}
	
	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.record.formula.functions.FunctionWithArraySupport#supportArray(int)
	 */
	public boolean supportArray(int paramIndex){
		if (paramIndex == 0)
			return true;
		return false;
	}
//   end changes ZS
}
