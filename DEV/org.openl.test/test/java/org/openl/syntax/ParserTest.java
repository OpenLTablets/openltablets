/*
 * Created on May 13, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.syntax;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Assert;
import junit.framework.TestCase;

import org.openl.OpenL;
import org.openl.binding.INodeBinder;
import org.openl.binding.impl.DoubleNodeBinder;
import org.openl.binding.impl.IntNodeBinder;
import org.openl.binding.impl.LiteralBoundNode;
import org.openl.conf.OpenConfigurationException;
import org.openl.source.impl.StringSourceCodeModule;
import org.openl.syntax.code.IParsedCode;
import org.openl.syntax.exception.CompositeSyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.impl.ASyntaxNode;
import org.openl.syntax.impl.BinaryNode;
import org.openl.syntax.impl.NaryNode;
import org.openl.util.ASelector;
import org.openl.util.ISelector;
import org.openl.util.text.ILocation;
import org.openl.util.text.TextInfo;
import org.openl.util.tree.TreeIterator;

/**
 * @author snshor
 *
 */
public class ParserTest extends TestCase {

    public static void main(String[] args) {

        String src = " x < y + 10";
        OpenL op = OpenL.getInstance(OpenL.OPENL_J_NAME);
        IParsedCode pc = op.getParser().parseAsMethodBody(new StringSourceCodeModule(src, null));
        System.out.println(pc.getTopNode());

    }

    /**
     * Constructor for ParserTest.
     *
     * @param arg0
     */
    public ParserTest(String arg0) {
        super(arg0);
    }

    @SuppressWarnings("unchecked")
    public void _testLiteral(String src, String res, String type) throws OpenConfigurationException {

        OpenL op = OpenL.getInstance(OpenL.OPENL_J_NAME);
        IParsedCode pc = op.getParser().parseAsMethodBody(new StringSourceCodeModule(src, null));

        TreeIterator it = new TreeIterator<ISyntaxNode>(pc.getTopNode(), ASyntaxNode.TREE_ADAPTOR, TreeIterator.DEFAULT);

        ILiteralNode ln = (ILiteralNode) it.select(ASelector.selectClass(ILiteralNode.class)).next();

        Assert.assertEquals(res, ln.getImage());
        Assert.assertEquals(type, ln.getType());
    }

    public void _testMethodHeader(String src, String res, String type) throws OpenConfigurationException {

        OpenL op = OpenL.getInstance(OpenL.OPENL_J_NAME);
        IParsedCode pc = op.getParser().parseAsMethodHeader(new StringSourceCodeModule(src, null));

        Assert.assertEquals(0, pc.getErrors().length);

        ISyntaxNode syntaxNode = pc.getTopNode();

        Assert.assertEquals(type, syntaxNode.getType());
    }

    @SuppressWarnings("unchecked")
    public void _testModule(String src, final String type) throws OpenConfigurationException {

        OpenL op = OpenL.getInstance(OpenL.OPENL_J_NAME);
        IParsedCode pc = op.getParser().parseAsModule(new StringSourceCodeModule(src, null));

        SyntaxNodeException[] error = pc.getErrors();
        if (error.length > 0) {
            throw new CompositeSyntaxNodeException("Parsing Error:", error);
        }

        TreeIterator it = new TreeIterator(pc.getTopNode(), ASyntaxNode.TREE_ADAPTOR, TreeIterator.DEFAULT);

        ISelector sel = new ASelector() {
            public boolean select(Object obj) {
                return ((ISyntaxNode) obj).getType().equals(type);
            }
        };

        ISyntaxNode bn = (ISyntaxNode) it.select(sel).next();
        Assert.assertNotNull(bn);

        Assert.assertEquals(type, bn.getType());
    }

    @SuppressWarnings("unchecked")
    public <T extends ISyntaxNode> T  _testOperator(String src, Class<T> nodeType) throws OpenConfigurationException {
 
        OpenL op = OpenL.getInstance(OpenL.OPENL_J_NAME);
        IParsedCode pc = op.getParser().parseAsMethodBody(new StringSourceCodeModule(src, null));

        TreeIterator it = new TreeIterator(pc.getTopNode(), ASyntaxNode.TREE_ADAPTOR, TreeIterator.DEFAULT);

        return (T) it.select(ASelector.selectClass(nodeType)).next();
    }


    public void _testErrorMsg(String src, String messageStart)
    {
        OpenL op = OpenL.getInstance(OpenL.OPENL_J_NAME);
        IParsedCode pc = op.getParser().parseAsMethodBody(new StringSourceCodeModule(src, null));

        SyntaxNodeException[] errors = pc.getErrors();

        if (errors.length == 0)
          throw new RuntimeException("This expression must produce an error!");

        String message = errors[0].getMessage();

        if (!message.startsWith(messageStart))
            throw new RuntimeException(String.format("'%s' should start with '%s'", message, messageStart));


    }

    @SuppressWarnings("unchecked")
    public void _testType(String src, final String type) throws OpenConfigurationException {

        OpenL op = OpenL.getInstance(OpenL.OPENL_J_NAME);
        IParsedCode pc = op.getParser().parseAsMethodBody(new StringSourceCodeModule(src, null));

        SyntaxNodeException[] error = pc.getErrors();
        if (error.length > 0) {
            throw new CompositeSyntaxNodeException("Parsing Error:", error);
        }

        TreeIterator it = new TreeIterator(pc.getTopNode(), ASyntaxNode.TREE_ADAPTOR, TreeIterator.DEFAULT);

        ISelector sel = new ASelector() {
            public boolean select(Object obj) {
                return ((ISyntaxNode) obj).getType().equals(type);
            }
        };

        ISyntaxNode bn = (ISyntaxNode) it.select(sel).next();
        Assert.assertNotNull(bn);

        Assert.assertEquals(type, bn.getType());
    }

