/*
 * Created on Sep 11, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.dt;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.openl.OpenL;
import org.openl.binding.IBindingContext;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.domain.IDomain;
import org.openl.engine.OpenLManager;
import org.openl.meta.IMetaHolder;
import org.openl.meta.ValueMetaInfo;
import org.openl.rules.OpenlToolAdaptor;
import org.openl.rules.convertor.IString2DataConvertor;
import org.openl.rules.convertor.String2DataConvertorFactory;
import org.openl.rules.lang.xls.types.CellMetaInfo;
import org.openl.rules.table.ALogicalTable;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.IWritableGrid;
import org.openl.rules.table.LogicalTable;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.source.impl.SubTextSourceCodeModule;
import org.openl.syntax.exception.CompositeSyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.syntax.impl.ISyntaxConstants;
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
import org.openl.util.StringTool;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 *
 */
public abstract class FunctionalRow implements IDecisionRow {

    public static final String ARRAY_ELEMENTS_SEPARATOR_ESCAPER = "\\";

    public static final String ARRAY_ELEMENTS_SEPARATOR = ",";

//    public final static Object EMPTY_CELL = new Object();

    private static String NO_PARAM = "___NO_PARAM___";
    
    public static String CONSTRUCTOR = "constructor";
    
    private int row;

    private ILogicalTable decisionTable;

    private ILogicalTable paramsTable;

    private ILogicalTable codeTable;

    private ILogicalTable presentationTable;

    private String name;

    private IParameterDeclaration[] params;

    private int np_idx = 0;

    private Boolean hasNoParams = null;

    private CompositeMethod method;

    private Object[][] paramValues;    
    
    public static IOpenClass getType(String typeCode, IBindingContext cxt) throws Exception {
        if (typeCode.endsWith("[]")) {
            String baseCode = typeCode.substring(0, typeCode.length() - 2);
            // IOpenClass baseType = OpenlTool.getType(baseCode, openl);
            IOpenClass baseType = cxt.findType(ISyntaxConstants.THIS_NAMESPACE, baseCode);
            if (baseType == null) {
                return null;
            }
            return baseType.getAggregateInfo().getIndexedAggregateType(baseType, 1);
        }

        IOpenClass type = cxt.findType(ISyntaxConstants.THIS_NAMESPACE, typeCode);
        ;
        // if (type == null)
        // throw new Exception("Type " + typeCode + " not found");

        return type;
    }

    public static void loadParams(Object[] ary, int from, Object[] params, Object target, Object[] dtparams,
            IRuntimeEnv env) {
        for (int i = 0; i < params.length; i++) {
            Object x = params[i];

            if (x instanceof IOpenMethod) {
                x = ((IOpenMethod) x).invoke(target, dtparams, env);
            }
            else if (x instanceof ArrayHolder) {
                x = ((ArrayHolder) x).invoke(target, dtparams, env);
            }
//            else if (x == EMPTY_CELL)
//                x = null;

            ary[i + from] = x;
        }

    }

    public static Object loadSingleParam(IOpenClass paramType, String paramName, String ruleName, ILogicalTable cell,
            OpenlToolAdaptor ota) throws SyntaxNodeException {
        String src = cell.getGridTable().getCell(0, 0).getStringValue();
        Object value = cell.getGridTable().getCell(0, 0).getObjectValue();
         
        return loadSingleParamInternal(paramType, paramName, ruleName, cell, ota, src, value, false);
    }
    
//    private static void applyFormat(ILogicalTable cell, Object target){
//        if (target instanceof DoubleValue){
//            String originalFormat = cell.getGridTable().getCell(0, 0).getStyle().getTextFormat();
//        //Java does not support some Excel formats
//        String transformed = XlsNumberFormatter.transformToJavaFormat(originalFormat, null);
//
//            ((DoubleValue)target).setFormat(transformed);
//        }
//    }
    
