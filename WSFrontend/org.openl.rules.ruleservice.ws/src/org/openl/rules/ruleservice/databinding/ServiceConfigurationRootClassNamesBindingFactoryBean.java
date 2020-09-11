package org.openl.rules.ruleservice.databinding;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlSeeAlso;

import org.openl.rules.calc.CustomSpreadsheetResultOpenClass;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.rules.lang.xls.types.DatatypeOpenClass;
import org.openl.rules.ruleservice.core.OpenLService;
import org.openl.rules.ruleservice.core.RuleServiceInstantiationException;
import org.openl.rules.ruleservice.databinding.annotation.JacksonBindingConfigurationUtils;
import org.openl.rules.variation.ArgumentReplacementVariation;
import org.openl.rules.variation.ComplexVariation;
import org.openl.rules.variation.DeepCloningVariation;
import org.openl.rules.variation.JXPathVariation;
import org.openl.rules.variation.NoVariation;
import org.openl.rules.variation.Variation;
import org.openl.rules.variation.VariationsResult;
import org.openl.types.IOpenClass;
import org.openl.util.ClassUtils;

public class ServiceConfigurationRootClassNamesBindingFactoryBean extends ServiceConfigurationFactoryBean<Set<String>> {
    private static final String ROOT_CLASS_NAMES_BINDING = "rootClassNamesBinding";

    private Set<String> defaultAdditionalRootClassNames;

    public void setDefaultAdditionalRootClassNames(Set<String> defaultAdditionalRootClassNames) {
        this.defaultAdditionalRootClassNames = Objects.requireNonNull(defaultAdditionalRootClassNames,
            "defaultAdditionalRootClassNames cannot be null");
    }

    public Set<String> getDefaultAdditionalRootClassNames() {
        return defaultAdditionalRootClassNames;
    }

    private Set<String> extractDatatypesClasses(IOpenClass moduleOpenClass) {
        Set<String> datatypeClasses = new HashSet<>();
        for (IOpenClass openClass : moduleOpenClass.getTypes()) {
            if (openClass instanceof DatatypeOpenClass) {
                datatypeClasses.add(openClass.getInstanceClass().getName());
            }
        }
        return datatypeClasses;
    }

    @Override
    protected Set<String> createInstance() throws Exception {
        Set<String> ret = new HashSet<>(getDefaultAdditionalRootClassNames());
        ret.addAll(fromServiceDescription());
        ret.addAll(fromOpenLService());
        ret.addAll(fromConfiguration());
        checkXmlSeeAlso(ret);
        return Collections.unmodifiableSet(ret);
    }

    private void checkXmlSeeAlso(Set<String> rootClassNames, Class<?> clazz) {
        if (!JacksonBindingConfigurationUtils.isConfiguration(clazz)) {
            XmlSeeAlso xmlSeeAlso = clazz.getAnnotation(XmlSeeAlso.class);
            if (xmlSeeAlso != null) {
                for (Class<?> cls : xmlSeeAlso.value()) {
                    rootClassNames.add(cls.getName());
                    checkXmlSeeAlso(rootClassNames, cls);
                }
            }
        }
    }

    private void checkXmlSeeAlso(Set<String> rootClassNames) throws ServiceConfigurationException {
        OpenLService openLService = getOpenLService();
        Set<String> tmp = new HashSet<>(rootClassNames);
        for (String className : tmp) {
            try {
                Class<?> cls = openLService.getClassLoader().loadClass(className);
                checkXmlSeeAlso(rootClassNames, cls);
            } catch (RuleServiceInstantiationException | ClassNotFoundException e) {
                throw new ServiceConfigurationException(e);
            }
        }
    }

