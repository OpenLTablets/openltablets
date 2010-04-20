package org.openl.rules.dt.type.domains;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;
import org.openl.rules.validation.DecisionTableCreator;

public class DimensionPropertiesDomainsCollector {
    
    private Map<String, IDomainAdaptor> propertiesDomains = new HashMap<String,IDomainAdaptor>();
    
    private Set<String> propertiesNeedDomain = new HashSet<String>();
    
    private Map<String, IDomainCollector> domainCollectors = new HashMap<String, IDomainCollector>();
    
    // date domain collector should be one for all dates in project. 
    private DateDomainCollector dateDomainCollector = new DateDomainCollector();    
    
    public DimensionPropertiesDomainsCollector() {
        initDomainCollectors();
    }
    
    public Map<String, IDomainAdaptor> getGatheredPropertiesDomains() {
        return new HashMap<String, IDomainAdaptor>(propertiesDomains);
    }
    
    public Set<String> getPropertiesNeedDomain() {
        return new HashSet<String>(propertiesNeedDomain);
    }
    
    public void gatherPropertiesDomains(TableSyntaxNode[] tableSyntaxNodes) {
        propertiesDomains.clear();
        gatherAllDomains(tableSyntaxNodes);
        applyAllDomains();        
    }

    private void applyAllDomains() {
        IDomainAdaptor dateDomainAdaptor = null;
        for (String propNeedDomain : propertiesNeedDomain) {
            Class<?> propertyType = TablePropertyDefinitionUtils.getPropertyTypeByPropertyName(propNeedDomain);
            IDomainCollector domainCollector = domainCollectors.get(propNeedDomain);
            applyDomain(propNeedDomain, domainCollector.getGatheredDomain());
            if (dateDomainAdaptor == null && Date.class.equals(propertyType)) {
                dateDomainAdaptor = domainCollector.getGatheredDomain();
            } else if (propertyType.isArray() && propertyType.getComponentType().isEnum()) {
                applyArrayDomains(propNeedDomain, domainCollector);
            }
        }
        applyCurrentDateDomain(dateDomainAdaptor);
    }

    private void applyArrayDomains(String propNeedDomain, IDomainCollector domainCollector) {
        ArrayEnumDomainCollector arrayCollector = (ArrayEnumDomainCollector) domainCollector;
        IDomainAdaptor domainAdaptor = arrayCollector.getGatheredDomain();
        if (domainAdaptor != null) {
            propertiesDomains.put(propNeedDomain, domainAdaptor);
            for (int i = 1; i <= arrayCollector.getNumberOfDomainElements(); i++) {
                propertiesDomains.put(
                        String.format("%s%s%s", propNeedDomain, DecisionTableCreator.LOCAL_PARAM_SUFFIX, i), 
                        domainAdaptor);
            }
        }
    }

    private void applyDomain(String propNeedDomain, IDomainAdaptor gatheredDomain) {
        String key = propNeedDomain + DecisionTableCreator.LOCAL_PARAM_SUFFIX;
        if (gatheredDomain != null && !propertiesDomains.containsKey(key)) {
            propertiesDomains.put(propNeedDomain, gatheredDomain);
            propertiesDomains.put(key, gatheredDomain);
        }        
    }

    private void applyCurrentDateDomain(IDomainAdaptor dateDomainAdaptor) {
        if (dateDomainAdaptor != null && !propertiesDomains.containsKey(DecisionTableCreator.CURRENT_DATE_PARAM)) {
            propertiesDomains.put(DecisionTableCreator.CURRENT_DATE_PARAM, dateDomainAdaptor);
        }        
    }

    private void gatherAllDomains(TableSyntaxNode[] tableSyntaxNodes) {
        for (TableSyntaxNode tsn : tableSyntaxNodes) {
            for (String propNeedDomain : propertiesNeedDomain) {
                IDomainCollector domainCollector = domainCollectors.get(propNeedDomain);
                domainCollector.gatherDomains(tsn);
            }
        }        
    }

    private void initDomainCollectors() {
        initPropertiesNeedDomain();
        for (String propNeedDomain : propertiesNeedDomain) {
            domainCollectors.put(propNeedDomain, getDomainCollector(propNeedDomain));
        }        
    }

    private void initPropertiesNeedDomain() {
        String[] dimensionProperties = TablePropertyDefinitionUtils.getDimensionalTableProperties();
        
        for (String dimensionProp : dimensionProperties) {
            Class<?> propType = TablePropertyDefinitionUtils.getPropertyTypeByPropertyName(dimensionProp);
            boolean dateType = Date.class.equals(propType);
            boolean stringType = String.class.equals(propType);
            boolean enumtype = propType.isEnum();
            boolean arrayEnumType = propType.isArray() && propType.getComponentType().isEnum();
            if (dateType || stringType || enumtype || arrayEnumType) {
                propertiesNeedDomain.add(dimensionProp);                
            }
        }        
    }

    private IDomainCollector getDomainCollector(String propertyName) {        
        Class<?> propertyType = TablePropertyDefinitionUtils.getPropertyTypeByPropertyName(propertyName);
        IDomainCollector result = null; 
        if (Date.class.equals(propertyType)) {
            dateDomainCollector.addPropertyToSearch(propertyName);
            result = dateDomainCollector;
        } else if (String.class.equals(propertyType)) {
            result = new StringDomainCollector(propertyName);
        } else if (propertyType.isEnum()) {
            result = new EnumDomainCollector(propertyName);
        } else if (propertyType.isArray() && propertyType.getComponentType().isEnum()) {
            result = new ArrayEnumDomainCollector(propertyName);
        } 
        return result;
    }

}
