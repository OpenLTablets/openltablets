/*
 * Created on Apr 1, 2004
 *  
 * 
 * Developed by OpenRules, Inc. 2003, 2004
 *   
 */
package org.openl.util.print;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;


/**
 * @author snshor
 *
 */
public class NicePrinterAdaptor
{
  
  public void printReference(Object obj, int id, NicePrinter printer)
  {
    printer.getBuffer().append(shortTypeName(getTypeName(obj)));
    
    Object objID = getUniqueID(obj);
    if (objID == null)
      objID = String.valueOf(id);
    printer.getBuffer().append('(').append("id=").append(objID).append(')');
  }
    
  
  protected String getTypeName(Object obj)
  {
    return obj.getClass().getName();
  }


  public Object getUniqueID(Object obj)
  {
    Object id = getProperty(obj, "name");
    if (id == null)
      id = getProperty(obj, "id");
    return id;  
  }
  
  
  public Object getProperty(Object obj, String propertyName)
  {
    String methodName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    try
    {
      Method m = obj.getClass().getMethod(methodName, new Class[0]);
      
      String res = (String)m.invoke(obj, new Object[0]); 
      return res;
    } catch (Throwable t)
    {
      return null;
    }
  }
  
  
  
    

  public NicePrinterAdaptor getAdaptor(Object obj)
  {
  	return this;
  }

  public boolean isPrimitive(Object obj)
  {
    return isPrimitiveClass(obj.getClass());
  }

  public void printNull(NicePrinter printer)
  {
    printer.getBuffer().append("null");
  }

  public void printPrimitive(Object obj, NicePrinter printer)
  {
  	if (obj.getClass() == Double.class)
  	{
  		printer.getBuffer().append(DoublePrinter.printDouble(((Double)obj).doubleValue()));
  	}
    else printer.getBuffer().append(obj);
  }

  public void printCollection(Collection c, int newID, NicePrinter printer)
  {
    Object[] ary = new Object[c.size()];
    Iterator it = c.iterator();
    for (int i = 0; it.hasNext(); i++)
    {
      ary[i] = it.next();
    }
    printArray(ary, newID, printer);
  }

  public void printArray(Object ary, int newID, NicePrinter printer)
  {
    int len = Array.getLength(ary);
    
    if (len == 0)
    {
      printer.getBuffer().append("[]");
      return;
    }
    
    printer.getBuffer().append('{');
    printer.incIdent();
    
    for (int i = 0; i < len; i++)
    {
      printer.startNewLine();
      printer.getBuffer().append('[').append(i).append("]=");
      printer.print(Array.get(ary, i), this);
    }
    printer.startNewLine();
    printer.getBuffer().append('}');
    
    printer.decIdent();
    
      
  }


  public void printObject(Object obj, int newID, NicePrinter printer)
  {
    printer.getBuffer().append(obj);
  } 
  
  public void printMap(Map map, Comparator mapEntryComparator, NicePrinter printer)
  {
    int len = map.size();
    
    if (len == 0)
    {
      printer.getBuffer().append("[]");
      return;
    }
    
    Map.Entry[] entries = new Map.Entry[len];
    
    Iterator it = map.entrySet().iterator();
    for (int i = 0; it.hasNext(); i++)
    {
      entries[i] = (Map.Entry)it.next();
    }
    
    
    if (mapEntryComparator == null)
    {
    	mapEntryComparator = mapComparator;
    }
    
    Arrays.sort(entries, mapEntryComparator);
    
    
    printer.getBuffer().append('{');
    printer.incIdent();
    
    for (int i = 0; i < len; i++)
    {
      printer.startNewLine();
      printer.getBuffer().append(entries[i].getKey()).append("=");
      printer.print(entries[i].getValue(), this);
    }
    printer.startNewLine();
    printer.getBuffer().append('}');
    
    printer.decIdent();
    
  }
  
  
  
  
  static public String shortTypeName(String classname)
  {
    
    int idx = classname.lastIndexOf('.');
    
    return idx < 0 ? classname : classname.substring(idx+1);
  }
  
  static public boolean isPrimitiveClass(Class c)
  {

    for (int i = 0; i < primitiveClasses.length; i++)
    {
      if (primitiveClasses[i] == c) return true;
    }
    
    return false;
  }

  static Class[] primitiveClasses = { Integer.class, Double.class,
      Boolean.class, Character.class, Float.class, Byte.class, Long.class,
      Short.class, String.class, Date.class};


	static final public Comparator mapComparator = new MapEntryComparator(); 

	static class MapEntryComparator implements Comparator
	{
    
	  public int compare(Object arg1, Object arg2)
	  {
		Map.Entry e1 = (Map.Entry)arg1;
		Map.Entry e2 = (Map.Entry)arg2;
      
		String key1 = String.valueOf(e1.getKey());
		String key2 = String.valueOf(e2.getKey());
      
		if (key1.equals("id"))
		  return -1;

		if (key2.equals("id"))
		  return 1;

		if (key1.equals("name"))
		  return -1;

		if (key2.equals("name"))
		  return 1;
      
		return key1.compareTo(key2);
      
	  }
    
  }

  
  
}
