package org.openl.syntax.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.openl.message.OpenLMessage;

/**
 * Added possibility to handle list of {@link OpenLMessage}.
 *  
 */
public class CompositeOpenlException extends CompositeSyntaxNodeException {
   
    private static final long serialVersionUID = 5130142151601932536L;
    
    private List<OpenLMessage> errorMessages = new ArrayList<OpenLMessage>();
    
    public CompositeOpenlException(String message, SyntaxNodeException[] errors, List<OpenLMessage> errorMessages) {
        super(message, errors);
        if (errorMessages != null) {
            this.errorMessages = new ArrayList<OpenLMessage>(errorMessages); 
        }
    }
    
    @Override
    public String getMessage() {
        String superMessage = super.getMessage();
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        for (OpenLMessage message : errorMessages) {
            printWriter.println(message);
        }

        printWriter.close();
        
        return new StringBuffer().append(superMessage).append(stringWriter.toString()).toString(); 
    }
    
    public OpenLMessage[] getErrorMessages() {
        return new ArrayList<OpenLMessage>(errorMessages).toArray(new OpenLMessage[errorMessages.size()]); 
    }
}
