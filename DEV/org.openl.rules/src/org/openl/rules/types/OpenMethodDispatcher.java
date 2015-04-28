package org.openl.rules.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openl.binding.MethodUtil;
import org.openl.binding.exception.DuplicatedMethodException;
import org.openl.exception.OpenLRuntimeException;
import org.openl.rules.context.RulesRuntimeContextFactory;
import org.openl.rules.lang.xls.binding.TableVersionComparator;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.method.ITablePropertiesMethod;
import org.openl.rules.method.TableUriMethod;
import org.openl.rules.table.properties.DimensionPropertiesMethodKey;
import org.openl.runtime.IRuntimeContext;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.types.*;
import org.openl.types.impl.MethodKey;
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
     * @param delegate method to decorate
     */
    protected void decorate(IOpenMethod delegate) {

        // Check that IOpenMethod object is not null.
        //
        if (delegate == null) {
            throw new IllegalArgumentException("Method cannot be null");
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

    public List<IOpenMethod> getCandidates() {
        return candidates;
    }

    /**
     * Invokes appropriate method using runtime context.
     */
    public Object invoke(Object target, Object[] params, IRuntimeEnv env) {

        // Gets the runtime context.
        //
        IRuntimeContext context = env.getContext();

        if (context == null) {
            // Using empty context: all methods will be matched by properties.
            context = RulesRuntimeContextFactory.buildRulesRuntimeContext();
        }

        // Get matching method.
        //
        IOpenMethod method = findMatchingMethod(candidates, context);

        // Check that founded required method.
        //
        if (method == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Method signature: ");
            MethodUtil.printMethod(getName(), getSignature(), sb);
            sb.append("\n");
            sb.append("Context: ");
            sb.append(context.toString());

            String message = String.format("Appropriate overloaded method for '%1$s' not found. Details: \n%2$s",
                getName(),
                sb.toString());

            throw new OpenLRuntimeException(message);
        }

        return method.invoke(target, params, env);
    }

    /**
     * In case we have several versions of one table we should add only the
     * newest or active version of table.
     * 
     * @param newMethod The methods that we are trying to add.
     * @param key Method key of these methods based on signature.
     * @param existedMethod The existing method.
     */
    protected IOpenMethod useActiveOrNewerVersion(IOpenMethod existedMethod, IOpenMethod newMethod, MethodKey key) throws DuplicatedMethodException {
        int compareResult = TableVersionComparator.getInstance().compare(existedMethod, newMethod);
        if (compareResult > 0) {
            return newMethod;
        } else if (compareResult == 0) {
            /**
             * Throw the error with the right message for the case
             * when the methods are equal
             */
            if (newMethod instanceof TableUriMethod && existedMethod instanceof TableUriMethod) {
                String newMethodHashUrl = ((TableUriMethod) newMethod).getTableUri();
                String existedMethodHashUrl = ((TableUriMethod) existedMethod).getTableUri();

                if (!newMethodHashUrl.equals(existedMethodHashUrl)) {
                    // Modules to which methods belongs to
                    List<String> modules = new ArrayList<String>();
                    if (newMethod instanceof IModuleInfo) {
                        // Get the name of the module for the newMethod
                        String moduleName = ((IModuleInfo) newMethod).getModuleName();
                        if (moduleName != null) {
                            modules.add(moduleName);
                        }
                    }
                    if (existedMethod instanceof IModuleInfo) {
                        // Get the name of the module for the existedMethod
                        String moduleName = ((IModuleInfo) existedMethod).getModuleName();
                        if (moduleName != null) {
                            modules.add(moduleName);
                        }
                    }

                    if (modules.isEmpty()) {
                        // Case module names where not set to the methods
                        throw new DuplicatedMethodException(String.format(
                                "Method \"%s\" has already been used with the same version, active status, properties set and different method body!",
                                existedMethod.getName()),
                                existedMethod);
                    } else {
                        // Case when the module names where set to the methods
                        String modulesString = modules.get(0);
                        if (modules.size() > 1) {
                            throw new DuplicatedMethodException(String.format(
                                    "Method \"%s\" has already been used in modules \"%s\" and \"%s\" with the same version, active status, properties set and different method body!",
                                    existedMethod.getName(),
                                    modulesString,
                                    modules.get(1)),
                                    existedMethod);
                        } else {
                            throw new DuplicatedMethodException(String.format(
                                    "Method \"%s\" has already been used in module \"%s\" with the same version, active status, properties set and different method body!",
                                    existedMethod.getName(),
                                    modulesString),
                                    existedMethod);
                        }
                    }
                }
            } else {
                throw new IllegalStateException("Implementation supports only TableUriMethod!");
            }
        }
        return existedMethod;
    }

    private int searchTheSameMethod(IOpenMethod candidate) {
        int i = 0;
        for (IOpenMethod existedMethod : candidates) {
            if (existedMethod instanceof ITablePropertiesMethod && candidate instanceof ITablePropertiesMethod) {
                DimensionPropertiesMethodKey existedMethodPropertiesKey = new DimensionPropertiesMethodKey(existedMethod);
                DimensionPropertiesMethodKey newMethodPropertiesKey = new DimensionPropertiesMethodKey(candidate);
                if (newMethodPropertiesKey.equals(existedMethodPropertiesKey)) {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }
    
    private Set<MethodKey> candidateKeys = new HashSet<MethodKey>();

    /**
     * Try to add method as overloaded version of decorated method.
     * 
     * @param candidate method to add
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
			int i = searchTheSameMethod(candidate);
			if (i < 0) {
				candidates.add(candidate);
			} else {
				IOpenMethod existedMethod = candidates.get(i);
				try {
					candidate = useActiveOrNewerVersion(existedMethod,
							candidate, candidateKey);
					candidates.set(i, candidate);
				} catch (DuplicatedMethodException e) {
					if (!candidateKeys.contains(candidateKey)){
						if (existedMethod instanceof IMemberMetaInfo) {
							IMemberMetaInfo memberMetaInfo = (IMemberMetaInfo) existedMethod;
							if (memberMetaInfo.getSyntaxNode() != null) {
								if (memberMetaInfo.getSyntaxNode() instanceof TableSyntaxNode) {
									SyntaxNodeException error = SyntaxNodeExceptionUtils.createError(e
                                                    .getMessage(), e,
                                            memberMetaInfo.getSyntaxNode());
									((TableSyntaxNode) memberMetaInfo
											.getSyntaxNode())
											.addError(error);
								}
							}
						}
						candidateKeys.add(candidateKey);
					}
					throw e;
				}
			}
        } else {
            // Throw appropriate exception.
            //
            StringBuilder sb = new StringBuilder();
            MethodUtil.printMethod(getName(), getSignature(), sb);

            throw new OpenLRuntimeException("Invalid method signature to overload: " + sb.toString());
        }
    }

    /**
     * Resolve best matching method to invoke.
     * 
     * @param candidates list of candidates
     * @param context runtime context
     * @return method to invoke
     */
    protected abstract IOpenMethod findMatchingMethod(List<IOpenMethod> candidates, IRuntimeContext context);

    public IOpenMethod getTargetMethod() {
        return this.delegate;
    }
}