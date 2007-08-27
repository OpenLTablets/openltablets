/*
 * Created on Jun 10, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.conf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openl.OpenConfigurationException;
import org.openl.binding.AmbiguousMethodException;
import org.openl.binding.ICastFactory;
import org.openl.binding.INodeBinder;
import org.openl.syntax.IGrammar;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IMethodCaller;
import org.openl.types.IOpenCast;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenFactory;
import org.openl.types.IOpenField;

/**
 * @author snshor
 *
 */
public class OpenLConfiguration implements IOpenLConfiguration
{

  //	static WeakCache configurations = new WeakCache();
  static HashMap configurations = new HashMap();

//  static HashMap sharedConfigurations = new HashMap();


	static public void reset()
	{
		configurations = new HashMap();
	}
	



  static synchronized public void register(
    String name,
    IUserContext ucxt,
    IOpenLConfiguration oplc,
    boolean shared)
    throws OpenConfigurationException
  {
		Object key = null;

  	if (shared)
  	{
  		key = name;
  	}
  	
		else key = Cache.makeKey(name, ucxt);

    IOpenLConfiguration old = (IOpenLConfiguration)configurations.get(key);
    if (old != null)
    {
      throw new OpenConfigurationException(
        "The configuration " + name + " already exists",
        null,
        null);
    }
    configurations.put(key, oplc);

  }

  static public IOpenLConfiguration getInstance(String name, IUserContext ucxt)
    throws OpenConfigurationException
  {
  	IOpenLConfiguration opc = (IOpenLConfiguration)configurations.get(name);
  	
  	if (opc != null)
  	  return opc;
  	  
    Object key = Cache.makeKey(name, ucxt);

    return (IOpenLConfiguration)configurations.get(key);

  }

  static synchronized public void unregister(String name, IUserContext ucxt)
    throws OpenConfigurationException
  {
    Object key = Cache.makeKey(name, ucxt);

    //		IOpenLConfiguration old = (IOpenLConfiguration)configurations.get(key);
    //		if (old == null)
    //		{
    //			throw new OpenConfigurationException("The configuration " + name + " does not exists", null, null);
    //		}	
    configurations.remove(key);
    configurations.remove(name);

  }

  String uri;

  IOpenLConfiguration parent;
  IConfigurableResourceContext configurationContext;

  ClassFactory grammarFactory;

  NodeBinderFactoryConfiguration binderFactory;

  LibraryFactoryConfiguration methodFactory;

  TypeCastFactory typeCastFactory;

  TypeFactoryConfiguration typeFactory;

  public void validate(IConfigurableResourceContext cxt)
    throws OpenConfigurationException
  {
    if (grammarFactory != null)
      grammarFactory.validate(cxt);
    else if (parent == null)
      throw new OpenConfigurationException(
        "Grammar class is not set",
        getUri(),
        null);

    if (binderFactory != null)
      binderFactory.validate(cxt);
    else if (parent == null)
      throw new OpenConfigurationException(
        "Bindings are not set",
        getUri(),
        null);

    // Methods  and casts are optional			
    //		else if (parent == null)
    //			throw new OpenConfigurationException("Methods are not set", getUri(), null);  

    if (methodFactory != null)
      methodFactory.validate(cxt);

    if (typeCastFactory != null)
      typeCastFactory.validate(cxt);

    if (typeFactory != null)
      typeFactory.validate(cxt);

    if (openFactories != null)
    {
      for (Iterator iter = openFactories.values().iterator(); iter.hasNext();)
      {
        OpenFactoryConfiguration element =
          (OpenFactoryConfiguration)iter.next();
        element.validate(cxt);
      }
    }

  }

  /* (non-Javadoc)
   * @see org.openl.syntax.IGrammarFactory#getGrammar()
   */
  public IGrammar getGrammar() throws OpenConfigurationException
  {
    return grammarFactory == null
      ? parent.getGrammar()
      : (IGrammar)grammarFactory.getResource(configurationContext);
  }

  /* (non-Javadoc)
   * @see org.openl.binding.INodeBinderFactory#getNodeBinder(org.openl.syntax.ISyntaxNode)
   */
  public INodeBinder getNodeBinder(ISyntaxNode node)
  {
    INodeBinder binder =
      binderFactory == null
        ? null
        : binderFactory.getNodeBinder(node, configurationContext);
    if (binder != null)
      return binder;
    return parent == null ? null : parent.getNodeBinder(node);
  }

