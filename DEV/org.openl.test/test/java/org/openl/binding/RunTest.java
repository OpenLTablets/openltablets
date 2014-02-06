package org.openl.binding;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Assert;
import junit.framework.TestCase;

import org.openl.OpenL;
import org.openl.engine.OpenLManager;
import org.openl.exception.OpenLRuntimeException;
import org.openl.meta.DoubleValue;
import org.openl.source.SourceType;
import org.openl.source.impl.StringSourceCodeModule;
import org.openl.syntax.exception.CompositeSyntaxNodeException;
import org.openl.util.RangeWithBounds;
import org.openl.util.RangeWithBounds.BoundType;

public class RunTest extends TestCase {
    private static interface AssertionExpression<T> {
        void makeAssertion(T expected, T result);
    }

    private static AssertionExpression<Object> equalsAssertion = new AssertionExpression<Object>() {
        public void makeAssertion(Object expected, Object result) {
            Assert.assertEquals(expected, result);
        }
    };

    private static AssertionExpression<Object> nopAssertion = new AssertionExpression<Object>() {
        public void makeAssertion(Object expected, Object result) {
            // do nothing
        }
    };

    public static void main(String[] args) {
        new RunTest().testRun();
    }

    void _runNoError(String expr, Object expected, String openl) {
        _runNoError(expr, expected, openl, SourceType.METHOD_BODY);
    }

    void _runNoError(String expr, Object expected, String openlName, SourceType parseType) {
        _runNoError(expr, expected, openlName, parseType, equalsAssertion);
    }

    @SuppressWarnings("unchecked")
    static <T extends Object> void _runNoError(String expression, T expected, String openlName, SourceType parseType,
            AssertionExpression<T> assertion) {
        OpenL openl = OpenL.getInstance(openlName);
        T res = (T)OpenLManager.run(openl, new StringSourceCodeModule(expression, null), parseType);
        assertion.makeAssertion(expected, res);
    }


    public static void _runWithError(String expr, Class<? extends Throwable> expected, String openl, SourceType parseType) {
    	_runWithError(expr, expected, null, openl, parseType);
    }
    
    public static void _runWithError(String expr, Class<? extends Throwable> expected, String message, String openl) {
    	_runWithError(expr, expected, message, openl, SourceType.METHOD_BODY);
    }
    
    public static void _runWithError(String expr, Class<? extends Throwable> expected, String message, String openl, SourceType parseType) {
        Throwable ex = null;
        try {
            _runNoError(expr, expected, openl, parseType, nopAssertion);
        } catch (Throwable t) {
            ex = t;
        }
        Assert.assertNotNull(ex);
        Assert.assertEquals(expected, ex.getClass());
        if (message != null)
        	Assert.assertEquals(message,  ex.getMessage());

    }

    public void testBig() {
        _runNoError("Vector x = new Vector(); x.size()", 0, OpenL.OPENL_J_NAME);

        _runNoError("BigDecimal x = 10, y = 20; x > y", false, OpenL.OPENL_J_NAME);
        _runNoError("BigDecimal x = 10, y = 20; x < y", true, OpenL.OPENL_J_NAME);
        _runNoError("BigDecimal x = 10, y = 20; x + y == 30", true, OpenL.OPENL_J_NAME);
        _runNoError("BigDecimal x = 10, y = 20; x + y  - 5", new BigDecimal(25), OpenL.OPENL_J_NAME);

        _runNoError("BigInteger x = 10; BigDecimal y = x; x + x  - 5", new BigInteger("15", 10), OpenL.OPENL_J_NAME);
        _runNoError("BigInteger x = 10; BigDecimal y = x; x == y", true, OpenL.OPENL_J_NAME);
        _runNoError("BigInteger x = 10; BigDecimal y = x; y == x", true, OpenL.OPENL_J_NAME);

        _runNoError("BigInteger x = 10; x != null", true, OpenL.OPENL_J_NAME);
        _runNoError("BigInteger x = 10; null != x", true, OpenL.OPENL_J_NAME);

        _runNoError("BigDecimal x = 10; x != null", true, OpenL.OPENL_J_NAME);
        _runNoError("BigDecimal x = 10; null != x", true, OpenL.OPENL_J_NAME);

        _runNoError("BigInteger x = 10; x == null", false, OpenL.OPENL_J_NAME);
        _runNoError("BigInteger x = 10; null == x", false, OpenL.OPENL_J_NAME);

        _runNoError("BigDecimal x = 10; x == null", false, OpenL.OPENL_J_NAME);
        _runNoError("BigDecimal x = 10; null == x", false, OpenL.OPENL_J_NAME);

    }

