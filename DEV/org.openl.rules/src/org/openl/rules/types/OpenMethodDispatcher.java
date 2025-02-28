package org.openl.rules.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openl.base.INamedThing;
import org.openl.binding.MethodUtil;
import org.openl.exception.OpenLRuntimeException;
import org.openl.rules.context.IRulesRuntimeContextOptimizationForOpenMethodDispatcher;
import org.openl.rules.lang.xls.binding.TableVersionComparator;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.rules.lang.xls.binding.wrapper.IRulesMethodWrapper;
import org.openl.rules.lang.xls.binding.wrapper.WrapperLogic;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.method.ITablePropertiesMethod;
import org.openl.rules.table.properties.DimensionPropertiesMethodKey;
import org.openl.runtime.IRuntimeContext;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.IParameterDeclaration;
import org.openl.types.impl.MethodKey;
import org.openl.types.impl.MethodSignature;
import org.openl.types.impl.ParameterDeclaration;
import org.openl.vm.IRuntimeEnv;
import org.openl.vm.Tracer;

/**
 * Class that decorates the <code>IOpenMehtod</code> interface for method overload support.
 *
 * @author Alexey Gamanovich
 */
public abstract class OpenMethodDispatcher implements IOpenMethod {

    /**
     * Delegate method. Used as a descriptor of method for all overloaded version to delegate requests about method info
     * such as signature, name, etc.
     */
    private IOpenMethod delegate;

    /**
     * Method key. Used for method signatures comparison.
     */
    private MethodKey delegateKey;

    /**
     * List of method candidates.
     */
    private final List<IOpenMethod> candidates = new ArrayList<>();
    private final Map<Integer, DimensionPropertiesMethodKey> candidatesToDimensionKey = new HashMap<>();

    private XlsModuleOpenClass xlsModuleOpenClass;
    private final List<IMethodSignature> signatures = new ArrayList<>();
    private final List<IOpenClass> types = new ArrayList<>();
    private IMethodSignature signature;
    private IOpenClass type;

    protected OpenMethodDispatcher() {
    }

    public OpenMethodDispatcher(IOpenMethod delegate, XlsModuleOpenClass xlsModuleOpenClass) {
        // Save method as delegate. It used by decorator to delegate requests
        // about method info such as signature, name, etc.
        //
        this.delegate = WrapperLogic.unwrapOpenMethod(Objects.requireNonNull(delegate, "Method cannot be null"));
        this.signature = delegate.getSignature();
        this.signatures.add(this.signature);
        this.type = delegate.getType();
        this.types.add(this.type);

        this.xlsModuleOpenClass = Objects.requireNonNull(xlsModuleOpenClass, "xlsModuleOpenClass cannot be null");

        // Evaluate method key.
        //
        this.delegateKey = new MethodKey(delegate);

        // First method candidate is himself.
        //
        this.candidates.add(this.delegate);
        if (this.delegate instanceof ITablePropertiesMethod) {
            int idx = this.candidates.size() - 1;
            this.candidatesToDimensionKey.put(idx, new DimensionPropertiesMethodKey(this.delegate));
        }
    }

    /**
     * Gets the signature of method.
     */
    @Override
    public IMethodSignature getSignature() {
        return signature;
    }

    /**
     * Gets the declaring class.
     */
    @Override
    public XlsModuleOpenClass getDeclaringClass() {
        return xlsModuleOpenClass;
    }

    /**
     * Gets <code>null</code>. The decorator hasn't info about overloaded methods.
     */
    @Override
    public IMemberMetaInfo getInfo() {
        return null;
    }

    /**
     * Gets the type of method.
     */
    @Override
    public IOpenClass getType() {
        return type;
    }

    @Override
    public boolean isStatic() {
        return delegate.isStatic();
    }

    @Override
    public boolean isConstructor() {
        return false;
    }

    /**
     * Gets the user-friendly name.
     */
    @Override
    public String getDisplayName(int mode) {
        return MethodUtil.printSignature(this, mode);
    }

    /**
     * Gets the method name.
     */
    @Override
    public String getName() {
        return delegate.getName();
    }

    /**
     * Gets <code>this</code>. The decorator cannot resolve which overloaded method should be returned.
     */
    @Override
    public IOpenMethod getMethod() {
        return this;
    }

    public List<IOpenMethod> getCandidates() {
        return Collections.unmodifiableList(candidates);
    }

    /**
     * Invokes appropriate method using runtime context.
     */
    @Override
    public Object invoke(Object target, Object[] params, IRuntimeEnv env) {
        return Tracer.invoke(this::invokeInner, target, params, env, this);
    }

    /**
     * Finds appropriate method using runtime context. This method used to optimize runtime where the same method is
     * used more that one time.
     */
    public IOpenMethod findMatchingMethod(IRuntimeEnv env) {
        // Gets the runtime context.
        //
        IRuntimeContext context = env.getContext();

        // Get matching method.
        //
        IOpenMethod method;

        if (context instanceof IRulesRuntimeContextOptimizationForOpenMethodDispatcher) {
            IRulesRuntimeContextOptimizationForOpenMethodDispatcher rulesRuntimeContextOptimizationForOpenMethodDispatcher = (IRulesRuntimeContextOptimizationForOpenMethodDispatcher) context;
            method = rulesRuntimeContextOptimizationForOpenMethodDispatcher.getMethodForOpenMethodDispatcher(this);
            if (method == null) {
                method = findMatchingMethod(candidates, context);
                rulesRuntimeContextOptimizationForOpenMethodDispatcher.putMethodForOpenMethodDispatcher(this, method);
            }
        } else {
            method = findMatchingMethod(candidates, context);
        }

        // Check that founded required method.
        //
        if (method == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Method signature: ");
            MethodUtil.printMethod(this, sb);
            sb.append("\n");
            sb.append("Context: ");
            sb.append(context.toString());

            String message = String
                    .format("Appropriate overloaded method for '%1$s' is not found. Details: \n%2$s", getName(), sb);

            throw new OpenLRuntimeException(message);
        }

        method = WrapperLogic.extractNonLazyMethod(method);

        if (method instanceof IRulesMethodWrapper) {
            method = ((IRulesMethodWrapper) method).getDelegate();
        }

        return method;
    }

