/**
 * Created Jan 2, 2007
 */
package org.openl.rules.testmethod;

import java.util.ArrayList;
import java.util.List;

import org.openl.OpenL;
import org.openl.binding.IBindingContext;
import org.openl.binding.IMemberBoundNode;
import org.openl.binding.exception.AmbiguousMethodException;
import org.openl.rules.data.DataNodeBinder;
import org.openl.rules.data.ITable;
import org.openl.rules.lang.xls.binding.ATableBoundNode;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.syntax.impl.IdentifierNode;
import org.openl.syntax.impl.Tokenizer;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenMethodHeader;
import org.openl.types.impl.MethodsHelper;

/**
 * @author snshor
 */
public class TestMethodNodeBinder extends DataNodeBinder {

    private static final String FORMAT_ERROR_MESSAGE = "Testmethod table format: Testmethod <methodname> <testname>";

    @Override
    protected String getFormatErrorMessage() {
        return FORMAT_ERROR_MESSAGE;
    }

    @Override
    protected ATableBoundNode makeNode(TableSyntaxNode tableSyntaxNode, XlsModuleOpenClass module) {
        return new TestMethodBoundNode(tableSyntaxNode, module);
    }

    @Override
    public IMemberBoundNode preBind(TableSyntaxNode tableSyntaxNode,
            OpenL openl,
            IBindingContext bindingContext,
            XlsModuleOpenClass module) throws Exception {
        if (bindingContext.isExecutionMode()) {
            return null;// skipped in execution mode
        }

        ILogicalTable table = tableSyntaxNode.getTable();

        IOpenSourceCodeModule source = new GridCellSourceCodeModule(table.getSource(), bindingContext);

        IdentifierNode[] parsedHeader = Tokenizer.tokenize(source, " \n\r");
        checkParsedHeader(parsedHeader, source);

        String typeName = parsedHeader[TYPE_INDEX].getIdentifier();
        String tableName = parsedHeader[TABLE_NAME_INDEX].getIdentifier();

        IOpenMethod[] testedMethods = MethodsHelper.getMethods(typeName, module.getMethods());
        IOpenMethodHeader header = TestMethodHelper.makeHeader(tableName, module);

        int i = 0;
        TestMethodBoundNode bestCaseTestMethodBoundNode = null;
        IOpenMethod bestCaseOpenMethod = null;
        SyntaxNodeException[] bestCaseErrors = null;
        boolean hasNoErrorBinding = false;
        for (IOpenMethod testedMethod : testedMethods) {
            tableSyntaxNode.crearErrors();
            TestMethodBoundNode testMethodBoundNode = (TestMethodBoundNode) makeNode(tableSyntaxNode, module);
            TestSuiteMethod testSuite = new TestSuiteMethod(testedMethod, header, testMethodBoundNode);
            testMethodBoundNode.setTestSuite(testSuite);
            TestMethodOpenClass testMethodOpenClass = new TestMethodOpenClass(tableName, testedMethod);

            // Check that table type loaded properly.
            //
            if (testMethodOpenClass.getInstanceClass() == null) {
                String message = String.format("Type '%s' was defined with errors", typeName);
                throw SyntaxNodeExceptionUtils.createError(message, null, parsedHeader[TYPE_INDEX]);
            }
            try {
                ITable dataTable = makeTable(module,
                    tableSyntaxNode,
                    tableName,
                    testMethodOpenClass,
                    bindingContext,
                    openl);
                testMethodBoundNode.setTable(dataTable);
                if (testMethodBoundNode.getTableSyntaxNode().hasErrors() && (bestCaseErrors == null || bestCaseErrors.length > testMethodBoundNode.getTableSyntaxNode()
                    .getErrors().length)) {
                    bestCaseErrors = testMethodBoundNode.getTableSyntaxNode().getErrors();
                    bestCaseTestMethodBoundNode = testMethodBoundNode;
                    bestCaseOpenMethod = testedMethod;
                } else {
                    if (!testMethodBoundNode.getTableSyntaxNode().hasErrors()){
                        if (!hasNoErrorBinding) {
                            bestCaseTestMethodBoundNode = testMethodBoundNode;
                            bestCaseOpenMethod = testedMethod;
                            hasNoErrorBinding = true;
                        } else {
                            List<IOpenMethod> list = new ArrayList<IOpenMethod>();
                            list.add(testedMethod);
                            list.add(bestCaseOpenMethod);
                            throw new AmbiguousMethodException(tableName, IOpenClass.EMPTY, list);
                        }
                    }
                    bestCaseErrors = new SyntaxNodeException[0];
                }
            } catch (AmbiguousMethodException e) {
                throw e;
            } catch (Exception e) {
                if (i < testedMethods.length - 1) {
                    continue;
                }
                throw e;
            }
        }

        if (bestCaseTestMethodBoundNode != null) {
            tableSyntaxNode.crearErrors();
            for (SyntaxNodeException error : bestCaseErrors) {
                bestCaseTestMethodBoundNode.getTableSyntaxNode().addError(error);
            }
            return bestCaseTestMethodBoundNode;
        }

        String message = String.format("Type not found: '%s'", typeName);
        throw SyntaxNodeExceptionUtils.createError(message, null, parsedHeader[TYPE_INDEX]);
    }
}
