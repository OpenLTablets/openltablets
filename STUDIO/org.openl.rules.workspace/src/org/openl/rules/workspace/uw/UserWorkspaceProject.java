package org.openl.rules.workspace.uw;

import java.util.Collection;

import org.openl.rules.repository.CommonVersion;
import org.openl.rules.workspace.abstracts.Project;
import org.openl.rules.workspace.abstracts.ProjectException;
import org.openl.rules.workspace.abstracts.ProjectVersion;

public interface UserWorkspaceProject extends Project, UserWorkspaceProjectFolder {
    void close() throws ProjectException;
    void open() throws ProjectException;
    void openVersion(CommonVersion version) throws ProjectException;
    void checkOut() throws ProjectException;
    void checkIn() throws ProjectException;

    Collection<ProjectVersion> getVersions();

    // is checked-out by me? -- in LW + locked by me
    boolean isCheckedOut();
    // is opened by me? -- in LW
    boolean isOpened();
    // is deleted in DTR
    boolean isDeleted();
    // is locked in DTR
    boolean isLocked();
    // no such project in DTR
    boolean isLocalOnly();
    
    boolean isRulesProject();
    boolean isDeploymentProject();

    void undelete() throws ProjectException;
    void erase() throws ProjectException;
}