    private static Object loadSingleParamInternal(IOpenClass paramType, String paramName, String ruleName, ILogicalTable cell,
            OpenlToolAdaptor ota, String src, Object value, boolean isPartOfArray) throws SyntaxNodeException {
        // TODO: parse values considering underlying excel format. Note: this
        // class doesn't know anything about Excel. Keep it storage format
        // agnostic (don't introduce excel dependencies). Also consider adding
        // meta info.
        if (src != null && (src = src.trim()).length() != 0) {
            if (ota != null && ota.getHeader() != null) {
                IOpenMethodHeader old_header = ota.getHeader();
                OpenMethodHeader newHeader = new OpenMethodHeader(old_header.getName(), paramType, old_header
                        .getSignature(), old_header.getDeclaringClass());
                ota.setHeader(newHeader);
    
                if (src.startsWith("{") && src.endsWith("}")) {
                    GridCellSourceCodeModule srcCode = new GridCellSourceCodeModule(cell.getGridTable());
                    return ota.makeMethod(srcCode);
                }
    
                if (src.startsWith("=")
                        && (src.length() > 2 || src.length() == 2 && Character.isLetterOrDigit(src.charAt(1)))) {
                    IOpenSourceCodeModule srcCode = new SubTextSourceCodeModule(new GridCellSourceCodeModule(cell
                            .getGridTable()), 1);
    
                    return ota.makeMethod(srcCode);
                }
            }
            
            Class<?> expectedType = paramType.getInstanceClass();

            IString2DataConvertor conv = String2DataConvertorFactory.getConvertor(expectedType);
    
            try {
                Object res;
                
                // FIXME: It's absolute crunch! Revise parsing mechanism for cell values.
                if (value != null && expectedType.isAssignableFrom(value.getClass())){
                    // We've already parsed the expected value
                    res = value;
                    
                    // FIXME: just for the case trying to parse it with
                    // previously used approach. If it goes OK, then consider it
                    // results to be proper. The parsing mechanism must be
                    // rewritten.
                    try {
                        res = conv.parse(src, null, ota.getBindingContext());
                    } catch (Throwable t) {
                        // ignore error
                    }
                } else {
                    res = conv.parse(src, null, ota.getBindingContext());
                }
                
//                applyFormat(cell, res);                
                
                if (res instanceof IMetaHolder) {
                    setMetaInfo((IMetaHolder) res, cell, paramName, ruleName);
                }
                
                boolean multiValue = false;
                if (isPartOfArray) {                    
                    multiValue = true;
                }  
                setCellMetaInfo(cell, paramName, paramType, multiValue);
                validateValue(res, paramType);
                return res;
            } catch (Throwable t) {
                throw SyntaxNodeExceptionUtils.createError(null, t,  null, new GridCellSourceCodeModule(cell.getGridTable()));
            }
        } else {
            // Set meta info for empty cells. To suggest an appropriate editor according to cell type.
            setCellMetaInfo(cell, paramName, paramType, false);
        }
        return null;
    }

    public static Object[] mergeParams(Object target, Object[] dtParams, IRuntimeEnv env, Object[] params) {
        Object[] newParams = new Object[dtParams.length + params.length];

        System.arraycopy(dtParams, 0, newParams, 0, dtParams.length);

        loadParams(newParams, dtParams.length, params, target, dtParams, env);

        return newParams;
    }

    @Deprecated
    private static Object notNull(Object x) {
        return x == null ? "" : x;
    }

    public static void setCellMetaInfo(ILogicalTable cell, String paramName, IOpenClass paramType, boolean multivalue) {        
        CellMetaInfo meta = new CellMetaInfo(CellMetaInfo.Type.DT_DATA_CELL, paramName, paramType, multivalue);
        IWritableGrid.Tool.putCellMetaInfo(cell.getGridTable(), 0, 0, meta);
    }

    public static void setMetaInfo(IMetaHolder holder, ILogicalTable cell, String paramName, String ruleName) {
        ValueMetaInfo vmi = new ValueMetaInfo();
        vmi.setShortName(paramName);
        vmi.setFullName(ruleName == null ? paramName : ruleName + "." + paramName);
        vmi.setSourceUrl(new GridCellSourceCodeModule(cell.getGridTable()).getUri(0));
        holder.setMetaInfo(vmi);
    }

