package org.openl.rules.dt.type.domains;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Set;

import org.openl.domain.EnumDomain;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.properties.ITableProperties;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;

public class ArrayDomainCollector implements IDomainCollector {

    private String propertyToSearch; 
    
    private Set<Object> arrayEnumProperties = new HashSet<Object>();
    
    public ArrayDomainCollector(String propertyToSearch) {
        this.propertyToSearch = propertyToSearch;
    }

    public void gatherDomains(TableSyntaxNode tsn) {
        ITableProperties tableProperties = tsn.getTableProperties();
        if (tableProperties != null) {
            Object[] propValues = (Object[])tableProperties.getPropertyValue(propertyToSearch);
            if (propValues != null) {
                for (Object propValue : propValues) {
                    if (propValue != null) {                        
                        arrayEnumProperties.add(propValue);
                    }
                }                        
            }
        }    
    }

    @SuppressWarnings("unchecked")
    public IDomainAdaptor getGatheredDomain() {
        IDomainAdaptor result = null;
        if (!arrayEnumProperties.isEmpty()) {
            Class<?> propertyType = TablePropertyDefinitionUtils.getPropertyTypeByPropertyName(propertyToSearch);
            Class<?> componentType = propertyType.getComponentType();
            
            Object[] resultArray = (Object[])Array.newInstance(componentType, arrayEnumProperties.size());
            
            EnumDomain enumDomain = new EnumDomain(arrayEnumProperties.toArray(resultArray));
            result = new EnumDomainAdaptor(enumDomain);            
        } else {
            // all values from enum will be used as domain. 
        }
        return result;
    }
    
    public int getNumberOfDomainElements() {
        return arrayEnumProperties.size();
    }

}
