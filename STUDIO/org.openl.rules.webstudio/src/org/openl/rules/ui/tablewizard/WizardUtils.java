package org.openl.rules.ui.tablewizard;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openl.base.INamedThing;
import org.openl.main.OpenLWrapper;
import org.openl.rules.lang.xls.classes.ClassFinder;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.lang.xls.syntax.WorkbookSyntaxNode;
import org.openl.rules.lang.xls.syntax.XlsModuleSyntaxNode;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.java.JavaOpenClass;

/**
 * @author Aliaksandr Antonik.
 */
public class WizardUtils {
    private static final Pattern REGEXP_PARAMETER = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");

    public static void autoRename(Collection<? extends TableArtifact> conditions, String prefix) {
        int i = 0;
        for (TableArtifact c : conditions) {
            c.setName(prefix + ++i);
        }
    }

    public static String checkParameterName(String name) {
        if (StringUtils.isEmpty(name)) {
            return "Parameter name can not be empty";
        }

        if (!isValidParameter(name)) {
            return "Invalid name for parameter";
        }

        return null;
    }

    public static IOpenClass getProjectOpenClass() {
        return WebStudioUtils.getProjectModel().getCompiledOpenClass().getOpenClassWithErrors();
    }

    public static WorkbookSyntaxNode[] getWorkbookNodes() {
        return WebStudioUtils.getProjectModel().getWorkbookNodes();
    }

    public static XlsModuleSyntaxNode getXlsModuleNode() {
        return WebStudioUtils.getProjectModel().getXlsModuleNode();
    }

    public static TableSyntaxNode[] getTableSyntaxNodes() {
        return WebStudioUtils.getProjectModel().getTableSyntaxNodes();
    }

    /**
     * Checks a string to be a valid parameter name
     *
     * @param s String to check, must not be <code>null</code>
     * @return if <code>s</code> is a valid parameter name.
     */
    public static boolean isValidParameter(String s) {
        return REGEXP_PARAMETER.matcher(s).matches();
    }
    
    /**
     * Get imported classes for current project
     * 
     * @return collection, containing an imported classes
     */
    public static Collection<IOpenClass> getImportedClasses() {
        Set<IOpenClass> classes = new TreeSet<IOpenClass>(new Comparator<IOpenClass>() {

            @Override
            public int compare(IOpenClass o1, IOpenClass o2) {
                return o1.getDisplayName(INamedThing.SHORT).compareToIgnoreCase(o2.getDisplayName(INamedThing.SHORT));
            }
        });        
        
        ClassFinder finder = new ClassFinder();
        for (String packageName : getXlsModuleNode().getAllImports()) {
            if ("org.openl.rules.enumeration".equals(packageName)) {
                // This package is added automatically in XlsLoader.addInnerImports() for inner usage, not for user.
                continue;
            }
            ClassLoader classLoader = WebStudioUtils.getProjectModel().getCompiledOpenClass().getClassLoader();
            for (Class<?> type : finder.getClasses(packageName, classLoader)) {
                IOpenClass openType = JavaOpenClass.getOpenClass(type);
                if (!isValid(openType))
                    continue;
                
                classes.add(openType);
            }
        }
                
        return classes;
    }

    /**
     * Check if type is valid (for example, it can be used in a DataType tables,
     * Data tables etc)
     * 
     * @param openType checked type
     * @return true if class is valid.
     */
    private static boolean isValid(IOpenClass openType) {
        Class<?> instanceClass = openType.getInstanceClass();

        int modifiers = instanceClass.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
            return false;
        }

        if (OpenLWrapper.class.isAssignableFrom(instanceClass)) {
            // generated class for tutorial for example.
            return false;
        }

        Map<String, IOpenField> fields = openType.getFields();
        // Every field has a "class" field. We skip a classes that doesn't
        // have any other field.
        return fields.size() > 1;

    }
}
