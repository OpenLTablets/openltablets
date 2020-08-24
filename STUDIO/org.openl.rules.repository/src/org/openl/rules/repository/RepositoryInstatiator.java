package org.openl.rules.repository;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.openl.rules.repository.api.Repository;
import org.openl.rules.repository.exceptions.RRepositoryException;
import org.openl.util.ClassUtils;
import org.openl.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertyResolver;

/**
 * A factory to create repositories using Java reflection. This instantiator uses the following workflow:
 * <ol>
 * <li>Create a repository instance, using the default constructor</li>
 * <li>Check the instance on implementing {@link Repository} interface</li>
 * <li>Set all parameters using set-methods like Java beans. These methods must apply one String argument. The order of
 * method invocation is undefined. Blank parameters are skipped.</li>
 * <li>Invoke initialize() method</li>
 * <li></li>
 * </ol>
 *
 * @author Yury Molchan
 */
public class RepositoryInstatiator {

    public static final String REPOSITORY_PREFIX = "repository.";

    private static Logger log() {
        return LoggerFactory.getLogger(RepositoryInstatiator.class);
    }

    /**
     * Create new repository instance.
     *
     * @param configName the name of the configuration, e.g. design, deploy-config or production like.
     * @param propertyResolver the propertyResolver of the app.
     * @return the initialized repository.
     */
    public static Repository newRepository(String configName,
            PropertyResolver propertyResolver) throws RRepositoryException {
        String factoryClass = propertyResolver.getProperty(REPOSITORY_PREFIX + configName + ".factory");
        try {
            Repository repository = newInstance(factoryClass);
            setParams(repository, propertyResolver, configName);
            initialize(repository);
            return repository;
        } catch (Exception e) {
            String message = "Failed to initialize repository: " + configName;
            log().error(message, e);
            throw new RRepositoryException(message, e);
        }
    }

    /**
     * Create new repository instance.
     *
     * @param factory the class name to instantiate.
     * @param params the initialization parameters.
     * @return the initialized repository.
     */
    public static Repository newRepository(String factory, Map<String, String> params) {
        Repository repository = newInstance(factory);
        if (params != null) {
            setParams(repository, params);
        }
        initialize(repository);

        return repository;
    }

    private static Repository newInstance(String factory) {
        Object instance;
        try {
            // Instantiate a repository
            Class<?> clazz = Class.forName(factory);
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate a repository: " + factory, e);
        } catch (UnsupportedClassVersionError e) {
            throw new IllegalStateException("Library is compiled using newer version of JDK.", e);
        }
        try {
            return (Repository) instance;
        } catch (ClassCastException e) {
            throw new IllegalStateException(String.format("%s must be an implementation of %s.",
                instance.getClass().getTypeName(),
                Repository.class.getTypeName()), e);
        }
    }

    private static void setParams(Object instance, Map<String, String> params) {
        Class<?> clazz = instance.getClass();
        for (Map.Entry<String, String> param : params.entrySet()) {
            String value = param.getValue();
            if (StringUtils.isNotBlank(value)) {
                String name = param.getKey();
                injectValue(instance, clazz, value, name);
            }
        }
    }

    private static void injectValue(Object instance, Class<?> clazz, String value, String fieldName) {
        String setter = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        try {
            Method setMethod = clazz.getMethod(setter, String.class);
            setMethod.invoke(instance, value);
        } catch (NoSuchMethodException e) {
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(setter)) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 1) {
                        try {
                            method.invoke(instance, convert(parameterTypes[0], value));
                            // Found needed setter
                            break;
                        } catch (NoSuchMethodException | IllegalAccessException ignore) {
                            // Cannot convert using this method. Skip.
                        } catch (InvocationTargetException e1) {
                            // The underlying method throws an exception
                            throw new IllegalStateException(
                                "Failed to invoke " + setter + "(" + parameterTypes[0]
                                    .getSimpleName() + ") method in: " + clazz,
                                e1);
                        }
                    }
                }
            }
            // Didn't find setter, skip this param. For example not always exists setUri(String).
        } catch (Exception e) {
            throw new IllegalStateException(
                String.format("Failed to invoke method '%s.%s(String)'.", clazz.getTypeName(), fieldName),
                e);
        }
    }

    private static void setParams(Object instance,
            PropertyResolver propertyResolver,
            String configName) {
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            String fieldName = field.getName();
            if ("id".equals(fieldName)) {
                if (StringUtils.isNotBlank(configName)) {
                    injectValue(instance, clazz, configName, fieldName);
                }
                continue;
            }
            String propertyName = buildPropertyName(configName, fieldName);
            String propertyValue = propertyResolver.getProperty(propertyName);
            boolean propertyExists = StringUtils.isNotBlank(propertyValue);
            if (propertyExists) {
                injectValue(instance, clazz, propertyValue, fieldName);
            }
        }

    }

    private static String buildPropertyName(String configName, String name) {
        return REPOSITORY_PREFIX + configName + "." + toPropertiesCase(name);
    }

    /**
     * Convert field names from camelCase("paramName") style to "param-name" style
     *
     * @param key camelCased parameter name
     * @return hyphen-cased parameter name
     */
    private static String toPropertiesCase(String key) {
        StringBuilder sb = new StringBuilder();
        char[] chars = key.toCharArray();
        for (char aChar : chars) {
            char c = aChar;
            if (Character.isUpperCase(c)) {
                c = Character.toLowerCase(c);
                sb.append("-").append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static Object convert(Class<?> parameterType,
            String value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (parameterType.isPrimitive()) {
            parameterType = ClassUtils.primitiveToWrapper(parameterType);
        }
        Method valueOfMethod = parameterType.getMethod("valueOf", String.class);
        return valueOfMethod.invoke(null, value);
    }

    private static void initialize(Object instance) {
        Class<?> clazz = instance.getClass();
        try {
            // Try to find initialize() method
            Method initMethod = clazz.getMethod("initialize");
            // Execute initialize() method to finish instantiation of the
            // repository.
            initMethod.invoke(instance);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(String.format("Failed on method '%s.initialize()' call.", clazz), e);
        }
    }
}
