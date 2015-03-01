/*
 * Created on May 13, 2003 Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.syntax.impl;

import org.openl.source.IOpenSourceCodeModule;
import org.openl.util.text.ILocation;

/**
 * @author snshor
 * 
 */
public class IdentifierNode extends TerminalNode {

    private String identifier;

    public IdentifierNode(String type, ILocation location, String identifier, IOpenSourceCodeModule module) {
        super(type, location, module);

       this.identifier = identifier.length() <= 4 ? identifier.intern() : identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    protected void printMySelf(int level, StringBuilder buf) {
        super.printMySelf(level, buf);
        buf.append("=" + identifier);
    }

}
