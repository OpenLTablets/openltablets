package org.openl.rules.workspace.dtr;

import java.util.List;

import org.openl.rules.common.CommonVersion;
import org.openl.rules.common.ProjectException;
import org.openl.rules.project.abstraction.ADeploymentProject;
import org.openl.rules.project.abstraction.AProject;
import org.openl.rules.workspace.WorkspaceUser;
import org.openl.rules.workspace.abstracts.ProjectsContainer;

/**
 * Design Time Repository. <p/> Version Storage for development phase. <p/>
 * Rules and Deployment projects are treated separately.
 *
 * @author Aleh Bykhavets
 *
 */
public interface DesignTimeRepository extends ProjectsContainer {

    /**
     * Copies deployment project in/into the DTR. <p/> Source project can be
     * local project, a version of project in the DTR, any other class that
     * implements DeploymentDescriptorProject interface.
     *
     * @param project source deployment project
     * @param name name of new project, must be unique
     * @param user who is copies project
     * @throws ProjectException if failed
     */
    void copyDDProject(ADeploymentProject project, String name, WorkspaceUser user)
            throws ProjectException;

    /**
     * Copies rules project in/into Design Time Repository. <p/> Source project
     * can be LocalWorkspaceProject, a version of project in the DTR, any other
     * class that implements Project interface.
     *
     * @param project source rules project
     * @param name name of new project, must be unique
     * @param user who is copies project
     * @throws RepositoryException if failed
     */
    void copyProject(AProject project, String name, WorkspaceUser user) throws ProjectException;

    /**
     * Creates new deployment project in the DTR.
     *
     * @param name name of new deployment project, must be unique
     * @throws RepositoryException if failed
     */
    void createDDProject(String name) throws RepositoryException;

    /**
     * Creates new rules project in the Design Time Repository.
     *
     * @param name name of new rules project, must be unique
     * @throws RepositoryException if failed
     */
    void createProject(String name) throws RepositoryException;

    /**
     * Gets deployment project from the DTR.
     *
     * @param name name of deployment project
     * @return instance of deployment project
     * @throws RepositoryException if failed
     */
    ADeploymentProject getDDProject(String name) throws RepositoryException;

    /**
     * Gets specified version of deployment project from the DTR.
     *
     * @param name name of deployment project
     * @param version exact version of project
     * @return specified version of deployment project
     * @throws RepositoryException if failed
     */
    ADeploymentProject getDDProject(String name, CommonVersion version) throws RepositoryException;

    /**
     * Returns list of all deployment projects from the DTR.
     *
     * @return list of deployment projects
     * @throws RepositoryException if failed
     */
    List<ADeploymentProject> getDDProjects() throws RepositoryException;

    /**
     * Gets particular version of a rules project.
     *
     * @param name name of rules project
     * @param version exact version of project
     * @return specified version of rules project
     * @throws RepositoryException if failed
     */
    AProject getProject(String name, CommonVersion version) throws RepositoryException;

    /**
     * Checks whether the DTR has deployment project with specified name.
     *
     * @param name name of deployment project to be checked
     * @return <code>true</code> if deployment project with specified name
     *         exists already
     */
    boolean hasDDProject(String name);

    /**
     * Updates rules project in Design Time Repository. WorkspaceUser parameter
     * should guarantee that project is updated by user who is locking the
     * project. Project can be of any implementation -- LocalWorkspaceProject,
     * or even older version of the project. DTR will take name of
     * <code>project</code> argument and update a DTR project with the same
     * name.
     *
     * @param project new version of rules project
     * @param user who is updating project
     * @param major new major version of project
     * @param minor new minor version of project
     * @throws RepositoryException if failed
     */
    void updateProject(AProject project, WorkspaceUser user, int major, int minor) throws RepositoryException;

    
    void addListener(DesignTimeRepositoryListener listener);
    void removeListener(DesignTimeRepositoryListener listener);
    List<DesignTimeRepositoryListener> getListeners();

    boolean isFailed();

}
