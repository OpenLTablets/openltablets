package org.openl.rules.workspace.abstracts;

import java.util.Collection;

/**
 * An interfaces to be implemented by containers of special types of
 * {@link Project}s;
 * 
 * @author Aleh Bykhavets
 */
public interface ProjectsContainer<T extends Project> {
    /**
     * Returns collection of projects in the container. The order of the
     * projects returned depends on the specific container. Never returns
     * <code>null</code>.
     * 
     * @return all projects in the container.
     */
    Collection<T> getProjects();

    /**
     * Returns a project by its name.
     * 
     * @param name project name
     * @return project by name
     * @throws ProjectException if container does not contain a project with
     *             given name
     */
    T getProject(String name) throws ProjectException;

    /**
     * Returns <code>true</code> if there is a project with given name.
     * 
     * @param name name of a project whose presence in the container is to be
     *            tested
     * @return if there is a project with given name
     */
    boolean hasProject(String name);

    /**
     * Returns a <code>ProjectArtefact</code> identified by its
     * <code>artefactPath</code>. The implementation must regard first
     * segment of the path as project name, then it should call
     * {@link Project#getArtefactByPath(ArtefactPath)} with the rest of
     * <code>artefactPath</code> as parameter.
     * 
     * @param artefactPath identifies <code>ProjectArtefact</code> to return
     *            in container hierarchy.
     * @return <code>ProjectArtefact</code> on given <code>artefactPath</code>
     * @throws ProjectException if <code>artefactPath</code> does not point to
     *             a <code>ProjectArtefact</code> inside container
     */
    ProjectArtefact getArtefactByPath(ArtefactPath artefactPath) throws ProjectException;
}
