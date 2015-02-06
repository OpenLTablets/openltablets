/*
 * Created on Nov 3, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.convertor;

import org.openl.binding.IBindingContext;
import org.openl.classloader.OpenLBundleClassLoader;
import org.openl.meta.DoubleValue;
import org.openl.rules.helpers.DoubleRange;
import org.openl.rules.helpers.IntRange;
import org.openl.types.IOpenClass;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * @author snshor
 */
public class String2DataConvertorFactory {

    /**
     * Strong reference to common converters
     */
    private static HashMap<Class<?>, IString2DataConvertor> convertors;

    private static Map<Class<?>, IString2DataConvertor> convertorsCache = new WeakHashMap<Class<?>, IString2DataConvertor>();
    static ThreadLocal<IBindingContext> threadBindingContext = new ThreadLocal<IBindingContext>();

    static {
        convertors = new HashMap<Class<?>, IString2DataConvertor>();
        convertors.put(Object.class, new String2StringConvertor());
        convertors.put(int.class, new String2IntConvertor());
        convertors.put(double.class, new String2DoubleConvertor());
        convertors.put(char.class, new String2CharConvertor());
        convertors.put(boolean.class, new String2BooleanConvertor());
        convertors.put(long.class, new String2LongConvertor());
        convertors.put(byte.class, new String2ByteConvertor());
        convertors.put(short.class, new String2ShortConvertor());
        convertors.put(float.class, new String2FloatConvertor());

        convertors.put(Integer.class, new String2IntConvertor());
        convertors.put(Byte.class, new String2ByteConvertor());
        convertors.put(Short.class, new String2ShortConvertor());
        convertors.put(Float.class, new String2FloatConvertor());
        convertors.put(Double.class, new String2DoubleConvertor());
        convertors.put(Character.class, new String2CharConvertor());
        convertors.put(Boolean.class, new String2BooleanConvertor());
        convertors.put(Long.class, new String2LongConvertor());

        convertors.put(String.class, new String2StringConvertor());
        convertors.put(Date.class, new String2DateConvertor());
        convertors.put(Calendar.class, new String2CalendarConvertor());
        convertors.put(Class.class, new String2ClassConvertor());
        convertors.put(IOpenClass.class, new String2OpenClassConvertor());
        convertors.put(DoubleValue.class, new String2DoubleValueConvertor());
        convertors.put(IntRange.class, new String2IntRangeConvertor());
        convertors.put(DoubleRange.class, new String2DoubleRangeConvertor());
        convertors.put(BigInteger.class, new String2BigIntegerConvertor());
        convertors.put(BigDecimal.class, new String2BigDecimalConvertor());

        convertorsCache.putAll(convertors);
    }

    public static synchronized <T> T parse(Class<T> clazz, String data, IBindingContext bindingContext) {
        // Share binding context to the String2ClassConvertor and String2OpenClassConvertor
        threadBindingContext.set(bindingContext);
        IString2DataConvertor<T> convertor = getConvertor(clazz);
        T parsedValue = convertor.parse(data, null);
        threadBindingContext.remove();
        return parsedValue;
    }

    public static synchronized <T> IString2DataConvertor<T> getConvertor(Class<T> clazz) {

        IString2DataConvertor<T> convertor = convertorsCache.get(clazz);

        if (convertor != null) {
            return convertor;
        }

        // FIXME String2EnumConvertor and String2ConstructorConvertor hold strong reference 
        // to Class, so classloader for them can't be unloaded without unregisterClassLoader() method.
        if (clazz.isEnum()) {
            convertor = new String2EnumConvertor(clazz);
        } else if (clazz.isArray()) {
            Class<?> componentType = clazz.getComponentType();
            convertor = new String2ArrayConvertor(componentType);
        } else {
            convertor = new String2ConstructorConvertor<T>(clazz);
        }

        convertorsCache.put(clazz, convertor);

        return convertor;
    }

    /**
     * Removes the specified Class from convertors cache.
     *
     * @param clazz Class to unregister.
     */
    private static void unregisterConvertorForClass(Class<?> clazz) {
        convertorsCache.remove(clazz);
    }

    /**
     * Unregister all Classes from the specified class loader.
     *
     * @param classLoader ClassLoader to unregister.
     */
    public static synchronized void unregisterClassLoader(ClassLoader classLoader) {
        List<Class<?>> toRemove = new ArrayList<Class<?>>();
        for (Class<?> clazz : convertorsCache.keySet()) {
            if (convertors.containsKey(clazz)) {
                // Don't remove common converters
                continue;
            }
            ClassLoader cl = clazz.getClassLoader();
            if (cl == classLoader) {
                toRemove.add(clazz);
            }
            if (classLoader instanceof OpenLBundleClassLoader) {
                if (((OpenLBundleClassLoader) classLoader).containsClassLoader(cl)) {
                    toRemove.add(clazz);
                }
            }
        }
        for (Class<?> clazz : toRemove) {
            unregisterConvertorForClass(clazz);
        }
    }
}