    @SuppressWarnings("unchecked")
    private static void validateValue(Object res, IOpenClass paramType) {
        IDomain domain = paramType.getDomain();
        if (domain == null || domain.selectObject(res)) {
            return;
        }

        String errorMsg = "The value " + res + " is outside of domain " + domain.toString();

        throw new RuntimeException(errorMsg);

    }

    public FunctionalRow(String name, int row, ILogicalTable decisionTable) {
        this.row = row;
        this.decisionTable = decisionTable;
        paramsTable = decisionTable.getLogicalRegion(IDecisionTableConstants.PARAM_COLUMN, row, 1, 1);
        codeTable = decisionTable.getLogicalRegion(IDecisionTableConstants.CODE_COLUMN, row, 1, 1);
        presentationTable = decisionTable.getLogicalRegion(IDecisionTableConstants.PRESENTATION_COLUMN, row, 1, 1);
        this.name = name;
    }
    
    public static int calcHeight(ILogicalTable table) {
        int h = table.getLogicalHeight();

        int last = -1;

        for (int i = 0; i < h; i++) {
            String src = table.getLogicalRow(i).getGridTable().getCell(0, 0).getStringValue();
            if (src != null && src.trim().length() != 0) {
                last = i;
            }
        }
        return  last + 1;
    }

    public IDecisionValue calculateCondition(int rule, Object target, Object[] dtParams, IRuntimeEnv env) {
        Object value = paramValues[rule];
        if (value == null) {
            return IDecisionValue.NxA_VALUE;
        }
        if (value instanceof IDecisionValue) {
            return (IDecisionValue) value;
        }

        Object[] params = hasNoParams() ? dtParams : mergeParams(target, dtParams, env, (Object[]) value);
        // (Object[]) ArrayTool.merge(dtParams, value);
        // dtParams.length == method.getSignature().getParameterTypes().length ?
        // dtParams :
        // (Object[]) ArrayTool.merge(dtParams, value);

        Boolean res = (Boolean) method.invoke(target, params, env);

        return res == null || res.booleanValue() ? IDecisionValue.TRUE_VALUE : IDecisionValue.FALSE_VALUE;

    }

    public Object executeAction(int col, Object target, Object[] dtParams, IRuntimeEnv env) {
        Object value = paramValues[col];
        if (value == null) {
            return null;
        }

        if (hasNoParams()) {
            return method.invoke(target, dtParams, env);
        } else {
            return method.invoke(target, mergeParams(target, dtParams, env, (Object[]) value),

            // (Object[]) ArrayTool.merge(dtParams, value),
                    env);
        }

        // return res.booleanValue() ? IDecisionValue.True :
        // IDecisionValue.False;

    }

    public DTParameterInfo findParameterInfo(String name) {
        for (int i = 0; i < params.length; i++) {
            if (params[i].equals(name)) {
                return getParameterInfo(i);
            }
        }
        return null;
    }

    private CompositeMethod generateMethod(IMethodSignature signature, OpenL openl, IBindingContextDelegator cxt,
            IOpenClass declaringClass, IOpenClass methodType) throws Exception {

        IOpenSourceCodeModule src = new GridCellSourceCodeModule(codeTable.getGridTable());

        IParameterDeclaration[] mparams = getParams(cxt);

        IMethodSignature newSignature = hasNoParams() ? signature : ((MethodSignature) signature).merge(mparams);

        OpenMethodHeader methodHeader = new OpenMethodHeader(null, methodType, newSignature, declaringClass);

        return OpenLManager.makeMethod(openl, src, methodHeader, cxt);

    }

    public IOpenMethod getMethod() {
        return method;
    }

    public String getName() {
        return name;
    }

    // ////////////////////////////////////////////////////////////////

    public DTParameterInfo getParameterInfo(int i) {
        return new DTParameterInfo(i, this);
    }

    public String[] getParamPresentation() {
        int len = paramsTable.getLogicalHeight();
        String[] res = new String[len];
        int fromHeight = 0;
        for (int i = 0; i < res.length; i++) {

            int gridHeight = paramsTable.getLogicalRow(i).getGridTable().getGridHeight();
            IGridTable singleParamGridTable = (IGridTable) presentationTable.getGridTable().rows(fromHeight,
                    fromHeight + gridHeight - 1);
            res[i] = singleParamGridTable.getCell(0, 0).getStringValue();

            fromHeight += gridHeight;
        }

        return res;
    }

