/*
 * Created on Jun 4, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */
 
package org.openl.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.openl.util.Log;

//import org.openl.util.Log;


/**
 * Class PropertyFileLoader loads a property file using the following algorithm:
 * 
 * 1) if exists property <code>propertiesFileProperty</code> it's value becomes <code>property_file_name</code>
 * otherwise  <code>propertiesFileDefaultName</code> is used.
 * 
 * 2) It tries to load properties file in the following order:
 *   2.1) as URL
 *   2.2) as resource in context classpath
 *   2.3) as file in context filesystem
 * 
 * @see org.openl.conf.IConfigurableResourceContext
 * 
 * @author snshor
 *
 */
public class PropertyFileLoader 
{
	String propertiesFileDefaultName;
	String propertiesFileProperty;
	
	Properties properties = null;
	
	IConfigurableResourceContext context;
	
	PropertyFileLoader parent = null;
	
	
	static final public Properties NO_PROPERTIES = new Properties();
	
	
	
	public PropertyFileLoader(String propertiesFileDefaultName, String propertiesFileProperty, IConfigurableResourceContext context, PropertyFileLoader parent)
	{
		this.context = context;
		this.propertiesFileDefaultName = propertiesFileDefaultName;
		this.propertiesFileProperty = propertiesFileProperty;
		this.parent= parent;
	}
	
	public String getProperty(String propertyName)
	{
		String res = getProperties().getProperty(propertyName);
		
		if (res != null)
			return res;
			
		res = getContext().findProperty(propertyName);

		if (res != null)
			return res;
	
	  return parent == null ? null : parent.getProperty(propertyName); 		
	}


	Properties getProperties()
	{
		if (properties != null)
		  return properties;
		
		// check the propertiesFileProperty first
		
		String propertiesFileName = getContext().findProperty(propertiesFileProperty);
		if (propertiesFileName == null)
		{
			propertiesFileName = propertiesFileDefaultName;
		} 
		
		//is it valid URL?
		
		Log.debug("Looking for " + propertiesFileName);
		if (!loadAsURL(propertiesFileName) && !loadAsResource(propertiesFileName) && !loadAsFile(propertiesFileName))
		{
			properties = parent == null ?  NO_PROPERTIES : parent.getProperties(); 
		}
		
		return properties;
		
	}
	
	
	boolean loadAsURL(String url)
	{
		try
		{
			InputStream in = new URL(url).openStream();
			properties = new Properties();
			properties.load(in);
			in.close();
			return true;
		}
		catch(Throwable t)
		{
//			Log.debug("Loading as url: ", t);
			return false;
		}
	}
	
	boolean loadAsResource(String name)
	{
		try
		{
			
			URL url = getContext().findClassPathResource(name);
			if (url == null) return false;
			InputStream in = url.openStream();
			Properties p = new Properties();
			p.load(in);
			properties = p;
			in.close();
			return true;
		}
		catch(Throwable t)
		{
//			Log.debug("Loading as resource: ", t);
			return false;
		}
	}
	
	boolean loadAsFile(String url)
	{
		try
		{
			File f = getContext().findFileSystemResource(url);
			if (f == null)
			  return false; 
			InputStream in = new FileInputStream(f);
			properties = new Properties();
			properties.load(in);
			in.close();
			return true;
		}
		catch(Throwable t)
		{
//			System.out.println("File not as found: " + url);
			return false;
		}
	}
	
	
	
	protected IConfigurableResourceContext getContext()
	{
		return context;
	}
	

	

}
