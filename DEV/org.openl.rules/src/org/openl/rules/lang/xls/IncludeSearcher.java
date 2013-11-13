/**
 * 
 */
package org.openl.rules.lang.xls;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.openl.conf.IConfigurableResourceContext;
import org.openl.message.OpenLMessagesUtils;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.source.impl.FileSourceCodeModule;
import org.openl.source.impl.URLSourceCodeModule;
import org.openl.util.PathTool;
import org.openl.util.StringTool;

/**
 * Searches for includes.
 *
 */
public class IncludeSearcher {
    
    private static final String INCLUDE = "include/";
    private String searchPath;
    private IConfigurableResourceContext ucxt;
    
    public IncludeSearcher(IConfigurableResourceContext ucxt, String searchPath) {
        this.ucxt = ucxt;
        this.searchPath = searchPath;
    }
    
    public IOpenSourceCodeModule findInclude(String include) {

        if (searchPath == null) {
            searchPath = INCLUDE;
        }

        String[] path = StringTool.tokenize(searchPath, ";");

        for (int i = 0; i < path.length; i++) {

            try {
                String p = PathTool.mergePath(path[i], include);
                URL url = ucxt.findClassPathResource(p);

                if (url != null) {
                    return new URLSourceCodeModule(url);
                }

                File f = ucxt.findFileSystemResource(p);

                if (f != null) {
                    return new FileSourceCodeModule(f, null);
                }

                // let's try simple concat and use url
                String u2 = path[i] + include;
                URL xurl = new URL(u2);

                // URLConnection uc;
                InputStream is = null;

                try {
                    is = xurl.openStream();
                } catch (IOException iox) {
                    return null;
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }

                return new URLSourceCodeModule(xurl);
            } catch (Throwable t) {
                OpenLMessagesUtils.addWarn(String.format("Cannot find '%s' ()", include, t.getMessage()));
            }
        }

        return null;
    }
}