package org.openl.rules.table.properties;

/**
 * 
 * @author snshor
 * Created Jul 21, 2009 
 *
 *	This class is used to load TableProperties as data beans
 */

public class PropertiesOpenClass 
{
	
	Class<?> propertiesBeanClass;
	TablePropertiesDefinition[] definitions;
	
	public PropertiesOpenClass(Class<?> propertiesBeanClass, TablePropertiesDefinition[] definitions)
	{
		this.propertiesBeanClass = propertiesBeanClass;
		this.definitions = definitions;
		initialize();
	}

	private void initialize() {
		// TODO Auto-generated method stub
		
	}
}
