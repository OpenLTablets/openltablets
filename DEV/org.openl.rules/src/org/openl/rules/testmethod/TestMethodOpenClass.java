package org.openl.rules.testmethod;

import org.openl.rules.context.DefaultRulesRuntimeContext;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.IOpenMethod;
import org.openl.types.impl.ADynamicClass;
import org.openl.types.impl.DynamicObject;
import org.openl.types.impl.DynamicObjectField;
import org.openl.types.java.JavaOpenClass;
import org.openl.vm.IRuntimeEnv;

public class TestMethodOpenClass extends ADynamicClass {

    public TestMethodOpenClass(String tableName, IOpenMethod testedMethod) {

        super(null, tableName + "TestClass", DynamicObject.class);

        init(testedMethod);
    }

    public Object newInstance(IRuntimeEnv env) {
        return new DynamicObject(this);
    }

    protected void init(IOpenMethod testedMethod) {
        addParameterFields(testedMethod);

        addExpectedResult(testedMethod);

        addDescription();

        addContext();
        
        addExpectedError();
    }

    protected void addParameterFields(IOpenMethod testedMethod) {
        IOpenClass[] parameterTypes = testedMethod.getSignature().getParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++) {
            String name = testedMethod.getSignature().getParameterName(i);
            IOpenField parameterField = new DynamicObjectField(this, name, parameterTypes[i]);

            addField(parameterField);
        }
    }

    protected void addExpectedError() {
        IOpenField errorField = new DynamicObjectField(this,
            TestMethodHelper.EXPECTED_ERROR,
            JavaOpenClass.STRING);
        addField(errorField);
    }

    protected void addContext() {
        IOpenField contextField = new DynamicObjectField(this,
            TestMethodHelper.CONTEXT_NAME,
            JavaOpenClass.getOpenClass(DefaultRulesRuntimeContext.class));
        addField(contextField);
    }

    protected void addDescription() {
        IOpenField descriptionField = new DynamicObjectField(this,
            TestMethodHelper.DESCRIPTION_NAME,
            JavaOpenClass.STRING);
        addField(descriptionField);
    }

    protected void addExpectedResult(IOpenMethod testedMethod) {
        IOpenField resultField = new DynamicObjectField(this,
            TestMethodHelper.EXPECTED_RESULT_NAME,
            testedMethod.getType());
        addField(resultField);
    }
}
