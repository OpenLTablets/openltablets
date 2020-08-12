package org.openl.rules.ruleservice.publish.jaxrs.swagger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverters;
import io.swagger.jackson.ModelResolver;

@SuppressWarnings("rawtypes")
public class SwaggerObjectMapperHack {
    private final List converters;
    private List<Object> oldConverters;

    public SwaggerObjectMapperHack() {
        try {
            ModelConverters modelConverters = ModelConverters.getInstance();
            Field convertersField = ModelConverters.class.getDeclaredField("converters");
            convertersField.setAccessible(true);
            this.converters = (List) convertersField.get(modelConverters);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void apply(ObjectMapper objectMapper) {
        List<ModelConverter> hackedConverters = new ArrayList<>();
        hackedConverters.add(new SwaggerSupportConverter());
        oldConverters = new ArrayList<>();
        for (Object converter : converters) {
            oldConverters.add(converter);
            hackedConverters
                .add(converter instanceof ModelResolver ? new ModelResolver(objectMapper) : (ModelConverter) converter);
        }
        converters.clear();
        SwaggerInheritanceFixConverter swaggerInheritanceFixConverter = new SwaggerInheritanceFixConverter(objectMapper,
            hackedConverters);
        converters.addAll(Collections.singleton(swaggerInheritanceFixConverter));
    }

    @SuppressWarnings("unchecked")
    public void revert() {
        converters.clear();
        if (oldConverters != null) {
            converters.addAll(oldConverters);
        }
    }
}
