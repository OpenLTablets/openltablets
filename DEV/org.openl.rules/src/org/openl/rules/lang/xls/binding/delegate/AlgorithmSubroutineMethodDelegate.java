package org.openl.rules.lang.xls.binding.delegate;

import java.util.List;
import java.util.Map;

import org.openl.binding.BindingDependencies;
import org.openl.rules.lang.xls.binding.ATableBoundNode;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.properties.ITableProperties;
import org.openl.rules.tbasic.AlgorithmSubroutineMethod;
import org.openl.rules.tbasic.runtime.operations.RuntimeOperation;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenMethodHeader;
import org.openl.vm.IRuntimeEnv;

public class AlgorithmSubroutineMethodDelegate extends AlgorithmSubroutineMethod implements DispatchDelegateOpenMethod{
    AlgorithmSubroutineMethod delegate;
    XlsModuleOpenClass xlsModuleOpenClass;
    
    public AlgorithmSubroutineMethodDelegate(XlsModuleOpenClass xlsModuleOpenClass, AlgorithmSubroutineMethod delegate) {
        super(null);
        this.delegate = delegate;
        this.xlsModuleOpenClass = xlsModuleOpenClass;
    }
    
    public Object invoke(Object target, Object[] params, IRuntimeEnv env) {
        return DispatcherLogic.dispatch(xlsModuleOpenClass, delegate, target, params, env);
    }

    public String toString() {
        return delegate.toString();
    }

    public IOpenClass getDeclaringClass() {
        return delegate.getDeclaringClass();
    }

    public String getDisplayName(int mode) {
        return delegate.getDisplayName(mode);
    }

    public IOpenMethodHeader getHeader() {
        return delegate.getHeader();
    }

    public String getTableUri() {
        return delegate.getTableUri();
    }

    public IOpenMethod getMethod() {
        return delegate.getMethod();
    }

    public String getName() {
        return delegate.getName();
    }

    public IMethodSignature getSignature() {
        return delegate.getSignature();
    }

    public IOpenClass getType() {
        return delegate.getType();
    }

    public IGridRegion getGridRegion() {
        return delegate.getGridRegion();
    }

    public boolean isStatic() {
        return delegate.isStatic();
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public String getSourceUrl() {
        return delegate.getSourceUrl();
    }

    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    public void setAlgorithmSteps(List<RuntimeOperation> operations) {
        delegate.setAlgorithmSteps(operations);
    }

    public void setLabels(Map<String, RuntimeOperation> localLabelsRegister) {
        delegate.setLabels(localLabelsRegister);
    }

    public BindingDependencies getDependencies() {
        return delegate.getDependencies();
    }

    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    public TableSyntaxNode getSyntaxNode() {
        return delegate.getSyntaxNode();
    }

    public void setBoundNode(ATableBoundNode node) {
        delegate.setBoundNode(node);
    }

    public ATableBoundNode getBoundNode() {
        return delegate.getBoundNode();
    }

    public ITableProperties getMethodProperties() {
        return delegate.getMethodProperties();
    }

    public IMemberMetaInfo getInfo() {
        return delegate.getInfo();
    }

    
}
