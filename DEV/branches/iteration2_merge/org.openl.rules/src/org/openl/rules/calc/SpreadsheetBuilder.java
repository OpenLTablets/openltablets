package org.openl.rules.calc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openl.IOpenSourceCodeModule;
import org.openl.base.INamedThing;
import org.openl.binding.DuplicatedVarException;
import org.openl.binding.IBindingContext;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.impl.BoundError;
import org.openl.binding.impl.module.ModuleBindingContext;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.meta.DoubleValue;
import org.openl.meta.IMetaInfo;
import org.openl.meta.StringValue;
import org.openl.rules.data.IString2DataConvertor;
import org.openl.rules.data.String2DataConvertorFactory;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.LogicalTable;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.syntax.impl.ISyntaxConstants;
import org.openl.syntax.impl.IdentifierNode;
import org.openl.syntax.impl.TokenizerParser;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethodHeader;
import org.openl.types.impl.OpenMethodHeader;
import org.openl.types.java.JavaOpenClass;

public class SpreadsheetBuilder 
{

	
	IBindingContext cxt;
	Spreadsheet spreadsheet;
	TableSyntaxNode tsn;
	
	Map<Integer, SpreadsheetHeaderDefinition> rowHeaders = new HashMap<Integer, SpreadsheetHeaderDefinition>();
	Map<Integer, SpreadsheetHeaderDefinition> columnHeaders = new HashMap<Integer, SpreadsheetHeaderDefinition>();
	
	Map<String, SpreadsheetHeaderDefinition> varDefinitions = new HashMap<String, SpreadsheetHeaderDefinition>(); 


	public SpreadsheetBuilder(IBindingContext cxt, Spreadsheet spreadsheet, TableSyntaxNode tsn) {
		this.cxt = cxt;
		this.spreadsheet = spreadsheet;
		this.tsn = tsn;
	}

	
	
	public void addColumnHeader(int col, StringValue sv, int cxtLevel) 
	{
		SpreadsheetHeaderDefinition h =  columnHeaders.get(col);
		if (h == null)
		{
			h = new SpreadsheetHeaderDefinition(-1, col);
			columnHeaders.put(col, h);
		}
		parseHeader(h, sv);
		
	}	
	

	public void addRowHeader(int row, StringValue sv, int cxtLevel) 
	{
		SpreadsheetHeaderDefinition h =  rowHeaders.get(row);
		if (h == null)
		{
			h = new SpreadsheetHeaderDefinition(row, -1);
			rowHeaders.put(row, h);
		}
		
		parseHeader(h, sv);
	}	
		
	public void parseHeader(SpreadsheetHeaderDefinition h, StringValue sv)
	{
		try
		{
		
			SymbolicTypeDef parsed = parseHeaderElement(sv);
			String hname = parsed.name.getIdentifier();
			
			SpreadsheetHeaderDefinition h1 = varDefinitions.get(hname);
			
			if (h1 != null)
			{
				throw new DuplicatedVarException(null, hname);
			}
			else varDefinitions.put(hname, h);
		
			h.addVarHeader(parsed);
			
			
		}
		catch(Throwable t)
		{
			BoundError b = null;
			if (t instanceof BoundError) {
				b = (BoundError) t;
			}
			else
				b = new BoundError(t, sv.asSourceCodeModule());
			tsn.addError(b);
			cxt.addError(b);
		}
		
		
	}
	
	
	SymbolicTypeDef parseHeaderElement(StringValue sv) throws BoundError
	{
		IdentifierNode[] nodes = TokenizerParser.tokenize(sv.asSourceCodeModule(), ":");
		switch(nodes.length)
		{
			case 1:
				return new SymbolicTypeDef(nodes[0], null);
			case 2:
				return new SymbolicTypeDef(nodes[0], nodes[1]);
			default:
				throw new BoundError("Valid header format: name [: type]", sv.asSourceCodeModule());
		}
	}
	
	
	static class SymbolicTypeDef
	{
		IdentifierNode name;
		IdentifierNode type;
		SymbolicTypeDef(IdentifierNode name, IdentifierNode type) {
			super();
			this.name = name;
			this.type = type;
		}
	}


	public void build(ILogicalTable tableBody) {
		rowNamesTable = tableBody.getLogicalColumn(0).rows(1);
		columnNamesTable = tableBody.getLogicalRow(0).columns(1);
		
		
		for (int row = 0; row < rowNamesTable.getLogicalHeight(); row++) 
		{
			addRowNames(row, rowNamesTable.getLogicalRow(row));
		}
		
		for (int col = 0; col < columnNamesTable.getLogicalWidth(); col++) 
		{
			addColumnNames(col, columnNamesTable.getLogicalColumn(col));
		}
		
		buildType();
		buildCells();
		
	}
	
