package org.openl.rules.testmethod;

import java.util.ArrayList;
import java.util.List;

import org.openl.rules.context.IRulesRuntimeContext;
import org.openl.types.IParameterDeclaration;

public final class TestUtils {

    public static ParameterWithValueDeclaration[] getContextParams(TestSuite test, TestDescription testCase) {
        List<ParameterWithValueDeclaration> params = new ArrayList<ParameterWithValueDeclaration>();

        TestSuiteMethod testMethod = test.getTestSuiteMethod();
        IRulesRuntimeContext context = testCase.getRuntimeContext();

        for (int i = 0; i < testMethod.getColumnsCount(); i++) {
            String columnName = testMethod.getColumnName(i);
            if (columnName != null && columnName.startsWith(TestMethodHelper.CONTEXT_NAME)) {

                Object value = context != null ? context.getValue(columnName.replace(TestMethodHelper.CONTEXT_NAME + ".", "")) : null;

                params.add(new ParameterWithValueDeclaration(columnName, value, IParameterDeclaration.IN));
            }
        }

        return params.toArray(new ParameterWithValueDeclaration[params.size()]);
    }

}
