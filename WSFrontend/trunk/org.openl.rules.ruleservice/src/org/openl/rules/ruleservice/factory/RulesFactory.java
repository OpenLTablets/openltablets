package org.openl.rules.ruleservice.factory;

import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.core.ReflectUtils;

import org.apache.commons.lang.StringUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Type;
import org.openl.binding.impl.module.ModuleOpenClass.GetOpenClass;
import org.openl.binding.impl.module.ModuleOpenClass.ThisField;
import org.openl.rules.testmethod.TestSuiteMethod;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.IOpenMember;
import org.openl.types.IOpenMethod;
import org.openl.types.impl.ADynamicClass.OpenConstructor;
import org.openl.types.java.JavaOpenConstructor;

/**
 * The factory class that provides methods to generate interface class using
 * methods (rules) of IOpenClass.
 * 
 */
public class RulesFactory {

    private static final int PUBLIC_ABSTRACT_INTERFACE = Constants.ACC_PUBLIC + Constants.ACC_ABSTRACT + Constants.ACC_INTERFACE;
    private static final int PUBLIC_ABSTRACT = Constants.ACC_PUBLIC + Constants.ACC_ABSTRACT;
    private static final String JAVA_LANG_OBJECT = "java/lang/Object";

    /**
     * Generates interface class using collection of rules.
     * 
     * @param className name of result class
     * @param rules collection of rules what will be used as interface methods
     * @param classLoader class loader what will be used to load generated
     *            interface
     * @return generated interface
     * @throws Exception if an error has occurred
     */
    public static Class<?> generateInterface(String className, RuleInfo[] rules, ClassLoader classLoader) throws Exception {

        ClassWriter classWriter = new ClassWriter(false);
        
        String name = className.replace('.', '/');
        String sourceFileName = getClassFileName(name);
        
        classWriter.visit(Constants.V1_5,
            PUBLIC_ABSTRACT_INTERFACE,
            name,  
            JAVA_LANG_OBJECT,
            null,            
            sourceFileName); 

        for (RuleInfo ruleInfo : rules) {

            String ruleName = ruleInfo.getName();

            classWriter.visitMethod(PUBLIC_ABSTRACT,
                ruleName,
                getMethodTypes(ruleInfo),
                null,
                null);
        }

        classWriter.visitEnd();

        ReflectUtils.defineClass(className, classWriter.toByteArray(), classLoader);

        return Class.forName(className, true, classLoader);
    }

    /**
     * Generates interface class using methods and fields of given IOpenClass
     * instance.
     * 
     * @param className name of result class
     * @param openClass IOpenClass instance
     * @param classLoader class loader what will be used to load generated
     *            interface
     * @return generated interface
     * @throws Exception if an error has occured
     */
    public static Class<?> generateInterface(String className, IOpenClass openClass, ClassLoader classLoader) throws Exception {

        List<RuleInfo> rules = new ArrayList<RuleInfo>();
        IOpenMember[] members = OpenClassUtils.getClassMembers(openClass);

        for (IOpenMember member : members) {

            if (!isIgnoredMember(member)) {

                if (member instanceof IOpenMethod) {

                    IOpenMethod method = (IOpenMethod) member;
                    RuleInfo ruleInfo = getRuleInfoForMethod(method);
                    rules.add(ruleInfo);
                }

                if (member instanceof IOpenField) {

                    IOpenField field = (IOpenField) member;

                    if (field.isReadable()) {
                        RuleInfo ruleInfo = getRuleInfoForField(field);
                        rules.add(ruleInfo);
                    }
                }
            }
        }

        return generateInterface(className, rules.toArray(new RuleInfo[rules.size()]), classLoader);
    }

    /**
     * Gets rule information of IOpenField instance.
     * 
     * @param field IOpenField instance
     * @return rule info
     */
    private static RuleInfo getRuleInfoForField(IOpenField field) {

        String methodName = String.format("get%s", StringUtils.capitalize(field.getName()));
        Class<?>[] paramTypes = new Class<?>[0];
        Class<?> returnType = field.getType().getInstanceClass();

        RuleInfo ruleInfo = createRuleInfo(methodName, paramTypes, returnType);

        return ruleInfo;
    }

    /**
     * Gets rule information of IOpenMethod instance.
     * 
     * @param method IOpenMethod instance
     * @return rule info
     */
    private static RuleInfo getRuleInfoForMethod(IOpenMethod method) {

        IOpenClass[] paramClasses = method.getSignature().getParameterTypes();
        String methodName = method.getName();
        Class<?>[] paramTypes = OpenClassUtils.getInstanceClasses(paramClasses);
        Class<?> returnType = method.getType().getInstanceClass();

        RuleInfo ruleInfo = createRuleInfo(methodName, paramTypes, returnType);

        return ruleInfo;
    }

    /**
     * Creates rule info using rule name, parameters types and return type.
     * 
     * @param ruleName rule name
     * @param paramTypes parameters types
     * @param returnType return type
     * @return rule info
     */
    private static RuleInfo createRuleInfo(String ruleName, Class<?>[] paramTypes, Class<?> returnType) {

        RuleInfo ruleInfo = new RuleInfo();
        ruleInfo.setName(ruleName);
        ruleInfo.setParamTypes(paramTypes);
        ruleInfo.setReturnType(returnType);

        return ruleInfo;
    }

    /**
     * Checks that given member is ignored.
     * 
     * @param member member (method or field)
     * @return <code>true</code> - if member should be ignored (will be skipped
     *         due interface generation phase), <code>false</code> - otherwise
     */
    private static boolean isIgnoredMember(IOpenMember member) {
        return member instanceof OpenConstructor || 
                member instanceof JavaOpenConstructor || 
                member instanceof ThisField || 
                member instanceof GetOpenClass || 
                member instanceof TestSuiteMethod;
    }

    /**
     * Gets string that contains rule types (parameters types and return type).
     * 
     * @param ruleInfo rule info
     * @return string with rule types
     */
    private static String getMethodTypes(RuleInfo ruleInfo) {

        Class<?> returnType = ruleInfo.getReturnType();
        Class<?>[] paramTypes = ruleInfo.getParamTypes();

        StringBuilder builder = new StringBuilder("(");

        for (int i = 0; i < paramTypes.length; i++) {
            builder.append(Type.getType(paramTypes[i]));
        }

        builder.append(")");
        builder.append(Type.getType(returnType));

        return builder.toString();
    }

    private static String getClassFileName(String name) {
        
        String[] path = name.split("/");
        String className = path[path.length - 1];
        
        return String.format("%s.java", className);
    }
}