	private void buildType() 
	{
		
		SpreadsheetType stype = new SpreadsheetType(null, spreadsheet.getName()+ "Type", cxt.getOpenL());
		
		spreadsheet.setSpreadsheetType(stype);
		
		for(SpreadsheetHeaderDefinition h: varDefinitions.values())
		{
			IOpenClass htype = null;
			for(SymbolicTypeDef sx: h.vars)
			{
				if (sx.type != null)
				{
					BoundError b = null; 
					IOpenClass type = cxt.findType(ISyntaxConstants.THIS_NAMESPACE, sx.type.getIdentifier());
					if (type == null)
						b = new BoundError(sx.type, "Type not found: " + sx.type.getIdentifier());
					else if (htype == null)
						htype = type;
					else if (htype != type)
						b =  new BoundError(sx.type, "Type redefinition");
					if (b!= null)
					{	
						tsn.addError(b);
						cxt.addError(b);
					}	
						
				}	
			}
			if (htype != null)
				h.setType(htype);
			
			
			for(SymbolicTypeDef sx: h.vars)
			{
				stype.addField(new SpreadsheetHeaderField(stype, sx.name, h));
			}	
			
		}
		
	}

	ILogicalTable rowNamesTable; 
	ILogicalTable columnNamesTable;

	private void buildCells() 
	{
//		ILogicalTable cellTable = LogicalTable.mergeBounds(rowNamesTable, columnNamesTable);
		
		int h = rowNamesTable.getLogicalHeight();
		int w = columnNamesTable.getLogicalWidth();
		
		SpreadsheetType stype = spreadsheet.getSpreadsheetType();
		ModuleBindingContext scxt = new ModuleBindingContext(cxt, stype);
		
		SCell[][] cells = new SCell[h][w];
		
		spreadsheet.setCells(cells);

		for (int row = 0; row < h; row++) 
		{
			
			for (int col = 0; col < w; col++) {
				SCell scell = new SCell(row, col);
				cells[row][col] =scell;
				ILogicalTable cell = LogicalTable.mergeBounds(rowNamesTable.getLogicalRow(row), columnNamesTable.getLogicalColumn(col));
				IOpenSourceCodeModule src = new GridCellSourceCodeModule(cell.getGridTable());
				String srcStr = src.getCode();
				IOpenClass type = deriveCellType(scell, cell, columnHeaders.get(col), rowHeaders.get(row), srcStr);
				
				
				scell.setType(type);

				for( SymbolicTypeDef coldef :  columnHeaders.get(col).getVars())
				{	
				
					for( SymbolicTypeDef rowdef :  rowHeaders.get(row).getVars())
						
					{
						String fieldname = "$"+coldef.name.getIdentifier() + "$"+rowdef.name.getIdentifier();
						stype.addField(new SCellField(stype,fieldname , scell));
//						System.out.println("$"+coldef.name.getIdentifier() + "$"+rowdef.name.getIdentifier());
					}
				}
			}	
		}
		
		for (int row = 0; row < h; row++) 
		{
			IBindingContext rowCxt = getRowContext(row, scxt);
			
			for (int col = 0; col < w; col++) {
				IBindingContext colCxt = getColContext(col, rowCxt);
				
				
				ILogicalTable cell = LogicalTable.mergeBounds(rowNamesTable.getLogicalRow(row), columnNamesTable.getLogicalColumn(col));
				
				SCell scell = cells[row][col];
				IOpenSourceCodeModule src = new GridCellSourceCodeModule(cell.getGridTable());
				String srcStr = src.getCode();
				
				if (CellLoader.isFormula(srcStr))
					formulaCells.add(scell);
				
				
				
				String name = "$" + columnHeaders.get(col).getFirstname() + '$' + rowHeaders.get(row).getFirstname();
				
				IMetaInfo meta = new SpreadsheetCellMetaInfo(name, src);
				CellLoader loader = new CellLoader(colCxt, makeHeader(meta.getDisplayName(INamedThing.SHORT), spreadsheet.getHeader(), scell.getType()), makeConvertor(scell.getType()) );
				
				
				
				try {
					Object cellvalue = loader.loadSingleParam(src, meta);
					scell.setValue(cellvalue);
				} catch (BoundError e) {
					tsn.addError(e);
					cxt.addError(e);
				}
				
			}
			
		}
	}

	Map<Integer, IBindingContext> rowContexts = new HashMap<Integer, IBindingContext>();
	
	private IBindingContext getRowContext(int row, IBindingContext scxt) 
	{
		IBindingContext cxt = rowContexts.get(row);
		if (cxt == null)
		{
			cxt = makeRowContext(row, scxt);
			rowContexts.put(row, cxt);
		}	
		
		return cxt;
	}

