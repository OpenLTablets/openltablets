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
public class IntRangeDomain extends IFiniteDomain.FixedSizeDomain<Integer> implements
	IFiniteDomain<Integer>, IIntDomain
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

    public Iterator<Integer> iterator()
    {
	return intIterator();
    }

    public IType getElementType()
    {
	// TODO Auto-generated method stub
	return null;
    }

    public boolean selectObject(Integer n)
    {
	return containsNumber(n);
    }

    public boolean selectType(IType type)
    {
	// FIXME
	return true;
    }

    public String toString()
    {
	return "[" + min + ".." + max + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof IntRangeDomain)) {
            return false;
        }

        IntRangeDomain other = (IntRangeDomain) obj;
        
        return min == other.min && max == other.max;
    }
    
    @Override
    public int hashCode() {
        int hashCode = 1;
        
        hashCode = 31 * hashCode + min;
        hashCode = 31 * hashCode + max;

        return hashCode;
    }

    /**
     * @param min the min to set
     */
    protected void setMin(int min) {
        this.min = min;
    }

    /**
     * @param max the max to set
     */
    protected void setMax(int max) {
        this.max = max;
    }

}
