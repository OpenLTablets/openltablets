/*
 * Created on May 28, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.vm;

import org.openl.IOpenRunner;
import org.openl.runtime.IContext;

/**
 * @author snshor
 * 
 */
public interface IRuntimeEnv {
	Object[] getLocalFrame();
	
	IOpenRunner getRunner();
	
	Object getThis();
	
	Object[] popLocalFrame();
	
	Object popThis();
	
	void pushLocalFrame(Object[] frame);
	
	void pushThis(Object thisObject);
	
	/**
	 * Gets the runtime context.
	 * 
	 * @return <code>IContext</code> instance
	 */
	IContext getContext();
}
