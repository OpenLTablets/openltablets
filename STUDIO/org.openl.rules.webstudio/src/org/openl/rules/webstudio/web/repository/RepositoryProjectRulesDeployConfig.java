package org.openl.rules.webstudio.web.repository;

import com.thoughtworks.xstream.XStreamException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openl.commons.web.jsf.FacesUtils;
import org.openl.rules.common.ProjectException;
import org.openl.rules.project.abstraction.AProjectResource;
import org.openl.rules.project.abstraction.UserWorkspaceProject;
import org.openl.rules.project.instantiation.ReloadType;
import org.openl.rules.project.model.RulesDeploy;
import org.openl.rules.ui.WebStudio;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

@ManagedBean
@ViewScoped
public class RepositoryProjectRulesDeployConfig {
    private static final String RULES_DEPLOY_CONFIGURATION_FILE = "rules-deploy.xml";
    private final Logger log = LoggerFactory.getLogger(RepositoryProjectRulesDeployConfig.class);

    @ManagedProperty(value = "#{repositoryTreeState}")
    private RepositoryTreeState repositoryTreeState;

    private WebStudio studio = WebStudioUtils.getWebStudio(true);

    private final XmlRulesDeployGuiWrapperSerializer serializer = new XmlRulesDeployGuiWrapperSerializer();

    private RulesDeployGuiWrapper rulesDeploy;
    private UserWorkspaceProject lastProject;

    public RepositoryProjectRulesDeployConfig() {
    }

    public void setRepositoryTreeState(RepositoryTreeState repositoryTreeState) {
        this.repositoryTreeState = repositoryTreeState;
    }

    public RulesDeployGuiWrapper getRulesDeploy() {
        UserWorkspaceProject project = getProject();
        if (lastProject != project) {
            rulesDeploy = null;
            lastProject = project;
        }
        if (project == null) {
            return null;
        }
        if (rulesDeploy == null) {
            if (hasRulesDeploy(project)) {
                rulesDeploy = loadRulesDeploy(project);
            }
        }
        return rulesDeploy;
    }

    public void createRulesDeploy() {
        rulesDeploy = new RulesDeployGuiWrapper(new RulesDeploy());

        // default values
        rulesDeploy.setProvideRuntimeContext(true);
    }

    public void deleteRulesDeploy() {
        UserWorkspaceProject project = getProject();
        if (hasRulesDeploy(project)) {
            try {
                project.deleteArtefact(RULES_DEPLOY_CONFIGURATION_FILE);
                repositoryTreeState.refreshSelectedNode();
                studio.reset(ReloadType.FORCED);
            } catch (ProjectException e) {
                FacesUtils.addErrorMessage("Cannot delete " + RULES_DEPLOY_CONFIGURATION_FILE + " file");
                log.error(e.getMessage(), e);
            }
        }

        rulesDeploy = null;
    }

    public void saveRulesDeploy() {
        try {
            UserWorkspaceProject project = getProject();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(serializer.serialize(rulesDeploy).getBytes("UTF-8"));

            if (project.hasArtefact(RULES_DEPLOY_CONFIGURATION_FILE)) {
                AProjectResource artefact = (AProjectResource) project.getArtefact(RULES_DEPLOY_CONFIGURATION_FILE);
                artefact.setContent(inputStream);
            } else {
                project.addResource(RULES_DEPLOY_CONFIGURATION_FILE, inputStream);
                repositoryTreeState.refreshSelectedNode();
                studio.reset(ReloadType.FORCED);
            }
        } catch (ProjectException e) {
            FacesUtils.addErrorMessage("Cannot save " + RULES_DEPLOY_CONFIGURATION_FILE + " file");
            log.error(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            FacesUtils.addErrorMessage("UTF-8 charset is not supported. Cannot save " + RULES_DEPLOY_CONFIGURATION_FILE + " file");
            log.error(e.getMessage(), e);
        }
    }

    private UserWorkspaceProject getProject() {
        return repositoryTreeState.getSelectedProject();
    }

    private boolean hasRulesDeploy(UserWorkspaceProject project) {
        return project.hasArtefact(RULES_DEPLOY_CONFIGURATION_FILE);
    }

    private RulesDeployGuiWrapper loadRulesDeploy(UserWorkspaceProject project) {
        InputStream content = null;
        try {
            AProjectResource artefact = (AProjectResource) project.getArtefact(RULES_DEPLOY_CONFIGURATION_FILE);
            content = artefact.getContent();
            return serializer.deserialize(content);
        } catch (ProjectException e) {
            FacesUtils.addErrorMessage("Cannot read " + RULES_DEPLOY_CONFIGURATION_FILE + " file");
            log.error(e.getMessage(), e);
        } catch (XStreamException e) {
            FacesUtils.addErrorMessage("Cannot parse " + RULES_DEPLOY_CONFIGURATION_FILE + " file");
            log.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(content);
        }

        return null;
    }

    public void validateServiceName(FacesContext context, UIComponent toValidate, Object value) {
        String name = (String) value;

        if (!StringUtils.isBlank(name)) {
            String message = "Invalid service name: only latin letters, numbers and _ are allowed, name must begin with a letter";
            FacesUtils.validate(name.matches("[a-zA-Z][a-zA-Z_\\-\\d]*"), message);
        }
    }

    public void validateServiceClass(FacesContext context, UIComponent toValidate, Object value) {
        String className = (String) value;
        if (!StringUtils.isBlank(className)) {
            FacesUtils.validate(className.matches("([\\w$]+\\.)*[\\w$]+"), "Invalid class name");
        }
    }
}
