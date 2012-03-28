/*
 * Created on Oct 2, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.xls.ce;

import org.openl.ICompileContext;
import org.openl.OpenL;
import org.openl.conf.BaseOpenLBuilder;
import org.openl.conf.IConfigurableResourceContext;
import org.openl.conf.OpenConfigurationException;
import org.openl.rules.lang.xls.XlsParser;
import org.openl.rules.lang.xls.XlsVM;
import org.openl.rules.lang.xls.ce.XlsBinderCE;
import org.openl.xls.RulesCompileContext;

/**
 * @author snshor
 * 
 */
public class OpenLBuilder extends BaseOpenLBuilder {

    public OpenL build(String category) throws OpenConfigurationException {

        OpenL openl = new OpenL();

        openl.setParser(new XlsParser(getUserEnvironmentContext()));
        openl.setBinder(new XlsBinderCE(getUserEnvironmentContext()));
        openl.setVm(new XlsVM());
        openl.setCompileContext(buildCompileContext());

        return openl;
    }

    private ICompileContext buildCompileContext() {
        ICompileContext compileContext = new RulesCompileContext();

        IConfigurableResourceContext resourceContext = getResourceContext();

        if (resourceContext != null) {

            String propertyValue = resourceContext.findProperty("validation");

            if (propertyValue != null) {
                Boolean value = Boolean.valueOf(propertyValue);
                compileContext.setValidationEnabled(value);
            }
        }

        return compileContext;
    }
}
