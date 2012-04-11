package org.openl.rules.workspace.lw;

import org.openl.rules.workspace.WorkspaceException;
import org.openl.rules.workspace.WorkspaceUser;

public interface LocalWorkspaceManager {
    LocalWorkspace getWorkspace(WorkspaceUser user) throws WorkspaceException;
}
