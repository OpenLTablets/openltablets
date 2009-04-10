/**
 * Created Jul 21, 2007
 */
package org.openl.util;

import java.util.TreeMap;

import junit.framework.TestCase;

/**
 * @author snshor
 *
 */
public class TreeMapTest extends TestCase
{

	public void testTree()
	{
		TreeMap map = new TreeMap();
		
		for (int i = 0; i < 10; i++)
		{
			map.put(new Integer(i*2), new Integer(i*2));
		}
		
		System.out.println(map.headMap(new Integer(5)));
		System.out.println(map.tailMap(new Integer(5)));
		
		System.out.println(map.headMap(new Integer(55)));
		System.out.println(map.tailMap(new Integer(55)));
		
		System.out.println(map.subMap(new Integer(4),new Integer(8)));
		
		System.out.println(map.subMap(new Integer(5),new Integer(8)));
		System.out.println(map.subMap(new Integer(-5),new Integer(8)));
	}
	

	
	public void testInterval()
	{
		IntervalMap map = new IntervalMap();
		
		
//		map.put("1", "9", "18");
		map.putInterval("2", "4", "23");
		
//		map.put("6", "9", "68");
//		map.put("3", "7", "36");
		
		map.putInterval("5", "7", "56");
//		map.put("1", "9", "18X");
		
		System.out.println("0=" + map.getInInterval("0"));
		System.out.println("1=" + map.getInInterval("1"));
		System.out.println("2=" + map.getInInterval("2"));
		System.out.println("3=" + map.getInInterval("3"));
		System.out.println("4=" + map.getInInterval("4"));
		System.out.println("5=" + map.getInInterval("5"));
		System.out.println("6=" + map.getInInterval("6"));
		System.out.println("7=" + map.getInInterval("7"));
		System.out.println("8=" + map.getInInterval("8"));
		System.out.println("9=" + map.getInInterval("9"));
		
	}
	
}
