package org.openl.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.apache.commons.lang.time.DateUtils;

/**
 * Domain for range of dates.
 * 
 * @author PUdalau
 */
public class DateRangeDomain extends FixedSizeDomain<Date> {
    private class DateIterator implements Iterator<Date> {
        private Calendar current;

        DateIterator() {
            current = (Calendar) min.clone();
            current.add(Calendar.DAY_OF_MONTH, -1);
        }

        public boolean hasNext() {
            return current.before(max);
        }

        public Date next() {
            current.add(Calendar.DAY_OF_MONTH, 1);
            return current.getTime();
        }

        public void remove() {
        }
    }

    private Calendar min = new GregorianCalendar();
    private Calendar max = new GregorianCalendar();

    /**
     * Creates date range inside the specified bounds(including bounds).
     * 
     * @param min left bound.
     * @param max right bound.
     */
    public DateRangeDomain(Date min, Date max) {
        setMin(min);
        setMax(max);
    }

    /**
     * Creates date range within interval:
     * <code>[currentDate - yearsPerioud;currentDate + yearsPerioud]</code>
     */
    public DateRangeDomain(int yearsPeriond) {
        this(min(yearsPeriond), max(yearsPeriond));
    }

    private static Date min(int yearsPeriond) {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.YEAR, -yearsPeriond);
        return date.getTime();
    }

    private static Date max(int yearsPeriond) {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.YEAR, yearsPeriond);
        return date.getTime();
    }

    /**
     * @return The left bound of range.
     */
    public Date getMin() {
        return min.getTime();
    }

    /**
     * @return The right bound of range.
     */
    public Date getMax() {
        return max.getTime();
    }

    /**
     * Sets left bound of range.
     */
    public void setMin(Date min) {
        this.min.setTime(DateUtils.truncate(min, Calendar.DAY_OF_MONTH));
    }

    /**
     * Sets right bound of range.
     */
    public void setMax(Date max) {
        this.max.setTime(DateUtils.truncate(max, Calendar.DAY_OF_MONTH));
    }

    public Iterator<Date> iterator() {
        return new DateIterator();
    }

    private static final long ONE_HOUR = 60 * 60 * 1000L;

    public static long daysBetween(Calendar d1, Calendar d2) {
        return ((d2.getTimeInMillis() - d1.getTimeInMillis() + ONE_HOUR) / (ONE_HOUR * 24));
    }

    public int size() {
        return (int) daysBetween(min, max) + 1;
    }

    public IType getElementType() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean selectObject(Date obj) {
        return obj.before(max.getTime()) && obj.after(min.getTime());
    }

    public boolean selectType(IType type) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param value Date to get index.
     * @return The index of specified date or negative number if specified date
     *         does not belong to the range.
     */
    public int getIndex(Date value) {
        if (value.after(getMax())) {
            return -1;
        }

        Calendar date = new GregorianCalendar();
        date.setTime((Date) value);
        int index = (int) daysBetween(min, date);
        return index;
    }

    /**
     * @param index Index of the date.
     * @return Returns The date within the range or <code>null</code> if date
     *         with specified index does not belong to the range.
     */
    public Date getValue(int index) {
        if (index >= size()) {
            return null;
        }
        Calendar date = (Calendar) min.clone();
        date.add(Calendar.DATE, index);
        return date.getTime();
    }

    @Override
    public String toString() {
        return "[" + getMin() + ";" + getMax() + "]";
    }
}
