package org.openl.rules.webstudio.web.repository;

import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.rules.webstudio.web.jsf.JSFConst;
import org.openl.rules.webstudio.web.jsf.util.FacesUtils;
import org.openl.rules.webstudio.web.servlet.RulesUserSession;
import org.openl.rules.workspace.abstracts.ProjectVersion;
import org.openl.rules.workspace.deploy.DeployID;
import org.openl.rules.workspace.uw.UserWorkspace;
import org.openl.rules.workspace.uw.UserWorkspaceDeploymentProject;

/**
 * Repository Utilities
 * 
 * @author Aleh Bykhavets
 * 
 */
public class RepositoryUtils {
    private final static Log log = LogFactory.getLog(RepositoryUtils.class);

    public static final Comparator<ProjectVersion> VERSIONS_REVERSE_COMPARATOR = new Comparator<ProjectVersion>() {
        public int compare(ProjectVersion o1, ProjectVersion o2) {
            return o2.compareTo(o1);
        }
    };

    public static RulesUserSession getRulesUserSession() {
        return (RulesUserSession) FacesUtils.getSessionMap().get(JSFConst.RULES_USER_SESSION_ATTR);
    }

    /**
     * @return user's workspace or <code>null</code>
     */
    public static UserWorkspace getWorkspace() {
        try {
            return getRulesUserSession().getUserWorkspace();
        } catch (Exception e) {
            log.error("Error obtaining user workspace", e);
        }
        return null;
    }

    public static DeployID getDeployID(UserWorkspaceDeploymentProject ddProject) {
        StringBuilder sb = new StringBuilder(ddProject.getName());
        ProjectVersion projectVersion = ddProject.getVersion();
        if (projectVersion != null) {
            sb.append('#').append(projectVersion.getVersionName());
        }
        return new DeployID(sb.toString());
    }
}
