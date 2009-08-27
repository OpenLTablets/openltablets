/**
 * Created Feb 28, 2007
 */
package org.openl.rules.table.xls;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openl.rules.table.ui.FormattedCell;
import org.openl.util.Log;

/**
 * @author snshor
 *
 */
public class XlsDateFormat extends XlsFormat {

    SimpleDateFormat format;

    /**
     *
     * @param fmt
     * @return
     */
    static public String convertTojavaFormat(String fmt) {
        // TODO this will require much more work than that

        return fmt.replace('m', 'M');
    }

    public XlsDateFormat(SimpleDateFormat fmt) {
        format = fmt;
    }

    public XlsDateFormat(String fmt) {
        String javaFormat = convertTojavaFormat(fmt);
        format = new SimpleDateFormat(javaFormat);
    }

    public FormattedCell filterFormat(FormattedCell cell) {
        if (cell.value == null) {
            return cell;
        }

        String fDate = format(cell.value);
        if (fDate == null) {
            return cell;
        }

        cell.content = fDate;
        cell.setFilter(this);

        return cell;
    }

    @Override
    public String format(Object value) {
        if (!(value instanceof Date)) {
            Log.error("Should be date" + value);
            return null;
        }
        Date date = (Date) value;
        String fDate = format.format(date);
        return fDate;
    }

    @Override
    public Object parse(String value) {
        try {
            return format.parse(value);
        } catch (ParseException e) {
            Log.warn("Could not parse Date: " + value, e);
            return value;
        }
    }

}
