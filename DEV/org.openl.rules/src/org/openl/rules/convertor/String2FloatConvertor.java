package org.openl.rules.convertor;

import java.text.DecimalFormat;

class String2FloatConvertor extends String2NumberConverter<Float> {

    @Override
    public String format(Float data, String format) {
        if (data == null) return null;
        // Restore decimal precision
        double number = Double.parseDouble(Float.toString(data));
        return getFormatter(format).format(number);
    }

    @Override
    Float convert(Number number, String data) {
        float value = number.floatValue();
        double dValue = number.doubleValue();
        if (!Double.isInfinite(dValue) && Float.isInfinite(value)) {
            throw new NumberFormatException("A number \"" + data + "\" is out of range.");
        }
        return value;
    }

    @Override
    DecimalFormat getFormatter(String format) {
        DecimalFormat formatter = super.getFormatter(format);
        // Always show .0 at the end for integer numbers
        formatter.setMinimumFractionDigits(1);
        return formatter;
    }
}
