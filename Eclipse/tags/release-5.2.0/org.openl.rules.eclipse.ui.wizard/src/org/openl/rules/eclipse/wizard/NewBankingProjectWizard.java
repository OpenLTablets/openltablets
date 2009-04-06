package org.openl.rules.eclipse.wizard;

import org.openl.eclipse.wizard.base.NewProjectFromTemplateWizard;
import org.openl.eclipse.wizard.base.NewProjectFromTemplateWizardCustomizer;

/**
 * @author smesh
 */
public class NewBankingProjectWizard
  extends NewProjectFromTemplateWizard
{

  public NewBankingProjectWizard()
  {
    super(new NewProjectFromTemplateWizardCustomizer(
      RulesWizardPlugin.getDefault().getBundle(),
      "NewBankingProjectWizard")
    {
    });
  }

}
