package org.openl.rules.workspace;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openl.rules.repository.api.UserInfo;
import org.openl.rules.workspace.dtr.impl.DesignTimeRepositoryImpl;
import org.openl.rules.workspace.lw.impl.LocalWorkspaceManagerImpl;
import org.openl.rules.workspace.uw.UserWorkspace;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class MultiUserWorkspaceManagerTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private MultiUserWorkspaceManager manager;

    @Before
    public void init() throws Exception {
        LocalWorkspaceManagerImpl localWorkspaceManager = new LocalWorkspaceManagerImpl();
        localWorkspaceManager.setWorkspaceHome(tempFolder.getRoot().getAbsolutePath());
        localWorkspaceManager.init();

        manager = new MultiUserWorkspaceManager();
        manager.setLocalWorkspaceManager(localWorkspaceManager);
        manager.setDesignTimeRepository(new DesignTimeRepositoryImpl());
    }

    @Test
    public void removeWorkspaceOnSessionTimeout() {
        WorkspaceUserImpl user = new WorkspaceUserImpl("user1",
            (username) -> new UserInfo("user1", "user1@email", "User1"));
        UserWorkspace workspace1 = manager.getUserWorkspace(user);

        // Must return cached version
        UserWorkspace workspace2 = manager.getUserWorkspace(user);
        assertSame(workspace1, workspace2);

        // Session timeout
        workspace1.release();

        // Must create new instance
        workspace2 = manager.getUserWorkspace(user);
        assertNotSame(workspace1, workspace2);
    }

}