package org.openl.rules.workspace.uw.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openl.rules.repository.CommonVersion;
import static org.openl.rules.security.Privileges.*;
import static org.openl.rules.security.SecurityUtils.check;
import org.openl.rules.workspace.WorkspaceUser;
import org.openl.rules.workspace.abstracts.ArtefactPath;
import org.openl.rules.workspace.abstracts.DeploymentDescriptorProject;
import org.openl.rules.workspace.abstracts.Project;
import org.openl.rules.workspace.abstracts.ProjectArtefact;
import org.openl.rules.workspace.abstracts.ProjectDescriptor;
import org.openl.rules.workspace.abstracts.ProjectException;
import org.openl.rules.workspace.deploy.DeploymentException;
import org.openl.rules.workspace.deploy.ProductionDeployer;
import org.openl.rules.workspace.dtr.DesignTimeRepository;
import org.openl.rules.workspace.dtr.RepositoryDDProject;
import org.openl.rules.workspace.dtr.RepositoryException;
import org.openl.rules.workspace.dtr.RepositoryProject;
import org.openl.rules.workspace.lw.LocalProject;
import org.openl.rules.workspace.lw.LocalWorkspace;
import org.openl.rules.workspace.uw.UserWorkspace;
import org.openl.rules.workspace.uw.UserWorkspaceDeploymentProject;
import org.openl.rules.workspace.uw.UserWorkspaceListener;
import org.openl.rules.workspace.uw.UserWorkspaceProject;
import org.openl.util.Log;

public class UserWorkspaceImpl implements UserWorkspace {

