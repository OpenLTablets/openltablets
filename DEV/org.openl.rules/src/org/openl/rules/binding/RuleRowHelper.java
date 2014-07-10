package org.openl.rules.binding;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openl.binding.IBindingContext;
import org.openl.domain.IDomain;
import org.openl.exception.OpenLCompilationException;
import org.openl.meta.IMetaHolder;
import org.openl.meta.ValueMetaInfo;
import org.openl.rules.OpenlToolAdaptor;
import org.openl.rules.convertor.IObjectToDataConvertor;
import org.openl.rules.convertor.IString2DataConvertor;
import org.openl.rules.convertor.ObjectToDataConvertorFactory;
import org.openl.rules.convertor.String2DataConvertorFactory;
import org.openl.rules.dt.element.ArrayHolder;
import org.openl.rules.lang.xls.types.CellMetaInfo;
import org.openl.rules.table.ICell;
import org.openl.rules.table.IGrid;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.LogicalTableHelper;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.source.impl.SubTextSourceCodeModule;
import org.openl.syntax.exception.CompositeSyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.syntax.impl.ISyntaxConstants;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenMethodHeader;
import org.openl.types.impl.CompositeMethod;
import org.openl.types.impl.OpenMethodHeader;
import org.openl.util.Log;
import org.openl.util.StringTool;
import org.openl.vm.IRuntimeEnv;

public class RuleRowHelper {

    public static final String ARRAY_ELEMENTS_SEPARATOR_ESCAPER = "\\";
    public static final String ARRAY_ELEMENTS_SEPARATOR = ",";
    public static final String CONSTRUCTOR = "constructor";

    public static int calculateHeight(ILogicalTable table) {

        int height = table.getHeight();

        int last = -1;

        for (int i = 0; i < height; i++) {
            String source = table.getRow(i).getSource().getCell(0, 0).getStringValue();

            if (source != null && source.trim().length() != 0) {
                last = i;
            }
        }

        return last + 1;
    }

    public static String[] extractElementsFromCommaSeparatedArray(ILogicalTable cell) {

        String[] tokens = null;
        String src = cell.getSource().getCell(0, 0).getStringValue();

        if (src != null) {
            tokens = StringTool.splitAndEscape(src, ARRAY_ELEMENTS_SEPARATOR, ARRAY_ELEMENTS_SEPARATOR_ESCAPER);
        }

        return tokens;
    }

    /**
     * Method to support loading Arrays through
     * {@link #ARRAY_ELEMENTS_SEPARATOR} in one cell. Gets the cell string
     * value. Split it by {@link #ARRAY_ELEMENTS_SEPARATOR}, and process every
     * token as single parameter. Returns array of parameters.
     * 
     * @param paramType
     * @param paramName
     * @param ruleName
     * @param cell
     * @param openlAdaptor
     * @return Array of parameters.
     * @throws SyntaxNodeException
     */
    public static Object loadCommaSeparatedParam(IOpenClass paramType,
            String paramName,
            String ruleName,
            ILogicalTable cell,
            OpenlToolAdaptor openlAdaptor) throws SyntaxNodeException {

        Object arrayValues = null;
        String[] tokens = null;
        tokens = extractElementsFromCommaSeparatedArray(cell);

        if (tokens != null) {

            ArrayList<Object> values = new ArrayList<Object>(tokens.length);

            for (String token : tokens) {

                Object res = RuleRowHelper.loadSingleParam(paramType,
                    paramName,
                    ruleName,
                    cell,
                    openlAdaptor,
                    token,
                        true);

                if (res == null) {
                    res = paramType.nullObject();

                    // Set cell meta info manually.
                    //
                    //
                    if(!openlAdaptor.getBindingContext().isExecutionMode()) {
                    	setCellMetaInfo(cell, paramName, paramType, true);
                    }
                }

                values.add(res);
            }

            int valuesArraySize = values.size();
            arrayValues = paramType.getAggregateInfo().makeIndexedAggregate(paramType, new int[] { valuesArraySize });

            for (int i = 0; i < valuesArraySize; i++) {
                Array.set(arrayValues, i, values.get(i));
            }
        }else {
            arrayValues = paramType.getAggregateInfo().makeIndexedAggregate(paramType, new int[] { 0 });
            if(!openlAdaptor.getBindingContext().isExecutionMode()) {
                setCellMetaInfo(cell, paramName, paramType, true);
            }
        }

        return arrayValues;
    }