    @SuppressWarnings("unchecked")
    public void _testNumberParseAndBind(INodeBinder binder, String src, Object res, Class<?> type) throws Exception {

        OpenL op = OpenL.getInstance(OpenL.OPENL_J_NAME);
        IParsedCode pc = op.getParser().parseAsMethodBody(new StringSourceCodeModule(src, null));

        TreeIterator it = new TreeIterator<ISyntaxNode>(pc.getTopNode(), ASyntaxNode.TREE_ADAPTOR, TreeIterator.DEFAULT);

        ILiteralNode ln = (ILiteralNode) it.select(ASelector.selectClass(ILiteralNode.class)).next();

        LiteralBoundNode literalBoundNode = (LiteralBoundNode) binder.bind(ln, null);
        Assert.assertEquals(type, literalBoundNode.getType().getInstanceClass());
        Assert.assertEquals(res, literalBoundNode.getValue());
    }


    public void testArray() {
        _testType("new int[10]", "op.new.array");
        _testType("new int[10][]", "op.new.array");
        _testType("new int[10][20][]", "op.new.array");
    }

    public void testAssign() {
        _testType("int x = y; z", "local.var.declaration");
    }

    public void testChain() {
        _testType("x.y", "chain");
        _testType("x.y[10].foo(z)", "chain");
        _testType("x.y", "chain");
    }

    public void testFunc() throws OpenConfigurationException {
        _testType("sin(5, 10)", "function");
    }

    public void testErr1() throws OpenConfigurationException {

        _testErrorMsg("sin(5, 10", "Need to close '('");
    }

    public void testErr2() throws OpenConfigurationException {
        _testErrorMsg("\"abc", "Need to close");
    }

    public void testErr3() throws OpenConfigurationException {
        _testErrorMsg("x=y{y=z}", "Encountered");
    }

    public void testErr4() throws OpenConfigurationException {
        _testErrorMsg("return u", "Encountered");
    }

    public void testErr5() throws OpenConfigurationException {
        _testErrorMsg("\"ab\\zc\"", "Unexpected symbol 'z'");
    }

    public void testIf() {
        _testType("if (x) a();", "control.if");
    }




    public void testLiteral() throws OpenConfigurationException {
        // we should remove suffix the next line produces NumberFormatException
        // Assert.assertEquals(new Long(5), Long.decode("5L"));

        // _testLiteral("-5L", "-5L", "literal.integer");
        _testLiteral("0xff", "0xff", "literal.integer");
        _testLiteral("5L", "5L", "literal.integer");
        _testLiteral("\"ab\\n\"", "\"ab\\n\"", "literal.string");
        _testLiteral("2001-01-01", "2001-01-01", "literal.date");
        _testLiteral("11:40", "11:40", "literal.time");
        _testLiteral("11:40:33", "11:40:33", "literal.time");
        _testLiteral("11:40:33.744", "11:40:33.744", "literal.time");
        _testLiteral("2001-01-01 11:40:33.744", "2001-01-01 11:40:33.744", "literal.datetime");

    }
    
    public void testRange() throws OpenConfigurationException {
      _testType("$Step1:$Step7","range.variable");
    }
    

    public void testLocation() throws OpenConfigurationException {
        String test1 = "\tx";
        OpenL op = OpenL.getInstance(OpenL.OPENL_J_NAME);
        IParsedCode pc = op.getParser().parseAsMethodBody(new StringSourceCodeModule(test1, null));
        ILocation loc = pc.getTopNode().getSourceLocation();
        Assert.assertEquals(1, loc.getStart().getAbsolutePosition(new TextInfo(test1)));

    }

    public void testMethod() {
        _testModule("int x(){return 5;}", "module.top");
    }

    public void testMethodHeader() {
        _testMethodHeader("int x(a a1, b b1)", null, "method.header");
    }

    public void testOperator() throws OpenConfigurationException {
        BinaryNode binaryNode = _testOperator("x+y", BinaryNode.class);
        Assert.assertEquals("op.binary.add", binaryNode.getType());
        
        binaryNode = _testOperator("x-3", BinaryNode.class);
        Assert.assertEquals("op.binary.subtract", binaryNode.getType());

        binaryNode = _testOperator("x%3", BinaryNode.class);
        Assert.assertEquals("op.binary.rem", binaryNode.getType());

        binaryNode = _testOperator("x is less than 3", BinaryNode.class);
        Assert.assertEquals("op.binary.lt", binaryNode.getType());

        binaryNode = _testOperator("x or y", BinaryNode.class);
        Assert.assertEquals("op.binary.or", binaryNode.getType());

        binaryNode = _testOperator("x and y", BinaryNode.class);
        Assert.assertEquals("op.binary.and", binaryNode.getType());

        NaryNode naryNode = _testOperator("x?y: z", NaryNode.class);
        Assert.assertEquals("op.ternary.qmark", naryNode.getChild(0).getType());

    }

    public void testParse() {

        // new Parser().parse("y - (x.z + t); x+y", null, new JGrammar());

    }

    public void testNumberParseAndBind() throws Exception {
        _testNumberParseAndBind(new IntNodeBinder(), "1000000", 1000000, int.class);
        _testNumberParseAndBind(new IntNodeBinder(), "1000000000000", 1000000000000L, long.class);
        _testNumberParseAndBind(new IntNodeBinder(), "10000000000000000000", new BigInteger("10000000000000000000"), BigInteger.class);
        
        _testNumberParseAndBind(new DoubleNodeBinder(), "1e+308", Double.valueOf("1e+308"), double.class);
        _testNumberParseAndBind(new DoubleNodeBinder(), "2e+308", new BigDecimal("2e+308"), BigDecimal.class);
    }

}
