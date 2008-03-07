package org.openl.rules.eclipse.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.openl.eclipse.wizard.base.NewProjectFromTemplateWizardCustomizer;
import org.openl.eclipse.wizard.base.OpenLCore;

/**
 * @author Aliaksandr Antonik.
 */
public class OpenLUtil {
    public static void addOpenLCapabilities(IProject project) throws CoreException {
        OpenLCore.addOpenLCapabilities(project,
                new NewProjectFromTemplateWizardCustomizer(
                        RulesWizardPlugin.getDefault().getBundle(),
                        "NewSimpleOpenLRulesProjectWizard") {}
        );
    }

    public static void removeOpenLCapabilities(IProject project) throws CoreException {
        OpenLCore.removeOpenLCapabilities(project);
    }
}
