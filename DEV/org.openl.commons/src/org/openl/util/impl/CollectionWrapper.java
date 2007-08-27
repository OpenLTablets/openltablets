/*
 * Created on May 6, 2004
 *
 * Developed by OpenRules Inc 2003-2004
 */
package org.openl.util.impl;

import java.util.Collection;
import java.util.Iterator;

import org.openl.util.IOpenCollection;
import org.openl.util.IOpenIterator;
import org.openl.util.meta.ICollectionMetaInfo;

/**
 * @author snshor
 */
public class CollectionWrapper implements IOpenCollection
{
	Collection collection;
	ICollectionMetaInfo metaInfo;

	public CollectionWrapper(Collection collection, ICollectionMetaInfo metaInfo)
	{
		this.collection = collection;
		this.metaInfo = metaInfo;
	}

	/**
	 * @param o
	 * @return
	 */
	public boolean add(Object o)
	{
		return collection.add(o);
	}

	/**
	 * @param c
	 * @return
	 */
	public boolean addAll(Collection c)
	{
		return collection.addAll(c);
	}

	/**
	 * 
	 */
	public void clear()
	{
		collection.clear();
	}

	/**
	 * @param o
	 * @return
	 */
	public boolean contains(Object o)
	{
		return collection.contains(o);
	}

	/**
	 * @param c
	 * @return
	 */
	public boolean containsAll(Collection c)
	{
		return collection.containsAll(c);
	}

	/**
	 * @return
	 */
	public boolean isEmpty()
	{
		return collection.isEmpty();
	}

	/**
	 * @param o
	 * @return
	 */
	public boolean remove(Object o)
	{
		return collection.remove(o);
	}

	/**
	 * @param c
	 * @return
	 */
	public boolean removeAll(Collection c)
	{
		return collection.removeAll(c);
	}

	/**
	 * @param c
	 * @return
	 */
	public boolean retainAll(Collection c)
	{
		return collection.retainAll(c);
	}

	/**
	 * @return
	 */
	public int size()
	{
		return collection.size();
	}

	/**
	 * @return
	 */
	public Object[] toArray()
	{
		return collection.toArray();
	}

	/**
	 * @param a
	 * @return
	 */
	public Object[] toArray(Object[] a)
	{
		return collection.toArray(a);
	}

	/**
	 *
	 */

	public ICollectionMetaInfo getMetaInfo()
	{
		return metaInfo;
	}

	/**
	 *
	 */

	public IOpenIterator openIterator()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 *
	 */

	public Iterator iterator()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 *
	 */

	public boolean equals(Object obj)
	{
		// TODO Auto-generated method stub
		return super.equals(obj);
	}

	/**
	 *
	 */

	public int hashCode()
	{
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	/**
	 *
	 */

	public String toString()
	{
		// TODO Auto-generated method stub
		return super.toString();
	}

}
