package org.openl.rules.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.commons.web.jsf.FacesUtils;
import org.openl.config.ConfigurationManager;
import org.openl.dependency.IDependencyManager;
import org.openl.dependency.loader.IDependencyLoader;
import org.openl.rules.project.abstraction.RulesProject;
import org.openl.rules.project.dependencies.ResolvingRulesProjectDependencyLoader;
import org.openl.rules.project.dependencies.RulesProjectDependencyManager;
import org.openl.rules.project.instantiation.ReloadType;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.model.ProjectDescriptor;
import org.openl.rules.project.resolving.RulesProjectResolver;
import org.openl.rules.runtime.RulesFileDependencyLoader;
import org.openl.rules.ui.tree.view.CategoryDetailedView;
import org.openl.rules.ui.tree.view.CategoryInversedView;
import org.openl.rules.ui.tree.view.CategoryView;
import org.openl.rules.ui.tree.view.FileView;
import org.openl.rules.ui.tree.view.RulesTreeView;
import org.openl.rules.ui.tree.view.TypeView;
import org.openl.rules.webstudio.web.admin.SystemSettingsBean;
import org.openl.rules.webstudio.web.servlet.RulesUserSession;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.rules.workspace.uw.UserWorkspace;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * TODO Remove JSF dependency
 * TODO Separate user session from app session
 * 
 * @author snshor
 */
public class WebStudio {

    interface StudioListener extends EventListener {
        void studioReset();
    }

    private final Log log = LogFactory.getLog(WebStudio.class);

    public static final String TRACER_NAME = "tracer";

    private final RulesTreeView TYPE_VIEW = new TypeView();
    private final RulesTreeView FILE_VIEW = new FileView();
    private final RulesTreeView CATEGORY_VIEW = new CategoryView();
    private final RulesTreeView CATEGORY_DETAILED_VIEW = new CategoryDetailedView();
    private final RulesTreeView CATEGORY_INVERSED_VIEW = new CategoryInversedView();

    private final RulesTreeView[] treeViews = { TYPE_VIEW, FILE_VIEW, CATEGORY_VIEW,
        CATEGORY_DETAILED_VIEW, CATEGORY_INVERSED_VIEW };

    private static final String USER_SETTINGS_FILENAME = "user-settings.properties";

    private String workspacePath;
    private ArrayList<BenchmarkInfoView> benchmarks = new ArrayList<BenchmarkInfoView>();
    private List<StudioListener> listeners = new ArrayList<StudioListener>();
    private String tableUri;
    private ProjectModel model = new ProjectModel(this);
    private RulesProjectResolver projectResolver;
    private List<ProjectDescriptor> projects = null;
    private boolean updateSystemProperties;

    private RulesTreeView treeView;
    private String tableView;
    private boolean showFormulas;

    private Module currentModule;

    private boolean collapseProperties = true;

    private RulesProjectDependencyManager dependencyManager;

    private ConfigurationManager systemConfigManager;
    private ConfigurationManager userSettingsManager;

    private boolean needRestart = false;

    public WebStudio(HttpSession session) {
        systemConfigManager = WebApplicationContextUtils.getWebApplicationContext(session.getServletContext())
            .getBean(ConfigurationManager.class);

        initWorkspace(session);
        initUserSettings(session);
        updateSystemProperties = systemConfigManager.getBooleanProperty(SystemSettingsBean.UPDATE_SYSTEM_PROPERTIES);
        initDependencyManager();
    }

    public WebStudio() {
        this(FacesUtils.getSession());
    }

    private void initWorkspace(HttpSession session) {
        UserWorkspace userWorkspace = WebStudioUtils.getUserWorkspace(session);

        if (userWorkspace == null) {
            return;
        }

        workspacePath = userWorkspace.getLocalWorkspace().getLocation().getAbsolutePath();
        projectResolver = RulesProjectResolver.loadProjectResolverFromClassPath();
        projectResolver.setWorkspace(workspacePath);
    }