    public void testComparable() {
        _runNoError("String x = \"abc\";String y = \"abc\"; x < y", false, OpenL.OPENL_J_NAME);
        _runNoError("String x = \"abc\";String y = \"abc\"; x <= y", true, OpenL.OPENL_J_NAME);

        // TODO fix String < Integer - must be compile time error
//         _runNoError("String x = \"abc\";Integer y = 10; x <= y", true, OpenL.OPENL_J_NAME);
    }

    public void testDoubleRange() {
        AssertionExpression<RangeWithBounds> assertion = new AssertionExpression<RangeWithBounds>() {
            public void makeAssertion(RangeWithBounds expected, RangeWithBounds result) {
                Assert.assertEquals(expected.getMax().doubleValue(), result.getMax().doubleValue(), 0.001);
                Assert.assertEquals(expected.getMin().doubleValue(), result.getMin().doubleValue(), 0.001);
            }
        };

        _runNoError("10.0", new RangeWithBounds(10.0, 10.0), OpenL.OPENL_J_NAME, SourceType.DOUBLE_RANGE, assertion);
        _runNoError("< 10.0K", new RangeWithBounds(Double.NEGATIVE_INFINITY, 9999.99999999999), OpenL.OPENL_J_NAME,
                SourceType.DOUBLE_RANGE, assertion);
        _runNoError("<=33.3M", new RangeWithBounds(Double.NEGATIVE_INFINITY, 33300000.0), OpenL.OPENL_J_NAME,
                SourceType.DOUBLE_RANGE, assertion);
        _runNoError("5.0-$10.0", new RangeWithBounds(5.0, 10.0), OpenL.OPENL_J_NAME, SourceType.DOUBLE_RANGE, assertion);
        _runNoError("2B<", new RangeWithBounds(2000000000.0001, Double.POSITIVE_INFINITY), OpenL.OPENL_J_NAME,
                SourceType.DOUBLE_RANGE, assertion);
        _runNoError("2.1B+", new RangeWithBounds(2100000000, Double.POSITIVE_INFINITY), OpenL.OPENL_J_NAME,
                SourceType.DOUBLE_RANGE, assertion);

        _runWithError("10.0-2.0", CompositeSyntaxNodeException.class, OpenL.OPENL_J_NAME, SourceType.DOUBLE_RANGE);
        _runNoError("10.0-12,599.0", new RangeWithBounds(10.0, 12599.0), OpenL.OPENL_J_NAME, SourceType.DOUBLE_RANGE, assertion);
        _runNoError("$10,222.0 .. 12,599.0   ", new RangeWithBounds(10222.0, 12599.0), OpenL.OPENL_J_NAME,
                SourceType.DOUBLE_RANGE, assertion);

    }

    public void testLong() {
        _runNoError("long x = 4; x + 5.0", 9.0, OpenL.OPENL_J_NAME);
        _runNoError("long x = 4; x - 5.0", -1.0, OpenL.OPENL_J_NAME);
        _runNoError("long x = 4; x - 5", -1L, OpenL.OPENL_J_NAME);
//        _runNoError("Long x = null; x - 5.0", -1.0, OpenL.OPENL_J_NAME);
        
    }

