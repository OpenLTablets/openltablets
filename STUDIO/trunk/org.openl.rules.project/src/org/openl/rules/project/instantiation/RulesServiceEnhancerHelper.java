package org.openl.rules.project.instantiation;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.core.ReflectUtils;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.openl.rules.context.IRulesRuntimeContext;
import org.openl.rules.runtime.RuleInfo;
import org.openl.rules.runtime.RulesFactory;

public abstract class RulesServiceEnhancerHelper {

    /**
     * Suffix of enhanced class name.
     */
    private static final String UNDECORATED_CLASS_NAME_SUFFIX = "$Undecorated";
    /**
     * Suffix of enhanced class name.
     */
    private static final String ENHANCED_CLASS_NAME_SUFFIX = "$RulesEnhanced";

    public static boolean isEnhancedClass(Class<?> rulesInterface) {
        for (Method method : rulesInterface.getMethods()) {
            if (method.getParameterTypes().length == 0 || method.getParameterTypes()[0] != IRulesRuntimeContext.class) {
                return false;
            }
        }
        return true;
    }

    /**
     * Undecorates methods signatures of given clazz.
     * 
     * @param clazz class to undecorate
     * @param classLoader The classloader where generated class should be
     *            placed.
     * @return new class with undecorated methods signatures: removed
     *         {@link IRulesRuntimeContext} as the first parameter for each
     *         method.
     * @throws Exception
     */
    public static Class<?> undecorateMethods(Class<?> clazz, ClassLoader classLoader) throws Exception {
    	final Log log = LogFactory.getLog(RulesServiceEnhancerHelper.class);
    	
        String className = clazz.getName() + UNDECORATED_CLASS_NAME_SUFFIX;

        log.debug(String.format("Generating proxy interface without runtime context for '%s' class", clazz.getName()));

        return undecorateInterface(className, clazz, classLoader);
    }
    
    private static InputStream getClassAsStream(Class<?> clazz, ClassLoader classLoader){
        String name = clazz.getName().replace('.', '/') + ".class";
        return clazz.getClassLoader().getResourceAsStream(name);
    }

    private static Class<?> undecorateInterface(String className, Class<?> original, ClassLoader classLoader) throws Exception {

        ClassWriter classWriter = new ClassWriter(0);
        ClassVisitor classVisitor = new UndecoratingClassWriter(0, classWriter, className);
        ClassReader classReader = new ClassReader(getClassAsStream(original, classLoader));
        classReader.accept(classVisitor, 0);
        // classWriter.visitEnd();

        // Create class object.
        //
        ReflectUtils.defineClass(className, classWriter.toByteArray(), classLoader);

        // Return loaded to classpath class object.
        //
        return Class.forName(className, true, classLoader);
    }

    /**
     * TODO: replace with a configurable implementation
     * 
     * 
     * Check that method should be ignored by enhancer.
     * 
     * @param method method to check
     * @return <code>true</code> if method should be ignored; <code>false</code>
     *         - otherwise
     */
    private static boolean isIgnored(Method method) {
        // Ignore methods what are inherited from Object.class
        // Note that ignored inherited methods only.
        return ArrayUtils.contains(Object.class.getMethods(), method);
    }

    /**
     * Decorates methods signatures of given clazz.
     * 
     * @param clazz class to decorate
     * @param classLoader The classloader where generated class should be
     *            placed.
     * @return new class with decorated methods signatures: added
     *         {@link IRulesRuntimeContext} as the first parameter for each
     *         method.
     * @throws Exception
     */
    public static Class<?> decorateMethods(Class<?> clazz, ClassLoader classLoader) throws Exception {
    	final Log log = LogFactory.getLog(RulesServiceEnhancerHelper.class);
    	
        Method[] methods = clazz.getMethods();
        List<RuleInfo> rules = getRulesDecorated(methods);

        String className = clazz.getName() + ENHANCED_CLASS_NAME_SUFFIX;
        RuleInfo[] rulesArray = rules.toArray(new RuleInfo[rules.size()]);

        log.debug(String.format("Generating proxy interface for '%s' class", clazz.getName()));

        return RulesFactory.generateInterface(className, rulesArray, classLoader);
    }

    /**
     * Gets list of rules.
     * 
     * @param methods array of methods what represents rule methods
     * @return list of rules meta-info
     */
    private static List<RuleInfo> getRulesDecorated(Method[] methods) {

        List<RuleInfo> rules = new ArrayList<RuleInfo>(methods.length);

        for (Method method : methods) {

            // Check that method should be ignored or not.
            if (isIgnored(method)) {
                continue;
            }

            String methodName = method.getName();

            Class<?>[] paramTypes = method.getParameterTypes();
            Class<?> returnType = method.getReturnType();
            Class<?>[] newParams = new Class<?>[] { IRulesRuntimeContext.class };
            Class<?>[] extendedParamTypes = (Class<?>[]) ArrayUtils.addAll(newParams, paramTypes);

            RuleInfo ruleInfo = RulesFactory.createRuleInfo(methodName, extendedParamTypes, returnType);

            rules.add(ruleInfo);
        }

        return rules;
    }

    /**
     * {@link ClassWriter} for creation undecorated class: for removing
     * {@link IRulesRuntimeContext} from signature of each method.
     * 
     * @author PUdalau
     */
    private static class UndecoratingClassWriter extends ClassVisitor {

        private static final String RUNTIME_CONTEXT = "Lorg/openl/rules/context/IRulesRuntimeContext;";
        private String className;

        public UndecoratingClassWriter(int arg0, ClassVisitor delegatedClassVisitor, String className) {
            super(arg0, delegatedClassVisitor);
            this.className = className;
        }

        @Override
        public void visit(int arg0, int arg1, String arg2, String arg3, String arg4, String[] arg5) {
            super.visit(arg0, arg1, className.replace('.', '/'), arg3, arg4, arg5);
        }

        @Override
        public MethodVisitor visitMethod(int arg0, String arg1, String arg2, String arg3, String[] arg4) {
            return super.visitMethod(arg0, arg1, removeRuntimeContextFromSignature(arg2), arg3, arg4);
        }

        private String removeRuntimeContextFromSignature(String signature) {
            if (signature.startsWith("(" + RUNTIME_CONTEXT)) {
                return "(" + signature.substring(RUNTIME_CONTEXT.length() + 1);
            } else {
                throw new RuntimeException("wrong signature!");
            }
        }
    }
}
