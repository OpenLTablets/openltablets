package org.openl.rules.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;

public class DatesTest {

    @Test
    public void testDate() {
        assertNull(Dates.toString(null));
        assertEquals(new Date(-1899, 0, 1), Dates.Date(1, 1, 1));
        assertEquals(new Date(118, 6, 12), Dates.Date(2018, 7, 12));
        assertEquals(new Date(80, 6, 12), Dates.Date(1980, 7, 12));
        assertEquals(new Date(-1820, 6, 12), Dates.Date(80, 7, 12));
        assertEquals(new Date(116, 1, 29), Dates.Date(2016, 2, 29));
        assertEquals(new Date(116, 1, 29, 11, 11, 11), Dates.Date(2016, 2, 29, 11, 11, 11));
    }

    @Test
    public void testDateWrongMonth() {
        assertNull(Dates.Date(2018, 13, 1));
    }

    @Test
    public void testDateWrongDay() {
        assertNull(Dates.Date(2018, 2, 29));
    }

    @Test
    public void testDateWrongYear() {
        assertNull(Dates.Date(0, 1, 1));
    }

    @Test
    public void testDateWrongHour() {
        assertNull(Dates.Date(2022, 1, 1, 24, 1, 1));
    }

    @Test
    public void testDateWrongMinute() {
        assertNull(Dates.Date(2022, 1, 1, 1, 60, 1));
    }

    @Test
    public void testDateWrongSecond() {
        assertNull(Dates.Date(2022, 1, 1, 1, 1, 60));
    }

    @Test
    public void testToString() {
        assertNull(Dates.toString(null));
        assertEquals("07/12/1980", Dates.toString(new Date(80, 6, 12, 23, 59)));
        assertEquals("07/12/1980", Dates.toString(new Date(80, 6, 12)));
        assertEquals("12.07.1980", Dates.toString(new Date(80, 6, 12, 23, 59), "dd.MM.yyyy"));
        assertEquals("12-Jul-1980", Dates.toString(new Date(80, 6, 12), "dd-MMM-yyyy"));
    }

    @Test
    public void testToDate() {
        assertNull(Dates.toDate(null));
        assertNull(Dates.toDate(""));
        assertNull(Dates.toDate(" "));
        assertNull(Dates.toDate("  \t  "));

        assertEquals(new Date(50, 11, 31), Dates.toDate("12/31/50"));
        assertEquals(new Date(80, 6, 12), Dates.toDate("7/12/80"));
        assertEquals(new Date(138, 6, 12), Dates.toDate("7/12/38"));
        assertEquals(new Date(-1820, 6, 12), Dates.toDate("7/12/080"));
        assertEquals(new Date(80, 6, 12), Dates.toDate("07/12/1980"));

        assertNull(Dates.toDate(null, null));
        assertNull(Dates.toDate("", ""));
        assertNull(Dates.toDate(" ", " "));
        assertNull(Dates.toDate("  \t  ", "  \t"));

        assertEquals(new Date(80, 6, 12), Dates.toDate("07/12/1980", null));
        assertEquals(new Date(80, 6, 12), Dates.toDate("07/12/1980", ""));
        assertEquals(new Date(80, 6, 12), Dates.toDate("07/12/1980", " "));
        assertEquals(new Date(80, 6, 12), Dates.toDate("07/12/1980", "  \t"));

        assertEquals(new Date(80, 6, 12), Dates.toDate("7/12/80", "MM/dd/yy"));
        assertEquals(new Date(-1820, 6, 12), Dates.toDate("7/12/80", "M/d/yyyy"));
        assertEquals(new Date(-1820, 6, 12), Dates.toDate("07/12/0080", "M/d/yyyy"));
        assertEquals(new Date(80, 6, 12), Dates.toDate("Date is: 12 Jul 1980 [+]", "'Date is: 'dd MMM yyyy"));
        assertEquals(new Date(80, 6, 12), Dates.toDate("Date is: 12 Jul 1980 year", "'Date is: 'dd MMM yyyy 'year'"));
        assertEquals(new Date(80, 0, 1), Dates.toDate("Date: 1980 year", "'Date: 'yyyy 'year'"));
        assertEquals(new Date(70, 0, 19), Dates.toDate("Date: 19 days from the 1th January 1970", "'Date: 'd 'days'"));

        assertNull(Dates.toDate("13/13/2013"));
        assertNull(Dates.toDate("12/12/2013", "a"));
    }

