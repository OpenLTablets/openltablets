/**
 * Created Dec 5, 2006
 */
package org.openl.rules.test;

import org.openl.OpenL;
import org.openl.binding.MethodNotFoundException;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.syntax.impl.FileSourceCodeModule;

/**
 * @author snshor
 * 
 */
public class Tools {

    static public OpenL getOpenL() {
        OpenL openl = OpenL.getInstance("org.openl.xls");
        return openl;
    }

    static public XlsModuleOpenClass createModule(String fname) {

        OpenL openl = getOpenL();

        XlsModuleOpenClass xmo = (XlsModuleOpenClass) openl.compileModule(new FileSourceCodeModule(fname, null));
        return xmo;
    }

    static public Object run(String file, String methodName, Object[] params) throws MethodNotFoundException {
        Object res = getOpenL().evaluateMethod2(new FileSourceCodeModule(file, null), methodName, null, params);
        return res;
    }

}
