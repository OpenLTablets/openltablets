/*
 * Created on Sep 11, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.dt.element;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openl.OpenL;
import org.openl.binding.IBindingContext;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.engine.OpenLManager;
import org.openl.rules.OpenlToolAdaptor;
import org.openl.rules.binding.RuleRowHelper;
import org.openl.rules.dt.IDecisionTableConstants;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.LogicalTableHelper;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.syntax.exception.CompositeSyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.syntax.impl.IdentifierNode;
import org.openl.syntax.impl.Tokenizer;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenMethodHeader;
import org.openl.types.IParameterDeclaration;
import org.openl.types.impl.CompositeMethod;
import org.openl.types.impl.MethodSignature;
import org.openl.types.impl.OpenMethodHeader;
import org.openl.types.impl.ParameterDeclaration;
import org.openl.types.java.JavaOpenClass;
import org.openl.util.Log;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 * 
 */
public abstract class FunctionalRow implements IDecisionRow {

    private static final String NO_PARAM = "___NO_PARAM___";

    private String name;
    private int row;

    private CompositeMethod method;

    private IParameterDeclaration[] params;
    private Object[][] paramValues;
    private Boolean hasNoParams = null;
    
    
    private ILogicalTable decisionTable;
    private ILogicalTable paramsTable;
    private ILogicalTable codeTable;
    private ILogicalTable presentationTable;

    private int noParamsIndex = 0;
    
    
    public FunctionalRow(String name, int row, ILogicalTable decisionTable) {

        this.name = name;
        this.row = row;
        this.decisionTable = decisionTable;

        this.paramsTable = decisionTable.getLogicalRegion(IDecisionTableConstants.PARAM_COLUMN_INDEX, row, 1, 1);
        this.codeTable = decisionTable.getLogicalRegion(IDecisionTableConstants.CODE_COLUMN_INDEX, row, 1, 1);
        this.presentationTable = decisionTable.getLogicalRegion(IDecisionTableConstants.PRESENTATION_COLUMN_INDEX, row, 1, 1);
    }

    public String getName() {
        return name;
    }

    public IOpenMethod getMethod() {
        return method;
    }

    public IParameterDeclaration[] getParams() {
        return params;
    }

    public Object[][] getParamValues() {
        return paramValues;
    }

    public DecisionTableParameterInfo getParameterInfo(int i) {
        return new DecisionTableParameterInfo(i, this);
    }

    public IOpenSourceCodeModule getSourceCodeModule() {

        if (method != null) {
            return method.getMethodBodyBoundNode().getSyntaxNode().getModule();
        }

        return null;
    }

    public int numberOfParams() {
        return params.length;
    }
    
    /**
     * Whole representation of decision table.
     * Horizontal representation of the table where conditions are listed from top to bottom. And must be 
     * readed from left to right</br> 
     * Example:
     * 
     * <table cellspacing="2">
     * <tr>
     * <td align="center" bgcolor="#ccffff"><b>Rule</b></td>
     * <td align="center" bgcolor="#ccffff"></td>
     * <td align="center" bgcolor="#ccffff"></td>
     * <td align="center" bgcolor="#8FCB52">Rule</td>
     * <td align="center" bgcolor="#8FCB52">Rule1</td>
     * <td align="center" bgcolor="#8FCB52">Rule2</td>
     * <td align="center" bgcolor="#8FCB52">Rule3</td>
     * 
     * </tr>
     * <tr>
     * <td align="center" bgcolor="#ccffff"><b>C1</b></td>
     * <td align="center" bgcolor="#ccffff">paramLocal1==paramInc</td>
     * <td align="center" bgcolor="#ccffff">String paramLocal1</td>
     * <td align="center" bgcolor="#ffff99">Local Param 1</td>
     * <td align="center" bgcolor="#ffff99">value11</td>
     * <td align="center" bgcolor="#ffff99">value12</td>
     * <td align="center" bgcolor="#ffff99">value13</td>
     * </tr>
     * 
     * <tr>
     * <td align="center" bgcolor="#ccffff"><b>C2</b></td>
     * <td align="center" bgcolor="#ccffff">paramLocal2==paramInc</td>
     * <td align="center" bgcolor="#ccffff">String paramLocal2</td>
     * <td align="center" bgcolor="#ffff99">Local Param 2</td>
     * <td align="center" bgcolor="#ffff99">value21</td>
     * <td align="center" bgcolor="#ffff99">value22</td>
     * <td align="center" bgcolor="#ffff99">value23</td>
     * </tr>
     * </table>
     * 
     * @param dataTableBody
     * @param tableType
     * @return <code>TRUE</code> if table is horizontal.
     */
    public ILogicalTable getDecisionTable() {
        return decisionTable;
    }