    public IParameterDeclaration[] getParams() {
        return params;
    }

    private IParameterDeclaration[] getParams(IBindingContext cxt) throws Exception {
        if (params == null) {

            Set<String> paramNames = new HashSet<String>();
            int len = paramsTable.getLogicalHeight();
            params = new IParameterDeclaration[len];

            for (int i = 0; i < len; i++) {

                ILogicalTable paramTable = paramsTable.getLogicalRow(i);
                IOpenSourceCodeModule src = new GridCellSourceCodeModule(paramTable.getGridTable());
                IdentifierNode[] nodes = Tokenizer.tokenize(src, " \n\r");
                if (nodes.length == 0) {
                    // no parameters
                    params[i] = no_parameter();
                    continue;
                }

                String errMsg;

                if (nodes.length != 2) {
                    errMsg = "Parameter Cell format: <type> <name>";
                    throw SyntaxNodeExceptionUtils.createError(errMsg, null, null, src);
                }

                String typeCode = nodes[0].getIdentifier();
                IOpenClass ptype = getType(typeCode, cxt);
                if (ptype == null) {
                    throw SyntaxNodeExceptionUtils.createError( "Type not found: " + typeCode, nodes[0]);
                }

                String pname = nodes[1].getIdentifier();
                if (paramNames.contains(pname)) {
                    throw SyntaxNodeExceptionUtils.createError("Duplicated parameter name: " + pname, nodes[1]);
                }

                paramNames.add(pname);

                params[i] = new ParameterDeclaration(ptype, pname);

            }

        }
        return params;
    }

    public Object[][] getParamValues() {
        return paramValues;
    }

    public IOpenSourceCodeModule getSourceCodeModule() {

        return method == null ? null : method.getMethodBodyBoundNode().getSyntaxNode().getModule();
    }

    private ILogicalTable getValueCell(int col) {
        return decisionTable.getLogicalRegion(col + IDecisionTableConstants.DATA_COLUMN, row, 1, 1);
    }

    protected boolean hasNoParams() {
        if (hasNoParams == null) {
            hasNoParams = params[0].getName().startsWith(NO_PARAM) ? Boolean.TRUE : Boolean.FALSE;
        }
        return hasNoParams.booleanValue();
    }

    private Object loadParam(ILogicalTable dataTable, IOpenClass paramType, String paramName, String ruleName,
            OpenlToolAdaptor ota) throws SyntaxNodeException {
        boolean indexed = paramType.getAggregateInfo().isAggregate(paramType);


        if (!indexed) {
            return loadSingleParam(paramType, paramName, ruleName, dataTable, ota);
        }
        
        IOpenClass indexedParamType = paramType.getAggregateInfo().getComponentType(paramType); 

        dataTable = ALogicalTable.make1ColumnTable(dataTable);

        int h = calcHeight(dataTable);

        if (h == 0) {
            return null;
        }
        
        if (h == 1)
        {
            // attempt to load as a single paramType(will work in case of expressions)
            try {
                return loadSingleParam(paramType, paramName, ruleName, dataTable, ota);
            } catch (Exception e) {
                
                Log.debug(e);
                // do nothing, assume the type was wrong or this was not an expression
                // let the regular flow of events take it's course
            }
        }    
        

        CompositeMethod[] methods = null;
        Object ary = indexedParamType.getAggregateInfo().makeIndexedAggregate(indexedParamType, new int[] { h });

        for (int i = 0; i < h; i++) {
            ILogicalTable cell = dataTable.getLogicalRow(i);
            Object x = loadSingleParam(indexedParamType, paramName, ruleName, cell, ota);
            if (x instanceof CompositeMethod) {
                if (methods == null) {
                    methods = new CompositeMethod[h];
                }
                methods[i] = (CompositeMethod) x;
            } else {
                Array.set(ary, i, x);
            }
        }

        return methods == null ? ary : new ArrayHolder(ary, methods);

    }

