/*
 * Created on Jul 25, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import java.util.List;
import java.util.Map;

import org.openl.OpenL;
import org.openl.binding.IBindingContext;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.ILocalVar;
import org.openl.binding.INodeBinder;
import org.openl.binding.exception.AmbiguousMethodException;
import org.openl.binding.exception.AmbiguousVarException;
import org.openl.binding.exception.DuplicatedVarException;
import org.openl.binding.exception.FieldNotFoundException;
import org.openl.binding.impl.cast.IOpenCast;
import org.openl.exception.OpenLCompilationException;
import org.openl.syntax.ISyntaxNode;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.types.IMethodCaller;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;

/**
 * @author snshor
 * 
 */
public class BindingContextDelegator implements IBindingContextDelegator {

    protected IBindingContext delegate;

    public BindingContextDelegator(IBindingContext delegate) {
        this.delegate = delegate;
    }

    public void addAlias(String name, String value) {
        delegate.addAlias(name, value);
    }

    public IOpenField findRange(String namespace, String rangeStartName, String rangeEndName) throws AmbiguousVarException,
                                                                                             FieldNotFoundException,
                                                                                             OpenLCompilationException {
        return delegate.findRange(namespace, rangeStartName, rangeEndName);
    }

    public void addError(SyntaxNodeException error) {
        delegate.addError(error);
    }

    public ILocalVar addParameter(String namespace, String name, IOpenClass type) throws DuplicatedVarException {
        return delegate.addParameter(namespace, name, type);
    }

    public void addType(String namespace, IOpenClass type) throws OpenLCompilationException {
        throw new UnsupportedOperationException();
    }

    public void addTypes(Map<String, IOpenClass> types) throws OpenLCompilationException {
        throw new UnsupportedOperationException();
    }

    // FIXME: method should throw any type of custom exception
    public void removeType(String namespace, IOpenClass type) throws Exception {
        throw new UnsupportedOperationException();
    }

    public ILocalVar addVar(String namespace, String name, IOpenClass type) throws DuplicatedVarException {
        return delegate.addVar(namespace, name, type);
    }

    public INodeBinder findBinder(ISyntaxNode node) {
        return delegate.findBinder(node);
    }

    public IOpenField findFieldFor(IOpenClass type, String fieldName, boolean strictMatch) {
        return delegate.findFieldFor(type, fieldName, strictMatch);
    }

    public IMethodCaller findMethodCaller(String namespace, String name, IOpenClass[] parTypes) throws AmbiguousMethodException {
        return delegate.findMethodCaller(namespace, name, parTypes);
    }

    public IOpenClass findType(String namespace, String typeName) {
        return delegate.findType(namespace, typeName);
    }

    public IOpenField findVar(String namespace, String name, boolean strictMatch) throws AmbiguousVarException {
        return delegate.findVar(namespace, name, strictMatch);
    }

    public String getAlias(String name) {
        return delegate.getAlias(name);
    }

    public IOpenCast getCast(IOpenClass from, IOpenClass to) {
        return delegate.getCast(from, to);
    }

    public IBindingContext getDelegate() {
        return delegate;
    }

    public SyntaxNodeException[] getErrors() {
        return delegate.getErrors();
    }

    public int getLocalVarFrameSize() {
        return delegate.getLocalVarFrameSize();
    }

    public int getNumberOfErrors() {
        return delegate.getNumberOfErrors();
    }

    public OpenL getOpenL() {
        return delegate.getOpenL();
    }

    public int getParamFrameSize() {
        return delegate.getParamFrameSize();
    }

    public IOpenClass getReturnType() {
        return delegate.getReturnType();
    }

    public List<SyntaxNodeException> popErrors() {
        return delegate.popErrors();
    }

    public void popLocalVarContext() {
        delegate.popLocalVarContext();
    }

    public void pushErrors() {
        delegate.pushErrors();
    }

    public void pushLocalVarContext() {
        delegate.pushLocalVarContext();
    }

    public void setDelegate(IBindingContext delegate) {
        this.delegate = delegate;
    }

    public void setReturnType(IOpenClass type) {
        delegate.setReturnType(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openl.binding.IBindingContextDelegator#setTopDelegate(org.openl.binding
     * .IBindingContext)
     */
    public void setTopDelegate(IBindingContext delegate) {
        if (this.delegate == null) {
            this.delegate = delegate;
            return;
        }

        if (this.delegate instanceof IBindingContextDelegator) {
            ((IBindingContextDelegator) this.delegate).setTopDelegate(delegate);
        }
    }

    public boolean isExecutionMode() {
        return delegate.isExecutionMode();
    }

    public Map<String, Object> getExternalParams() {
        return delegate.getExternalParams();
    }

    public void setExternalParams(Map<String, Object> params) {
        delegate.setExternalParams(params);
    }
}
