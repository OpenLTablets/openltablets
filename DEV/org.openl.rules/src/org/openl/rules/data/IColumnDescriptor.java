/*
 * Created on Oct 23, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */
 
package org.openl.rules.data;

import org.openl.OpenlToolAdaptor;
import org.openl.binding.IBindingContext;
import org.openl.rules.table.ILogicalTable;
import org.openl.types.IOpenClass;


/**
 * @author snshor
 *
 */
public interface IColumnDescriptor 
{
	
	String getName();
	boolean isConstructor();
	boolean isReference();
	
//	IOpenClass getType();
	
	void populateLiteral(Object target, ILogicalTable values, OpenlToolAdaptor ota)
	  throws Exception;
	
	Object getLiteral(IOpenClass paramType, ILogicalTable values) throws Exception;
	

	void populateLink(Object target, ILogicalTable values, IDataBase db, IBindingContext cxt)
	  throws Exception;

	Object getLink(IOpenClass fieldType, ILogicalTable values, IDataBase db, IBindingContext cxt) throws Exception;


	Object getColumnValue(Object target);
	
	IString2DataConvertor getConvertor();
	/**
	 * @return
	 */
	String getDisplayName();
	/**
	 * @return
	 */
	IOpenClass getType();

}
