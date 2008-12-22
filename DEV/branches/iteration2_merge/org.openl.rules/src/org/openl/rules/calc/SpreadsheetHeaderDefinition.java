package org.openl.rules.calc;

import java.util.ArrayList;
import java.util.List;

import org.openl.rules.calc.SpreadsheetBuilder.SymbolicTypeDef;
import org.openl.types.IOpenClass;
import org.openl.vm.IRuntimeEnv;

public class SpreadsheetHeaderDefinition 
{
	
	IOpenClass type = null; 
	
	int row;
	int column;
	
	List<SymbolicTypeDef> vars = new ArrayList<SymbolicTypeDef>();

	public SpreadsheetHeaderDefinition(int row, int col) {
		this.row = row;
		this.column = col;
	}

	public void addVarHeader(SymbolicTypeDef parsed) 
	{
		vars.add(parsed);
	}

	public IOpenClass getType() {
		return type;
	}

	public void setType(IOpenClass type) {
		this.type = type;
	}

	public Object getArray(SpreadsheetResult target, IRuntimeEnv env) {
		if (row < 0)
			return target.getColumn(column, env);
		else
			return target.getRow(row, env);
	}

	public List<SymbolicTypeDef> getVars() {
		return vars;
	}

	public void setVars(List<SymbolicTypeDef> vars) {
		this.vars = vars;
	}


}
