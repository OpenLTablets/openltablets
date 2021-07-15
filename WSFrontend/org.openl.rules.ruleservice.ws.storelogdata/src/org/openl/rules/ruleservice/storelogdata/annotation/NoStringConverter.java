package org.openl.rules.ruleservice.storelogdata.annotation;

import org.openl.rules.ruleservice.storelogdata.Converter;

public final class NoStringConverter implements Converter<String, Object> {
    @Override
    public Object apply(String value) {
        throw new UnsupportedOperationException();
    }
}
