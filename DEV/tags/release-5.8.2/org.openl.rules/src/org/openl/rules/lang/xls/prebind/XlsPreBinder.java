package org.openl.rules.lang.xls.prebind;

import java.util.Set;

import org.openl.CompiledOpenClass;
import org.openl.OpenL;
import org.openl.binding.IMemberBoundNode;
import org.openl.conf.IUserContext;
import org.openl.rules.binding.RulesModuleBindingContext;
import org.openl.rules.datatype.binding.AliasDatatypeBoundNode;
import org.openl.rules.datatype.binding.DatatypeTableBoundNode;
import org.openl.rules.lang.xls.XlsBinder;
import org.openl.rules.lang.xls.XlsHelper;
import org.openl.rules.lang.xls.binding.XlsMetaInfo;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.lang.xls.syntax.XlsModuleSyntaxNode;
import org.openl.rules.property.PropertyTableBoundNode;
import org.openl.syntax.exception.CompositeSyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;

/**
 * Serves to prebind code. Prebind means to process all datatypes, properties
 * and headers for another table types.
 * 
 * @author pudalau
 */
public class XlsPreBinder extends XlsBinder {
    private IPrebindHandler prebindHandler;

    public XlsPreBinder(IUserContext userContext, IPrebindHandler prebindHandler) {
        super(userContext);
        this.prebindHandler = prebindHandler;
    }

    protected void finilizeBind(IMemberBoundNode memberBoundNode,
            TableSyntaxNode tableSyntaxNode,
            RulesModuleBindingContext moduleContext) {
        if (memberBoundNode instanceof DatatypeTableBoundNode || memberBoundNode instanceof AliasDatatypeBoundNode || memberBoundNode instanceof PropertyTableBoundNode) {
            try {
                memberBoundNode.finalizeBind(moduleContext);

            } catch (SyntaxNodeException error) {
                processError(error, tableSyntaxNode, moduleContext);

            } catch (CompositeSyntaxNodeException ex) {

                for (SyntaxNodeException error : ex.getErrors()) {
                    processError(error, tableSyntaxNode, moduleContext);
                }

            } catch (Throwable t) {

                SyntaxNodeException error = SyntaxNodeExceptionUtils.createError(t, tableSyntaxNode);
                processError(error, tableSyntaxNode, moduleContext);
            }
        }
    }

    @Override
    protected XlsLazyModuleOpenClass createModuleOpenClass(XlsModuleSyntaxNode moduleNode,
            OpenL openl,
            Set<CompiledOpenClass> moduleDependencies) {
        return new XlsLazyModuleOpenClass(null,
            XlsHelper.getModuleName(moduleNode),
            new XlsMetaInfo(moduleNode),
            openl,
            prebindHandler,
            moduleDependencies);
    }
}