    private static final Comparator<UserWorkspaceProject> PROJECTS_COMPARATOR
    = new Comparator<UserWorkspaceProject>(){
        public int compare(UserWorkspaceProject o1, UserWorkspaceProject o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private final WorkspaceUser user;
    private final LocalWorkspace localWorkspace;
    private final DesignTimeRepository designTimeRepository;
    private final ProductionDeployer deployer;

    private final HashMap<String, UserWorkspaceProject> userRulesProjects;
    private final HashMap<String, UserWorkspaceDeploymentProject> userDProjects;

    private final List<UserWorkspaceListener> listeners = new ArrayList<UserWorkspaceListener>();

    public UserWorkspaceImpl(WorkspaceUser user, LocalWorkspace localWorkspace,
            DesignTimeRepository designTimeRepository, ProductionDeployer deployer) {
        this.user = user;
        this.localWorkspace = localWorkspace;
        this.designTimeRepository = designTimeRepository;
        this.deployer = deployer;

        userRulesProjects = new HashMap<String, UserWorkspaceProject>();
        userDProjects = new HashMap<String, UserWorkspaceDeploymentProject>();
    }

    public Collection<UserWorkspaceProject> getProjects() {
        check(PRIVILEGE_VIEW);

        try {
            refreshRulesProjects();
        } catch (ProjectException e) {
            // ignore
            Log.error("Failed to resfresh projects", e);
        }

        ArrayList<UserWorkspaceProject> result = new ArrayList<UserWorkspaceProject>(userRulesProjects.values());

        Collections.sort(result, PROJECTS_COMPARATOR);

        return result;
    }

    public UserWorkspaceProject getProject(String name) throws ProjectException {
        check(PRIVILEGE_VIEW);

        refreshRulesProjects();
        UserWorkspaceProject uwp = userRulesProjects.get(name);

        if (uwp == null) {
            throw new ProjectException("Cannot find project ''{0}''", null, name);
        }

        return uwp;
    }

    public boolean hasProject(String name) {
        if (userRulesProjects.get(name) != null) return true;
        if (localWorkspace.hasProject(name)) return true;
        if (designTimeRepository.hasProject(name)) return true;

        return false;
    }

    public boolean hasDDProject(String name) {
        if (userDProjects.get(name) != null) return true;
        if (designTimeRepository.hasDDProject(name)) return true;

        return false;
    }

    public ProjectArtefact getArtefactByPath(ArtefactPath artefactPath) throws ProjectException {
        check(PRIVILEGE_VIEW);

        String projectName = artefactPath.segment(0);
        UserWorkspaceProject uwp = getProject(projectName);

        ArtefactPath pathInProject = artefactPath.withoutFirstSegment();
        return uwp.getArtefactByPath(pathInProject);
    }

    public void activate() throws ProjectException {
        refresh();
    }

    public void passivate() {
        localWorkspace.saveAll();

        userRulesProjects.clear();
    }

    public void release() {
        localWorkspace.release();
        userRulesProjects.clear();

        for (UserWorkspaceListener listener : listeners)
            listener.workspaceReleased(this);
    }

    public void refresh() throws ProjectException {
        refreshRulesProjects();
        refreshDeploymentProjects();
    }

    protected void refreshRulesProjects() throws RepositoryException {
        localWorkspace.refresh();

        // add new
        for (RepositoryProject rp : designTimeRepository.getProjects()) {
            String name = rp.getName();

            LocalProject lp = null;
            if (localWorkspace.hasProject(name)) {
                try {
                    lp = localWorkspace.getProject(name);
                } catch (ProjectException e) {
                    // ignore
                }
            }

            UserWorkspaceProjectImpl uwp = (UserWorkspaceProjectImpl) userRulesProjects.get(name);
            if (uwp == null) {
                uwp = new UserWorkspaceProjectImpl(this, lp, rp);
                userRulesProjects.put(name, uwp);
            } else if (uwp.isLocalOnly()) {
                uwp.updateArtefact(lp, rp);
            }
        }

        // LocalProjects that hasn't corresponding project in DesignTimeRepository
        for (LocalProject lp : localWorkspace.getProjects()) {
            String name = lp.getName();

            if (!designTimeRepository.hasProject(name)) {

                UserWorkspaceProjectImpl uwp = (UserWorkspaceProjectImpl) userRulesProjects.get(name);
                if (uwp == null) {
                    uwp = new UserWorkspaceProjectImpl(this, lp, null);
                    userRulesProjects.put(name, uwp);
                } else {
                    uwp.updateArtefact(lp, null);
                }
            }
        }

        Iterator<Map.Entry<String,UserWorkspaceProject>> entryIterator = userRulesProjects.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, UserWorkspaceProject> entry = entryIterator.next();
            if (!designTimeRepository.hasProject(entry.getKey()) && !localWorkspace.hasProject(entry.getKey())) {
                entryIterator.remove();
            }
        }
    }

    public File getLocalWorkspaceLocation() {
        return localWorkspace.getLocation();
    }

    public void deploy(DeploymentDescriptorProject deployProject) throws DeploymentException, RepositoryException {
        check(PRIVILEGE_DEPLOY);

        Collection<ProjectDescriptor> projectDescriptors = deployProject.getProjectDescriptors();
        Collection<Project> projects = new ArrayList<Project>();
        for (ProjectDescriptor descriptor : projectDescriptors) {
            projects.add(designTimeRepository.getProject(descriptor.getProjectName(), descriptor.getProjectVersion()));
        }

        deployer.deploy(projects);
    }

    public void createProject(String name) throws ProjectException {
        check(PRIVILEGE_CREATE_EMPTY);

        designTimeRepository.createProject(name);

        refresh();
    }

    public void uploadLocalProject(String name) throws ProjectException {
        check(PRIVILEGE_CREATE);

        createProject(name);
        UserWorkspaceProjectImpl workspaceProject = (UserWorkspaceProjectImpl) getProject(name);
        workspaceProject.checkOutLocal();
        workspaceProject.checkIn();
    }

    // --- protected

    protected LocalProject openLocalProjectFor(RepositoryProject repositoryProject) throws ProjectException {
        check(PRIVILEGE_READ);

        return localWorkspace.addProject(repositoryProject);
    }

    protected LocalProject openLocalProjectFor(RepositoryProject repositoryProject, CommonVersion version) throws ProjectException {
        check(PRIVILEGE_READ);

        RepositoryProject oldRP = designTimeRepository.getProject(repositoryProject.getName(), version);
        return localWorkspace.addProject(oldRP);
    }

    protected RepositoryDDProject getDDProjectFor(RepositoryDDProject deploymentProject, CommonVersion version) throws ProjectException {
        check(PRIVILEGE_VIEW_DEPLOYMENT);

        RepositoryDDProject oldDP = designTimeRepository.getDDProject(deploymentProject.getName(), version);
        return oldDP;
    }

    protected void checkInProject(LocalProject localProject, int major, int minor) throws RepositoryException {
        check(PRIVILEGE_EDIT);

        designTimeRepository.updateProject(localProject, user, major, minor);
    }

    public WorkspaceUser getUser() {
        return user;
    }

    public void createDDProject(String name) throws RepositoryException {
        check(PRIVILEGE_CREATE_DEPLOYMENT);

        designTimeRepository.createDDProject(name);
    }

    public void copyProject(UserWorkspaceProject project, String name) throws ProjectException {
        check(PRIVILEGE_CREATE);

        designTimeRepository.copyProject(project, name, user);
        refresh();
    }

    public void copyDDProject(UserWorkspaceDeploymentProject project, String name) throws ProjectException {
        check(PRIVILEGE_CREATE_DEPLOYMENT);

        designTimeRepository.copyDDProject(project, name, user);
        refresh();
    }

    public void addWorkspaceListener(UserWorkspaceListener listener) {
        listeners.add(listener);
    }

    public boolean removeWorkspaceListener(UserWorkspaceListener listener) {
        return listeners.remove(listener);
    }

    public DesignTimeRepository getDesignTimeRepository() {
        /* todo: this method required only for deployment,
            but it is also used by LocalUploadController to check if a project is present in DTR,
            we should better introduce special method for this and after that
            check PRIVILEGE_DEPLOY  
         */
        check(PRIVILEGE_CREATE);

        return designTimeRepository;
    }

    public UserWorkspaceDeploymentProject getDDProject(String name) throws RepositoryException {
        check(PRIVILEGE_VIEW_DEPLOYMENT);

        try {
            RepositoryDDProject ddp = designTimeRepository.getDDProject(name);

            UserWorkspaceDeploymentProject userDProject = userDProjects.get(name);
            if (userDProject == null) {
                // create new
                userDProject = new UserWorkspaceDeploymentProjectImpl(this, ddp);

                userDProjects.put(name, userDProject);
            }

            return userDProject;
        } catch (RepositoryException e) {
            // no such project
            userDProjects.remove(name);
            // re-throw exception
            throw e;
        }
    }

    public List<UserWorkspaceDeploymentProject> getDDProjects() throws RepositoryException {
        check(PRIVILEGE_VIEW_DEPLOYMENT);
        
        refreshDeploymentProjects();

        ArrayList<UserWorkspaceDeploymentProject> result = new ArrayList<UserWorkspaceDeploymentProject>(userDProjects.values());
        Collections.sort(result, PROJECTS_COMPARATOR);

        return result;
    }

    protected void refreshDeploymentProjects() throws RepositoryException {
        List<RepositoryDDProject> dtrProjects = designTimeRepository.getDDProjects();

        // add new
        HashMap<String, RepositoryDDProject> dtrProjectsMap = new HashMap<String, RepositoryDDProject>();
        for (RepositoryDDProject ddp : dtrProjects) {
            String name = ddp.getName();
            dtrProjectsMap.put(name, ddp);

            UserWorkspaceDeploymentProjectImpl userDProject = (UserWorkspaceDeploymentProjectImpl) userDProjects.get(name);

            if (userDProject == null) {
                // add new
                userDProject = new UserWorkspaceDeploymentProjectImpl(this, ddp);
                userDProjects.put(name, userDProject);
            } else {
                // update existing
                userDProject.updateArtefact(ddp);
            }
        }

        // remove deleted
        Iterator<UserWorkspaceDeploymentProject> i = userDProjects.values().iterator();
        while (i.hasNext()) {
            UserWorkspaceDeploymentProject userDProject = i.next();
            String name = userDProject.getName();

            if (!dtrProjectsMap.containsKey(name)) {
                i.remove();
            }
        }
    }
}
