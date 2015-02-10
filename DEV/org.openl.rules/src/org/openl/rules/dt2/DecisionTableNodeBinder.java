/*
 * Created on Oct 3, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.dt2;

import org.openl.OpenL;
import org.openl.binding.IMemberBoundNode;
import org.openl.rules.lang.xls.binding.AExecutableNodeBinder;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.types.impl.OpenMethodHeader;

/**
 * @author snshor
 */
public class DecisionTableNodeBinder extends AExecutableNodeBinder {

    @Override
    protected IMemberBoundNode createNode(TableSyntaxNode tableSyntaxNode,
            OpenL openl,
            OpenMethodHeader header,
            XlsModuleOpenClass module) {

        return new DecisionTableBoundNode(tableSyntaxNode, openl, header, module);
    }
}
