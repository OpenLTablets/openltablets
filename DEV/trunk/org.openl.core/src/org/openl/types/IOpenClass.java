/*
 * Created on May 9, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.types;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openl.binding.IOpenLibrary;
import org.openl.domain.IType;
import org.openl.meta.IMetaHolder;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 *
 * OpenClass represents a generalized abstraction of a "class". Because we want
 * openL to be used in a many different incarnations we have made a decision to
 * keep an OpenClass as general as possible. It should be close in spirit to the
 * RDF:Class and OWL:Class.
 * @TODO put some href here
 *
 */

public interface IOpenClass extends IType, IOpenLibrary, IOpenClassHolder, IMetaHolder {

    IOpenClass[] EMPTY = {};

    /**
     * @return an iterator of all the fieldValues
     */
    @Deprecated
    Iterator<IOpenField> fields();
    
    Map<String, IOpenField> getFields();

    IAggregateInfo getAggregateInfo();

    IOpenField getField(String name);

    /**
     * This method returns a class field by it's name; in case of strictMatch
     * the name search will be case-sensitive. In case of strictMatch == false,
     * the search will be case-insensitive and may produce
     * AmbiguousVarException. In the future we might implement more
     * sophisticated matching techniques to help users to deal with typos in
     * their code
     *
     * @param name
     * @param strictMatch
     * @return
     * @since 5.0
     */
    IOpenField getField(String name, boolean strictMatch);

    IOpenField getIndexField();

    /**
     * @return the actual Java implementation of the instance, should return
     *         primitive classes in case of int, char etc.
     */
    Class<?> getInstanceClass();

    IOpenMethod getMethod(String name, IOpenClass[] classes);

    /**
     * @return the schema it has been created with
     */
    IOpenSchema getSchema();

    // ********* instance related methods ***********//

    /**
     * @return true if the instance of the class can not be created
     */
    boolean isAbstract();

    /**
     * @param c Class to check
     * @return true if the instance of corresponding Class class belongs to the
     *         open class.
     */
    boolean isAssignableFrom(Class<?> c);

    /**
     * @param ioc IOpenClass to check
     * @return true if the instance of corresponding IOpenClass class belongs to
     *         the open class.
     */
    boolean isAssignableFrom(IOpenClass ioc);

    /**
     * @return true if instance is one of the class or it's superclasses.
     *         Warning: there may be a confusion in case of Java primitives.
     *         Java spec says that a corresponding method of java.lang.Class
     *         will return false in case of primitive types.
     */
    boolean isInstance(Object instance);

    /**
     * This is analog of Java Class.isPrimitive(). We have a little bit
     * different perspective on the things, and consider classes as
     * java.util.Date, java.lang.String and java.lang.Class also primitive. In
     * general, a class is considered primitive if a) it can be easily
     * serialized/deserialized to string b) does not have read/write fieldValues
     * in java.beans sense
     *
     * One of the use of the simple classes is that they provide "terminal"
     * points in object graph discovery.
     *
     *
     * We do not provide (yet) an automatic discovery mechanism for "simple"
     * classes, so it is developer's responsibility to extends OpenL set of
     * simple classes with his own
     *
     * @return
     */
    boolean isSimple();
    
    @Deprecated
    Iterator<IOpenMethod> methods();
    
    List<IOpenMethod> getMethods();

    Object newInstance(IRuntimeEnv env);

    Object nullObject();

    /**
     * We do not have a limitation on number of superclasses. This feature is
     * not fully supported yet
     */
    Iterator<IOpenClass> superClasses();
    
    /**
	 * Add new type to internal types list. If the type with the same name
	 * already exists exception will be thrown.
	 * 
	 * @param type
	 *            IOpenClass instance
	 * @throws Exception
	 *             if an error had occurred.
	 */
	void addType(String namespace, IOpenClass type) throws Exception;

	/**
	 * Finds type with given name in internal type list. If type with given name
	 * exists in list it will be returned; <code>null</code> - otherwise.
	 * 
	 * @param typeName
	 *            name of type to search
	 * @return {@link IOpenClass} instance or <code>null</code>
	 */
	IOpenClass findType(String namespace, String typeName);
	
	/**
	 * Return the whole map of internal types. Where the key is namespace of the type, 
	 * the value is {@link IOpenClass}.
	 * 
	 * @return map of internal types 
	 */
	Map<String, IOpenClass> getTypes();
}