    private static final Date DEF_DATE = new Date();
    private static final Double ZERO_DOUBLE = 0.0d;

    @Test
    public void test_dateDif_shouldThrowIllegalArgumentException_whenUnitNameInUnknown() {
        assertThrows(IllegalArgumentException.class, () -> {
            Dates.dateDif(null, null, "SOME_NAME");
        });
    }

    @Test
    public void test_dateDif_shouldReturnNull_whenOneDateParameterIsNull() {
        assertNull(Dates.dateDif(null, DEF_DATE, "D"));
        assertNull(Dates.dateDif(DEF_DATE, null, "D"));
        assertNull(Dates.dateDif(null, DEF_DATE, "W"));
        assertNull(Dates.dateDif(DEF_DATE, null, "W"));
        assertNull(Dates.dateDif(null, DEF_DATE, "M"));
        assertNull(Dates.dateDif(DEF_DATE, null, "M"));
        assertNull(Dates.dateDif(null, DEF_DATE, "Y"));
        assertNull(Dates.dateDif(DEF_DATE, null, "Y"));
        assertNull(Dates.dateDif(null, DEF_DATE, "MD"));
        assertNull(Dates.dateDif(DEF_DATE, null, "MD"));
        assertNull(Dates.dateDif(null, DEF_DATE, "YD"));
        assertNull(Dates.dateDif(DEF_DATE, null, "YD"));
        assertNull(Dates.dateDif(null, DEF_DATE, "YM"));
        assertNull(Dates.dateDif(DEF_DATE, null, "YM"));
        assertNull(Dates.dateDif(null, DEF_DATE, "MF"));
        assertNull(Dates.dateDif(DEF_DATE, null, "MF"));
        assertNull(Dates.dateDif(null, DEF_DATE, "WF"));
        assertNull(Dates.dateDif(DEF_DATE, null, "WF"));
        assertNull(Dates.dateDif(null, DEF_DATE, "YF"));
        assertNull(Dates.dateDif(DEF_DATE, null, "YF"));
        assertNull(Dates.dateDif(null, DEF_DATE, "YMF"));
        assertNull(Dates.dateDif(DEF_DATE, null, "YMF"));
    }

    @Test
    public void test_dateDif_shouldReturnZero_whenDateParametersAreNull() {
        assertNull(Dates.dateDif(null, null, "D"));
        assertNull(Dates.dateDif(null, null, "W"));
        assertNull(Dates.dateDif(null, null, "M"));
        assertNull(Dates.dateDif(null, null, "Y"));
        assertNull(Dates.dateDif(null, null, "MD"));
        assertNull(Dates.dateDif(null, null, "YD"));
        assertNull(Dates.dateDif(null, null, "YM"));
        assertNull(Dates.dateDif(null, null, "MF"));
        assertNull(Dates.dateDif(null, null, "WF"));
        assertNull(Dates.dateDif(null, null, "YF"));
        assertNull(Dates.dateDif(null, null, "YMF"));
    }

    @Test
    public void test_dateDif_shouldReturnIntResult() {
        final Date start = getDate(28, 3, 2012);
        final Date end = getDate(29, 3, 2013);

        assertEquals(Double.valueOf(366), Dates.dateDif(start, end, "D"));
        assertEquals(Double.valueOf(52), Dates.dateDif(start, end, "W"));
        assertEquals(Double.valueOf(12), Dates.dateDif(start, end, "M"));
        assertEquals(Double.valueOf(1), Dates.dateDif(start, end, "Y"));
        assertEquals(Double.valueOf(1), Dates.dateDif(start, end, "MD"));
        assertEquals(Double.valueOf(1), Dates.dateDif(start, end, "YD"));
        assertEquals(ZERO_DOUBLE, Dates.dateDif(start, end, "YM"));

        assertEquals(Double.valueOf(10), Dates.dateDif(start, getDate(27, 2, 2013), "M"));
        assertEquals(Double.valueOf(2), Dates.dateDif(start, getDate(27, 1, 2015), "Y"));
        assertEquals(Double.valueOf(23), Dates.dateDif(start, getDate(20, 1, 2015), "MD"));
        assertEquals(Double.valueOf(298), Dates.dateDif(start, getDate(20, 1, 2015), "YD"));
        assertEquals(ZERO_DOUBLE, Dates.dateDif(getDate(31, 1, 2013), getDate(1, 3, 2013), "MD"));
    }

