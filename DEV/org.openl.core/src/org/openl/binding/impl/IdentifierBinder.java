/*
 * Created on May 19, 2003 Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl;

import org.openl.binding.IBindingContext;
import org.openl.binding.IBoundNode;
import org.openl.syntax.ISyntaxNode;
import org.openl.syntax.impl.ISyntaxConstants;
import org.openl.syntax.impl.IdentifierNode;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;

/**
 * @author snshor
 */
public class IdentifierBinder extends ANodeBinder {

    public IBoundNode bind(ISyntaxNode node, IBindingContext bindingContext) throws Exception {

        boolean strictMatch = isStrictMatch(node);
        String fieldName = ((IdentifierNode) node).getIdentifier();

        // According to "6.4.2. Obscuring" of Java Language Specification:
        // A simple name may occur in contexts where it may potentially be interpreted as the name of a variable,
        // a type, or a package. In these situations, the rules of §6.5 specify that a variable will be chosen
        // in preference to a type, and that a type will be chosen in preference to a package.
        // See http://docs.oracle.com/javase/specs/jls/se8/html/jls-6.html#jls-6.4.2 for details
        // Implementation below tries to follow that specification.

        IOpenField field = bindingContext.findVar(ISyntaxConstants.THIS_NAMESPACE, fieldName, strictMatch);

        if (field != null) {
            return new FieldBoundNode(node, field);
        }

        IOpenClass type = bindingContext.findType(ISyntaxConstants.THIS_NAMESPACE, fieldName);

        if (type != null) {
            return new TypeBoundNode(node, type);
        }

        String message = String.format("Field not found: '%s'", fieldName);
        BindHelper.processError(message, node, bindingContext, false);

        return new ErrorBoundNode(node);
    }

    @Override
    public IBoundNode bindTarget(ISyntaxNode node, IBindingContext bindingContext, IBoundNode target) {

        try {
            String fieldName = ((IdentifierNode) node).getIdentifier();

            return BindHelper.bindAsField(fieldName, node, bindingContext, target);
        } catch (Throwable t) {
            BindHelper.processError(node, t, bindingContext);

            return new ErrorBoundNode(node);
        }
    }

    private boolean isStrictMatch(ISyntaxNode node) {
        return !node.getType().contains(".nostrict");
    }

}