    public void testRange() {
        // _runNoError("-1-2", new RangeWithBounds(10, 10), OpenL.OPENL_J_NAME,
        // "range.literal");
        _runNoError("10", new RangeWithBounds(10, 10), OpenL.OPENL_J_NAME, SourceType.INT_RANGE);
        _runNoError("< 10K", new RangeWithBounds(Integer.MIN_VALUE, 10000, BoundType.INCLUDING, BoundType.EXCLUDING), OpenL.OPENL_J_NAME, SourceType.INT_RANGE);
        _runNoError("<=33.3M", new RangeWithBounds(Integer.MIN_VALUE, 33300000), OpenL.OPENL_J_NAME, SourceType.INT_RANGE);
        _runNoError("5-$10", new RangeWithBounds(5, 10), OpenL.OPENL_J_NAME, SourceType.INT_RANGE);
        _runNoError("2B<", new RangeWithBounds(2000000000, Integer.MAX_VALUE, BoundType.EXCLUDING, BoundType.INCLUDING), OpenL.OPENL_J_NAME, SourceType.INT_RANGE);
        _runNoError("2.1B+", new RangeWithBounds(2100000000, Integer.MAX_VALUE), OpenL.OPENL_J_NAME, SourceType.INT_RANGE);

        _runWithError("10-2", CompositeSyntaxNodeException.class, OpenL.OPENL_J_NAME, SourceType.INT_RANGE);
        _runNoError("10-12,599", new RangeWithBounds(10, 12599), OpenL.OPENL_J_NAME, SourceType.INT_RANGE);
        _runNoError("$10,222 .. 12,599   ", new RangeWithBounds(10222, 12599), OpenL.OPENL_J_NAME, SourceType.INT_RANGE);

    }