  /* (non-Javadoc)
   * @see org.openl.binding.IMethodFactory#getMethodCaller(java.lang.String, java.lang.String, org.openl.types.IOpenClass[], org.openl.binding.ICastFactory)
   */
  public IMethodCaller getMethodCaller(
    String namespace,
    String name,
    IOpenClass[] params,
    ICastFactory casts)
    throws AmbiguousMethodException
  {
    IMethodCaller mc =
      methodFactory == null
        ? null
        : methodFactory.getMethodCaller(
          namespace,
          name,
          params,
          casts,
          configurationContext);

    if (mc != null)
      return mc;

    return parent == null
      ? null
      : parent.getMethodCaller(namespace, name, params, casts);

  }

  /* (non-Javadoc)
   * @see org.openl.binding.ICastFactory#getCast(java.lang.String, org.openl.types.IOpenClass, org.openl.types.IOpenClass)
   */
  public IOpenCast getCast(IOpenClass from, IOpenClass to)
  {
    IOpenCast cast =
      typeCastFactory == null
        ? null
        : typeCastFactory.getCast(from, to, configurationContext);
    if (cast != null)
      return cast;
    return parent == null ? null : parent.getCast(from, to);
  }

  /**
   * @return
   */
  public String getUri()
  {
    return uri;
  }

  /**
   * @param string
   */
  public void setUri(String string)
  {
    uri = string;
  }

  /**
   * @return
   */
  public ClassFactory getGrammarFactory()
  {
    return grammarFactory;
  }

  /**
   * @param factory
   */
  public void setGrammarFactory(ClassFactory factory)
  {
    grammarFactory = factory;
  }

  /* (non-Javadoc)
   * @see org.openl.binding.IVarFactory#getVar(java.lang.String, java.lang.String)
   */
  public IOpenField getVar(String namespace, String name)
  {
    IOpenField field =
      methodFactory == null
        ? null
        : methodFactory.getVar(namespace, name, configurationContext);
    if (field != null)
      return field;
    return parent == null ? null : parent.getVar(namespace, name);
  }

  public IOpenClass getType(String namespace, String name)
  {
    IOpenClass type =
      typeFactory == null
        ? null
        : typeFactory.getType(namespace, name, configurationContext);
    if (type != null)
      return type;
    return parent == null ? null : parent.getType(namespace, name);
  }

  /**
   * @return
   */
  public IConfigurableResourceContext getConfigurationContext()
  {
    return configurationContext;
  }

  /**
   * @param context
   */
  public void setConfigurationContext(IConfigurableResourceContext context)
  {
    configurationContext = context;
  }

  /**
   * @return
   */
  public NodeBinderFactoryConfiguration getBinderFactory()
  {
    return binderFactory;
  }

  /**
   * @param factory
   */
  public void setBinderFactory(NodeBinderFactoryConfiguration factory)
  {
    binderFactory = factory;
  }

  /**
   * @param configuration
   */
  public void setParent(IOpenLConfiguration configuration)
  {
    parent = configuration;
  }

  /**
   * @return
   */
  public LibraryFactoryConfiguration getMethodFactory()
  {
    return methodFactory;
  }

  /**
   * @param factory
   */
  public void setMethodFactory(LibraryFactoryConfiguration factory)
  {
    methodFactory = factory;
  }

  /**
   * @return
   */
  public TypeCastFactory getTypeCastFactory()
  {
    return typeCastFactory;
  }

  /**
   * @param factory
   */
  public void setTypeCastFactory(TypeCastFactory factory)
  {
    typeCastFactory = factory;
  }

  Map openFactories = null;

  public synchronized void addOpenFactory(IOpenFactoryConfiguration opfc)
    throws OpenConfigurationException
  {
    if (openFactories == null)
      openFactories = new HashMap();

    if (opfc.getName() == null)
      throw new OpenConfigurationException(
        "The factory must have a name",
        opfc.getUri(),
        null);
    if (openFactories.containsKey(opfc.getName()))
      throw new OpenConfigurationException(
        "Duplicated name: " + opfc.getName(),
        opfc.getUri(),
        null);

    openFactories.put(opfc.getName(), opfc);
  }

  public IOpenFactory getOpenFactory(String name)
  {
    OpenFactoryConfiguration conf =
      openFactories == null
        ? null
        : (OpenFactoryConfiguration)openFactories.get(name);

    if (conf != null)
      return conf.getOpenFactory(configurationContext);

    if (parent != null)
      return parent.getOpenFactory(name);

    return null;
  }

  /**
   * @param configuration
   */
  public void setTypeFactory(TypeFactoryConfiguration configuration)
  {
    typeFactory = configuration;
  }

}
