/*
 * Created on Jun 2, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.IBoundCode;
import org.openl.binding.IBoundNode;
import org.openl.syntax.code.IParsedCode;
import org.openl.syntax.error.ISyntaxNodeError;

/**
 * @author snshor
 *
 */
public class BoundCode implements IBoundCode {

    private IParsedCode parsedCode;
    private IBoundNode topNode;
    private ISyntaxNodeError[] error;
    // TODO this mekes sense only in context of a single method, once we move
    // further into more sophisticated types of code
    // bound code will be split int class hierarchy
    private int localFrameSize;

    public BoundCode(IParsedCode parsedCode, IBoundNode topNode, ISyntaxNodeError[] error, int localFrameSize) {
        this.parsedCode = parsedCode;
        this.topNode = topNode;
        this.error = error;
        this.localFrameSize = localFrameSize;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundCode#getError()
     */
    public ISyntaxNodeError[] getErrors() {
        return error;
    }

    /**
     * @return
     */
    public int getLocalFrameSize() {
        return localFrameSize;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundCode#getParsedCode()
     */
    public IParsedCode getParsedCode() {
        return parsedCode;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.IBoundCode#getTopNode()
     */
    public IBoundNode getTopNode() {
        return topNode;
    }

}
