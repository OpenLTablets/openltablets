package org.openl.rules.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import org.openl.rules.runtime.RulesEngineFactory;

public class EmptyArrayDataTest {

    private static final String SRC = "test/rules/data/EmptyArrayTest.xls";

    @Test
    public void testMultiRowArrayLoad() throws Exception {
        RulesEngineFactory<Object> engineFactory = new RulesEngineFactory<>(SRC);
        engineFactory.setExecutionMode(true);

        Object instance = engineFactory.newEngineInstance();

        Class<?> clazz = engineFactory.getInterfaceClass();

        Method getMyDatasMethod = clazz.getMethod("getMyDatas");

        Class<?> policyClazz = engineFactory.getCompiledOpenClass()
            .getClassLoader()
            .loadClass("org.openl.generated.beans.MyData");

        Object[] myDatas = (Object[]) getMyDatasMethod.invoke(instance);

        assertEquals(2, myDatas.length);

        Method getStringsMethod = policyClazz.getMethod("getStrings");

        String[] strings1 = (String[]) getStringsMethod.invoke(myDatas[0]);

        assertEquals(3, strings1.length);
        assertEquals("28", strings1[2]);

        String[] strings2 = (String[]) getStringsMethod.invoke(myDatas[1]);
        assertEquals(0, strings2.length);

        Method getPrimitivesMethod = policyClazz.getMethod("getPrimitives");

        long[] primitives = (long[]) getPrimitivesMethod.invoke(myDatas[0]);
        assertEquals(0, primitives.length);

        Method getPrimitives2Method = policyClazz.getMethod("getPrimitives2");

        int[][] primitives2 = (int[][]) getPrimitives2Method.invoke(myDatas[0]);
        assertEquals(0, primitives2.length);
    }
}