package org.openl.rules.dt.type;

import java.util.Date;

import org.openl.domain.DateRangeDomain;

import com.exigen.ie.constrainer.IntVar;

/**
 * Adaptor for date ranges. Helps to access dates in range by index and retrieve
 * index of date within the range.
 * 
 * @author PUdalau
 * 
 */
public class DateRangeDomainAdaptor implements IDomainAdaptor {
    private DateRangeDomain domain;

    public DateRangeDomainAdaptor(DateRangeDomain domain) {
        this.domain = domain;
    }

    public int getIndex(Object value) {
        return domain.getIndex((Date) value);
    }

    public int getIntVarDomainType() {
        return IntVar.DOMAIN_PLAIN;
    }

    public int getMax() {
        return domain.getIndex(domain.getMax());
    }

    public int getMin() {
        return 0;
    }

    public Object getValue(int index) {
        return domain.getValue(index);
    }

}
