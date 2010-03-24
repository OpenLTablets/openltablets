/*
 * Created on May 30, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.syntax.code;

import org.openl.source.IOpenSourceCodeModule;
import org.openl.syntax.ISyntaxNode;
import org.openl.syntax.error.ISyntaxNodeError;

/**
 * The <code>IParsedCode</code> interface is designed to provide a common
 * protocol for objects what describes parsed source code.
 * 
 * @author snshor
 * 
 */
public interface IParsedCode {

    /**
     * Gets errors what was found during parsing process.
     * 
     * @return syntax errors
     */
    ISyntaxNodeError[] getErrors();

    /**
     * Gets link to source code that was used in parsing process.
     * 
     * @return source code
     */
    IOpenSourceCodeModule getSource();

    /**
     * Gets link to top node of parsed objects hierarchy. Parsed code
     * represented as a tree of parsed code objects (nodes).
     * 
     * @return top node
     */
    ISyntaxNode getTopNode();
}
