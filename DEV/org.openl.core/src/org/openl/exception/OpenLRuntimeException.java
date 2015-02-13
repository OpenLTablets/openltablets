/*
 * Created on Jul 29, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Stack;

import org.apache.commons.io.output.StringBuilderWriter;
import org.openl.binding.IBoundNode;
import org.openl.main.SourceCodeURLTool;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.syntax.ISyntaxNode;
import org.openl.util.text.ILocation;

/**
 * Exception, that happens at runtime time of OpenL, when rules are invoking.
 * NOTE! Don`t use it as wrapper for java runtime exceptions on compile time of OpenL. 
 * 
 * @author snshor
 *
 */
public class OpenLRuntimeException extends RuntimeException implements OpenLException {

    private static final long serialVersionUID = -8422089115244904493L;

    private IBoundNode node;
    private Stack<IBoundNode> openlCallStack = new Stack<IBoundNode>();
    
    public OpenLRuntimeException() {
        super();
    }
    
    public OpenLRuntimeException(String message, Throwable cause) {
        super(message, cause);        
    }
    
    public OpenLRuntimeException(String message) {
        super(message);    
    }
    
    public OpenLRuntimeException(Throwable cause) {
        super(cause);    
    }

    public OpenLRuntimeException(Throwable cause, IBoundNode node) {
        super(cause);
        this.node = node;
    }
    
    public OpenLRuntimeException(String message, IBoundNode node) {
        super(message);
        this.node = node;
    }

    public String getOriginalMessage(){
        return super.getMessage();
    }

    @Override
    public String getMessage() {
        StringBuilderWriter messageWriter = new StringBuilderWriter();
        PrintWriter pw = new PrintWriter(messageWriter);
        pw.println(super.getMessage());
        if (node != null) {
            SourceCodeURLTool.printCodeAndError(getLocation(), getSourceModule(), pw);
            SourceCodeURLTool.printSourceLocation(this, pw);
        }
        return messageWriter.toString();
    }

    public ILocation getLocation() {
        if (node != null) {
            ISyntaxNode syntaxNode = node.getSyntaxNode();
            if (syntaxNode != null) {
                return syntaxNode.getSourceLocation();
            }
        }
        return null;
    }

    public IOpenSourceCodeModule getSourceModule() {
        if (node != null) {
            ISyntaxNode syntaxNode = node.getSyntaxNode();
            if (syntaxNode != null) {
                return syntaxNode.getModule();
            }
        }
        return null;
    }

    public IBoundNode getNode() {
        return node;
    }

    public Stack<IBoundNode> getOpenlCallStack() {
        return openlCallStack;
    }
    
    public void pushMethodNode(IBoundNode node) {
        openlCallStack.push(node);
    }

    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream printStream) {
        synchronized (printStream) {
            printStackTrace(new PrintWriter(printStream, true));
        }
    }

    @Override
    public void printStackTrace(PrintWriter writer) {
        OpenLExceptionUtils.printRuntimeError(this, writer);
    }

}
