/*
 * Created on May 9, 2003 Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.source;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

/**
 * IOpenSourceCodeModule is an abstraction of rules source code.
 * 
 * @author snshor
 */
public interface IOpenSourceCodeModule {

    InputStream getByteStream();

    Reader getCharacterStream();

    String getCode();

    int getStartPosition();

    int getTabSize();

    String getUri(int textpos);
    
    Map<String, Object> getParams();
    void setParams(Map<String, Object> params);
}
