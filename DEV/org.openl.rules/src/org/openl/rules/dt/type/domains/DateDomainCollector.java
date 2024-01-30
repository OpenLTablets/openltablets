package org.openl.rules.dt.type.domains;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openl.domain.DateRangeDomain;

public class DateDomainCollector implements IDomainCollector {

    private final Set<String> propertiesToSearch = new HashSet<>();

    private final List<Date> dateValues = new ArrayList<>();

    @Override
    public void gatherDomains(Map<String, Object> methodProperties) {
        if (methodProperties != null) {
            for (String propertyName : propertiesToSearch) {
                Date propValue = (Date) methodProperties.get(propertyName);
                if (propValue != null) {
                    dateValues.add(propValue);
                }
            }
        }
    }

    @Override
    public IDomainAdaptor getGatheredDomain() {
        if (!dateValues.isEmpty()) {
            Collections.sort(dateValues);
            DateRangeDomain domain = new DateRangeDomain(dateValues.get(0), dateValues.get(dateValues.size() - 1));
            return new DateRangeDomainAdaptor(domain);
        } else {
            return null;
        }
    }

    public void addPropertyToSearch(String propertyToSearch) {
        propertiesToSearch.add(propertyToSearch);
    }
}
