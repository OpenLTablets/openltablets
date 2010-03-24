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
        char first = fieldName.charAt(0);

        if (Character.isLetter(first) && Character.isUpperCase(first)) {

            IOpenClass type = bindingContext.findType(ISyntaxConstants.THIS_NAMESPACE, fieldName);

            if (type != null) {
                return new TypeBoundNode(node, type);
            }

            IOpenField om = bindingContext.findVar(ISyntaxConstants.THIS_NAMESPACE, fieldName, strictMatch);

            if (om != null) {
                return new FieldBoundNode(node, om);
            }
        } else {
            IOpenField field = bindingContext.findVar(ISyntaxConstants.THIS_NAMESPACE, fieldName, strictMatch);

            if (field != null) {
                return new FieldBoundNode(node, field);
            }

            IOpenClass type = bindingContext.findType(ISyntaxConstants.THIS_NAMESPACE, fieldName);

            if (type != null) {
                return new TypeBoundNode(node, type);
            }
        }

        String message = String.format("Field not found: '%s'", fieldName);
        BindHelper.processError(message, node, bindingContext);

        return new ErrorBoundNode(node);
        //        throw new BoundError("Field not found: " + fieldName, null, node);
    }

    @Override
    public IBoundNode bindTarget(ISyntaxNode node, IBindingContext bindingContext, IBoundNode target) {

        try {
            String fieldName = ((IdentifierNode) node).getIdentifier();
            IOpenField field = bindingContext.findFieldFor(target.getType(), fieldName, false);

            if (field == null) {
                String message = String.format("Field not found: '%s'", fieldName);
                BindHelper.processError(message, node, bindingContext);

                return new ErrorBoundNode(node);
                //                throw new FieldNotFoundException("Identifier: ", fieldName, target.getType());
            }

            if (target.isStaticTarget() != field.isStatic()) {

                if (field.isStatic()) {
                    BindHelper.processWarn("Access of a static field from non-static object", node);
                } else {
                    BindHelper.processError("Access non-static field from a static object", node, bindingContext);

                    return new ErrorBoundNode(node);
                }
            }

            return new FieldBoundNode(node, field, target);

        } catch (Throwable t) {
            BindHelper.processError(node, t, bindingContext);

            return new ErrorBoundNode(node);
        }
    }

    private boolean isStrictMatch(ISyntaxNode node) {
        return !node.getType().contains(".nostrict");
    }

}