    private Set<String> fromOpenLService() throws ServiceConfigurationException {
        OpenLService openLService = getOpenLService();

        boolean found = false;

        Set<String> ret = new HashSet<>();
        try {
            if (openLService.getOpenClass() != null) {
                for (IOpenClass type : openLService.getOpenClass().getTypes()) {
                    if (type instanceof CustomSpreadsheetResultOpenClass) {
                        CustomSpreadsheetResultOpenClass customSpreadsheetResultOpenClass = (CustomSpreadsheetResultOpenClass) type;
                        XlsModuleOpenClass module = (XlsModuleOpenClass) openLService.getOpenClass();
                        CustomSpreadsheetResultOpenClass csrt = (CustomSpreadsheetResultOpenClass) module
                            .findType(customSpreadsheetResultOpenClass.getName());
                        Class<?> beanClass = csrt.getBeanClass();
                        ret.add(beanClass.getName());
                        if (!found) {
                            for (Method method : openLService.getServiceClass().getMethods()) {
                                if (!found && ClassUtils.isAssignable(beanClass, method.getReturnType())) {
                                    found = true;
                                }
                            }
                        }
                    }
                }
                if (openLService.getOpenClass() instanceof XlsModuleOpenClass) {
                    XlsModuleOpenClass xlsModuleOpenClass = ((XlsModuleOpenClass) openLService.getOpenClass());
                    if (xlsModuleOpenClass.getSpreadsheetResultOpenClassWithResolvedFieldTypes() != null) {
                        Class<?> spreadsheetResultBeanClass = xlsModuleOpenClass
                            .getSpreadsheetResultOpenClassWithResolvedFieldTypes()
                            .toCustomSpreadsheetResultOpenClass()
                            .getBeanClass();
                        ret.add(spreadsheetResultBeanClass.getName());
                        if (!found) {
                            for (Method method : openLService.getServiceClass().getMethods()) {
                                if (!found && ClassUtils.isAssignable(spreadsheetResultBeanClass,
                                    method.getReturnType())) {
                                    found = true;
                                }
                            }
                        }
                    }
                }
                if (!found) {
                    ret.clear();
                }
                Set<String> datatypeClasses = extractDatatypesClasses(openLService.getOpenClass());
                if (!datatypeClasses.isEmpty()) {
                    found = true;
                }
                ret.addAll(datatypeClasses);
            }
        } catch (RuleServiceInstantiationException e) {
            throw new ServiceConfigurationException(e);
        }

        return found ? ret : Collections.emptySet();
    }

    private Set<String> fromConfiguration() throws ServiceConfigurationException {
        OpenLService openLService = getOpenLService();
        Set<String> ret = new HashSet<>();
        if (openLService.isProvideVariations()) {
            ret.add(Variation.class.getName());
            ret.add(NoVariation.class.getName());
            ret.add(ArgumentReplacementVariation.class.getName());
            ret.add(ComplexVariation.class.getName());
            ret.add(DeepCloningVariation.class.getName());
            ret.add(JXPathVariation.class.getName());
            ret.add(VariationsResult.class.getName());
        }

        return ret;
    }

    private Set<String> fromServiceDescription() throws Exception {
        Set<String> ret = new HashSet<>();
        Object value = getValue(ROOT_CLASS_NAMES_BINDING);
        if (value instanceof String) {
            StringBuilder classes = null;
            String v = (String) value;
            String[] rootClasses = v.split(",");
            for (String className : rootClasses) {
                if (className != null && className.trim().length() > 0) {
                    String trimmedClassName = className.trim();
                    ret.add(trimmedClassName);
                    if (classes == null) {
                        classes = new StringBuilder();
                    } else {
                        classes.append(", ");
                    }
                    classes.append(trimmedClassName);
                }
            }
            return ret;
        } else {
            if (value != null) {
                throw new ServiceConfigurationException(
                    String.format("Expected string value for '%s' in the configuration for service '%s'.",
                        ROOT_CLASS_NAMES_BINDING,
                        getServiceDescription().getName()));
            }
        }
        return ret;
    }

    @Override
    public Class<?> getObjectType() {
        return Set.class;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.defaultAdditionalRootClassNames == null) {
            this.defaultAdditionalRootClassNames = new HashSet<>();
        }
    }
}