    public static IOpenClass getType(String typeCode, IBindingContext bindingContext) {

        if (typeCode.endsWith("[]")) {
        	// FIXME: refactor, use JavaClassGeneratorHelper#getDimension(String)
        	//
            int dims = 0;
            String baseCode = typeCode;
            while (baseCode.endsWith("[]")) {
                baseCode = baseCode.substring(0, baseCode.length() - 2);
                dims++;
            }
            IOpenClass baseType = bindingContext.findType(ISyntaxConstants.THIS_NAMESPACE, baseCode);

            if (baseType == null) {
                return null;
            }

            return baseType.getAggregateInfo().getIndexedAggregateType(baseType, dims);
        }

        IOpenClass type = bindingContext.findType(ISyntaxConstants.THIS_NAMESPACE, typeCode);

        return type;
    }

    public static void loadParams(Object[] array,
            int from,
            Object[] paramValues,
            Object target,
            Object[] params,
            IRuntimeEnv env) {

        for (int i = 0; i < paramValues.length; i++) {

            Object value = paramValues[i];

            if (value instanceof IOpenMethod) {
                value = ((IOpenMethod) value).invoke(target, params, env);
            } else if (value instanceof ArrayHolder) {
                value = ((ArrayHolder) value).invoke(target, params, env);
            }

            array[i + from] = value;
        }
    }

    public static Object loadSingleParam(IOpenClass paramType,
            String paramName,
            String ruleName,
            ILogicalTable table,
            OpenlToolAdaptor openlAdapter) throws SyntaxNodeException {

        ICell theCell = table.getSource().getCell(0, 0);

        if (paramType.getInstanceClass().equals(String.class)) {
            // if param type is of type String, load as String
            String src = theCell.getStringValue();
            if (src != null) src = src.intern();
            return loadSingleParam(paramType, paramName, ruleName, table, openlAdapter, src, false);
        }
        
        // load value as native type
        if (theCell.hasNativeType()) {
            if (theCell.getNativeType() == IGrid.CELL_TYPE_NUMERIC) {
                Object res = loadNativeValue(theCell,
                    paramType,
                    openlAdapter.getBindingContext(),
                    paramName,
                    ruleName,
                    table);
                if (res != null) {
                    try {
                        validateValue(res, paramType);
                    } catch (Throwable t) {
                        throw SyntaxNodeExceptionUtils.createError(null,
                            t,
                            null,
                            new GridCellSourceCodeModule(table.getSource(), openlAdapter.getBindingContext()));
                    }
                    if(!openlAdapter.getBindingContext().isExecutionMode())
                        setCellMetaInfo(table, paramName, paramType, false);
                    return res;
                }
            }
        }
        
        // don`t move it up, as this call will convert native values such as numbers and dates to strings, it 
        // has negative performance implication
        String src = theCell.getStringValue();
        if (src != null) src = src.intern();
        return loadSingleParam(paramType, paramName, ruleName, table, openlAdapter, src, false);
    }

    private static Object loadNativeValue(ICell cell,
            IOpenClass paramType,
            IBindingContext bindingContext,
            String paramName,
            String ruleName,
            ILogicalTable table) {
        Class<?> expectedType = paramType.getInstanceClass();
        double value = cell.getNativeNumber();
        IObjectToDataConvertor objectConvertor = ObjectToDataConvertorFactory.getConvertor(expectedType, double.class);
        Object res = null;
        if (objectConvertor != ObjectToDataConvertorFactory.NO_Convertor) {
            res = objectConvertor.convert(value, bindingContext);
        } else {
            objectConvertor = ObjectToDataConvertorFactory.getConvertor(expectedType, Double.class);            
            if (objectConvertor != ObjectToDataConvertorFactory.NO_Convertor) {
                res = objectConvertor.convert(value, bindingContext);
            }
            else {
                objectConvertor = ObjectToDataConvertorFactory.getConvertor(expectedType, Date.class);
                if (objectConvertor != ObjectToDataConvertorFactory.NO_Convertor) {
                    Date dateValue = cell.getNativeDate();
                    res = objectConvertor.convert(dateValue, bindingContext);
                } else if (((int) value) == value) {
                    objectConvertor = ObjectToDataConvertorFactory.getConvertor(expectedType, Integer.class);
                    if (objectConvertor != ObjectToDataConvertorFactory.NO_Convertor)
                        res = objectConvertor.convert((int) value, bindingContext);

                }
            }
        }
         
        if (res != null && res instanceof IMetaHolder) {
            setMetaInfo((IMetaHolder) res, table, paramName, ruleName, bindingContext);
        }

        return res;
    }

