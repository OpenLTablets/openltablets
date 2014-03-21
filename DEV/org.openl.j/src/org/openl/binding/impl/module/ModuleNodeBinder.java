/*
 * Created on Jul 24, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.binding.impl.module;

import java.util.List;

import org.openl.binding.IBindingContext;
import org.openl.binding.IBoundNode;
import org.openl.binding.IMemberBoundNode;
import org.openl.binding.impl.ANodeBinder;
import org.openl.binding.impl.BindHelper;
import org.openl.syntax.ISyntaxNode;
import org.openl.syntax.exception.CompositeSyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeException;

/**
 * @author snshor
 *
 */
public class ModuleNodeBinder extends ANodeBinder {

    public IBoundNode bind(ISyntaxNode node, IBindingContext bindingContext) throws Exception {

        // children should all have type IMemberBoundNode
        IBoundNode[] children = bindChildren(node, bindingContext);
        // TODO fix schema, name
        ModuleOpenClass module = new ModuleOpenClass(null, "UndefinedType", bindingContext.getOpenL());
        processErrors(module.getErrors(), node, bindingContext);
        ModuleBindingContext moduleContext = new ModuleBindingContext(bindingContext, module);

        for (IBoundNode child : children) {
            ((IMemberBoundNode) child).addTo(moduleContext.getModule());
        }

        for (IBoundNode child : children) {
            ((IMemberBoundNode) child).finalizeBind(moduleContext);
        }

        return new ModuleNode(node, moduleContext.getModule());
    }

    private void processErrors(List<Throwable> errors, ISyntaxNode node, IBindingContext bindingContext) {
        if (errors != null) {
            for (Throwable error : errors) {
                if (error instanceof SyntaxNodeException) {
                    BindHelper.processError((SyntaxNodeException) error, bindingContext);
                } else if (error instanceof CompositeSyntaxNodeException) {
                    BindHelper.processError((CompositeSyntaxNodeException) error, bindingContext);
                } else {
                    BindHelper.processError(error, node, bindingContext);
                }
            }
        }
    }

}
