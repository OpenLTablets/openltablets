package org.openl.rules.calc;

import org.openl.syntax.impl.IdentifierNode;

public class SpreadsheetHeaderField extends ASpreadsheetField {

//    private IdentifierNode name;
     SpreadsheetHeaderDefinition header;

    public SpreadsheetHeaderField(SpreadsheetOpenClass declaringClass,
            IdentifierNode name,
            SpreadsheetHeaderDefinition header) {

        super(declaringClass, name.getIdentifier(), header.getType());

//        this.name = name;
        this.header = header;
    }

//    @Override
//    public Object calculate(SpreadsheetResultCalculator spreadsheetResult, Object targetModule, Object[] params, IRuntimeEnv env) {
//        return header.getArray(spreadsheetResult, env);
//    }

}
