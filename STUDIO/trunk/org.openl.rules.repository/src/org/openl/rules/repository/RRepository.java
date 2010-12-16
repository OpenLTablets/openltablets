package org.openl.rules.repository;

import java.util.List;

import org.openl.rules.repository.api.FolderAPI;
import org.openl.rules.repository.exceptions.RRepositoryException;

/**
 * OpenL Rules Repository. A Repository can have any number of OpenL Rules
 * Projects.
 *
 * @author Aleh Bykhavets
 *
 */
public interface RRepository {

    @Deprecated
    RDeploymentDescriptorProject createDDProject(String name) throws RRepositoryException;

    /**
     * Creates a project in the repository. Name of new project must be unique.
     *
     * @param name name of new project
     * @return newly created project
     * @throws RRepositoryException if failed
     */
    @Deprecated
    RProject createProject(String name) throws RRepositoryException;

    @Deprecated
    RDeploymentDescriptorProject getDDProject(String name) throws RRepositoryException;

    @Deprecated
    List<RDeploymentDescriptorProject> getDDProjects() throws RRepositoryException;

    /**
     * Returns name of the repository. It can be type of repository plus
     * location.
     *
     * @return name of repository
     */
    String getName();

    /**
     * Gets project by name.
     *
     * @param name
     * @return project
     * @throws RRepositoryException if failed or no project with specified name
     */
    @Deprecated
    RProject getProject(String name) throws RRepositoryException;

    /**
     * Gets list of projects from the repository.
     *
     * @return list of projects
     * @throws RRepositoryException if failed
     */
    @Deprecated
    List<RProject> getProjects() throws RRepositoryException;

    /**
     * Gets list of projects from the repository that are marked for deletion.
     *
     * @return list of projects that are marked for deletion
     */
    @Deprecated
    List<RProject> getProjects4Deletion() throws RRepositoryException;

    boolean hasDDProject(String name) throws RRepositoryException;

    /**
     * Checks whether project with given name exists in the repository.
     *
     * @param name
     * @return <code>true</code> if project with such name exists
     * @throws RRepositoryException
     */
    boolean hasProject(String name) throws RRepositoryException;

    /**
     * Releases resources allocated by this Rules Repository instance.
     */
    void release();


    //TODO new method names and comments
    
    
    FolderAPI createDeploymentProject(String name) throws RRepositoryException;

    /**
     * Creates a project in the repository. Name of new project must be unique.
     *
     * @param name name of new project
     * @return newly created project
     * @throws RRepositoryException if failed
     */
    FolderAPI createRulesProject(String name) throws RRepositoryException;

    FolderAPI getDeploymentProject(String name) throws RRepositoryException;
 
    List<FolderAPI> getDeploymentProjects() throws RRepositoryException;

    /**
     * Gets project by name.
     *
     * @param name
     * @return project
     * @throws RRepositoryException if failed or no project with specified name
     */
    FolderAPI getRulesProject(String name) throws RRepositoryException;

    /**
     * Gets list of projects from the repository.
     *
     * @return list of projects
     * @throws RRepositoryException if failed
     */
    List<FolderAPI> getRulesProjects() throws RRepositoryException;

    /**
     * Gets list of projects from the repository that are marked for deletion.
     *
     * @return list of projects that are marked for deletion
     */
    List<FolderAPI> getRulesProjectsForDeletion() throws RRepositoryException;
}
