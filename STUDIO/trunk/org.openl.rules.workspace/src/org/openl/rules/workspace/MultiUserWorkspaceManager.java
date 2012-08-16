package org.openl.rules.workspace;

import java.util.HashMap;
import java.util.Map;

import org.openl.rules.workspace.dtr.DesignTimeRepository;
import org.openl.rules.workspace.lw.LocalWorkspace;
import org.openl.rules.workspace.lw.LocalWorkspaceManager;
import org.openl.rules.workspace.uw.UserWorkspace;
import org.openl.rules.workspace.uw.UserWorkspaceListener;
import org.openl.rules.workspace.uw.impl.UserWorkspaceImpl;

/**
 * Manager of Multiple User Workspaces. <p/> It takes care of creation and
 * releasing of User Workspaces.
 *
 * Must be configured in spring configuration as a singleton.
 *
 * @author Aleh Bykhavets
 */
public class MultiUserWorkspaceManager implements UserWorkspaceListener {
    /** Design Time Repository */
    private DesignTimeRepository designTimeRepository;
    /** Manager of Local Workspaces */
    private LocalWorkspaceManager localWorkspaceManager;
    /** Cache for User Workspaces */
    private Map<String, UserWorkspace> userWorkspaces = new HashMap<String, UserWorkspace>();

    protected UserWorkspace createUserWorkspace(WorkspaceUser user) throws WorkspaceException {
        LocalWorkspace usersLocalWorkspace = localWorkspaceManager.getWorkspace(user);
        return new UserWorkspaceImpl(user, usersLocalWorkspace, designTimeRepository);
    }

    public LocalWorkspaceManager getLocalWorkspaceManager() {
        return localWorkspaceManager;
    }

    /**
     * Returns . <p/> It creates Workspace (including local) for specified user
     * on first request.
     *
     * @param user active user
     * @return new or cached instance of user workspace
     *
     * @throws WorkspaceException if failed
     */
    public UserWorkspace getUserWorkspace(WorkspaceUser user) throws WorkspaceException {
        UserWorkspace uw = userWorkspaces.get(user.getUserId());
        if (uw == null) {
            uw = createUserWorkspace(user);
            userWorkspaces.put(user.getUserId(), uw);
        } else {
            LocalWorkspace usersLocalWorkspace = localWorkspaceManager.getWorkspace(user);
            usersLocalWorkspace.setUserWorkspace(uw);
        }

        return uw;
    }

    public void setDesignTimeRepository(DesignTimeRepository designTimeRepository) {
        this.designTimeRepository = designTimeRepository;
    }

    public void setLocalWorkspaceManager(LocalWorkspaceManager localWorkspaceManager) {
        this.localWorkspaceManager = localWorkspaceManager;
    }

    /**
     * UserWorkspace should notify manager that life cycle of the workspace is
     * ended and it must be removed from cache.
     */
    public void workspaceReleased(UserWorkspace workspace) {
        userWorkspaces.remove(((UserWorkspaceImpl) workspace).getUser().getUserId());
    }
}