    public String[] getParamPresentation() {

        int length = paramsTable.getLogicalHeight();

        String[] result = new String[length];
        int fromHeight = 0;

        for (int i = 0; i < result.length; i++) {

            int gridHeight = paramsTable.getLogicalRow(i).getGridTable().getGridHeight();

            IGridTable singleParamGridTable = (IGridTable) presentationTable.getGridTable().rows(fromHeight,
                fromHeight + gridHeight - 1);
            result[i] = singleParamGridTable.getCell(0, 0).getStringValue();

            fromHeight += gridHeight;
        }

        return result;
    }

    public void prepare(IOpenClass methodType,
            IMethodSignature signature,
            OpenL openl,
            ModuleOpenClass module,
            IBindingContextDelegator bindingContextDelegator,
            RuleRow ruleRow) throws Exception {

        method = generateMethod(signature, openl, bindingContextDelegator, module, methodType);

        OpenlToolAdaptor openlAdaptor = new OpenlToolAdaptor(openl, bindingContextDelegator);

        IOpenMethodHeader header = new OpenMethodHeader(name, null, signature, module);
        openlAdaptor.setHeader(header);

        paramValues = prepareParamValues(openlAdaptor, ruleRow);
    }

    private IParameterDeclaration[] getParams(IBindingContext bindingContext) throws Exception {

        if (params == null) {

            Set<String> paramNames = new HashSet<String>();
            int length = paramsTable.getLogicalHeight();

            params = new IParameterDeclaration[length];

            for (int i = 0; i < length; i++) {

                ILogicalTable paramTable = paramsTable.getLogicalRow(i);
                IOpenSourceCodeModule source = new GridCellSourceCodeModule(paramTable.getGridTable());
                
                IdentifierNode[] nodes = Tokenizer.tokenize(source, " \n\r");
                
                if (nodes.length == 0) {
                    // no parameters
                    params[i] = makeNoParamParameterDeclaration();
                    continue;
                }

                String errMsg;

                if (nodes.length != 2) {
                    errMsg = "Parameter Cell format: <type> <name>";
                    throw SyntaxNodeExceptionUtils.createError(errMsg, null, null, source);
                }

                String typeCode = nodes[0].getIdentifier();
                IOpenClass type = RuleRowHelper.getType(typeCode, bindingContext);
                
                if (type == null) {
                    throw SyntaxNodeExceptionUtils.createError("Type not found: " + typeCode, nodes[0]);
                }

                String name = nodes[1].getIdentifier();
                
                if (paramNames.contains(name)) {
                    throw SyntaxNodeExceptionUtils.createError("Duplicated parameter name: " + name, nodes[1]);
                }

                paramNames.add(name);

                params[i] = new ParameterDeclaration(type, name);
            }
        }

        return params;
    }