    private void initUserSettings(HttpSession session) {
        String userMode = systemConfigManager.getStringProperty("user.mode");
        String settingsLocation = systemConfigManager.getStringProperty("user.settings.home")
                + (userMode != "single" ? File.separator + WebStudioUtils.getRulesUserSession(session).getUserName() : "")
                + File.separator + USER_SETTINGS_FILENAME;
        String defaultSettingsLocation = session.getServletContext().getRealPath(
                "/WEB-INF/conf/" + USER_SETTINGS_FILENAME);

        userSettingsManager = new ConfigurationManager(false, settingsLocation, defaultSettingsLocation, true);

        treeView = getTreeView(userSettingsManager.getStringProperty("rules.tree.view"));
        tableView = userSettingsManager.getStringProperty("table.view");
        showFormulas = userSettingsManager.getBooleanProperty("table.formulas.show");
    }

    private void initDependencyManager() {
        dependencyManager = new RulesProjectDependencyManager();
        dependencyManager.setExternalParameters(systemConfigManager.getProperties());

        dependencyManager.setExecutionMode(false);

        IDependencyLoader loader1 = new ResolvingRulesProjectDependencyLoader(projectResolver);
        IDependencyLoader loader2 = new RulesFileDependencyLoader();

        dependencyManager.setDependencyLoaders(Arrays.asList(loader1, loader2));    
    }

    public ConfigurationManager getSystemConfigManager() {
        return systemConfigManager;
    }

    public ConfigurationManager getUserSettingsManager() {
        return userSettingsManager;
    }

    public RulesTreeView[] getTreeViews() {
        return treeViews;
    }

    public void addBenchmark(BenchmarkInfoView bi) {
        benchmarks.add(0, bi);
    }

    public void addEventListener(StudioListener listener) {
        listeners.add(listener);
    }

    public void saveProject(HttpSession session) {
        try {
            RulesProject project = getCurrentProject(session);
            if (project == null) {
                return;
            }
            project.save();
            reset(ReloadType.FORCED);
            model.getProjectTree();
        } catch (Exception e) {
            log.error("Can not Save changes", e);
            // TODO Display message - e.getMessage()
        }
    }

    public void editProject(HttpSession session) {
        try {
            RulesProject project = getCurrentProject(session);
            if (project == null) {
                return;
            }
            project.edit();
            reset(ReloadType.FORCED);
            model.getProjectTree();
        } catch (Exception e) {
            log.error("Can not Open project in Edit mode", e);
            // TODO Display message - e.getMessage()
        }
    }

    public BenchmarkInfoView[] getBenchmarks() {
        return benchmarks.toArray(new BenchmarkInfoView[benchmarks.size()]);
    }

    /**
     * TODO Hold current project in session
     * */
    public RulesProject getCurrentProject(HttpSession session) {
        if (currentModule != null) {
            try {
                String projectFolder = currentModule.getProject().getProjectFolder().getName();
                RulesUserSession rulesUserSession = WebStudioUtils.getRulesUserSession(session);
                RulesProject project = rulesUserSession.getUserWorkspace().getProject(projectFolder);
                return project;
            } catch (Exception e) {
                log.error("Error when trying to get current project", e);
            }
        }
        return null;
    }

    public RulesProject getCurrentProject() {
        return getCurrentProject(FacesUtils.getSession());
    }

