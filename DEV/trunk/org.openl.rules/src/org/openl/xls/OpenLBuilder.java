/*
 * Created on Oct 2, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.xls;

import org.openl.ICompileContext;
import org.openl.OpenConfigurationException;
import org.openl.OpenL;
import org.openl.conf.BaseOpenLBuilder;
import org.openl.conf.IConfigurableResourceContext;
import org.openl.rules.lang.xls.XlsBinder;
import org.openl.rules.lang.xls.XlsParser;
import org.openl.rules.lang.xls.XlsVM;

/**
 * @author snshor
 * 
 */
public class OpenLBuilder extends BaseOpenLBuilder {

    public OpenL build(String category) throws OpenConfigurationException {

        OpenL openl = new OpenL();

        openl.setParser(new XlsParser(getUserEnvironmentContext()));
        openl.setBinder(new XlsBinder(getUserEnvironmentContext()));
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