    private Object loadParam(ILogicalTable dataTable,
            IOpenClass paramType,
            String paramName,
            String ruleName,
            OpenlToolAdaptor openlAdaptor) throws SyntaxNodeException {

        boolean indexed = paramType.getAggregateInfo().isAggregate(paramType);

        if (!indexed) {
            return RuleRowHelper.loadSingleParam(paramType, paramName, ruleName, dataTable, openlAdaptor);
        }

        IOpenClass indexedParamType = paramType.getAggregateInfo().getComponentType(paramType);
        dataTable = LogicalTableHelper.make1ColumnTable(dataTable);

        int height = RuleRowHelper.calculateHeight(dataTable);

        if (height == 0) {
            return null;
        }

        if (height == 1 && !RuleRowHelper.isCommaSeparatedArray(dataTable)) {
            // attempt to load as a single paramType(will work in case of
            // expressions)
            try {
                return RuleRowHelper.loadSingleParam(paramType, paramName, ruleName, dataTable, openlAdaptor);
            } catch (Exception e) {

                Log.debug(e);
                // do nothing, assume the type was wrong or this was not an
                // expression
                // let the regular flow of events take it's course
            }
        }

        return loadArrayParameters(dataTable, paramName, ruleName, openlAdaptor, indexedParamType);
    }

    private Object loadArrayParameters(ILogicalTable dataTable,
            String paramName,
            String ruleName,
            OpenlToolAdaptor openlAdaptor,
            IOpenClass paramType) throws SyntaxNodeException {
        
        int height = RuleRowHelper.calculateHeight(dataTable);        
        
        if (height == 1 && RuleRowHelper.isCommaSeparatedArray(dataTable)) { // load comma separated array
           return loadCommaSeparatedArrayParams(dataTable, paramName, ruleName, openlAdaptor, paramType);
        } else {
            return loadSimpleArrayParams(dataTable, paramName, ruleName, openlAdaptor, paramType);
        }        
    }
    
    private Object loadCommaSeparatedArrayParams(ILogicalTable dataTable,
            String paramName,
            String ruleName,
            OpenlToolAdaptor openlAdaptor,
            IOpenClass paramType) throws SyntaxNodeException {
        
        List<CompositeMethod> methodsList = null;        
        Object ary = null;
        ILogicalTable paramSource = dataTable.getLogicalRow(0);
        Object params = RuleRowHelper.loadCommaSeparatedParam(paramType, paramName, ruleName, paramSource, openlAdaptor);
        if (params.getClass().isArray()) {                
            Object[] paramsArray = ((Object[])params);
            int paramsLength = paramsArray.length;
            ary = paramType.getAggregateInfo().makeIndexedAggregate(paramType, new int[] { paramsLength });
            for (int i = 0; i < paramsLength; i++) {
                if (paramsArray[i] instanceof CompositeMethod) {
                    methodsList = new ArrayList<CompositeMethod>(addMethod(methodsList, (CompositeMethod)paramsArray[i]));
                } else {
                    Array.set(ary, i, paramsArray[i]);
                }
            }                
        }
        return methodsList == null ? ary : new ArrayHolder(ary, 
            methodsList.toArray(new CompositeMethod[methodsList.size()]));
    }
    
    private Object loadSimpleArrayParams(ILogicalTable dataTable,
            String paramName,
            String ruleName,
            OpenlToolAdaptor openlAdaptor,
            IOpenClass paramType) throws SyntaxNodeException {
        
        int height = RuleRowHelper.calculateHeight(dataTable);
                
        List<CompositeMethod> methodsList = null;
        Object ary = paramType.getAggregateInfo().makeIndexedAggregate(paramType, new int[] { height });
        
        for (int i = 0; i < height; i++) { // load array values represented as number of cells 
            
            ILogicalTable cell = dataTable.getLogicalRow(i);
            Object parameter = RuleRowHelper.loadSingleParam(paramType, paramName, ruleName, cell, openlAdaptor);
            
            if (parameter instanceof CompositeMethod) {
                methodsList = new ArrayList<CompositeMethod>(addMethod(methodsList, (CompositeMethod)parameter));
            } else {
                Array.set(ary, i, parameter);
            }
        }
        return methodsList == null ? ary : new ArrayHolder(ary, 
            methodsList.toArray(new CompositeMethod[methodsList.size()]));
        
    }
    
