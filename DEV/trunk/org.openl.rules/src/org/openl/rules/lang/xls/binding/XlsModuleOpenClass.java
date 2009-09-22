/*
 * Created on Oct 23, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.lang.xls.binding;

import java.util.HashMap;
import java.util.Map;

import org.openl.OpenL;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.rules.data.IDataBase;
import org.openl.rules.data.impl.DataBase;
import org.openl.rules.types.OpenMethodDispatcher;
import org.openl.rules.types.impl.SimpleOpenMethodDispatcher;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenSchema;

/**
 * @author snshor
 * 
 */
public class XlsModuleOpenClass extends ModuleOpenClass {
	
	/**
	 * Map of internal types. XLS document can have internal types defined using
	 * <code>Datatype</code> tables, e.g. domain model.
	 */
	private Map<String, IOpenClass> internalTypes = new HashMap<String, IOpenClass>();
	
	IDataBase dataBase = new DataBase();
	
	/**
	 * @param schema
	 * @param name
	 */
	public XlsModuleOpenClass(IOpenSchema schema, String name, XlsMetaInfo metaInfo, OpenL openl) {
		super(schema, name, openl);
		this.metaInfo = metaInfo;
	}
	
	/**
	 * @return
	 */
	public IDataBase getDataBase() {
		return dataBase;
	}
	
	public XlsMetaInfo getXlsMetaInfo() {
		return (XlsMetaInfo) metaInfo;
	}
	
	/**
	 * Add new type to internal types list. If the type with the same name
	 * already exists exception will be thrown.
	 * 
	 * @param type
	 *            IOpenClass instance
	 * @throws Exception
	 *             if an error had occurred.
	 */
	@Override
	public void addType(String namespace, IOpenClass type) throws Exception {
		
		String typeName = buildFullTypeName(namespace, type.getName());
		
		if (internalTypes.containsKey(typeName)) {
			throw new Exception("The type " + typeName + " has been defined already");
		}
		
		internalTypes.put(typeName, type);
	}
	
	/**
	 * Finds type with given name in internal type list. If type with given name
	 * exists in list it will be returned; <code>null</code> - otherwise.
	 * 
	 * @param typeName
	 *            name of type to search
	 * @return {@link IOpenClass} instance or <code>null</code>
	 */
	@Override
	public IOpenClass findType(String namespace, String typeName) {
		
		String name = buildFullTypeName(namespace, typeName);
		
		return internalTypes.get(name);
	}
	
	/**
	 * Adds method to <code>XlsModuleOpenClass</code>.
	 * 
	 * @param method
	 *            method object
	 */
	@Override
	public void addMethod(IOpenMethod method) {
		
		// Get method key.
		//
		MethodKey key = new MethodKey(method);
		
		Map<MethodKey, IOpenMethod> methods = methodMap();
		
		// Checks that method aleready exists in method map. If it already
		// exists then "overload" it using decorator; otherwise - just add to
		// method map.
		//
		if (methods.containsKey(key)) {
			
			// Gets the existed method from map.
			// 
			IOpenMethod existedMethod = methods.get(key);
			
			// Checks the instance of existed method. If it's the
			// OpenMethodDecorator then just add the method-candidate to
			// decorator; otherwise - replace existed method with new instance
			// of OpenMethodDecorator for existed method and add new one.
			//
			if (existedMethod instanceof OpenMethodDispatcher) {
				OpenMethodDispatcher decorator = (OpenMethodDispatcher) existedMethod;
				decorator.addMethod(method);
			} else {
				
				// Create decorator for existed method.
				//
				OpenMethodDispatcher decorator = new SimpleOpenMethodDispatcher(existedMethod);
				
				// Add new method to decorator as candidate.
				//
				decorator.addMethod(method);
				
				// Replace existed method with decorator using the same key.
				//
				methodMap().put(key, decorator);
			}
		} else {
			
			// Just add original method.
			//
			methodMap().put(key, method);
		}
	}
	
	/**
	 * Builds full type name using namespace and type names.
	 * 
	 * @param namespace
	 *            type namespace
	 * @param type
	 *            type name
	 * @return full name string
	 */
	private String buildFullTypeName(String namespace, String type) {
		
		return String.format("%s.%s", namespace, type);
	}
}
