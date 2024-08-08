package org.openl.util;

import static java.util.Locale.ENGLISH;

import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * A util to manipulate with Java classes.
 *
 * @author Yury Molchan
 */
public final class ClassUtils {

    private ClassUtils() {
    }

    public static ClassLoader getCurrentClassLoader(Class<?> clazz) {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader.
        }

        // The following code is added to reduce the cases when something go wrong.
        // It is a candidate for deletion and refactoring, but not now.
        if (cl == null && clazz != null) {
            // Use a class loader of the given class.
            cl = clazz.getClassLoader();
        }
        if (cl == null) {
            // getClassLoader() returning null indicates the bootstrap ClassLoader
            // It is very exceptional case.
            cl = ClassLoader.getSystemClassLoader();
        }

        return cl;
    }

    /**
     * <p>
     * Converts the specified primitive Class object to its corresponding wrapper Class object.
     * </p>
     *
     * @param cls the class to convert, may be null
     * @return the wrapper class for {@code cls} or {@code cls} if {@code cls} is not a primitive. {@code null} if null
     * input.
     */
    public static Class<?> primitiveToWrapper(final Class<?> cls) {
        if (cls == null) {
            return null;
        } else if (!cls.isPrimitive()) {
            return cls;
        } else if (cls == Double.TYPE) {
            return Double.class;
        } else if (cls == Integer.TYPE) {
            return Integer.class;
        } else if (cls == Boolean.TYPE) {
            return Boolean.class;
        } else if (cls == Long.TYPE) {
            return Long.class;
        } else if (cls == Float.TYPE) {
            return Float.class;
        } else if (cls == Void.TYPE) {
            return Void.TYPE;
        } else if (cls == Character.TYPE) {
            return Character.class;
        } else if (cls == Short.TYPE) {
            return Short.class;
        } else if (cls == Byte.TYPE) {
            return Byte.class;
        }
        throw new IllegalStateException("No wrappers are for the primitive: " + cls);
    }

    /**
     * <p>
     * Converts the specified wrapper class to its corresponding primitive class.
     * </p>
     *
     * <p>
     * This method is the counter part of {@code primitiveToWrapper()}. If the passed in class is a wrapper class for a
     * primitive type, this primitive type will be returned (e.g. {@code Integer.TYPE} for {@code Integer.class}). For
     * other classes, or if the parameter is <b>null</b>, the return value is <b>null</b>.
     * </p>
     *
     * @param cls the class to convert, may be <b>null</b>
     * @return the corresponding primitive type if {@code cls} is a wrapper class, <b>null</b> otherwise
     */
    public static Class<?> wrapperToPrimitive(final Class<?> cls) {
        if (cls == Double.class) {
            return Double.TYPE;
        } else if (cls == Integer.class) {
            return Integer.TYPE;
        } else if (cls == Boolean.class) {
            return Boolean.TYPE;
        } else if (cls == Long.class) {
            return Long.TYPE;
        } else if (cls == Float.class) {
            return Float.TYPE;
        } else if (cls == Void.TYPE) {
            return Void.TYPE;
        } else if (cls == Character.class) {
            return Character.TYPE;
        } else if (cls == Short.class) {
            return Short.TYPE;
        } else if (cls == Byte.class) {
            return Byte.TYPE;
        }
        return null;
    }

    /**
     * <p>
     * Gets the class name minus the package name from a {@code Class}.
     * </p>
     *
     * <p>
     * Consider using the Java 5 API {@link Class#getSimpleName()} instead. The one known difference is that this code
     * will return {@code "Map.Entry"} while the {@code java.lang.Class} variant will simply return {@code "Entry"}.
     * </p>
     *
     * @param cls the class to get the short name for.
     * @return the class name without the package name or an empty string
     */
    public static String getShortClassName(final Class<?> cls) {
        if (cls == null) {
            return StringUtils.EMPTY;
        }
        String canonicalName = cls.getCanonicalName();
        if (canonicalName == null) {
            return StringUtils.EMPTY;
        }
        String packageName = getPackageName(cls);
        if (packageName.isEmpty()) {
            return canonicalName;
        }
        return canonicalName.substring(packageName.length() + 1);
    }

    /**
     * <p>
     * Gets the package name of a {@code Class}.
     * </p>
     *
     * @param cls the class to get the package name for, may be {@code null}.
     * @return the package name or an empty string
     */
    public static String getPackageName(final Class<?> cls) {
        if (cls == null) {
            return StringUtils.EMPTY;
        }
        String className = cls.getName();
        final int lastDot = className.lastIndexOf('.'); // package separator
        if (lastDot == -1) {
            // primitive or no package
            return StringUtils.EMPTY;
        }

        int arrayIndex = className.indexOf("[L"); // array encoding
        int start = arrayIndex == -1 ? 0 : (arrayIndex + 2);

        return className.substring(start, lastDot);
    }

    /**
     * <p>
     * Checks if one {@code Class} can be assigned to a variable of another {@code Class}.
     * </p>
     *
     * <p>
     * Unlike the {@link Class#isAssignableFrom(java.lang.Class)} method, this method takes into account widenings of
     * primitive classes and {@code null} s.
     * </p>
     *
     * <p>
     * Primitive widenings allow an int to be assigned to a long, float or double. This method returns the correct
     * result for these cases.
     * </p>
     *
     * <p>
     * {@code Null} may be assigned to any reference type. This method will return {@code true} if {@code null} is
     * passed in and the toClass is non-primitive.
     * </p>
     *
     * <p>
     * Specifically, this method tests whether the type represented by the specified {@code Class} parameter can be
     * converted to the type represented by this {@code Class} object via an identity conversion widening primitive or
     * widening reference conversion. See <em><a href="http://docs.oracle.com/javase/specs/">The Java Language
     * Specification</a></em> , sections 5.1.1, 5.1.2 and 5.1.4 for details.
     * </p>
     *
     * <p>
     *
     * @param cls     the Class to check, may be null
     * @param toClass the Class to try to assign into
     * @return {@code true} if assignment possible
     */
    public static boolean isAssignable(Class<?> cls, final Class<?> toClass) {
        if (cls == null && toClass == null) {
            return true;
        }
        if (toClass == null) {
            return !cls.isPrimitive();
        }
        // have to check for null, as isAssignableFrom does not
        if (cls == null) {
            return !toClass.isPrimitive();
        }
        if (toClass.isPrimitive() && !cls.isPrimitive()) {
            // autoboxing: unboxing
            cls = wrapperToPrimitive(cls);
            if (cls == null) {
                return false;
            }
        } else if (cls.isPrimitive() && !toClass.isPrimitive()) {
            // autoboxing: boxing
            cls = primitiveToWrapper(cls);
        }

        if (cls == toClass) {
            return true;
        }
        if (cls.isPrimitive()) {
            if (!toClass.isPrimitive()) {
                return false;
            }
            if (Integer.TYPE == cls) {
                return Long.TYPE == toClass || Float.TYPE == toClass || Double.TYPE == toClass;
            }
            if (Long.TYPE.equals(cls)) {
                return Float.TYPE == toClass || Double.TYPE == toClass;
            }
            if (Boolean.TYPE == cls) {
                return false;
            }
            if (Double.TYPE == cls) {
                return false;
            }
            if (Float.TYPE == cls) {
                return Double.TYPE == toClass;
            }
            if (Character.TYPE == cls || Short.TYPE == cls) {
                return Integer.TYPE == toClass
                        || Long.TYPE == toClass
                        || Float.TYPE == toClass
                        || Double.TYPE == toClass;
            }
            if (Byte.TYPE.equals(cls)) {
                return Short.TYPE == toClass
                        || Integer.TYPE == toClass
                        || Long.TYPE == toClass
                        || Float.TYPE == toClass
                        || Double.TYPE == toClass;
            }
            // should never get here
            return false;
        }
        return toClass.isAssignableFrom(cls);
    }

    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1))) {
            return name;
        } else if (Character.isUpperCase(name.charAt(0))) {
            return name;
        } else {
            // See java.beans.NameGenerator.capitalize
            return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
        }
    }

    public static String decapitalize(String name) {
        return Introspector.decapitalize(name);
    }

    public static String getter(String name) {
        return "get" + capitalize(name);
    }

    public static String setter(String name) {
        return "set" + capitalize(name);
    }

    public static String toFieldName(String name) {
        // remove get or set prefix and decapitalize
        return decapitalize(name.substring(3));
    }

    private static final Pattern PACKAGE_NAME = Pattern.compile(
            "(?!^abstract$|^abstract\\..*|.*\\.abstract\\..*|.*\\.abstract$|^assert$|^assert\\..*|.*\\.assert\\..*|.*\\.assert$|^boolean$|^boolean\\..*|.*\\.boolean\\..*|.*\\.boolean$|^break$|^break\\..*|.*\\.break\\..*|.*\\.break$|^byte$|^byte\\..*|.*\\.byte\\..*|.*\\.byte$|^case$|^case\\..*|.*\\.case\\..*|.*\\.case$|^catch$|^catch\\..*|.*\\.catch\\..*|.*\\.catch$|^char$|^char\\..*|.*\\.char\\..*|.*\\.char$|^class$|^class\\..*|.*\\.class\\..*|.*\\.class$|^const$|^const\\..*|.*\\.const\\..*|.*\\.const$|^continue$|^continue\\..*|.*\\.continue\\..*|.*\\.continue$|^default$|^default\\..*|.*\\.default\\..*|.*\\.default$|^do$|^do\\..*|.*\\.do\\..*|.*\\.do$|^double$|^double\\..*|.*\\.double\\..*|.*\\.double$|^else$|^else\\..*|.*\\.else\\..*|.*\\.else$|^enum$|^enum\\..*|.*\\.enum\\..*|.*\\.enum$|^extends$|^extends\\..*|.*\\.extends\\..*|.*\\.extends$|^final$|^final\\..*|.*\\.final\\..*|.*\\.final$|^finally$|^finally\\..*|.*\\.finally\\..*|.*\\.finally$|^float$|^float\\..*|.*\\.float\\..*|.*\\.float$|^for$|^for\\..*|.*\\.for\\..*|.*\\.for$|^goto$|^goto\\..*|.*\\.goto\\..*|.*\\.goto$|^if$|^if\\..*|.*\\.if\\..*|.*\\.if$|^implements$|^implements\\..*|.*\\.implements\\..*|.*\\.implements$|^import$|^import\\..*|.*\\.import\\..*|.*\\.import$|^instanceof$|^instanceof\\..*|.*\\.instanceof\\..*|.*\\.instanceof$|^int$|^int\\..*|.*\\.int\\..*|.*\\.int$|^interface$|^interface\\..*|.*\\.interface\\..*|.*\\.interface$|^long$|^long\\..*|.*\\.long\\..*|.*\\.long$|^native$|^native\\..*|.*\\.native\\..*|.*\\.native$|^new$|^new\\..*|.*\\.new\\..*|.*\\.new$|^package$|^package\\..*|.*\\.package\\..*|.*\\.package$|^private$|^private\\..*|.*\\.private\\..*|.*\\.private$|^protected$|^protected\\..*|.*\\.protected\\..*|.*\\.protected$|^public$|^public\\..*|.*\\.public\\..*|.*\\.public$|^return$|^return\\..*|.*\\.return\\..*|.*\\.return$|^short$|^short\\..*|.*\\.short\\..*|.*\\.short$|^static$|^static\\..*|.*\\.static\\..*|.*\\.static$|^strictfp$|^strictfp\\..*|.*\\.strictfp\\..*|.*\\.strictfp$|^super$|^super\\..*|.*\\.super\\..*|.*\\.super$|^switch$|^switch\\..*|.*\\.switch\\..*|.*\\.switch$|^synchronized$|^synchronized\\..*|.*\\.synchronized\\..*|.*\\.synchronized$|^this$|^this\\..*|.*\\.this\\..*|.*\\.this$|^throw$|^throw\\..*|.*\\.throw\\..*|.*\\.throw$|^throws$|^throws\\..*|.*\\.throws\\..*|.*\\.throws$|^transient$|^transient\\..*|.*\\.transient\\..*|.*\\.transient$|^try$|^try\\..*|.*\\.try\\..*|.*\\.try$|^void$|^void\\..*|.*\\.void\\..*|.*\\.void$|^volatile$|^volatile\\..*|.*\\.volatile\\..*|.*\\.volatile$|^while$|^while\\..*|.*\\.while\\..*|.*\\.while$)(^(?:[a-z_]+(?:\\d*[a-zA-Z_]*)*)(?:\\.[a-z_]+(?:\\d*[a-zA-Z_]*)*)*$)");

    public static boolean isValidPackageName(String packageName) {
        if (packageName == null) {
            return false;
        }
        return PACKAGE_NAME.matcher(packageName).matches();
    }

    public static void set(Object target, String fieldName, Object value) throws Exception {
        var clz = target.getClass();
        try {
            // Try direct access to the public fields
            clz.getField(fieldName).set(target, value);
            return;
        } catch (NoSuchFieldException ignore) {
            // Ignore attempt. No public field has been found.
        }

        Class<?> type = value != null ? value.getClass() : getType(target, fieldName);

        var setterName = setter(fieldName);
        Method setter = null;
        Method setter2 = null;

        var methods = clz.getMethods();
        for (var method : methods) {
            if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                var parameterType = method.getParameterTypes()[0];
                if (isAssignable(type, parameterType)) {
                    if (setter != null) {
                        var setterType = setter.getParameterTypes()[0];
                        if (isAssignable(parameterType, setterType)) {
                            setter = method;
                        } else if (!isAssignable(setterType, parameterType)) {
                            throw new IllegalArgumentException("Method '" + setterName + "(" + type + ")' is ambiguous in " + clz);
                        }
                    } else {
                        setter = method;
                    }
                }
                setter2 = method;
            }
        }
        if (setter == null) {
            setter = setter2;
        }

        if (setter == null) {
            throw new IllegalAccessException("Field '" + fieldName + "' is not accessible in class " + clz.getName());
        }

        try {
            setter.invoke(target, value);
        } catch (InvocationTargetException ex) {
            var exception = ex.getTargetException();
            if (exception instanceof Exception) {
                throw (Exception) exception;
            }
            throw ex;
        }
    }

    public static Object get(Object target, String fieldName) throws Exception {
        var clz = target.getClass();
        try {
            // Try direct access to the public fields
            return clz.getField(fieldName).get(target);
        } catch (NoSuchFieldException ignore) {
            // Ignore attempt. No public field has been found.
        }

        var getter = findGetterMethod(target, fieldName);

        if (getter == null) {
            throw new IllegalAccessException("Field '" + fieldName + "' is not accessible in class " + clz.getName());
        }

        try {
            return getter.invoke(target);
        } catch (InvocationTargetException ex) {
            var exception = ex.getTargetException();
            if (exception instanceof Exception) {
                throw (Exception) exception;
            }
            throw ex;
        }
    }

    public static Class<?> getType(Object target, String fieldName) {
        Class<?> type;
        try {
            type = target.getClass().getDeclaredField(fieldName).getType();
        } catch (NoSuchFieldException ignore) {
            var getterMethod = findGetterMethod(target, fieldName);
            type = getterMethod != null ? getterMethod.getReturnType() : null;
        }
        return type;
    }

    private static Method findGetterMethod(Object target, String fieldName) {
        try {
            // Try to find a getter method
            return target.getClass().getMethod(getter(fieldName));
        } catch (NoSuchMethodException ignore) {
            try {
                return target.getClass().getMethod("is" + capitalize(fieldName));
            } catch (NoSuchMethodException ex) {
                return null;
            }
        }
    }
}
