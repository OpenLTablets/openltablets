package org.openl.rules.convertor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class String2DateConvertor implements IString2DataConvertor<Date> {

    private static final int YEAR_START_COUNT = 1900;
    private final Logger log = LoggerFactory.getLogger(String2DateConvertor.class);

    @Override
    public String format(Date data, String format) {
        if (data == null) return null;
        DateFormat df = format == null ? DateFormat.getDateInstance(DateFormat.SHORT) : new SimpleDateFormat(format);
        return df.format(data);
    }

    @Override
    public Date parse(String data, String format) {
        if (data == null) return null;
        if (data.length() == 0) throw new IllegalArgumentException("Cannot convert an empty String to date type");

        DateFormat df;
        if (format == null) {
            df = DateFormat.getDateInstance(DateFormat.SHORT, LocaleDependConvertor.getLocale());
        } else {
            df = new SimpleDateFormat(format, LocaleDependConvertor.getLocale());
        }
        df.setLenient(false);

        try {
            return df.parse(data);
        } catch (ParseException e) {
            try {
                int value = Integer.parseInt(data);
                Calendar cc = Calendar.getInstance();
                cc.set(YEAR_START_COUNT, 0, 1);
                cc.add(Calendar.DATE, value - 1);
                return cc.getTime();

            } catch (NumberFormatException t) {
                log.debug(t.getMessage(), t);
            }
            throw new IllegalArgumentException("Cannot convert \"" + data + "\" to date type");
        }
    }
}
