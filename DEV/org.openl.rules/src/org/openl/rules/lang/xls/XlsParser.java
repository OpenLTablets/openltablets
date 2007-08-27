/*
 * Created on Oct 2, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.lang.xls;

import org.openl.IOpenParser;
import org.openl.IOpenSourceCodeModule;
import org.openl.conf.ConfigurableResourceContext;
import org.openl.conf.IConfigurableResourceContext;
import org.openl.conf.IUserContext;
import org.openl.syntax.IParsedCode;
import org.openl.util.PropertiesLocator;

/**
 * @author snshor
 *  
 */
public class XlsParser implements IOpenParser
{

  IConfigurableResourceContext ucxt;

  /**
   * @param ucxt
   * @param gf
   */
  public XlsParser(IUserContext ucxt)
  {
    this.ucxt = new ConfigurableResourceContext(ucxt.getUserClassLoader(), new String[] {ucxt.getUserHome()});
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openl.IOpenParser#parseAsMethod(org.openl.IOpenSourceCodeModule)
   */
  public IParsedCode parseAsMethodBody(IOpenSourceCodeModule m)
  {
    throw new UnsupportedOperationException(
        ".xls files can not be parsed as a Method Body");
  }

  public IParsedCode parseAsMethodHeader(IOpenSourceCodeModule m)
  {
    throw new UnsupportedOperationException(
        ".xls files can not be parsed as a Method Header");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openl.IOpenParser#parseAsModule(org.openl.IOpenSourceCodeModule)
   */
  public IParsedCode parseAsModule(IOpenSourceCodeModule m)
  {
    
    
    return new XlsLoader(ucxt, getSearchPath()).parse(m);

  }

  public IParsedCode parseAsType(IOpenSourceCodeModule src)
  {
    throw new UnsupportedOperationException(
        ".xls files can not be parsed as a Method Header");
  }

  String searchPath;

  static final String SEARCH_PROPERTY_NAME = "org.openl.rules.include",
      SEARCH_FILE_NAME = "org/openl/rules/org.openl.rules.include.properties";

  protected String getSearchPath()
  {
    if (searchPath != null)
    {
      return searchPath;
    }

    searchPath = PropertiesLocator.findPropertyValue(SEARCH_PROPERTY_NAME, SEARCH_FILE_NAME, ucxt);
    return searchPath;

//    URL url = ucxt.findClassPathResource(SEARCH_FILE_NAME);
//    if (url != null)
//    {
//      InputStream is = null;
//      try
//      {
//        is = url.openStream();
//        Properties p = new Properties();
//        p.load(is);
//        searchPath = p.getProperty(SEARCH_PROPERTY_NAME);
//        return searchPath;
//      } catch (IOException e)
//      {
//        throw RuntimeExceptionWrapper.wrap(e);
//      } finally
//      {
//        try
//        {
//          if (is != null)
//            is.close();
//        } catch (Throwable t)
//        {
//          Log.error("Error closing stream", t);
//        }
//      }
//    }
//    
//    File f = ucxt.findFileSystemResource(SEARCH_FILE_NAME);
//    if (f != null)
//    {
//      InputStream is = null;
//      try
//      {
//        is = new FileInputStream(f);
//        Properties p = new Properties();
//        p.load(is);
//        searchPath = p.getProperty(SEARCH_PROPERTY_NAME);
//        return searchPath;
//      } catch (IOException e)
//      {
//        throw RuntimeExceptionWrapper.wrap(e);
//      } finally
//      {
//        try
//        {
//          if (is != null)
//            is.close();
//        } catch (Throwable t)
//        {
//          Log.error("Error closing stream", t);
//        }
//      }
//    }
//    
//    
//    return ucxt.findProperty(SEARCH_PROPERTY_NAME);

  }

}