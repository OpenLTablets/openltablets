package org.openl.rules.types.impl;

import java.util.HashMap;
import java.util.Map;

import org.openl.rules.table.properties.ITableProperties;

public class DefaultPropertiesIntersectionFinder {

    private Map<String, IntersectionConstraint<?>> constraints = new HashMap<String, IntersectionConstraint<?>>();

    public DefaultPropertiesIntersectionFinder() {
        initilaize();
    }

    public IntersectionType match(String propName, ITableProperties props1, ITableProperties props2) {
        IntersectionConstraint<?> mc = constraints.get(propName);

        if (mc == null)
            throw new RuntimeException("Unexpectedly could not find a constarint for the property: " + propName);

        return mc.match(props1, props2);
    }

    protected void initilaize() {
// <<< INSERT >>>
        constraints.put("effectiveDate", new IntersectionConstraint<java.util.Date>() { 

            @Override
            protected java.util.Date getPropertyValue(ITableProperties properties) {
                return properties.getEffectiveDate();
            }

            @Override
            protected IntersectionType matchNotNulls(java.util.Date firstValue, java.util.Date secondValue) {
                return intersectionForLE(firstValue, secondValue);
            }
        });
        constraints.put("expirationDate", new IntersectionConstraint<java.util.Date>() { 

            @Override
            protected java.util.Date getPropertyValue(ITableProperties properties) {
                return properties.getExpirationDate();
            }

            @Override
            protected IntersectionType matchNotNulls(java.util.Date firstValue, java.util.Date secondValue) {
                return intersectionForGE(firstValue, secondValue);
            }
        });
        constraints.put("startRequestDate", new IntersectionConstraint<java.util.Date>() { 

            @Override
            protected java.util.Date getPropertyValue(ITableProperties properties) {
                return properties.getStartRequestDate();
            }

            @Override
            protected IntersectionType matchNotNulls(java.util.Date firstValue, java.util.Date secondValue) {
                return intersectionForLE(firstValue, secondValue);
            }
        });
        constraints.put("endRequestDate", new IntersectionConstraint<java.util.Date>() { 

            @Override
            protected java.util.Date getPropertyValue(ITableProperties properties) {
                return properties.getEndRequestDate();
            }

            @Override
            protected IntersectionType matchNotNulls(java.util.Date firstValue, java.util.Date secondValue) {
                return intersectionForGE(firstValue, secondValue);
            }
        });
        constraints.put("lob", new IntersectionConstraint<java.lang.String>() { 

            @Override
            protected java.lang.String getPropertyValue(ITableProperties properties) {
                return properties.getLob();
            }

            @Override
            protected IntersectionType matchNotNulls(java.lang.String firstValue, java.lang.String secondValue) {
                return intersectionForEQ(firstValue, secondValue);
            }
        });
        constraints.put("usregion", new IntersectionConstraint<org.openl.rules.enumeration.UsRegionsEnum[]>() { 

            @Override
            protected org.openl.rules.enumeration.UsRegionsEnum[] getPropertyValue(ITableProperties properties) {
                return properties.getUsregion();
            }

            @Override
            protected IntersectionType matchNotNulls(org.openl.rules.enumeration.UsRegionsEnum[] firstValue, org.openl.rules.enumeration.UsRegionsEnum[] secondValue) {
                return intersectionForCONTAINS(firstValue, secondValue);
            }
        });
        constraints.put("country", new IntersectionConstraint<org.openl.rules.enumeration.CountriesEnum[]>() { 

            @Override
            protected org.openl.rules.enumeration.CountriesEnum[] getPropertyValue(ITableProperties properties) {
                return properties.getCountry();
            }

            @Override
            protected IntersectionType matchNotNulls(org.openl.rules.enumeration.CountriesEnum[] firstValue, org.openl.rules.enumeration.CountriesEnum[] secondValue) {
                return intersectionForCONTAINS(firstValue, secondValue);
            }
        });
        constraints.put("currency", new IntersectionConstraint<org.openl.rules.enumeration.CurrenciesEnum[]>() { 

            @Override
            protected org.openl.rules.enumeration.CurrenciesEnum[] getPropertyValue(ITableProperties properties) {
                return properties.getCurrency();
            }

            @Override
            protected IntersectionType matchNotNulls(org.openl.rules.enumeration.CurrenciesEnum[] firstValue, org.openl.rules.enumeration.CurrenciesEnum[] secondValue) {
                return intersectionForCONTAINS(firstValue, secondValue);
            }
        });
        constraints.put("lang", new IntersectionConstraint<org.openl.rules.enumeration.LanguagesEnum[]>() { 

            @Override
            protected org.openl.rules.enumeration.LanguagesEnum[] getPropertyValue(ITableProperties properties) {
                return properties.getLang();
            }

            @Override
            protected IntersectionType matchNotNulls(org.openl.rules.enumeration.LanguagesEnum[] firstValue, org.openl.rules.enumeration.LanguagesEnum[] secondValue) {
                return intersectionForCONTAINS(firstValue, secondValue);
            }
        });
        constraints.put("state", new IntersectionConstraint<org.openl.rules.enumeration.UsStatesEnum[]>() { 

            @Override
            protected org.openl.rules.enumeration.UsStatesEnum[] getPropertyValue(ITableProperties properties) {
                return properties.getState();
            }

            @Override
            protected IntersectionType matchNotNulls(org.openl.rules.enumeration.UsStatesEnum[] firstValue, org.openl.rules.enumeration.UsStatesEnum[] secondValue) {
                return intersectionForCONTAINS(firstValue, secondValue);
            }
        });
        constraints.put("region", new IntersectionConstraint<org.openl.rules.enumeration.RegionsEnum[]>() { 

            @Override
            protected org.openl.rules.enumeration.RegionsEnum[] getPropertyValue(ITableProperties properties) {
                return properties.getRegion();
            }

            @Override
            protected IntersectionType matchNotNulls(org.openl.rules.enumeration.RegionsEnum[] firstValue, org.openl.rules.enumeration.RegionsEnum[] secondValue) {
                return intersectionForCONTAINS(firstValue, secondValue);
            }
        });
// <<< END INSERT >>>
    }

}
