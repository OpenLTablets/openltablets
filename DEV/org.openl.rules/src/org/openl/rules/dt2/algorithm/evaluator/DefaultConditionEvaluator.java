package org.openl.rules.dt2.algorithm.evaluator;

import org.openl.domain.IDomain;
import org.openl.domain.IIntIterator;
import org.openl.domain.IIntSelector;
import org.openl.rules.dt2.element.ICondition;
import org.openl.rules.dt2.index.ARuleIndex;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.vm.IRuntimeEnv;

/**
 * Created Jul 11, 2007
 */

/**
 * @author snshor
 * 
 */
public class DefaultConditionEvaluator implements IConditionEvaluator {

    public IOpenSourceCodeModule getFormalSourceCode(ICondition condition) {
        return condition.getSourceCodeModule();
    }

    public IIntSelector getSelector(ICondition condition, Object target, Object[] dtparams, IRuntimeEnv env) {
        return new DefaultConditionSelector(condition, target, dtparams, env);
    }

    public boolean isIndexed() {
        return false;
    }

    /**
     * No indexing for default evaluator
     */
    public ARuleIndex makeIndex(ICondition condition, IIntIterator it) {
        throw new UnsupportedOperationException("The evaluator does not support indexing");
    }

    public IDomain<?> getRuleParameterDomain(ICondition condition) throws DomainCanNotBeDefined {
        throw new DomainCanNotBeDefined("Non-indexed Evaluator", getFormalSourceCode(condition).getCode());
    }

    public IDomain<?> getConditionParameterDomain(int paramIdx, ICondition condition) throws DomainCanNotBeDefined {
        throw new DomainCanNotBeDefined("Non-indexed Evaluator", getFormalSourceCode(condition).getCode());
    }

	@Override
	public String getOptimizedSourceCode() {
		return null;
	}

	@Override
	public void setOptimizedSourceCode(String code) {
	}

}
