package org.openl.spring.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

import org.openl.util.StringUtils;

class EnableCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        MultiValueMap<String, Object> attrs = metadata.getAllAnnotationAttributes(ConditionalOnEnable.class.getName());
        if (attrs != null) {
            for (Object value : attrs.get("value")) {
                String[] properties = (String[]) value;
                for (String property : properties) {
                    String propValue = context.getEnvironment().getProperty(property);
                    if ("false".equalsIgnoreCase(propValue) || StringUtils.isBlank(propValue)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
