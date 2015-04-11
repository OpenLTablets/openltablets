package org.openl.rules.ruleservice.databinding;

/*
 * #%L
 * OpenL - RuleService - RuleService - Web Services Databinding
 * %%
 * Copyright (C) 2013 - 2014 OpenL Tablets
 * %%
 * See the file LICENSE.txt for copying permission.
 * #L%
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openl.rules.ruleservice.databinding.jackson.Mixin;
import org.openl.rules.ruleservice.databinding.jackson.org.openl.rules.variation.ArgumentReplacementVariationType;
import org.openl.rules.ruleservice.databinding.jackson.org.openl.rules.variation.ComplexVariationType;
import org.openl.rules.ruleservice.databinding.jackson.org.openl.rules.variation.DeepCloningVariationType;
import org.openl.rules.ruleservice.databinding.jackson.org.openl.rules.variation.JXPathVariationType;
import org.openl.rules.ruleservice.databinding.jackson.org.openl.rules.variation.VariationType;
import org.openl.rules.ruleservice.databinding.jackson.org.openl.rules.variation.VariationsResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class JacksonObjectMapperFactoryBean {

    private final Logger log = LoggerFactory.getLogger(JacksonObjectMapperFactoryBean.class);

    private boolean supportVariations = false;

    private boolean enableDefaultTyping = false;

    private Set<String> overrideTypes;

    public ObjectMapper createJacksonObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        AnnotationIntrospector secondaryIntropsector = new JacksonAnnotationIntrospector();
        AnnotationIntrospector primaryIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        
        AnnotationIntrospector introspector = new AnnotationIntrospectorPair(primaryIntrospector, secondaryIntropsector);
        
        mapper.setAnnotationIntrospector(introspector);

        if (isEnableDefaultTyping()) {
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        } else {
            if (getOverrideTypes() != null) {
                List<Class<?>> clazzes = new ArrayList<Class<?>>();
                for (String className : getOverrideTypes()) {
                    try {
                        Class<?> clazz = loadClass(className);
                        clazzes.add(clazz);
                    } catch (ClassNotFoundException e) {
                        log.warn("Class \"{}\" not found!", className, e);
                    }
                }
                
                Iterator<Class<?>> itr = clazzes.iterator();
                while (itr.hasNext()) {
                    Class<?> clazz = itr.next();
                    Iterator<Class<?>> innerItr = clazzes.iterator();
                    while (innerItr.hasNext()) {
                        Class<?> c = innerItr.next();
                        if (!clazz.equals(c)) {
                            if (clazz.isAssignableFrom(c)) {
                                mapper.addMixInAnnotations(clazz, Mixin.class);
                                break;
                            }
                        }
                    }
                    mapper.registerSubtypes(clazz);
                }
            }
        }

        /*SimpleModule valueTypesModule = new SimpleModule("OpenL Value Types", Version.unknownVersion());
        // Value Types binding configuration
        valueTypesModule.addSerializer(ByteValue.class, new ByteValueSerializer());
        valueTypesModule.addSerializer(ShortValue.class, new ShortValueSerializer());
        valueTypesModule.addSerializer(IntValue.class, new IntValueSerializer());
        valueTypesModule.addSerializer(LongValue.class, new LongValueSerializer());
        valueTypesModule.addSerializer(FloatValue.class, new FloatValueSerializer());
        valueTypesModule.addSerializer(DoubleValue.class, new DoubleValueSerializer());
        valueTypesModule.addSerializer(BigIntegerValue.class, new BigIntegerValueSerializer());
        valueTypesModule.addSerializer(BigDecimalValue.class, new BigDecimalValueSerializer());
        valueTypesModule.addSerializer(StringValue.class, new StringValueSerializer());

        valueTypesModule.addDeserializer(ByteValue.class, new ByteValueDeserializer());
        valueTypesModule.addDeserializer(ShortValue.class, new ShortValueDeserializer());
        valueTypesModule.addDeserializer(IntValue.class, new IntValueDeserializer());
        valueTypesModule.addDeserializer(LongValue.class, new LongValueDeserializer());
        valueTypesModule.addDeserializer(FloatValue.class, new FloatValueDeserializer());
        valueTypesModule.addDeserializer(DoubleValue.class, new DoubleValueDeserializer());
        valueTypesModule.addDeserializer(BigIntegerValue.class, new BigIntegerValueDeserializer());
        valueTypesModule.addDeserializer(BigDecimalValue.class, new BigDecimalValueDeserializer());
        valueTypesModule.addDeserializer(StringValue.class, new StringValueDeserializer());

        mapper.registerModule(valueTypesModule);*/

        if (isSupportVariations()) {
            addMixInAnnotations(mapper, "org.openl.rules.variation.Variation", VariationType.class);
            addMixInAnnotations(mapper,
                "org.openl.rules.variation.ArgumentReplacementVariation",
                ArgumentReplacementVariationType.class);
            addMixInAnnotations(mapper, "org.openl.rules.variation.ComplexVariation", ComplexVariationType.class);
            addMixInAnnotations(mapper,
                "org.openl.rules.variation.DeepCloningVariation",
                DeepCloningVariationType.class);
            addMixInAnnotations(mapper, "org.openl.rules.variation.JXPathVariation", JXPathVariationType.class);
            addMixInAnnotations(mapper, "org.openl.rules.variation.VariationsResult", VariationsResultType.class);
        }

        /*mapper.addMixInAnnotations(SpreadsheetResult.class, SpreadSheetResultType.class);
        mapper.addMixInAnnotations(Point.class, PointType.class);
        mapper.addMixInAnnotations(DoubleRange.class, DoubleRangeType.class);
        mapper.addMixInAnnotations(IntRange.class, IntRangeType.class);*/

        /*mapper.addMixInAnnotations(IRulesRuntimeContext.class, IRulesRuntimeContextType.class);
        mapper.addMixInAnnotations(org.openl.rules.ruleservice.context.IRulesRuntimeContext.class,
            org.openl.rules.ruleservice.databinding.jackson.org.openl.rules.ruleservice.context.IRulesRuntimeContextType.class);*/

        return mapper;
    }

    private void addMixInAnnotations(ObjectMapper mapper, String className, Class<?> annotationClass) {
        try {
            mapper.addMixInAnnotations(loadClass(className), annotationClass);
        } catch (ClassNotFoundException e) {
            log.warn("Class \"{}\" not found!", className, e);
        }
    }

    private static Class<?> loadClass(String className) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(className);
    }

    public boolean isSupportVariations() {
        return supportVariations;
    }

    public void setSupportVariations(boolean supportVariations) {
        this.supportVariations = supportVariations;
    }

    public boolean isEnableDefaultTyping() {
        return enableDefaultTyping;
    }

    public void setEnableDefaultTyping(boolean enableDefaultTyping) {
        this.enableDefaultTyping = enableDefaultTyping;
    }

    public Set<String> getOverrideTypes() {
        return overrideTypes;
    }

    public void setOverrideTypes(Set<String> overrideTypes) {
        this.overrideTypes = overrideTypes;
    }
}