    public Module getCurrentModule() {
        return currentModule;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return Returns the RulesProjectResolver.
     */
    public RulesProjectResolver getProjectResolver() {
        return projectResolver;
    }

    public RulesTreeView getTreeView() {
        return treeView;
    }

    public String getTableView() {
        return tableView;
    }

    public void setTableView(String tableView) {
        this.tableView = tableView;
        userSettingsManager.setProperty("table.view", tableView);
    }
    
    public boolean isShowHeader() {
        return tableView.equals("developer");
    }

    public void setShowHeader(boolean showHeader) {
        setTableView(showHeader ? "developer" : "business");
    }

    public ProjectModel getModel() {
        return model;
    }

    public String getTableUri() {
        return tableUri;
    }

    /**
     * Returns path on the file system to user workspace this instance of web
     * studio works with.
     * 
     * @return path to openL projects workspace, i.e. folder containing openL
     *         projects.
     */
    public String getWorkspacePath() {
        return workspacePath;
    }

    public synchronized void invalidateProjects(){
        projects = null;
    }

    public synchronized List<ProjectDescriptor> getAllProjects() {
        if (projects == null) {
            projects = projectResolver.listOpenLProjects();
        }
        return projects;
    }

    public void removeBenchmark(int i) {
        benchmarks.remove(i);
    }

    public boolean removeListener(StudioListener listener) {
        return listeners.remove(listener);
    }

    public void reset(ReloadType reloadType) {
        try {
            if (reloadType == ReloadType.FORCED){
                invalidateProjects();
            }
            model.reset(reloadType);
            for (StudioListener listener : listeners) {
                listener.studioReset();
            }
        } catch (Exception e) {
            log.error("Error when trying to reset studio model", e);
        }
    }

    public void rebuildModel() {
        reset(ReloadType.SINGLE);
        model.buildProjectTree();
    }

    public void selectModule(String projectId, String moduleName) throws Exception {
        if (StringUtils.isBlank(projectId) || StringUtils.isBlank(moduleName)) {
            if (currentModule != null) {
                return;
            }

            if (getAllProjects().size() > 0) {
                setCurrentModule(getAllProjects().get(0).getModules().get(0));
            }
            return;
        }

        ProjectDescriptor project = getProject(projectId);
        if (project != null) {
            for (Module module : project.getModules()) {
                if (module.getName().equals(moduleName)) {
                    setCurrentModule(module);
                    return;
                }
            }
        }

        if (getAllProjects().size() > 0) {
            setCurrentModule(getAllProjects().get(0).getModules().get(0));
        }
    }

    public ProjectDescriptor getProject(final String id) {
        return (ProjectDescriptor) CollectionUtils.find(getAllProjects(), new Predicate() {
            public boolean evaluate(Object project) {
                return ((ProjectDescriptor) project).getId().equals(id);
            }
        });
    }

    /**
     * DOCUMENT ME!
     * 
     * @param module The current module to set.
     * 
     * @throws Exception
     */
    public void setCurrentModule(Module module) throws Exception {
        if (currentModule == null
                || !getModuleId(currentModule).equals(getModuleId(module))) {
            model.setModuleInfo(module);
            model.getRecentlyVisitedTables().clear();
        }
        currentModule = module;
        for (StudioListener listener : listeners) {
            listener.studioReset();
        }
    }

    public void setTreeView(RulesTreeView treeView) throws Exception {
        this.treeView = treeView;
        model.redraw();
        userSettingsManager.setProperty("rules.tree.view", treeView.getName());
    }

    public void setTreeView(String name) throws Exception {
        RulesTreeView mode = getTreeView(name);
        setTreeView(mode);
    }

    public RulesTreeView getTreeView(String name) {
        for (RulesTreeView mode : treeViews) {
            if (name.equals(mode.getName())) {
                return mode;
            }
        }
        return null;
    }

    public void setTableUri(String tableUri) {
        this.tableUri = tableUri;
    }

    public boolean isUpdateSystemProperties() {
        return updateSystemProperties;
    }

    public void setUpdateSystemProperties(boolean updateSystemProperties) {
        this.updateSystemProperties = updateSystemProperties;
        systemConfigManager.setProperty(SystemSettingsBean.UPDATE_SYSTEM_PROPERTIES, updateSystemProperties);
    }

    public boolean isShowFormulas() {
        return showFormulas;
    }

    public void setShowFormulas(boolean showFormulas) {
        this.showFormulas = showFormulas;
        userSettingsManager.setProperty("table.formulas.show", showFormulas);
    }

    public boolean isCollapseProperties() {
        return collapseProperties;
    }

    public void setCollapseProperties(boolean collapseProperties) {
        this.collapseProperties = collapseProperties;
    }

    public String getModuleId(Module module) {
        if (module != null) {
            return module.getProject().getId() + " - " + module.getName();
        }
        return null;
    }

    public TraceHelper getTraceHelper() {
        TraceHelper traceHelper = (TraceHelper) FacesUtils.getSessionParam(TRACER_NAME);

        if (traceHelper == null) {
            traceHelper = new TraceHelper();
            Map<String, Object> sessionMap = FacesUtils.getSessionMap();
            sessionMap.put(TRACER_NAME, traceHelper);
        }

        return traceHelper;
    }

    public IDependencyManager getDependencyManager() {
        return dependencyManager;
    }

    public void setNeedRestart(boolean needRestart) {
        this.needRestart = needRestart;
    }

    public boolean isNeedRestart() {
        return needRestart;
    }

}