    private List<CompositeMethod> addMethod(List<CompositeMethod> methods, CompositeMethod method) {
        if (methods == null) {            
            methods = new ArrayList<CompositeMethod>();
        }        
        methods.add(method);    
        
        return methods;
    }

    private Object[][] prepareParamValues(OpenlToolAdaptor ota, RuleRow ruleRow) throws Exception {
        
        int len = nValues();
        Object[][] values = new Object[len][];

        IParameterDeclaration[] paramDecl = getParams(ota.getBindingContext());

        ArrayList<SyntaxNodeException> errors = new ArrayList<SyntaxNodeException>();

        for (int col = 0; col < len; col++) {
            ILogicalTable valueCell = getValueCell(col);
            IGridTable paramGridColumn = valueCell.getGridTable();

            Object[] valueAry = new Object[paramDecl.length];

            int fromHeight = 0;
            boolean notEmpty = false;
            String ruleName = ruleRow == null ? "R" + (col + 1) : ruleRow.getRuleName(col);

            for (int j = 0; j < paramDecl.length; j++) {
                if (paramDecl[j] == null) {
                    continue;
                }

                int gridHeight = paramsTable.getLogicalRow(j).getGridTable().getGridHeight();
                IGridTable singleParamGridTable = (IGridTable) paramGridColumn.rows(fromHeight,
                    fromHeight + gridHeight - 1);

                Object v = null;
                try {
                    v = loadParam(LogicalTableHelper.logicalTable(singleParamGridTable),
                        paramDecl[j].getType(),
                        paramDecl[j].getName(),
                        ruleName,
                        ota);
                } catch (SyntaxNodeException error) {
                    errors.add(error);
                }

                if (v != null) {
                    notEmpty = true;
                }
                valueAry[j] = v;

                fromHeight += gridHeight;
            }

            if (notEmpty) {
                values[col] = valueAry;
            }
        }

        if (errors.size() > 0) {
            throw new CompositeSyntaxNodeException("Error:", errors.toArray(new SyntaxNodeException[0]));
        }

        return values;
    }

    protected boolean hasNoParams() {

        if (hasNoParams == null) {
            hasNoParams = params[0].getName().startsWith(NO_PARAM);
        }

        return hasNoParams.booleanValue();
    }

    protected Object[] mergeParams(Object target, Object[] dtParams, IRuntimeEnv env, Object[] params) {

        Object[] newParams = new Object[dtParams.length + params.length];

        System.arraycopy(dtParams, 0, newParams, 0, dtParams.length);
        RuleRowHelper.loadParams(newParams, dtParams.length, params, target, dtParams, env);

        return newParams;
    }

    private ILogicalTable getValueCell(int column) {
        return decisionTable.getLogicalRegion(column + IDecisionTableConstants.SERVICE_COLUMNS_NUMBER, row, 1, 1);
    }

    private int nValues() {
        return decisionTable.getLogicalWidth() - IDecisionTableConstants.SERVICE_COLUMNS_NUMBER;
    }

    private IParameterDeclaration makeNoParamParameterDeclaration() {
        return new ParameterDeclaration(JavaOpenClass.STRING, NO_PARAM + noParamsIndex++);
    }

    private CompositeMethod generateMethod(IMethodSignature signature,
            OpenL openl,
            IBindingContextDelegator bindingContextDelegator,
            IOpenClass declaringClass,
            IOpenClass methodType) throws Exception {

        IOpenSourceCodeModule source = new GridCellSourceCodeModule(codeTable.getGridTable());

        IParameterDeclaration[] methodParams = getParams(bindingContextDelegator);
        IMethodSignature newSignature = hasNoParams() ? signature : ((MethodSignature) signature).merge(methodParams);
        OpenMethodHeader methodHeader = new OpenMethodHeader(null, methodType, newSignature, declaringClass);

        return OpenLManager.makeMethod(openl, source, methodHeader, bindingContextDelegator);
    }

}