	Map<Integer, IBindingContextDelegator> colContexts = new HashMap<Integer, IBindingContextDelegator>();
	
	private IBindingContext getColContext(int col, IBindingContext scxt) 
	{
		IBindingContextDelegator cxt = colContexts.get(col);
		if (cxt == null)
		{
			cxt = makeColContext(col, scxt);
			colContexts.put(col, cxt);
		}	
		else cxt.setDelegate(scxt);
		
		return cxt;
	}



	private IBindingContextDelegator makeRowContext(int row, IBindingContext scxt) 
	{
		int w = spreadsheet.width();
		
		ModuleOpenClass rtype = new ModuleOpenClass(null, spreadsheet.getName()+ "RowType" + row, cxt.getOpenL());
		
		ModuleBindingContext rcxt = new ModuleBindingContext(scxt, rtype);
		
		for (int col = 0; col < w; col++) {
			SpreadsheetHeaderDefinition h = columnHeaders.get(col);
			SCell scell = spreadsheet.getCells()[row][col];
			for (SymbolicTypeDef coldef :  h.getVars()) {
				String fieldname = "$"+coldef.name.getIdentifier();
				rtype.addField(new SCellField(rtype, fieldname , scell));
			}
			
		}
		return rcxt;
	}

	private IBindingContextDelegator makeColContext(int col, IBindingContext scxt) 
	{
		int h = spreadsheet.height();
		
		ModuleOpenClass rtype = new ModuleOpenClass(null, spreadsheet.getName()+ "ColType" + col, cxt.getOpenL());
		
		ModuleBindingContext ccxt = new ModuleBindingContext(scxt, rtype);
		
		for (int row = 0; row < h; row++) {
			SpreadsheetHeaderDefinition rh = rowHeaders.get(row);
			SCell scell = spreadsheet.getCells()[row][col];
			for (SymbolicTypeDef rowdef :  rh.getVars()) {
				String fieldname = "$"+rowdef.name.getIdentifier();
				rtype.addField(new SCellField(rtype, fieldname , scell));
			}
			
		}
		return ccxt;
	}
	


	private IString2DataConvertor makeConvertor(IOpenClass type) {
		return String2DataConvertorFactory.getConvertor(type.getInstanceClass());
	}



	private IOpenMethodHeader makeHeader(String name, IOpenMethodHeader header,
			IOpenClass type) 
	{
		return new OpenMethodHeader(name, type, header.getSignature(), header.getDeclaringClass());
	}

	static final IOpenClass DEFAULT_CELL_TYPE = JavaOpenClass.getOpenClass(AnyCellValue.class);  

	private IOpenClass deriveCellType(SCell scell,
			ILogicalTable cell, SpreadsheetHeaderDefinition colHeader,
			SpreadsheetHeaderDefinition rowHeader, String cellvalue) 
	{
		if (colHeader.getType() != null)
			return colHeader.getType();
		
		else if (rowHeader.getType() != null)
			return rowHeader.getType();
		else
		{	
			try
			{
				if (CellLoader.isFormula(cellvalue))
					return JavaOpenClass.getOpenClass(DoubleValue.class);
				new String2DataConvertorFactory.String2DoubleConvertor().parse(cellvalue, null, null);
				return JavaOpenClass.getOpenClass(DoubleValue.class);
			}
			catch(Throwable t)
			{
				return JavaOpenClass.getOpenClass(StringValue.class);
			}
		}	
		
		
	}
	
	
	List<SCell> formulaCells = new ArrayList<SCell>(); 



	private void addRowNames(int row, ILogicalTable logicalRow) 
	{
		for (int i = 0; i < logicalRow.getLogicalWidth(); i++) 
		{
			IGridTable nameCell = logicalRow.getLogicalColumn(i).getGridTable();
			String value = nameCell.getStringValue(0, 0);
			if (value != null)
			{	
				String shortName = "srow" + row + "_" + i;
				StringValue sv =  new StringValue( value
					, shortName, null, nameCell.getUri()) ;
				addRowHeader(row, sv, i);
			}	
			
		}
	}
	
	private void addColumnNames(int col, ILogicalTable logicalCol) 
	{
		for (int i = 0; i < logicalCol.getLogicalHeight(); i++) 
		{
			IGridTable nameCell = logicalCol.getLogicalRow(i).getGridTable();
			String value = nameCell.getStringValue(0, 0);
			if (value != null)
			{	
				String shortName = "scol" + col + "_" + i;
				StringValue sv =  new StringValue( value
					, shortName, null, nameCell.getUri()) ;
				addColumnHeader(col, sv, i);
			}	
			
		}
	}
	
	

}
