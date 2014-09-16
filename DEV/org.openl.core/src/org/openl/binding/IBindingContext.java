/*
 * Created on May 19, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding;

import java.util.List;
import java.util.Map;

import org.openl.OpenL;
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
public interface IBindingContext extends ICastFactory {

	void addAlias(String name, String value);

	void addError(SyntaxNodeException error);

	ILocalVar addParameter(String namespace, String name, IOpenClass type)
			throws DuplicatedVarException;

	/**
	 * Adds new type to binding context.
	 * 
	 * @param namespace
	 *            type namespace
	 * @param type
	 *            type
	 * @throws Exception
	 *             if an error has occurred
	 */
	void addType(String namespace, IOpenClass type) throws Exception;
	
	void addTypes(Map<String, IOpenClass> types) throws Exception;

	/**
	 * Removes type from binding context.
	 * 
	 * @param namespace
	 *            type namespace
	 * @param type
	 *            type
	 * @throws Exception
	 *             if an error occurs
	 */
	void removeType(String namespace, IOpenClass type) throws Exception;

//	NOTE: A temporary implementation of multi-module feature.
/*
 void addImport(IOpenClass type);
 Collection<IOpenClass> getImports();
*/	

	ILocalVar addVar(String namespace, String name, IOpenClass type)
			throws DuplicatedVarException;

	INodeBinder findBinder(ISyntaxNode node);

	/**
	 * This method is implemented by default by calling type.getFiled(fieldName,
	 * strictMatch), but some context may override it to provide dynamic mapping
	 * functionality
	 * 
	 * @param type
	 * @param fieldName
	 * @param strictMatch
	 * @return
	 */
	IOpenField findFieldFor(IOpenClass type, String fieldName,
			boolean strictMatch);

	IMethodCaller findMethodCaller(String namespace, String name,
			IOpenClass[] parTypes) throws AmbiguousMethodException;

	IOpenClass findType(String namespace, String typeName);

	/**
	 * 
	 * @param namespace
	 * @param name
	 * @param strictMatch
	 * @return
	 * @throws AmbiguousVarException
	 * @see {@link IOpenClass#getField(String, boolean)}
	 */
	IOpenField findVar(String namespace, String vname, boolean strictMatch)
			throws AmbiguousVarException;

	/**
	 * 
	 * @param namespace
	 * @param rangeStartName
	 * @param rangeEndName
	 * @return reference to the variable holding a range object. The specifics of the range object is
	 * that it is defined by a pair of the variables called start and end. There is no common range interface, 
	 * the details must be contained in the implementation of a particular range type
	 * @throws AmbiguousVarException
	 */
	
    IOpenField findRange(String namespace, String rangeStartName, String rangeEndName)
        throws AmbiguousVarException, FieldNotFoundException, OpenLCompilationException;
	
	String getAlias(String name);

	IOpenCast getCast(IOpenClass from, IOpenClass to);

	SyntaxNodeException[] getErrors();

	int getLocalVarFrameSize();

	int getNumberOfErrors();

	OpenL getOpenL();

	int getParamFrameSize();

	IOpenClass getReturnType();

	List<SyntaxNodeException> popErrors();

	void popLocalVarContext();

	/**
	 * Used for doing temporary processing within current context
	 */
	void pushErrors();

	void pushLocalVarContext();

	/**
	 * @param type
	 */
	void setReturnType(IOpenClass type);

    /**
     * @return <code>true</code> if it is execution mode binding.
     */
	boolean isExecutionMode();
	void setExternalParams(Map<String, Object> params);
	Map<String, Object> getExternalParams();
}