    private static Object loadSingleParam(IOpenClass paramType,
                                          String paramName,
                                          String ruleName,
                                          ILogicalTable cell,
                                          OpenlToolAdaptor openlAdapter,
                                          String source,
                                          boolean isPartOfArray) throws SyntaxNodeException {

        // TODO: parse values considering underlying excel format. Note: this
        // class doesn't know anything about Excel. Keep it storage format
        // agnostic (don't introduce excel dependencies). Also consider adding
        // meta info.
        if (source != null && (source = source.trim()).length() != 0) {
            if (openlAdapter != null && openlAdapter.getHeader() != null) {
                IOpenMethodHeader old_header = openlAdapter.getHeader();
                OpenMethodHeader newHeader = new OpenMethodHeader(old_header.getName(),
                    paramType,
                    old_header.getSignature(),
                    old_header.getDeclaringClass());
                openlAdapter.setHeader(newHeader);

                if (source.startsWith("{") && source.endsWith("}")) {
                    GridCellSourceCodeModule srcCode = new GridCellSourceCodeModule(cell.getSource(),
                        openlAdapter.getBindingContext());

                    return openlAdapter.makeMethod(srcCode);
                }

                if (source.startsWith("=") && (source.length() > 2 || source.length() == 2 && Character.isLetterOrDigit(source.charAt(1)))) {

                    GridCellSourceCodeModule gridSource = new GridCellSourceCodeModule(cell.getSource(),
                        openlAdapter.getBindingContext());
                    IOpenSourceCodeModule code = new SubTextSourceCodeModule(gridSource, 1);

                    return openlAdapter.makeMethod(code);
                }
            }

            Class<?> expectedType = paramType.getInstanceClass();

            // try {
            // Set cell meta information at first.
            //
            if (!openlAdapter.getBindingContext().isExecutionMode())
                setCellMetaInfo(cell, paramName, paramType, isPartOfArray);

            // Try to get cell object value with appropriate string parser.
            // A parser instance will be selected using expected type of cell
            // value.
            //
            Object result;

            try {
                openlAdapter.getBindingContext();
                IString2DataConvertor convertor = String2DataConvertorFactory.getConvertor(expectedType);
                result = convertor.parse(source, null);
            } catch (Exception e) {
                // Parsing of loaded string value can be sophisticated process.
                // As a result various exception types can be thrown (e.g.
                // CompositeSyntaxNodeException) with not user-friendly message.
                // 
                String message = String.format("Cannot parse cell value '%s'", source);
                IOpenSourceCodeModule cellSourceCodeModule = new GridCellSourceCodeModule(cell.getSource(),
                    openlAdapter.getBindingContext());
                if (e instanceof CompositeSyntaxNodeException ) {
                    throw SyntaxNodeExceptionUtils.createError(message, cellSourceCodeModule);
                } else {
                    throw SyntaxNodeExceptionUtils.createError(message, e, null, cellSourceCodeModule);
                }
            }

            if (result instanceof IMetaHolder) {
                setMetaInfo((IMetaHolder) result, cell, paramName, ruleName, openlAdapter.getBindingContext());
            }

            try {
                validateValue(result, paramType);
            } catch (Exception e) {
                String message = String.format("Invalid cell value '%s'", source);
                IOpenSourceCodeModule cellSourceCodeModule = new GridCellSourceCodeModule(cell.getSource(),
                    openlAdapter.getBindingContext());

                throw SyntaxNodeExceptionUtils.createError(message, e, null, cellSourceCodeModule);
            }

            return result;
            // } catch (Throwable t) {
            // String message = String.format("Cannot load cell value '%s'",
            // source);
            // IOpenSourceCodeModule cellSourceCodeModule= new
            // GridCellSourceCodeModule(cell.getSource(),
            // openlAdapter.getBindingContext());
            //                
            // throw SyntaxNodeExceptionUtils.createError(message,
            // cellSourceCodeModule);
            // }
        } else {
            // Set meta info for empty cells. To suggest an appropriate editor
            // according to cell type.
            if (!openlAdapter.getBindingContext().isExecutionMode())
                setCellMetaInfo(cell, paramName, paramType, false);
        }

        return null;
    }