    public void testRun() {

        _runNoError("String $x$y=null; $x$y == null || $x$y.length() < 10", true, OpenL.OPENL_J_NAME);

        _runNoError("String x=null; x == null || x.length() < 10", true, OpenL.OPENL_J_NAME);
        _runNoError("String x=null; x != null && x.length() < 10", false, OpenL.OPENL_J_NAME);

        _runNoError("String x=null; Boolean b = true; b || x.length() < 10", true, OpenL.OPENL_J_NAME);
        _runNoError("String x=null; Boolean b = false; b && x.length() < 10", false, OpenL.OPENL_J_NAME);

        _runNoError("String x=\"abc\"; x == null || x.length() < 10", true, OpenL.OPENL_J_NAME);
        _runNoError("String x=\"abc\"; x != null && x.length() < 10", true, OpenL.OPENL_J_NAME);

        _runNoError("int x = 5; x += 4", new Integer(9), OpenL.OPENL_J_NAME);
        _runNoError(
                "DoubleValue d1 = new DoubleValue(5); DoubleValue d2 = new DoubleValue(4); d1 += d2; d1.getValue()",
                new Double(9), OpenL.OPENL_JAVA_NAME);
        _runNoError("int i=0; for(int j=0; j < 10; ) {i += j;j++;} i", new Integer(45), OpenL.OPENL_J_NAME);


        
        // Testing new implementation of s1 == s2 for Strings. To achieve old
        // identity test Strings must be upcasted to Object
        _runNoError("String a=\"a\"; String b = \"b\"; a + b == a + 'b'", new Boolean(true), OpenL.OPENL_J_NAME);
        _runNoError("String a=\"a\"; String b = \"b\"; a + b == a + 'c'", new Boolean(false), OpenL.OPENL_J_NAME);
        _runNoError("String a=\"a\"; String b = \"b\"; a + b != a + 'b'", new Boolean(false), OpenL.OPENL_J_NAME);
        _runNoError("String a=\"a\"; String b = \"b\"; a + b != a + 'c'", new Boolean(true), OpenL.OPENL_J_NAME);
        _runNoError("String a=\"a\"; String b = \"b\"; (Object)(a + b) == (Object)(a + 'b')", new Boolean(true),
            OpenL.OPENL_J_NAME);        
        _runNoError("String a=\"a\"; String b = \"b\"; (Object)(a + b) ==== (Object)(a + 'b')", new Boolean(false),
            OpenL.OPENL_J_NAME);
        
        _runNoError("boolean a=true; boolean b = false; a == !b", new Boolean(true), OpenL.OPENL_J_NAME);
        _runNoError("boolean a=true; boolean b = false; a != b", new Boolean(true), OpenL.OPENL_J_NAME);
        
        _runNoError("Integer x = 1; \"aaa\".substring(x)", "aaa".substring(1), OpenL.OPENL_J_NAME);

        
        
        _runNoError("int x=5, y=7; x & y", 5 & 7, OpenL.OPENL_J_NAME);
        _runNoError("int x=5, y=7; x | y", 5 | 7, OpenL.OPENL_J_NAME);
        _runNoError("int x=5, y=7; x ^ y", 5 ^ 7, OpenL.OPENL_J_NAME);

        _runNoError("boolean x=true, y=false; x ^ y", true ^ false, OpenL.OPENL_J_NAME);

        _runNoError("int x=5, y=7; x < y ? 'a'+1 : 'b'+1", 'a' + 1, OpenL.OPENL_J_NAME);
        _runNoError("int x=5, y=7; x < y ? 0.7 : 3", 0.7, OpenL.OPENL_J_NAME);
        _runNoError("int x=5, y=7; x < y ? 3 : (int)0.7", 3, OpenL.OPENL_J_NAME);

        _runNoError("int x=5, y=7; x << y ", 5 << 7, OpenL.OPENL_J_NAME);
        _runNoError("long x=5;int y=7; x << y ", (long) 5 << 7, OpenL.OPENL_J_NAME);

        _runNoError("long x=-1;int y=7; x >> y ", (long) -1 >> 7, OpenL.OPENL_J_NAME);
        _runNoError("long x=-1;int y=60; x >>> y ", (long) -1 >>> 60, OpenL.OPENL_J_NAME);

        _runNoError("System.out << 35 << \"zzzz\" ", System.out, OpenL.OPENL_J_NAME);

        _runNoError("|-10| ", 10, OpenL.OPENL_J_NAME);
        _runNoError("true ? 10 : 20", 10, OpenL.OPENL_J_NAME);
        _runNoError("true ? 10 : 20", 10, OpenL.OPENL_J_NAME);
        _runNoError("false ? 10 : 20", 20, OpenL.OPENL_J_NAME);

        _runNoError("10%", 0.1, OpenL.OPENL_J_NAME);
        _runNoError("10% of \n the  50", 5.0, OpenL.OPENL_J_NAME);

        _runNoError("long Of =-1;int y=60; Of >>> y ", (long) -1 >>> 60, OpenL.OPENL_J_NAME);
        _runNoError("5.0 ** 7.0 ", Math.pow(5, 7), OpenL.OPENL_JAVA_NAME);
        _runNoError("DoubleValue x = 5.0; x ** 7 ", new DoubleValue(Math.pow(5, 7)), OpenL.OPENL_JAVA_NAME);
        _runNoError("BigDecimal x = 5.0; x ** 7 ", new BigDecimal(Math.pow(5, 7)), OpenL.OPENL_JAVA_NAME);



    
    }
    
    
    public void testAggregate()
    {
    	

    	
    	_runNoError("String[] ary = {\"bb\", \"ddd\", \"aaa\"}; ary[2]", "aaa", OpenL.OPENL_J_NAME);
        _runNoError("String[] ary = {\"bb\", \"ddd\", \"aaa\"}; ary[!@ startsWith(\"b\")]", "bb", OpenL.OPENL_J_NAME);
      
        _runNoError("String[] ary = {\"bb\", \"ddd\", \"aaa\"}; int x = 3; ary[@ length() == x][0]", "ddd", OpenL.OPENL_J_NAME);
        
        _runNoError("String[] ary = {\"bb\", \"ddd\", \"aaa\"}; ary[^@ substring(1,2)][0]", "aaa", OpenL.OPENL_J_NAME);
        _runNoError("String[] ary = {\"bb\", \"ddd\", \"aaa\"}; ary[^@ substring(1)][1]", "bb", OpenL.OPENL_J_NAME);
        _runNoError("String[] ary = {\"aab\", \"ddd\", \"aac\", \"aaba\"}; ary[^@ substring(0,1)][0]", "aab", OpenL.OPENL_J_NAME);
        _runNoError("String[] ary = {\"aab\", \"ddd\", \"aac\", \"aaba\"}; ary[v@ substring(0,1)][0]", "ddd", OpenL.OPENL_J_NAME);
        
        _runNoError("String[] ary = {\"aab\", \"ddd\", \"aac\", \"aaba\"}; ary[~@ substring(0,1)].length", 2, OpenL.OPENL_J_NAME);
        _runNoError("String[] ary = {\"aab\", \"ddd\", \"aac\", \"aaba\"}; ary[~@ substring(0,1)][0].length", 3, OpenL.OPENL_J_NAME);
        _runNoError("String[] ary = {\"aab\", \"ddd\", \"aac\", \"aaba\"}; ary[~@ substring(0,1)][1].length", 1, OpenL.OPENL_J_NAME);
        _runNoError("String[] ary = {\"aab\", \"ddd\", \"aac\", \"aaba\"}; ary[~@ substring(0,1)][0][2]", "aaba", OpenL.OPENL_J_NAME);
        
        _runNoError("String[] ary = {\"daab\",\"aab\", \"ddd\", \"aac\", \"aaba\"}; ary[~@ substring(0,1)][0][1]", "ddd", OpenL.OPENL_J_NAME);
        
    	
        _runNoError("String[] ary = {\"aab\", \"ddd\", \"aac\", \"aaba\"}; ary[*@ substring(0,1)].length", 4, OpenL.OPENL_J_NAME);
        _runNoError("String[] ary = {\"aab\", \"ddd\", \"aac\", \"aaba\"}; ary[*!@ substring(0,1)].length", 2, OpenL.OPENL_J_NAME);
        
        
        //test named element
        
   //     _runNoError("String[] ary = {\"bb\", \"ddd\", \"aaa\"}; int x = 3; ary[(String s) @ length() == x][0]", "ddd", OpenL.OPENL_J_NAME);
        _runNoError("String[] ary = {\"bb\", \"ddd\", \"aaa\"}; ary[(String s) !@ s.startsWith(\"b\")]", "bb", OpenL.OPENL_J_NAME);
        _runNoError("String[] ary = {\"bb\", \"ddd\", \"aaa\"}; ary[( s ) !@ s.startsWith(\"aa\")]", "aaa", OpenL.OPENL_J_NAME);
        
        
        _runNoError("String[] ary = {\"bbcc\", \"dddee\",\"aaabbe\" ,\"aaabb\",}; ary[!@startsWith (ary[( s ) !@ s.toUpperCase().endsWith(\"BB\")])]", "aaabbe", OpenL.OPENL_J_NAME);
        
        _runNoError("String[] ary = {\"bbccdd\", \"dddee\",\"dddeedd\", \"ddd\" ,\"aaabbdd\",}; ary[(s1)!@ s1.startsWith (ary[( s2 ) !@ s2.toUpperCase().startsWith(\"DD\") && s2 != s1])]", "dddeedd", OpenL.OPENL_J_NAME);
        
        _runNoError("String[] ary1 = {\"bbccdd\", \"dddee\",\"dddeedd\", \"ddd\" ,\"aaabbdd\",}; String[] ary2 = {\"ZZZ\", \"XXXX\",\"YYYYYY\", \"ddd\" ,\"aaabbdd\",}; ary1[(s)!@ s.startsWith(\"aa\")] + ary2[(s)!@ s.startsWith(\"ZZ\")]", "aaabbddZZZ", OpenL.OPENL_J_NAME);

        _runWithError("String[] ary = {\"bb\", \"ddd\", \"aaa\"}; ary[(String s) !@ s.getDay() < 5]",   CompositeSyntaxNodeException.class, null, OpenL.OPENL_J_NAME);
        _runWithError("String[] ary = {\"bb\", \"ddd\", \"aaa\"}; ary[(Date d) !@ d.getDay() < 5]",     CompositeSyntaxNodeException.class, null, OpenL.OPENL_J_NAME);

        
        //test lists
        
        _runNoError("List list = new ArrayList(); list.add(\"bbccdd\"); list.add(\"dddee\");list.add(\"dddeedd\"); list.get(0)", "bbccdd", OpenL.OPENL_J_NAME);

        _runWithError("List list = new ArrayList(); list.add(\"bbccdd\"); list.add(\"dddee\");list.add(\"dddeedd\"); list[(Date d)!@ d.getDay() < 6]", OpenLRuntimeException.class, null, OpenL.OPENL_J_NAME);
        _runNoError("List list = new ArrayList(); list.add(\"bbccdd\"); list.add(\"dddee\");list.add(\"dddeedd\"); list[(String s)!@ s.contains(\"ee\")]", "dddee",  OpenL.OPENL_J_NAME);
        _runNoError("List list = new ArrayList(); list.add(\"bbccdd\"); list.add(\"dddee\");list.add(\"dddeedd\"); list[(String str) select first where str.contains(\"ee\")]", "dddee",  OpenL.OPENL_J_NAME);

        _runNoError("List list = new ArrayList(); list.add(\"bb\");list.add( \"ddd\");list.add(\"aaa\"); int x = 3; list[(String s) @ length() == x][0]", "ddd", OpenL.OPENL_J_NAME);
        _runNoError("List list = new ArrayList(); list.add(\"bb\");list.add( \"ddd\");list.add(\"aaa\"); int x = 3; list[(String s) select all having length() == x][0]", "ddd", OpenL.OPENL_J_NAME);
    

        _runNoError("List list = new ArrayList(); list.add(\"AABA\"); list.add(\"ddd\"); list.add( \"aac\"); list.add(\"aab\"); list[(String x) ^@ toLowerCase()][0]", "aab", OpenL.OPENL_J_NAME);
//        _runNoError("List list = new ArrayList(); list.add(\"aab\", \"ddd\", \"aac\", \"aaba\"}; ary[v@ substring(0,1)][0]", "ddd", OpenL.OPENL_J_NAME);
        
        _runNoError("List list = new ArrayList(); list.add(\"AABA\"); list.add(\"ddd\"); list.add( \"aac\"); list.add(\"aab\"); list[(String x) ~@ substring(0,1)][1][0]", "ddd", OpenL.OPENL_J_NAME);

        
        
        _runNoError("List list = new ArrayList(); list.add(\"AABA\"); list.add(\"ddd\"); list.add( \"aac\"); list.add(\"aab\"); list[(String x) *@ substring(0,1)].length", 4, OpenL.OPENL_J_NAME);
        _runNoError("List list = new ArrayList(); list.add(\"AABA\"); list.add(\"ddd\"); list.add( \"aac\"); list.add(\"aab\"); list[(String x) *!@ substring(0,1).toLowerCase()].length", 2, OpenL.OPENL_J_NAME);
        
    }
    
    
    public void testAggregate1()
    {
    
    }
    
    
    public void testStatic()
    {
        _runNoError("int.class", int.class, OpenL.OPENL_J_NAME);
        _runWithError("String.length()", CompositeSyntaxNodeException.class, OpenL.OPENL_J_NAME, SourceType.METHOD_BODY);
        

//        _runWithError("int x = 5; x.class", SyntaxNodeException.class, OpenL.OPENL_J_NAME, SourceType.METHOD_BODY);
    }
    
    
    public void testLongName()
    {
        _runNoError("new java.math.BigDecimal(10)", new java.math.BigDecimal(10), OpenL.OPENL_J_NAME);
        _runNoError("java.io.File f = new java.io.File(\"c:\temp\"); f.getParent()", "c:", OpenL.OPENL_J_NAME);
//        _runNoError("long t = java.lang.System.currentTimeInMillis(); ", 1, OpenL.OPENL_J_NAME);
           }
    
}
