package org.openl.rules.workspace.dtr.impl;

import java.util.Date;

import org.openl.rules.repository.RLock;
import org.openl.rules.workspace.WorkspaceUser;
import org.openl.rules.workspace.WorkspaceUserImpl;
import org.openl.rules.workspace.dtr.LockInfo;

/**
 * 
 * @author Aleh Bykhavets
 *
 */
public class LockInfoImpl implements LockInfo {

    /** nil object to avoid NullPointer exceptions */
    public static LockInfoImpl NO_LOCK = new LockInfoImpl();

    private boolean isLocked;
    private Date lockedAt;
    private WorkspaceUser lockedBy;

    private LockInfoImpl() {
        isLocked = false;
    }

    public LockInfoImpl(RLock ralLock) {
        isLocked = ralLock.isLocked();

        if (isLocked) {
            lockedAt = ralLock.getLockedAt();
            lockedBy = new WorkspaceUserImpl(ralLock.getLockedBy().getUserName());
        }
    }

    public Date getLockedAt() {
        return lockedAt;
    }

    public WorkspaceUser getLockedBy() {
        return lockedBy;
    }

    public boolean isLocked() {
        return isLocked;
    }
}