    /**
     * Invokes appropriate method using runtime context.
     */
    private <R> R invokeInner(Object target, Object[] params, IRuntimeEnv env) {
        IOpenMethod method = findMatchingMethod(env);
        Tracer.put(this, "rule", method);
        return (R) method.invoke(target, params, env);
    }

    /**
     * In case we have several versions of one table we should add only the newest or active version of table.
     *
     * @param existedMethod The existing method.
     * @param newMethod     The methods that we are trying to add.
     */
    private IOpenMethod useActiveOrNewerVersion(IOpenMethod existedMethod, IOpenMethod newMethod) {
        int compareResult = TableVersionComparator.getInstance().compare(existedMethod, newMethod);
        if (compareResult > 0) {
            return newMethod;
        } else if (compareResult == 0) {
            DuplicateMemberThrowExceptionHelper.throwDuplicateMethodExceptionIfMethodsAreNotTheSame(newMethod,
                    existedMethod);
        }
        return existedMethod;
    }

    private int searchTheSameMethod(DimensionPropertiesMethodKey newMethodPropertiesKey) {
        for (Map.Entry<Integer, DimensionPropertiesMethodKey> it : candidatesToDimensionKey.entrySet()) {
            DimensionPropertiesMethodKey existedMethodPropertiesKey = it.getValue();
            if (existedMethodPropertiesKey.hashCode() == newMethodPropertiesKey.hashCode() && newMethodPropertiesKey
                    .equals(existedMethodPropertiesKey)) {
                return it.getKey();
            }
        }
        return -1;
    }

    /**
     * Try to add method as overloaded version of decorated method.
     *
     * @param method method to add
     */
    public void addMethod(IOpenMethod method) {
        // Evaluate the candidate method key.
        //
        MethodKey candidateKey = new MethodKey(method);
        IOpenMethod candidate = WrapperLogic.unwrapOpenMethod(method);

        // Check that candidate has the same method signature and list of
        // parameters as a delegate. If they different then is two different
        // methods and delegate cannot be overloaded by candidate.
        //
        if (delegateKey.equals(candidateKey)) {
            int i = -1;
            DimensionPropertiesMethodKey dimensionMethodKey = null;
            if (candidate instanceof ITablePropertiesMethod) {
                dimensionMethodKey = new DimensionPropertiesMethodKey(candidate);
                i = searchTheSameMethod(dimensionMethodKey);
            }
            if (i < 0) {
                candidates.add(candidate);
                signatures.add(method.getSignature());
                types.add(method.getType());
                if (dimensionMethodKey != null) {
                    int idx = candidates.size() - 1;
                    candidatesToDimensionKey.put(idx, dimensionMethodKey);
                }
                type = getDeclaringClass().getRulesModuleBindingContext().findClosestClass(type, method.getType());
                signature = mergeMethodSignature(signature, method.getSignature());
            } else {
                IOpenMethod existedMethod = candidates.get(i);
                candidate = useActiveOrNewerVersion(existedMethod, candidate);
                candidates.set(i, candidate);
                candidatesToDimensionKey.put(i, new DimensionPropertiesMethodKey(candidate));
                signatures.set(i, method.getSignature());
                types.set(i, method.getType());

                IOpenClass t = types.get(0);
                for (int j = 1; j < types.size(); j++) {
                    t = getDeclaringClass().getRulesModuleBindingContext().findClosestClass(t, types.get(j));
                }
                type = t;
                IMethodSignature s = signatures.get(0);
                for (int j = 1; j < types.size(); j++) {
                    s = mergeMethodSignature(s, signatures.get(j));
                }
                signature = s;
            }
        } else {
            throw new IllegalStateException(String.format("Unexpected signature '%s' is found.",
                    MethodUtil.printSignature(this, INamedThing.REGULAR)));
        }
    }

    private IMethodSignature mergeMethodSignature(IMethodSignature signature1, IMethodSignature signature2) {
        IOpenClass[] parameterTypes = signature1.getParameterTypes();
        IParameterDeclaration[] parameterDeclarations = new IParameterDeclaration[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            IOpenClass t = getDeclaringClass().getRulesModuleBindingContext()
                    .findClosestClass(signature1.getParameterType(i), signature2.getParameterType(i));
            parameterDeclarations[i] = new ParameterDeclaration(t, signature1.getParameterName(i));
        }
        return new MethodSignature(parameterDeclarations);
    }

    /**
     * Resolve best matching method to invoke.
     *
     * @param candidates list of candidates
     * @param context    runtime context
     * @return method to invoke
     */
    protected abstract IOpenMethod findMatchingMethod(List<IOpenMethod> candidates, IRuntimeContext context);

    public IOpenMethod getTargetMethod() {
        return this.candidates.iterator().next();
    }

    public abstract TableSyntaxNode getDispatcherTable();
}