package org.openl.rules.workspace.lw;

import java.io.File;

import org.openl.rules.workspace.abstracts.Project;
import org.openl.rules.workspace.abstracts.ProjectException;
import org.openl.rules.workspace.abstracts.ProjectsContainer;
import org.openl.rules.workspace.uw.UserWorkspace;

/**
 * A container for <code>LocalProject</code>s. It is supposed to be able to store and restore projects on/from
 * filesystem.
 *
 * @author Aleh Bykhavets
 */
public interface LocalWorkspace extends ProjectsContainer<LocalProject> {
    /**
     * Adds a project to the workspace by making a copy from given <code>Project</code> and saving it to the
     * filesystem.
     *
     * @param project the project to make copy of when creating new one in the workspace
     * @return newly created <code>LocalProject</code>
     * @throws ProjectException if a project can not be added on some reason
     */
    LocalProject addProject(Project project) throws ProjectException;

    /**
     * Removes a project from the workspace by name.
     *
     * @param name project name to be removed
     * @throws ProjectException if project with given <code>name</code> does not exists or can not be removed
     */
    void removeProject(String name) throws ProjectException;

    /**
     * Refreshes the projects and their contents according to the changes in filesystem location that is used as
     * storage for workspace projects.
     */
    void refresh();

    /**
     * Saves all the projects in the workspace.
     */
    void saveAll();

    /**
     * The method should be called when working with the workspace is finished. It saves projects' state and releases
     * resources.
     */
    void release();

    /**
     * Returns directory in the filesystem used storage for workspace projects.
     *
     * @return storage directory for workspace projects
     */
    File getLocation();

    /**
     * Adds listener to the workspace that listens to workspace events.
     *
     * @param listener workspace listener.
     */
    void addWorkspaceListener(LocalWorkspaceListener listener);

    /**
     * Removes a listener from workspace. If there is no such listener nothing happens.
     *
     * @param listener listener to remove
     * @return if listener was really removed, <code>false</code> if there was no such listener
     */
    boolean removeWorkspaceListener(LocalWorkspaceListener listener);

    /**
     * Sets the user workspace that this local workspace is contained in.
     *
     * @param userWorkspace user workspace that contains this local workspace
     */
    void setUserWorkspace(UserWorkspace userWorkspace);
}
