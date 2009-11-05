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
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
// ZS
import org.apache.poi.ss.formula.ArrayEval;

public final class Row implements Function, ArrayMode {
// end changes ZS

    public ValueEval evaluate(ValueEval[] evals, int srcCellRow, short srcCellCol) {
        ValueEval retval = null;
        int rnum = -1;

        switch (evals.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
        case 1:
            if (evals[0] instanceof AreaEval) {
                AreaEval ae = (AreaEval) evals[0];
                rnum = ae.getFirstRow();
            }
            else if (evals[0] instanceof RefEval) {
                RefEval re = (RefEval) evals[0];
                rnum = re.getRow();
            }
            else { // anything else is not valid argument
                retval = ErrorEval.VALUE_INVALID;
            }
            break;
        case 0:
            rnum = srcCellRow;
        }

        if (retval == null) {
            retval = (rnum >= 0)
                    ? new NumberEval(rnum + 1) // +1 since excel rownums are 1 based
                    : (ValueEval) ErrorEval.VALUE_INVALID;
        }

        return retval;
    }
// ZS    
    /* (non-Javadoc)
     * @see org.apache.poi.hssf.record.formula.functions.ArrayMode#evaluateInArrayFormula(org.apache.poi.hssf.record.formula.eval.ValueEval[], int, short)
     */
    public ValueEval evaluateInArrayFormula(ValueEval[] evals, int srcCellRow, short srcCellCol) {
        if ( (evals.length == 1) && (evals[0] instanceof AreaEval) ) {
            AreaEval ae = (AreaEval) evals[0];
            
            ValueEval[][] result = new ValueEval[ae.getHeight()][1];
            for (int r=0; r<ae.getHeight(); r++){
            	result[r][0] = new NumberEval(ae.getFirstRow()+r+1);
            }
            return new ArrayEval(result);
        }
        return evaluate(evals, srcCellRow, srcCellCol);
    	
    }
//   end changes ZS 

}

