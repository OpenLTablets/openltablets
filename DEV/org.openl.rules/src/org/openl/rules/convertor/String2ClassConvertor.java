package org.openl.rules.convertor;

import org.openl.binding.IBindingContext;
import org.openl.syntax.impl.ISyntaxConstants;
import org.openl.types.IOpenClass;

import java.lang.reflect.Array;

class String2ClassConvertor implements IString2DataConvertor<Class<?>> {

    public static final String ARRAY_SUFIX = "[]";

    @Override
    public String format(Class<?> data, String format) {
        if (data == null) return null;
        return data.toString();
    }

    @Override
    public Class<?> parse(String data, String format) {
        if (data == null) return null;

        String typeName;
        if (data.endsWith(ARRAY_SUFIX)) {
            typeName = data.substring(0, data.length() - 2);
        } else {
            typeName = data;
        }

        IBindingContext cxt = String2DataConvertorFactory.threadBindingContext.get();
        IOpenClass openClass = cxt.findType(ISyntaxConstants.THIS_NAMESPACE, typeName);

        if (openClass == null) {
            throw new IllegalArgumentException("Type " + data + " is not found");
        }

        Class<?> clazz = openClass.getInstanceClass();
        if (data.endsWith(ARRAY_SUFIX)) {
            clazz = Array.newInstance(clazz, 0).getClass();
        }
        return clazz;
    }
}
