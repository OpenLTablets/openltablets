package org.openl.rules.ruleservice.databinding.jackson.org.openl.meta;

/*
 * #%L
 * OpenL - RuleService - RuleService - Web Services Databinding
 * %%
 * Copyright (C) 2013 - 2014 OpenL Tablets
 * %%
 * See the file LICENSE.txt for copying permission.
 * #L%
 */


import java.io.IOException;

import org.openl.meta.ByteValue;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

public class ByteValueType {
    public static class ByteValueSerializer extends StdScalarSerializer<ByteValue> {
        public ByteValueSerializer() {
            super(ByteValue.class);
        }

        @Override
        public void serialize(ByteValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
                                                                                               JsonGenerationException {
            jgen.writeNumber(value.getValue());
        }
    }

    @SuppressWarnings("serial")
    public static class ByteValueDeserializer extends StdScalarDeserializer<ByteValue> {

        public ByteValueDeserializer() {
            super(ByteValue.class);
        }

        @Override
        public ByteValue deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
                                                                                JsonProcessingException {
            Byte value = _parseByte(jp, ctxt);
            if (value == null) {
                return null;
            }
            return new ByteValue(value);
        }
    }
}
