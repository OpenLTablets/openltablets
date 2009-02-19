/**
 * 
 */
package org.openl.rules.tbasic.runtime.operations;

import org.openl.rules.tbasic.runtime.TBasicContextHolderEnv;
import org.openl.types.IMethodCaller;

/**
 * @author User
 *
 */
public abstract class OpenLEvaluationOperation extends RuntimeOperation {
    private IMethodCaller openLStatement;
    
    public OpenLEvaluationOperation(IMethodCaller openLStatement){
        this.openLStatement = openLStatement;
    }
    
    public Object evaluateStatement(TBasicContextHolderEnv environment){
        Object resultValue = null;
        
        if (openLStatement != null){
               resultValue = openLStatement.invoke(environment.getTbasicTarget(), environment.getTbasicParams(), environment);;
        }
        
        return resultValue;
    }

}
