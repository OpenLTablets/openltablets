package org.openl.rules.tbasic;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openl.rules.BaseOpenlBuilderHelper;

public class VoidReturnTest extends BaseOpenlBuilderHelper {
	private static final String str = "test/rules/tbasic0/VoidReturn.xls";
	
	public VoidReturnTest() {
		super(str);
	}
	
	@Test
	public void test() {
		assertEquals(1, getJavaWrapper().getCompiledClass().getBindingErrors().length);
		assertEquals("Can not convert from void to int", getJavaWrapper().getCompiledClass().getBindingErrors()[0].getMessage());
		assertTrue(getJavaWrapper().getCompiledClass().getParsingErrors().length == 0);
	}
}
