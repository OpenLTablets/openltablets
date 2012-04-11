package org.openl.xls;

import org.openl.impl.DefaultCompileContext;

/**
 * The current implementation of compile context used for rules projects and
 * contains part of code that is auto generated to simplify rules projects
 * configuration. Do not used this class separately.
 * 
 */
public class RulesCompileContext extends DefaultCompileContext {

    // <<< INSERT >>>
	{
		addValidator(new org.openl.rules.validation.UniquePropertyValueValidator("name"));
	}
    // <<< END INSERT >>>
}