    private IParameterDeclaration no_parameter() {
        return new ParameterDeclaration(JavaOpenClass.STRING, NO_PARAM + np_idx++);
    }

    public int numberOfParams() {
        return params.length;
    }

    private int nValues() {
        return decisionTable.getLogicalWidth() - IDecisionTableConstants.DATA_COLUMN;
    }

    public void prepare(IOpenClass methodType, IMethodSignature signature, OpenL openl, ModuleOpenClass dtModule,
            IBindingContextDelegator cxtd, RuleRow ruleRow) throws Exception {
        method = generateMethod(signature, openl, cxtd, dtModule, methodType);

        OpenlToolAdaptor ota = new OpenlToolAdaptor(openl, cxtd);

        IOpenMethodHeader h = new OpenMethodHeader(name, null, signature, dtModule);
        ota.setHeader(h);

        paramValues = prepareParamValues(ota, ruleRow);

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
                IGridTable singleParamGridTable = (IGridTable) paramGridColumn.rows(fromHeight, fromHeight + gridHeight
                        - 1);

                Object v = null;
                try {
                    v = loadParam(LogicalTable.logicalTable(singleParamGridTable), paramDecl[j].getType(), paramDecl[j]
                            .getName(), ruleName, ota);
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
    
    /**
     * Method to support loading Arrays through {@link #ARRAY_ELEMENTS_SEPARATOR} in one cell.
     * Gets the cell string value. Split it by {@link #ARRAY_ELEMENTS_SEPARATOR}, and process every token as single 
     * parameter. Returns array of parameters.
     * @param paramType
     * @param paramName
     * @param ruleName
     * @param cell
     * @param ota
     * @return Array of parameters.
     * @throws BoundError
     */
    public static Object loadCommaSeparatedParam(IOpenClass paramType, String paramName, String ruleName, ILogicalTable cell,
            OpenlToolAdaptor ota) throws SyntaxNodeException {
        Object arrayValues = null;
        String[] tokens = null;
        tokens = extractElementsFromCommaSeparatedArray(cell);
        if (tokens != null) {
            ArrayList<Object> values = new ArrayList<Object>(tokens.length);
            for(String token: tokens) {                
                Object res = loadSingleParamInternal(paramType, paramName, ruleName, cell, ota, token, null, true);
                if (res == null) {
                    res = paramType.nullObject();
                    
                    // Set cell meta info manually.   
                    //
                    //
                    setCellMetaInfo(cell, paramName, paramType, true);
                } 
                values.add(res);
            }  
            int valuesArraySize = values.size();
            arrayValues = paramType.getAggregateInfo().makeIndexedAggregate(paramType, new int[] { valuesArraySize });

            for (int i = 0; i < valuesArraySize; i++) {
                Array.set(arrayValues, i, values.get(i));
            }             
        }        
        return arrayValues;
    }

    public static String[] extractElementsFromCommaSeparatedArray(ILogicalTable cell) {
        String[] tokens = null;
        String src = cell.getGridTable().getCell(0, 0).getStringValue();        
        if (src != null) {
            tokens = StringTool.splitAndEscape(src, ARRAY_ELEMENTS_SEPARATOR, ARRAY_ELEMENTS_SEPARATOR_ESCAPER);
        }
        return tokens;
    }
    
    public static boolean isCommaSeparatedArray(ILogicalTable valuesTable) { 
        boolean result = false;
        String stringValue = valuesTable.getGridTable().getCell(0, 0).getStringValue();
        if (stringValue != null) {
            result = stringValue.contains(ARRAY_ELEMENTS_SEPARATOR);
        } else {
            result = false;
        } 
        return result;
    }
    
    private static class ArrayHolder {
        private Object ary;
        private CompositeMethod[] methods;

        public ArrayHolder(Object ary, CompositeMethod[] methods) {
            this.ary = ary;
            this.methods = methods;
        }

        public Object invoke(Object target, Object[] dtParams, IRuntimeEnv env) {
            for (int i = 0; i < methods.length; i++) {
                if (methods[i] != null) {
                    Object res = methods[i].invoke(target, dtParams, env);
                    Array.set(ary, i, res);
                }
            }

            return ary;
        }
    }
}
