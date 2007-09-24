/*
 * Created on Apr 28, 2004
 *
 * Developed by OpenRules Inc 2003-2004
 */
package org.openl.domain;

import java.util.Iterator;


/**
 * @author snshor
 */
public class IntRangeDomain  extends IFiniteDomain.FixedSizeDomain 
	implements IFiniteDomain, IIntDomain
{
	protected int min, max;
	

	public IntRangeDomain(int min, int max)
	{
		this.min = min;
		this.max = max;
	}

	/**
	 * @return
	 */
	public int getMax()
	{
		return max;
	}

	/**
	 * @return
	 */
	public int getMin()
	{
		return min;
	}

	public boolean contains(int value)
	{
		return min <= value && value <= max;
	}
	
	
	public boolean containsNumber(Number n)
	{
		return min <= n.doubleValue() && n.doubleValue() <= max;
	}	
	
	
	

	/**
	 *
	 */

	public IIntIterator intIterator()
	{
		return new RangeIterator();
	}
	
	
	class RangeIterator extends AIntIterator
	{
		int current;
		
		RangeIterator()
		{
			current = min - 1;
		}
		
		/**
		 *
		 */

		public boolean hasNext()
		{
			return current < max;
		}

		/**
		 *
		 */

		public int nextInt()
		{
			return ++current;
		}

		public Object next()
		{
			return new Integer(++current);
		}

		public int size()
		{
			return max - min + 1;
		}

}

	/**
	 *
	 */

	public int size()
	{
		return max - min + 1;
	}

	public Iterator iterator()
	{
	    return intIterator();
	}

	public IType getElementType()
	{
	    // TODO Auto-generated method stub
	    return null;
	}

	public boolean selectObject(Object obj)
	{
	    Number n = (Number)obj; 
	    return containsNumber(n);
	}

	public boolean selectType(IType type)
	{
	    //FIXME 
	    return true;
	}

	public String toString()
	{
	    return "[" + min + ".." + max + "]";
	}
	
	

}
