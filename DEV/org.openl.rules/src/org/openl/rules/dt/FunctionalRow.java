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

import org.openl.IOpenSourceCodeModule;
import org.openl.OpenL;
import org.openl.OpenlTool;
import org.openl.OpenlToolAdaptor;
import org.openl.binding.IBindingContext;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.IBoundError;
import org.openl.binding.impl.BoundError;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.domain.IDomain;
import org.openl.meta.IMetaHolder;
import org.openl.meta.ValueMetaInfo;
import org.openl.rules.data.IString2DataConvertor;
import org.openl.rules.data.String2DataConvertorFactory;
import org.openl.rules.lang.xls.types.CellMetaInfo;
import org.openl.rules.table.ALogicalTable;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.IWritableGrid;
import org.openl.rules.table.LogicalTable;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.syntax.SyntaxErrorException;
import org.openl.syntax.impl.ISyntaxConstants;
import org.openl.syntax.impl.IdentifierNode;
import org.openl.syntax.impl.SubTextSourceCodeModule;
import org.openl.syntax.impl.TokenizerParser;
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
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 * 
 */
public abstract class FunctionalRow implements IDecisionRow,
		IDecisionTableConstants
{

	int row;

	ILogicalTable decisionTable;

	ILogicalTable paramsTable;

	ILogicalTable codeTable;

	ILogicalTable presentationTable;

	String name;

	public FunctionalRow(String name, int row, ILogicalTable decisionTable)
	{
		this.row = row;
		this.decisionTable = decisionTable;
		this.paramsTable = decisionTable.getLogicalRegion(PARAM_COLUMN, row, 1, 1);
		this.codeTable = decisionTable.getLogicalRegion(CODE_COLUMN, row, 1, 1);
		this.presentationTable = decisionTable.getLogicalRegion(
				PRESENTATION_COLUMN, row, 1, 1);
		this.name = name;
	}

	public void prepare(IOpenClass methodType, IMethodSignature signature,
			OpenL openl, ModuleOpenClass dtModule, IBindingContextDelegator cxtd,
			RuleRow ruleRow) throws Exception
	{
		method = generateMethod(signature, openl, cxtd, dtModule, methodType);

		OpenlToolAdaptor ota = new OpenlToolAdaptor(openl, cxtd);

		IOpenMethodHeader h = new OpenMethodHeader(name, null, signature,
				dtModule);
		ota.setHeader(h);

		paramValues = prepareParamValues(ota, ruleRow);

	}

	int nValues()
	{
		return decisionTable.getLogicalWidth() - DATA_COLUMN;
	}

	ILogicalTable getValueCell(int col)
	{
		return decisionTable.getLogicalRegion(col + DATA_COLUMN, row, 1, 1);
	}

	Object[][] prepareParamValues(OpenlToolAdaptor ota, RuleRow ruleRow)
			throws Exception
	{
		int len = nValues();
		Object[][] values = new Object[len][];

		IParameterDeclaration[] paramDecl = getParams(ota.getBindingContext());

		ArrayList<IBoundError> errors = new ArrayList<IBoundError>();
		
		for (int col = 0; col < len; col++)
		{
			ILogicalTable valueCell = getValueCell(col);
			IGridTable paramGridColumn = valueCell.getGridTable();

			Object[] valueAry = new Object[paramDecl.length];

			int fromHeight = 0;
			boolean notEmpty = false;
			String ruleName = ruleRow == null ? "R" + (col + 1) : ruleRow
					.getRuleName(col);

			for (int j = 0; j < paramDecl.length; j++)
			{
				if (paramDecl[j] == null)
					continue;

				int gridHeight = paramsTable.getLogicalRow(j).getGridTable()
						.getGridHeight();
				IGridTable singleParamGridTable = (IGridTable) paramGridColumn.rows(
						fromHeight, fromHeight + gridHeight - 1);

				Object v = null;
				try
				{
				 v = loadParam(LogicalTable.logicalTable(singleParamGridTable),
						paramDecl[j].getType(), paramDecl[j].getName(), ruleName, ota);
				}
				catch(BoundError error)
				{
					errors.add(error);
				}
				
				if (v != null)
				{
					notEmpty = true;
				}
				valueAry[j] = v;

				fromHeight += gridHeight;
			}

			if (notEmpty)
				values[col] = valueAry;
		}

		if (errors.size() > 0)
		{
			throw new SyntaxErrorException("Error:", errors.toArray(new IBoundError[0]));
		}	
		
		return values;
	}

	Object loadParam(ILogicalTable dataTable, IOpenClass paramType,
			String paramName, String ruleName, OpenlToolAdaptor ota)
			throws BoundError
	{
		boolean indexed = paramType.getAggregateInfo().isAggregate(paramType);

		if (indexed)
			paramType = paramType.getAggregateInfo().getComponentType(paramType);

		if (!indexed)
			return loadSingleParam(paramType, paramName, ruleName, dataTable, ota);

		dataTable = ALogicalTable.make1ColumnTable(dataTable);

		int h = calcHeight(dataTable);

		if (h == 0)
			return null;

		
		CompositeMethod[] methods = null;
		Object ary = paramType.getAggregateInfo().makeIndexedAggregate(paramType,
				new int[] { h });

		for (int i = 0; i < h; i++)
		{
			ILogicalTable cell = dataTable.getLogicalRow(i);
			Object x = loadSingleParam(paramType, paramName, ruleName, cell,
					ota);
			if (x instanceof CompositeMethod)
			{
				if (methods == null)
					methods = new CompositeMethod[h];
				methods[i] = (CompositeMethod)x;
			}	
			else Array.set(ary, i, x);
		}

		return methods == null ? ary : new ArrayHolder(ary, methods);

	}
	
	
	static class ArrayHolder
	{
		Object ary;
		CompositeMethod[] methods;
		public ArrayHolder(Object ary, CompositeMethod[] methods)
		{
			this.ary = ary;
			this.methods = methods;
		}
		/**
		 * @param target
		 * @param dtParams
		 * @param env
		 * @return
		 */
		public Object invoke(Object target, Object[] dtParams, IRuntimeEnv env)
		{
			for (int i = 0; i < methods.length; i++)
			{
				if (methods[i] != null)
				{
					Object res = methods[i].invoke(target, dtParams, env);
					Array.set(ary, i, res);
				}	
			}
			
			return ary;
		}
	}

	int calcHeight(ILogicalTable table)
	{
		int h = table.getLogicalHeight();

		for (int i = 0; i < h; i++)
		{
			String src = table.getLogicalRow(i).getGridTable().getStringValue(0, 0);
			if (src == null || src.trim().length() == 0)
				return i;

		}

		return h;
	}

	static public Object loadSingleParam(IOpenClass paramType, String paramName,
			String ruleName, ILogicalTable cell, OpenlToolAdaptor ota)
			throws BoundError
	{
		String src = cell.getGridTable().getStringValue(0, 0);
		if (src == null || (src = src.trim()).length() == 0)
			return null;

		if (ota != null && ota.getHeader() != null)
		{
			IOpenMethodHeader old_header = ota.getHeader();
			OpenMethodHeader newHeader = new OpenMethodHeader(old_header.getName(),
					paramType, old_header.getSignature(), old_header.getDeclaringClass());
			ota.setHeader(newHeader);

			if (src.startsWith("{") && src.endsWith("}"))
			{
				GridCellSourceCodeModule srcCode = new GridCellSourceCodeModule(cell
						.getGridTable());
				return ota.makeMethod(srcCode);
			}

			if (src.startsWith("=") && src.length() > 2)
			{
				IOpenSourceCodeModule srcCode = new SubTextSourceCodeModule(
						new GridCellSourceCodeModule(cell.getGridTable()), 1);

				return ota.makeMethod(srcCode);
			}
		}

		IString2DataConvertor conv = String2DataConvertorFactory
				.getConvertor(paramType.getInstanceClass());

		try
		{
			Object res = conv.parse(src, null, ota.getBindingContext());
			if (res instanceof IMetaHolder)
			{
				setMetaInfo((IMetaHolder) res, cell, paramName, ruleName);
			}
			
			setCellMetaInfo(cell, paramName, paramType);
			validateValue(res, paramType);
			return res;
		} catch (Throwable t)
		{
			throw new BoundError(null, null, t, new GridCellSourceCodeModule(cell
					.getGridTable()));
		}
	}

	private static void validateValue(Object res, IOpenClass paramType)
	{
		IDomain domain = paramType.getDomain();
		if (domain == null || domain.selectObject(res))
			return;
		
		String errorMsg = "The value " + res + " is outside of domain " + domain.toString(); 

		throw new RuntimeException(errorMsg);
		
	}

	/**
	 * @param cell
	 * @param paramName
	 * @param paramType
	 */
	private static void setCellMetaInfo(ILogicalTable cell, String paramName, IOpenClass paramType)
	{
		CellMetaInfo meta = new CellMetaInfo(CellMetaInfo.Type.DT_DATA_CELL, paramType);
		IWritableGrid.Tool.putCellMetaInfo(cell.getGridTable(), 0, 0, meta);
	}

	public static void setMetaInfo(IMetaHolder holder, ILogicalTable cell,
			String paramName, String ruleName)
	{
		ValueMetaInfo vmi = new ValueMetaInfo();
		vmi.setShortName(paramName);
		vmi.setFullName(ruleName == null ? paramName : ruleName + "." + paramName);
		vmi.setSourceUrl(new GridCellSourceCodeModule(cell.getGridTable())
				.getUri(0));
		holder.setMetaInfo(vmi);
	}

	IParameterDeclaration[] params;

	static public IOpenClass getType(String typeCode, IBindingContext cxt)
			throws Exception
	{
		if (typeCode.endsWith("[]"))
		{
			String baseCode = typeCode.substring(0, typeCode.length() - 2);
//			IOpenClass baseType = OpenlTool.getType(baseCode, openl);
			IOpenClass baseType = cxt.findType(ISyntaxConstants.THIS_NAMESPACE, baseCode);
			if (baseType == null)
				return null;
			return baseType.getAggregateInfo().getIndexedAggregateType(baseType, 1);
		}

		IOpenClass type = cxt.findType(ISyntaxConstants.THIS_NAMESPACE, typeCode);;
		// if (type == null)
		// throw new Exception("Type " + typeCode + " not found");

		return type;
	}

	IParameterDeclaration no_parameter()
	{
		return new ParameterDeclaration(
			JavaOpenClass.STRING, NO_PARAM + np_idx++);
	}
  int np_idx = 0; 
  static String NO_PARAM = "___NO_PARAM___";
  
	
	public String[] getParamPresentation()
	{
		int len = paramsTable.getLogicalHeight();
		String[] res = new String[len];
		int fromHeight = 0;
		for (int i = 0; i < res.length; i++)
		{

			int gridHeight = paramsTable.getLogicalRow(i).getGridTable()
					.getGridHeight();
			IGridTable singleParamGridTable = (IGridTable) presentationTable
					.getGridTable().rows(fromHeight, fromHeight + gridHeight - 1);
			res[i] = singleParamGridTable.getStringValue(0, 0);

			fromHeight += gridHeight;
		}

		return res;
	}

	IParameterDeclaration[] getParams(IBindingContext cxt) throws Exception
	{
		if (params == null)
		{
			
			Set paramNames = new HashSet();
			int len = paramsTable.getLogicalHeight();
			params = new IParameterDeclaration[len];

			for (int i = 0; i < len; i++)
			{

				ILogicalTable paramTable = paramsTable.getLogicalRow(i);
				IOpenSourceCodeModule src = new GridCellSourceCodeModule(paramTable
						.getGridTable());
				IdentifierNode[] nodes = TokenizerParser.tokenize(src, " \n\r");
				if (nodes.length == 0)
				{
					// no parameters
					params[i] = no_parameter();
					continue;
				}

				String errMsg;

				if (nodes.length != 2)
				{
					errMsg = "Parameter Cell format: <type> <name>";
					BoundError err = new BoundError(null, errMsg, null, src);
					throw err;
				}

				String typeCode = nodes[0].getIdentifier();
				IOpenClass ptype = getType(typeCode, cxt);
				if (ptype == null)
					throw new BoundError(nodes[0], "Type not found: " + typeCode);

				String pname = nodes[1].getIdentifier();
				if (paramNames.contains(pname))
					throw new BoundError(nodes[1], "Duplicated parameter name: " + pname);
				
				paramNames.add(pname);
				
				params[i] = new ParameterDeclaration(ptype, pname);

			}

		}
		return params;
	}

	CompositeMethod generateMethod(IMethodSignature signature, OpenL openl,
			IBindingContextDelegator cxt, IOpenClass declaringClass,
			IOpenClass methodType) throws Exception
	{

		IOpenSourceCodeModule src = new GridCellSourceCodeModule(codeTable
				.getGridTable());

		IParameterDeclaration[] mparams = getParams(cxt);

		IMethodSignature newSignature = hasNoParams() ? signature
				: ((MethodSignature) signature).merge(mparams);

		OpenMethodHeader methodHeader = new OpenMethodHeader(null, methodType,
				newSignature, declaringClass);

		return OpenlTool.makeMethod(src, openl, methodHeader, cxt);

	}

	Boolean hasNoParams = null;
	
	boolean hasNoParams()
	{
		if (hasNoParams == null)
			hasNoParams = params[0].getName().startsWith(NO_PARAM) ? Boolean.TRUE : Boolean.FALSE;
		return hasNoParams.booleanValue();
	}

	static Object notNull(Object x)
	{
		return x == null ? "" : x;
	}

	// ////////////////////////////////////////////////////////////////

	CompositeMethod method;

	Object[][] paramValues;

	public IOpenMethod getMethod()
	{
		return method;
	}
	
	
	

	public IOpenSourceCodeModule getSourceCodeModule()
	{
		
		return method == null ? null : method.getMethodBodyBoundNode().getSyntaxNode().getModule();
	}

	public IDecisionValue calculateCondition(int rule, Object target,
			Object[] dtParams, IRuntimeEnv env)
	{
		Object value = paramValues[rule];
		if (value == null)
			return IDecisionValue.NxA;
		if (value instanceof IDecisionValue)
			return (IDecisionValue) value;

		Object[] params = hasNoParams() ? dtParams : mergeParams(target, dtParams,
				env, (Object[]) value);
		// (Object[]) ArrayTool.merge(dtParams, value);
		// dtParams.length == method.getSignature().getParameterTypes().length ?
		// dtParams :
		// (Object[]) ArrayTool.merge(dtParams, value);

		Boolean res = (Boolean) method.invoke(target, params, env);

		return res == null || res.booleanValue() ? IDecisionValue.True
				: IDecisionValue.False;

	}

	static public Object[] mergeParams(Object target, Object[] dtParams,
			IRuntimeEnv env, Object[] params)
	{
		Object[] newParams = new Object[dtParams.length + params.length];

		System.arraycopy(dtParams, 0, newParams, 0, dtParams.length);

		loadParams(newParams, dtParams.length, params, target, dtParams, env);
		
		return newParams;
	}
	
	
	static public void loadParams(Object[] ary, int from, Object[] params, Object target, Object[] dtparams, IRuntimeEnv env)
	{
		for (int i = 0; i < params.length; i++)
		{
			Object x = params[i];

			if (x instanceof IOpenMethod)
			{
				x = ((IOpenMethod) x).invoke(target, dtparams, env);
			}
			
			if (x instanceof ArrayHolder)
			{
				x = ((ArrayHolder)x).invoke(target, dtparams, env);
			}	

			ary[i + from] = x;
		}
		
	}
	

	public Object executeAction(int col, Object target, Object[] dtParams,
			IRuntimeEnv env)
	{
		Object value = paramValues[col];
		if (value == null)
			return null;

		if (hasNoParams())
			return method.invoke(target, dtParams, env);
		else
			return method.invoke(target, mergeParams(target, dtParams, env,
					(Object[]) value),

			// (Object[]) ArrayTool.merge(dtParams, value),
					env);

		// return res.booleanValue() ? IDecisionValue.True : IDecisionValue.False;

	}

	public String getName()
	{
		return name;
	}

	public IParameterDeclaration[] getParams()
	{
		return params;
	}

	public Object[][] getParamValues()
	{
		return paramValues;
	}

	public DTParameterInfo findParameterInfo(String name)
	{
		for (int i = 0; i < params.length; i++)
		{
			if (params[i].equals(name))
				return getParameterInfo(i);
		}
		return null;
	}

	public DTParameterInfo getParameterInfo(int i)
	{
		return new DTParameterInfo(i, this);
	}

	
	public int numberOfParams()
	{
		return params.length;
	}
	
}