    public static boolean isCommaSeparatedArray(ILogicalTable valuesTable) {
        if (!isFormula(valuesTable)) {
            String stringValue = valuesTable.getSource().getCell(0, 0).getStringValue();
            if (stringValue != null) {
                if (stringValue.contains(ARRAY_ELEMENTS_SEPARATOR)) {
                    /**string like "0," isn't array*/
                    if (stringValue.split(ARRAY_ELEMENTS_SEPARATOR).length > 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isFormula(ILogicalTable valuesTable) {

        String stringValue = valuesTable.getSource().getCell(0, 0).getStringValue();

        if (stringValue != null) {
            stringValue.trim();
            return stringValue.startsWith("=");
        }

        return false;
    }

    public static void setCellMetaInfo(ILogicalTable logicalCell, String paramName, IOpenClass paramType, boolean isMultiValue) {
        CellMetaInfo meta = new CellMetaInfo(CellMetaInfo.Type.DT_DATA_CELL, paramName, paramType, isMultiValue);
        ICell cell = logicalCell.getSource().getCell(0, 0);
        cell.setMetaInfo(meta);
    }

//    public static void setCellMetaInfo(ILogicalTable logicalCell, String paramName, IOpenClass paramType, boolean isMultiValue, List<MethodUsage> usedMethods) {
//        CellMetaInfo meta = new CellMetaInfo(CellMetaInfo.Type.DT_DATA_CELL, paramName, paramType, isMultiValue, usedMethods);
//        ICell cell = logicalCell.getSource().getCell(0, 0);
//        cell.setMetaInfo(meta);
//    }

    private static void setMetaInfo(IMetaHolder holder,
            ILogicalTable cell,
            String paramName,
            String ruleName,
            IBindingContext bindingContext) {
        if (!bindingContext.isExecutionMode()) {
            ValueMetaInfo valueMetaInfo = new ValueMetaInfo();
            valueMetaInfo.setShortName(paramName);
            valueMetaInfo.setFullName(ruleName == null ? paramName : ruleName + "." + paramName);
            valueMetaInfo.setSource(new GridCellSourceCodeModule(cell.getSource(), bindingContext));

            holder.setMetaInfo(valueMetaInfo);
        }
    }

    @SuppressWarnings("unchecked")
    public static void validateValue(Object value, IOpenClass paramType) throws Exception {
        IDomain<Object> domain = (IDomain<Object>)paramType.getDomain();

        if (domain == null) {
            // there is no domain so nothing to validate.
            return;
        } else {             
            try {
                // block is surrounded by try block, as EnumDomain implementation throws a
                // RuntimeException when value doesn`t belong to domain.                
                // 
                boolean contains = domain.selectObject(value);
                if (contains) {
                    // if value belongs to domain, return
                    return;
                } else {
                    throw new OpenLCompilationException(
                        String.format("The value '%s' is outside of domain %s", value, domain.toString()));
                }
            } catch (RuntimeException e) {
                throw new OpenLCompilationException(e.getMessage(), e.getCause());
            }
        }
    }

    public static Object loadParam(ILogicalTable dataTable,
            IOpenClass paramType,
            String paramName,
            String ruleName,
            OpenlToolAdaptor openlAdaptor,
            boolean indexed) throws SyntaxNodeException {

        if (!indexed) {
            return RuleRowHelper.loadSingleParam(paramType, paramName, ruleName, dataTable, openlAdaptor);
        }

        dataTable = LogicalTableHelper.make1ColumnTable(dataTable);

        int height = RuleRowHelper.calculateHeight(dataTable);

        if (height == 0) {
            return null;
        }

        // If data table contains one cell and parameter type is not array type
        // then load parameter value from single cell of table
        //
        // TODO: Is 'RuleRowHelper.isCommaSeparatedArray(dataTable)' check
        // required here? Can we make decision how to load data table using
        // value
        // of 'paramType' variable?
        // 
        if (height == 1 && !RuleRowHelper.isCommaSeparatedArray(dataTable) && !paramType.isArray()) {
            // attempt to load as a single paramType(will work in case of
            // expressions)
            try {
                return loadSingleParam(paramType, paramName, ruleName, dataTable, openlAdaptor);
            } catch (Exception e) {

                Log.debug(e);
                // do nothing, assume the type was wrong or this was not an
                // expression
                // let the regular flow of events take it's course
            }
        }

        // Load parameter value as an array of values.
        //
        return loadArrayParameters(dataTable, paramName, ruleName, openlAdaptor, paramType);
    }

    private static Object loadArrayParameters(ILogicalTable dataTable,
            String paramName,
            String ruleName,
            OpenlToolAdaptor openlAdaptor,
            IOpenClass arrayType) throws SyntaxNodeException {

        int height = RuleRowHelper.calculateHeight(dataTable);
        IOpenClass paramType = arrayType.getAggregateInfo().getComponentType(arrayType);

        if (height == 1 && RuleRowHelper.isCommaSeparatedArray(dataTable)) {
            // load comma separated array
            return loadCommaSeparatedArrayParams(dataTable, paramName, ruleName, openlAdaptor, paramType);
        } else if (height == 1 && isFormula(dataTable)) {
            return loadSingleParam(arrayType, paramName, ruleName, dataTable, openlAdaptor);
        } else {
            return loadSimpleArrayParams(dataTable, paramName, ruleName, openlAdaptor, paramType);
        }
    }

    private static Object loadCommaSeparatedArrayParams(ILogicalTable dataTable,
            String paramName,
            String ruleName,
            OpenlToolAdaptor openlAdaptor,
            IOpenClass paramType) throws SyntaxNodeException {

        ILogicalTable paramSource = dataTable.getRow(0);
        Object params = RuleRowHelper.loadCommaSeparatedParam(paramType, paramName, ruleName, paramSource, openlAdaptor);
        Class<?> paramClass = params.getClass();
        if (paramClass.isArray() && !paramClass.getComponentType().isPrimitive()) {
            return processAsObjectParams(paramType, (Object[]) params);
        }
        return params;
    }

    /**
     * Checks if the elements of parameters array are the instances of
     * {@link CompositeMethod}, if yes process it through {@link ArrayHolder}.
     * If no return Object[].
     * 
     * @param paramType parameter type
     * @param paramsArray array of parameters
     * @return {@link ArrayHolder} if elements of parameters array are instances
     *         of {@link CompositeMethod}, in other case Object[].
     */
    private static Object processAsObjectParams(IOpenClass paramType, Object[] paramsArray) {
        List<CompositeMethod> methodsList = null;
        Object ary = null;
        int paramsLength = paramsArray.length;
        ary = paramType.getAggregateInfo().makeIndexedAggregate(paramType, new int[] { paramsLength });
        for (int i = 0; i < paramsLength; i++) {
            if (paramsArray[i] instanceof CompositeMethod) {
                methodsList = new ArrayList<CompositeMethod>(addMethod(methodsList, (CompositeMethod) paramsArray[i]));
            } else {
                Array.set(ary, i, paramsArray[i]);
            }
        }

        return methodsList == null ? ary : new ArrayHolder(ary,
            methodsList.toArray(new CompositeMethod[methodsList.size()]));
    }

    private static List<CompositeMethod> addMethod(List<CompositeMethod> methods, CompositeMethod method) {
        if (methods == null) {
            methods = new ArrayList<CompositeMethod>();
        }
        methods.add(method);

        return methods;
    }

    private static Object loadSimpleArrayParams(ILogicalTable dataTable,
            String paramName,
            String ruleName,
            OpenlToolAdaptor openlAdaptor,
            IOpenClass paramType) throws SyntaxNodeException {

        int height = RuleRowHelper.calculateHeight(dataTable);

        List<CompositeMethod> methodsList = null;
        List<Object> values = new ArrayList<Object>();

        for (int i = 0; i < height; i++) { // load array values represented as
            // number of cells
            ILogicalTable cell = dataTable.getRow(i);
            Object parameter = RuleRowHelper.loadSingleParam(paramType, paramName, ruleName, cell, openlAdaptor);

            if (parameter instanceof CompositeMethod) {
                methodsList = new ArrayList<CompositeMethod>(addMethod(methodsList, (CompositeMethod) parameter));
            } else {
                if (parameter != null) {
                    values.add(parameter);
                }
            }
        }

        Object ary = paramType.getAggregateInfo().makeIndexedAggregate(paramType, new int[] { values.size() });

        for (int i = 0; i < values.size(); i++) {
            Array.set(ary, i, values.get(i));
        }

        return methodsList == null ? ary : new ArrayHolder(ary,
            methodsList.toArray(new CompositeMethod[methodsList.size()]));

    }
}
