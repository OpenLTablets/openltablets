package org.openl.rules.lang.xls.prebind;

import java.util.Set;

import org.openl.OpenL;
import org.openl.dependency.CompiledDependency;
import org.openl.rules.data.IDataBase;
import org.openl.rules.lang.xls.binding.XlsMetaInfo;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.rules.types.OpenMethodDispatcher;
import org.openl.types.IOpenField;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenSchema;

/**
 * ModuleOpenClass for prebinding that uses {@link IPrebindHandler} to convert
 * methods and fields to some invokable(after prebinding they are not invokable)
 * methods/fields before adding.
 * 
 * @author PUdalau
 */
public class XlsLazyModuleOpenClass extends XlsModuleOpenClass {
    private IPrebindHandler prebindHandler;

    public XlsLazyModuleOpenClass(IOpenSchema schema,
            String name,
            XlsMetaInfo metaInfo,
            OpenL openl,
            IDataBase dbase,
            IPrebindHandler prebindHandler,
            boolean useDescisionTableDispatcher,
            boolean dispatchingValidationEnabled) {
        this(schema, name, metaInfo, openl, dbase, prebindHandler, null, useDescisionTableDispatcher, dispatchingValidationEnabled);
    }

    public XlsLazyModuleOpenClass(IOpenSchema schema,
            String name,
            XlsMetaInfo metaInfo,
            OpenL openl,
            IDataBase dbase,
            IPrebindHandler prebindHandler,
            Set<CompiledDependency> usingModules,
            boolean useDescisionTableDispatcher,
            boolean dispatchingValidationEnabled) {
        super(schema, name, metaInfo, openl, dbase, usingModules, useDescisionTableDispatcher, dispatchingValidationEnabled);
        this.prebindHandler = prebindHandler;
    }

    @Override
    public void addMethod(IOpenMethod method) {
        if (prebindHandler != null) {
            super.addMethod(prebindHandler.processMethodAdded(method, this));
        } else {
            super.addMethod(method);
        }
    }

    @Override
    public void addField(IOpenField field) {
        if (prebindHandler != null) {
            super.addField(prebindHandler.processFieldAdded(field, this));
        } else {
            super.addField(field);
        }
    }
    
    @Override
    protected IOpenMethod decorateForMultimoduleDispatching(IOpenMethod openMethod) {
        if (openMethod instanceof OpenMethodDispatcher){
            return super.decorateForMultimoduleDispatching(openMethod);
        }
        return openMethod;
    }
}
