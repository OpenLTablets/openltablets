package org.openl.rules.helpers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import org.openl.binding.impl.cast.CastFactory;
import org.openl.meta.LongValue;
import org.openl.rules.helpers.ARangeParser.ParseStruct;
import org.openl.rules.helpers.ARangeParser.ParseStruct.BoundType;

public class DateRange {

    private static final int TO_DATE_RANGE_CAST_DISTANCE = CastFactory.AFTER_FIRST_WAVE_CASTS_DISTANCE + 8;

    private final long lowerBound;
    private final long upperBound;

    private final BoundType lowerBoundType;
    private final BoundType upperBoundType;

    public DateRange(Date bound) {
        this(bound, bound, BoundType.INCLUDING, BoundType.INCLUDING);
    }

    public DateRange(Date lowerBound, Date upperBound) {
        this(lowerBound, upperBound, BoundType.INCLUDING, BoundType.INCLUDING);
    }

    DateRange(Date lowerBound, Date upperBound, BoundType lowerBoundType, BoundType upperBoundType) {
        assert lowerBound != null;
        assert upperBound != null;
        this.lowerBoundType = lowerBoundType;
        this.upperBoundType = upperBoundType;
        long localLowerBound = lowerBound.getTime();
        if (this.lowerBoundType == BoundType.EXCLUDING) {
            localLowerBound += 1;
        }
        long localUpperBound = upperBound.getTime();
        if (this.upperBoundType == BoundType.EXCLUDING) {
            localUpperBound -= 1;
        }
        this.lowerBound = localLowerBound;
        this.upperBound = localUpperBound;
    }

    public DateRange(String source) {
        ParseStruct<Long> range = DateRangeParser.getInstance().parse(source);
        this.lowerBoundType = range.leftBoundType;
        this.upperBoundType = range.rightBoundType;

        Long lowerBound = range.min;
        if (this.lowerBoundType == BoundType.EXCLUDING) {
            lowerBound += 1;
        }
        Long upperBound = range.max;
        if (this.upperBoundType == BoundType.EXCLUDING) {
            upperBound -= 1;
        }
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException(String.format("%s must be greater or equal than %s", range.max, range.min));
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public Long getLowerBound() {
        return lowerBound;
    }

    public Long getUpperBound() {
        return upperBound;
    }

    public BoundType getLowerBoundType() {
        return lowerBoundType;
    }

    public BoundType getUpperBoundType() {
        return upperBoundType;
    }

    public boolean contains(Date d) {
        if (d == null) {
            return false;
        }
        long x = d.getTime();
        return lowerBound <= x && x <= upperBound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DateRange dateRange = (DateRange) o;
        return lowerBound == dateRange.lowerBound && upperBound == dateRange.upperBound && lowerBoundType == dateRange.lowerBoundType && upperBoundType == dateRange.upperBoundType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerBound, upperBound, lowerBoundType, upperBoundType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (Long.MIN_VALUE == lowerBound) {
            sb.append(upperBoundType == BoundType.INCLUDING ? "<= " : "< ");
            sb.append(DateRangeParser.dateTimeFormatter.format(getDateUpperBound()));
        } else if (Long.MAX_VALUE == upperBound) {
            sb.append(lowerBoundType == BoundType.INCLUDING ? ">= " : "> ");
            sb.append(DateRangeParser.dateTimeFormatter.format(getDateLowerBound()));
        } else {
            sb.append(lowerBoundType == BoundType.INCLUDING ? '[' : '(');
            sb.append(DateRangeParser.dateTimeFormatter.format(getDateLowerBound()));
            sb.append("; ");
            sb.append(DateRangeParser.dateTimeFormatter.format(getDateUpperBound()));
            sb.append(upperBoundType == BoundType.INCLUDING ? ']' : ')');
        }
        return sb.toString();
    }

    private LocalDateTime getDateUpperBound() {
        long time = upperBound;
        if (this.upperBoundType == BoundType.EXCLUDING) {
            time += 1;
        }
        return Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private LocalDateTime getDateLowerBound() {
        long time = lowerBound;
        if (this.lowerBoundType == BoundType.EXCLUDING) {
            time -= 1;
        }
        return Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // AUTOCAST METHODS
    public static DateRange autocast(Date x, DateRange y) {
        return new DateRange(x);
    }

    public static int distance(Date x, DateRange y) {
        return TO_DATE_RANGE_CAST_DISTANCE;
    }

    public static DateRange autocast(Calendar x, DateRange y) {
        return new DateRange(x.getTime());
    }

    public static int distance(Calendar x, DateRange y) {
        return TO_DATE_RANGE_CAST_DISTANCE;
    }

    public static DateRange autocast(long x, DateRange y) {
        return new DateRange(new Date(x));
    }

    public static int distance(long x, DateRange y) {
        return TO_DATE_RANGE_CAST_DISTANCE;
    }

    // END
    // CAST METHODS
    public static DateRange cast(LongValue x, DateRange y) {
        return new DateRange(new Date(x.longValue()));
    }

    public static int distance(LongValue x, DateRange y) {
        return TO_DATE_RANGE_CAST_DISTANCE;
    }
    // END

}
