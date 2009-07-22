package org.openl.codegen;

import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.IOpenMember;
import org.openl.types.IOpenMethod;
import org.openl.util.ISelector;

public interface ICodeGen 
{

	StringBuilder genModuleStart(IOpenClass ioc, StringBuilder sb);
	StringBuilder genModuleEnd(IOpenClass ioc, StringBuilder sb);

	
	StringBuilder genClass(IOpenClass ioc, ISelector<IOpenMember> sel, StringBuilder sb);
	StringBuilder genClassStart(IOpenClass ioc,  StringBuilder sb);
	StringBuilder genClassEnd(IOpenClass ioc,  StringBuilder sb);

	StringBuilder genMethod(IOpenMethod m, StringBuilder sb);
	StringBuilder genMethodStart(IOpenMethod m, StringBuilder sb);
	StringBuilder genMethodEnd(IOpenMethod m, StringBuilder sb);
	
	StringBuilder genField(IOpenField m, StringBuilder sb);
	StringBuilder genAttribute(IOpenField m, StringBuilder sb);

	StringBuilder genLiteralString(String src, StringBuilder sb);
	StringBuilder genLiteralInt(Integer src, StringBuilder sb);
	StringBuilder genLiteralDouble(Double src, int dprecision, StringBuilder sb);
	StringBuilder genLiteralChar(Character src, StringBuilder sb);
	StringBuilder genLiteralBool(Boolean src, StringBuilder sb);

	StringBuilder genLiteralArray(Object ary,  StringBuilder sb);

	
	StringBuilder genMultiLineComment(String comment,  StringBuilder sb);
	StringBuilder genSingleLineComment(String comment,  StringBuilder sb);
	
	
}
