/*
 * Created on Oct 7, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.dt;

import org.openl.OpenL;
import org.openl.binding.BindingDependencies;
import org.openl.binding.IBindingContext;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.rules.lang.xls.binding.AMethodBasedNode;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.method.ExecutableRulesMethod;
import org.openl.types.IOpenMethodHeader;

/**
 * @author snshor
 * 
 */
public class DecisionTableBoundNode extends AMethodBasedNode {

    public DecisionTableBoundNode(TableSyntaxNode tableSyntaxNode,
            OpenL openl,
            IOpenMethodHeader header,
            ModuleOpenClass module) {
        
        super(tableSyntaxNode, openl, header, module);
    }

    @Override
    protected ExecutableRulesMethod createMethodShell() {
        return new DecisionTable(getHeader(), this);
    }

    public void finalizeBind(IBindingContext bindingContext) throws Exception {
        new DecisionTableLoader().loadAndBind(getTableSyntaxNode(), getDecisionTable(), getOpenl(), getModule(), (IBindingContextDelegator) bindingContext);
    }

    public final DecisionTable getDecisionTable() {
        return (DecisionTable) getMethod();
    }

    @Override
    public void updateDependency(BindingDependencies dependencies) {
        getDecisionTable().updateDependency(dependencies);
    }
    
    @Override
    public void removeDebugInformation(IBindingContext cxt) throws Exception {
        super.removeDebugInformation(cxt);
        getDecisionTable().getAlgorithm().removeParamValuesForIndexedConditions();
    }

}
