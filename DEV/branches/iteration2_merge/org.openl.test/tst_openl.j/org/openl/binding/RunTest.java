package org.openl.binding;

import org.openl.OpenL;
import org.openl.rules.helpers.IntRange;
import org.openl.syntax.impl.StringSourceCodeModule;
import org.openl.util.RangeWithBounds;

import junit.framework.Assert;
import junit.framework.TestCase;

public class RunTest extends TestCase
{
	void _runNoError(String expr, Object expected, String openl)
	{
		_runNoError(expr, expected, openl, "method.body");
	}	
	
	void _runNoError(String expr, Object expected, String openl, String parseType)
	{
		OpenL op = OpenL.getInstance(openl);
		Object res = op.evaluate(new StringSourceCodeModule(expr, null), parseType);
		Assert.assertEquals(expected, res);
	}

	public void testRun()
	{

		_runNoError("String x=null; x == null || x.length() < 10", true, "org.openl.j");
		_runNoError("String x=null; x != null && x.length() < 10", false, "org.openl.j");

		_runNoError("String x=null; Boolean b = true; b || x.length() < 10", true, "org.openl.j");
		_runNoError("String x=null; Boolean b = false; b && x.length() < 10", false, "org.openl.j");

		_runNoError("String x=\"abc\"; x == null || x.length() < 10", true, "org.openl.j");
		_runNoError("String x=\"abc\"; x != null && x.length() < 10", true, "org.openl.j");
		
		_runNoError("int x = 5; x += 4", new Integer(9), "org.openl.j");
		_runNoError("DoubleValue d1 = new DoubleValue(5); DoubleValue d2 = new DoubleValue(4); d1 += d2; d1.getValue()", new Double(9), "org.openl.rules.java");
		_runNoError("int i=0; for(int j=0; j < 10; ) {i += j;j++;} i", new Integer(45), "org.openl.j");

		//Testing new implementation of s1 == s2 for Strings. To achieve old identity test Strings must be downcasted to Object	
		_runNoError("String a=\"a\"; String b = \"b\"; a + b == a + 'b'", new Boolean(true), "org.openl.j");
		_runNoError("String a=\"a\"; String b = \"b\"; (Object)(a + b) == (Object)(a + 'b')", new Boolean(false), "org.openl.j");


		
		_runNoError("int x=5, y=7; x & y", 5 & 7, "org.openl.j");
		_runNoError("int x=5, y=7; x | y", 5 | 7, "org.openl.j");
		_runNoError("int x=5, y=7; x ^ y", 5 ^ 7, "org.openl.j");

		_runNoError("boolean x=true, y=false; x ^ y", true ^ false, "org.openl.j");
		
		_runNoError("int x=5, y=7; x < y ? 'a'+1 : 'b'+1", 'a'+1, "org.openl.j");

		_runNoError("int x=5, y=7; x << y ", 5 << 7, "org.openl.j");
		_runNoError("long x=5;int y=7; x << y ", (long)5 << 7, "org.openl.j");

		_runNoError("long x=-1;int y=7; x >> y ", (long)-1 >> 7, "org.openl.j");
		_runNoError("long x=-1;int y=60; x >>> y ", (long)-1 >>> 60, "org.openl.j");
		
		
		_runNoError("System.out << 35 << \"zzzz\" ", System.out, "org.openl.j");
		
		_runNoError("|-10| ", 10, "org.openl.j");
		_runNoError("true ? 10 : 20", 10, "org.openl.j");
		_runNoError("true ? 10 : 20", 10, "org.openl.j");
		_runNoError("false ? 10 : 20", 20, "org.openl.j");
		
		_runNoError("10%", 0.1, "org.openl.j");
		_runNoError("10% of \n the  50", 5.0, "org.openl.j");
		
		
		_runNoError("long Of =-1;int y=60; Of >>> y ", (long)-1 >>> 60, "org.openl.j");
		
	}
	
	public void testRange()
	{
		_runNoError("< 10K", new RangeWithBounds(Integer.MIN_VALUE,9999), "org.openl.j", "range.literal");
		_runNoError("<=33.3M", new RangeWithBounds(Integer.MIN_VALUE,33300000), "org.openl.j", "range.literal");
		_runNoError("5-$10", new RangeWithBounds(5,10), "org.openl.j", "range.literal");
		_runNoError("2B<", new RangeWithBounds(2000000001,Integer.MAX_VALUE), "org.openl.j", "range.literal");
		_runNoError("2.1B+", new RangeWithBounds(2100000000,Integer.MAX_VALUE), "org.openl.j", "range.literal");
		
	}


	
	public static void main(String[] args)
	{
		new RunTest().testRun();
	}
}
