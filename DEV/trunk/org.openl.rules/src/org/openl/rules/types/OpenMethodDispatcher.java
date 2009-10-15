package org.openl.rules.types;

import java.util.ArrayList;
import java.util.List;

import org.openl.runtime.IContext;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.impl.AOpenClass.MethodKey;
import org.openl.vm.IRuntimeEnv;

/**
 * Class that decorates the <code>IOpenMehtod</code> interface for method
 * overload support.
 * 
 * @author Alexey Gamanovich
 * 
 */
public abstract class OpenMethodDispatcher implements IOpenMethod {
	
	/**
	 * Delegate method. Used as a descriptor of method for all overloaded
	 * version to delegate requests about method info such as signature, name,
	 * etc.
	 */
	private IOpenMethod delegate;
	
	/**
	 * Method key. Used for method signatures comparison.
	 */
	private MethodKey delegateKey;
	
	/**
	 * List of method candidates.
	 */
	private List<IOpenMethod> candidates = new ArrayList<IOpenMethod>();
	
	/**
	 * Creates new instance of decorator.
	 * 
	 * @param delegate
	 *            method to decorate
	 * @return instance of decorator
	 */
	protected void decorate(IOpenMethod delegate) {
		
		// Check that IOpenMethod object is not null.
		//
		if (delegate == null) {
			throw new NullPointerException("Method cannot be null");
		}
		
		// Save method as delegate. It used by decorator to delegate requests
		// about method info such as signature, name, etc.
		//
		this.delegate = delegate;
		
		// Evaluate method key.
		//
		this.delegateKey = new MethodKey(delegate);
		
		// First method candidate is himself.
		//
		this.candidates.add(delegate);
	}
	
	/**
	 * Gets the signature of method.
	 */
	public IMethodSignature getSignature() {
		return delegate.getSignature();
	}
	
	/**
	 * Gets the declaring class.
	 */
	public IOpenClass getDeclaringClass() {
		return delegate.getDeclaringClass();
	}
	
	/**
	 * Gets <code>null</code>. The decorator hasn't info about overloaded
	 * methods.
	 */
	public IMemberMetaInfo getInfo() {
		return null;
	}
	
	/**
	 * Gets the type of method.
	 */
	public IOpenClass getType() {
		return delegate.getType();
	}
	
	public boolean isStatic() {
		return delegate.isStatic();
	}
	
	/**
	 * Gets the user-friendly name.
	 */
	public String getDisplayName(int mode) {
		return delegate.getDisplayName(mode);
	}
	
	/**
	 * Gets the method name.
	 */
	public String getName() {
		return delegate.getName();
	}

    /**
     * Gets <code>this</code>. The decorator can't resolve which overloaded
     * method should be returned.
     */
    public IOpenMethod getMethod() {
        return this;
    }
	
	/**
	 * Invokes appropriate method using runtime context.
	 */
	public Object invoke(Object target, Object[] params, IRuntimeEnv env) {
		
		// Gets the runtime context.
		//
		IContext context = env.getContext();
		
		// Get matching method.
		//
		IOpenMethod method = findMatchingMethod(candidates, context);
		
		// Check that founded required method.
		//
		if (method == null) {
			throw new RuntimeException("Appropriate method not found");
		}
		
		return method.invoke(target, params, env);
	}
	
	/**
	 * Try to add method as overloaded version of decorated method.
	 * 
	 * @param candidate
	 *            method to add
	 */
	public void addMethod(IOpenMethod candidate) {
		
		// Evaluate the candidate method key.
		//
		MethodKey candidateKey = new MethodKey(candidate);
		
		// Check that candidate has the same method signature and list of
		// parameters as a delegate. If they different then is two different
		// methods and delegate cannot be overloaded by candidate.
		//
		if (delegateKey.equals(candidateKey)) {
			candidates.add(candidate);
		} else {
			// Throw appropriate exception.
			//
			throw new RuntimeException("Invalid method signature to overload");
		}
	}
	
	/**
	 * Resolve best matching method to invoke.
	 * 
	 * @param candidates
	 *            list of candidates
	 * @param context
	 *            runtime context
	 * @return method to invoke
	 */
	protected abstract IOpenMethod findMatchingMethod(List<IOpenMethod> candidates, IContext context);
}