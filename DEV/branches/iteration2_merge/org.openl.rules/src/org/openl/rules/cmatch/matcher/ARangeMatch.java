package org.openl.rules.cmatch.matcher;

import org.openl.rules.data.IString2DataConvertor;
import org.openl.rules.data.String2DataConvertorFactory;

public abstract class ARangeMatch implements IMatcher {
    protected abstract Class<?> getDirectClass();

    protected abstract Class<?> getRangeClass();

    public Object fromString(String checkValue) {
        if (checkValue.length() == 0)
            return null;

        RuntimeException directParseException = null;
        try {
            IString2DataConvertor convertor = String2DataConvertorFactory.getConvertor(getDirectClass());
            return convertor.parse(checkValue, null, null);
        } catch (RuntimeException e) {
            directParseException = e;
        }

        try {
            IString2DataConvertor convertor = String2DataConvertorFactory.getConvertor(getRangeClass());
            return convertor.parse(checkValue, null, null);
        } catch (Exception e) {
            // throw exception from direct parsing
            throw directParseException;
        }
    }

    public String getName() {
        return OP_MATCH;
    }

}
