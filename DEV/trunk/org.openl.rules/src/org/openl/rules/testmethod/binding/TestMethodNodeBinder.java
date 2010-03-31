/**
 * Created Jan 2, 2007
 */
package org.openl.rules.testmethod.binding;

import org.openl.binding.IBindingContext;
import org.openl.rules.data.DataNodeBinder;
import org.openl.rules.data.DataTableBoundNode;
import org.openl.rules.lang.xls.binding.ATableBoundNode;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.testmethod.TestMethodHelper;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.impl.AOpenClass;

/**
 * @author snshor
 * 
 */
public class TestMethodNodeBinder extends DataNodeBinder {

    private static final String FORMAT_ERROR_MESSAGE = "Testmethod table format: Testmethod <methodname> <testname>";

    @Override
    protected String getFormatErrorMessage() {
        return FORMAT_ERROR_MESSAGE;
    }

    @Override
    protected ATableBoundNode makeNode(TableSyntaxNode tsn, XlsModuleOpenClass module) {
        return new TestMethodBoundNode(tsn, module);
    }

    @Override
    protected synchronized IOpenClass getTableType(String typeName,
            IBindingContext bindingContext,
            XlsModuleOpenClass module,
            DataTableBoundNode dataNode,
            String tableName) {

        TestMethodHelper tmNode = ((TestMethodBoundNode) dataNode).getTmhelper();

        if (tmNode == null) {
            IOpenMethod m = AOpenClass.getSingleMethod(typeName, module.methods());

            tmNode = new TestMethodHelper(m, tableName);

            ((TestMethodBoundNode) dataNode).setTmhelper(tmNode);
        }

        return tmNode.getMethodBasedClass();
    }

}