    @Test
    public void test_dateDif_shouldReturnNegativeIntResult() {
        final Date start = getDate(28, 3, 2012);
        final Date end = getDate(29, 3, 2013);

        assertEquals(Double.valueOf(-366), Dates.dateDif(end, start, "D"));
        assertEquals(Double.valueOf(-52), Dates.dateDif(end, start, "W"));
        assertEquals(Double.valueOf(-12), Dates.dateDif(end, start, "M"));
        assertEquals(Double.valueOf(-1), Dates.dateDif(end, start, "Y"));
        assertEquals(Double.valueOf(-1), Dates.dateDif(end, start, "MD"));
        assertEquals(Double.valueOf(-1), Dates.dateDif(end, start, "YD"));
        assertEquals(ZERO_DOUBLE, Dates.dateDif(end, start, "YM"));
    }

    @Test
    public void test_dateDif_shouldReturnFractionalResult() {
        final Date start = getDate(28, 3, 2012);
        final Date end = getDate(29, 3, 2013);

        assertEquals(Double.valueOf(52.285714285714285d), Dates.dateDif(start, end, "WF"));
        assertEquals(Double.valueOf(12.03225806451613d), Dates.dateDif(start, end, "MF"));
        assertEquals(Double.valueOf(1.0027397260273974d), Dates.dateDif(start, end, "YF"));
        assertEquals(Double.valueOf(0.03225806451612903d), Dates.dateDif(start, end, "YMF"));
        assertEquals(Double.valueOf(10.967741935483872), Dates.dateDif(start, getDate(27, 2, 2013), "MF"));
        assertEquals(Double.valueOf(2.8356164383561646), Dates.dateDif(start, getDate(27, 1, 2015), "YF"));
    }

    @Test
    public void test_dateDif_shouldReturnNegativeFractionalResult() {
        final Date start = getDate(28, 3, 2012);
        final Date end = getDate(29, 3, 2013);

        assertEquals(Double.valueOf(-52.285714285714285d), Dates.dateDif(end, start, "WF"));
        assertEquals(Double.valueOf(-12.03225806451613d), Dates.dateDif(end, start, "MF"));
        assertEquals(Double.valueOf(-1.0027397260273974d), Dates.dateDif(end, start, "YF"));
        assertEquals(Double.valueOf(-0.03225806451612903d), Dates.dateDif(end, start, "YMF"));
    }

    @Test
    public void testSetters() {
        Date date = getDate(1, 1, 2022);
        assertNull(Dates.setDate(date, 2022, 22, 1));
        assertNull(Dates.setDate(date, -111, 1, 1));
        assertNull(Dates.setDate(date, 2022, 2, 222));
        assertNull(Dates.setTime(date, 2022, 2, 1));
        assertNull(Dates.setTime(date, 2, 222, 1));
        assertNull(Dates.setTime(date, 2, 2, 122));
        assertNull(Dates.setTime(date, 2, 2, -1));
        assertNull(Dates.setTime(date, 2, 2, 1, 1000));
        assertNull(Dates.setTime(date, 2, 2, 1, -1));
        assertEquals(new Date(122, 1, 3), Dates.setDate(date, 2022, 2, 3));
        assertEquals(new Date(122, 0, 1, 11, 11, 11), Dates.setTime(date, 11, 11, 11));
        long l = new Date(122, 0, 1, 11, 11, 11).getTime() + 11;
        assertEquals(l, Dates.setTime(date, 11, 11, 11, 11).getTime());
    }

    @Test
    public void isLeap(){
        assertTrue(Dates.isLeap(Dates.Date(2016, 2, 29)));
        assertFalse(Dates.isLeap(Dates.Date(2017, 2, 28)));
        assertFalse(Dates.isLeap(Dates.Date(1900, 2, 28)));
        assertTrue(Dates.isLeap(Dates.Date(2000, 2, 28)));
    }

    private static Date getDate(int day, int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

}
