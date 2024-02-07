package org.openl.util.formatters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openl.util.BooleanUtils;

public class BooleanFormatter implements IFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(BooleanFormatter.class);

    @Override
    public String format(Object value) {
        if (!(value instanceof Boolean)) {
            LOG.debug("Should be Boolean: {}", value);
            return null;
        }

        Boolean bool = (Boolean) value;
        return bool.toString();
    }

    @Override
    public Object parse(String value) {
        Boolean boolValue = BooleanUtils.toBooleanObject(value);
        if (boolValue == null) {
            LOG.debug("Could not parse Boolean: {}", value);
        }
        return boolValue;
    }

}
