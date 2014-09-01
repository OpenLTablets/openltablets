package org.openl.rules.ui;

import com.thoughtworks.xstream.XStreamException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.openl.commons.web.jsf.FacesUtils;
import org.openl.config.ConfigurationManager;
import org.openl.rules.common.CommonException;
import org.openl.rules.common.ProjectException;
import org.openl.rules.lang.xls.IXlsTableNames;
import org.openl.rules.lang.xls.XlsWorkbookSourceHistoryListener;
import org.openl.rules.project.abstraction.RulesProject;
import org.openl.rules.project.instantiation.ReloadType;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.model.ProjectDependencyDescriptor;
import org.openl.rules.project.model.ProjectDescriptor;
import org.openl.rules.project.resolving.ProjectDescriptorArtefactResolver;
import org.openl.rules.project.resolving.RulesProjectResolver;
import org.openl.rules.ui.tree.view.*;
import org.openl.rules.webstudio.util.ExportModule;
import org.openl.rules.webstudio.util.NameChecker;
import org.openl.rules.webstudio.web.admin.AdministrationSettings;
import org.openl.rules.webstudio.web.repository.upload.ProjectDescriptorUtils;
import org.openl.rules.webstudio.web.repository.upload.ZipProjectDescriptorExtractor;
import org.openl.rules.webstudio.web.repository.upload.zip.DefaultZipEntryCommand;
import org.openl.rules.webstudio.web.repository.upload.zip.FilePathsCollector;
import org.openl.rules.webstudio.web.repository.upload.zip.ZipWalker;
import org.openl.rules.webstudio.web.servlet.RulesUserSession;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.rules.workspace.WorkspaceUserImpl;
import org.openl.rules.workspace.filter.PathFilter;
import org.openl.rules.workspace.uw.UserWorkspace;
import org.openl.rules.workspace.uw.impl.ProjectExportHelper;
import org.openl.util.FileTypeHelper;
import org.openl.util.StringTool;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.ValidationException;
import java.io.*;
import java.util.*;

/**
 * TODO Remove JSF dependency
 * TODO Separate user session from app session
 * TODO Move settings to separate UserSettings class
 *
 * @author snshor
 */
public class WebStudio {

    interface StudioListener extends EventListener {
        void studioReset();
    }

    private final Logger log = LoggerFactory.getLogger(WebStudio.class);

    public static final String TRACER_NAME = "tracer";

    private final RulesTreeView TYPE_VIEW = new TypeView();
    private final RulesTreeView FILE_VIEW = new FileView();
    private final RulesTreeView CATEGORY_VIEW = new CategoryView();
    private final RulesTreeView CATEGORY_DETAILED_VIEW = new CategoryDetailedView();
    private final RulesTreeView CATEGORY_INVERSED_VIEW = new CategoryInversedView();

    private final RulesTreeView[] treeViews = {TYPE_VIEW, FILE_VIEW, CATEGORY_VIEW, CATEGORY_DETAILED_VIEW,
            CATEGORY_INVERSED_VIEW};

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
    private int testsPerPage;
    private boolean testsFailuresOnly;
    private int testsFailuresPerTest;
    private boolean showComplexResult;
    private boolean singleModuleModeByDefault;

    private ProjectDescriptor currentProject;
    private Module currentModule;

    private boolean collapseProperties = true;

    private ConfigurationManager systemConfigManager;
    private ConfigurationManager userSettingsManager;

    private boolean needRestart = false;

    private List<UploadedFile> uploadedFiles = new ArrayList<UploadedFile>();

    public WebStudio(HttpSession session) {
        systemConfigManager = (ConfigurationManager) WebApplicationContextUtils.getWebApplicationContext(
                session.getServletContext()).getBean("configManager");

        initWorkspace(session);
        initUserSettings(session);
        updateSystemProperties = systemConfigManager
                .getBooleanProperty(AdministrationSettings.UPDATE_SYSTEM_PROPERTIES);
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
                + (!userMode.equals("single") ? (File.separator + WebStudioUtils.getRulesUserSession(session)
                .getUserName()) : "") + File.separator + USER_SETTINGS_FILENAME;
        String defaultSettingsLocation = session.getServletContext().getRealPath(
                "/WEB-INF/conf/" + USER_SETTINGS_FILENAME);

        userSettingsManager = new ConfigurationManager(false, settingsLocation, defaultSettingsLocation, true);

        treeView = getTreeView(userSettingsManager.getStringProperty("rules.tree.view"));
        tableView = userSettingsManager.getStringProperty("table.view");
        showFormulas = userSettingsManager.getBooleanProperty("table.formulas.show");
        testsPerPage = userSettingsManager.getIntegerProperty("test.tests.perpage");
        testsFailuresOnly = userSettingsManager.getBooleanProperty("test.failures.only");
        testsFailuresPerTest = userSettingsManager.getIntegerProperty("test.failures.pertest");
        showComplexResult = userSettingsManager.getBooleanProperty("test.result.complex.show");
        singleModuleModeByDefault = userSettingsManager.getBooleanProperty("project.dependency.modules.single");
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

    public RulesProject getCurrentProject(HttpSession session) {
        if (currentProject != null) {
            try {
                String projectFolder = currentProject.getProjectFolder().getName();
                RulesUserSession rulesUserSession = WebStudioUtils.getRulesUserSession(session);
                return rulesUserSession.getUserWorkspace().getProject(projectFolder, false);
            } catch (Exception e) {
                log.error("Error when trying to get current project", e);
            }
        }
        return null;
    }

    public String exportModule() {
        try {
            File file = new File(currentModule.getRulesRootPath().getPath());
            if (file.isDirectory() || !file.exists()) {
                throw new ProjectException("Exporting module was failed");
            }

            final FacesContext facesContext = FacesUtils.getFacesContext();
            HttpServletResponse response = (HttpServletResponse) FacesUtils.getResponse();
            ExportModule.writeOutContent(response, file, file.getName());
            facesContext.responseComplete();
        } catch (ProjectException e) {
            log.error("Failed to export module", e);
        }
        return null;
    }

    public String exportProject() {
        File file = null;
        try {
            RulesProject forExport = getCurrentProject();
            String userName = WebStudioUtils.getRulesUserSession(FacesUtils.getSession()).getUserName();

            String fileName = String.format("%s-%s.zip", forExport.getName(), forExport.getVersion().getVersionName());
            file = ProjectExportHelper.export(new WorkspaceUserImpl(userName), forExport);

            final FacesContext facesContext = FacesUtils.getFacesContext();
            HttpServletResponse response = (HttpServletResponse) FacesUtils.getResponse();

            ExportModule.writeOutContent(response, file, fileName);
            facesContext.responseComplete();
        } catch (ProjectException e) {
            log.error("Failed to export module", e);
        } finally {
            FileUtils.deleteQuietly(file);
        }
        return null;
    }

    public RulesProject getCurrentProject() {
        return getCurrentProject(FacesUtils.getSession());
    }

    public ProjectDescriptor getCurrentProjectDescriptor() {
        return currentProject;
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
        return tableView.equals(IXlsTableNames.VIEW_DEVELOPER);
    }

    public void setShowHeader(boolean showHeader) {
        setTableView(showHeader ? IXlsTableNames.VIEW_DEVELOPER : IXlsTableNames.VIEW_BUSINESS);
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
     * projects.
     */
    public String getWorkspacePath() {
        return workspacePath;
    }

    public synchronized void invalidateProjects() {
        try {
            WebStudioUtils.getRulesUserSession(FacesUtils.getSession()).getUserWorkspace().refresh();
        } catch (CommonException e) {
            log.error(e.getMessage(), e);
        }

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
            if (reloadType == ReloadType.FORCED) {
                invalidateProjects();
                if (currentProject != null) {
                    String projectName = currentProject.getName();
                    if (currentModule != null) {
                        String moduleName = currentModule.getName();
                        currentProject = null; // To reload current project
                        selectModule(projectName, moduleName);
                    } else {
                        selectProject(projectName);
                    }
                }
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

    public void selectProject(String name) throws Exception {
        if (StringUtils.isBlank(name)) {
            if (currentProject != null) {
                return;
            }

            if (getAllProjects().size() > 0) {
                currentProject = getAllProjects().get(0);
            }
            return;
        }

        currentProject = getProjectByName(name);

        if (currentProject == null && getAllProjects().size() > 0) {
            currentProject = getAllProjects().get(0);
        }

        currentModule = null;
    }

    public void selectModule(String projectName, String moduleName) throws Exception {
        if (StringUtils.isBlank(projectName) || StringUtils.isBlank(moduleName)) {
            if (currentModule != null) {
                return;
            }

            if (getAllProjects().size() > 0) {
                setCurrentModule(getAllProjects().get(0).getModules().get(0));
            }
            return;
        }

        ProjectDescriptor project;
        if (currentProject != null && projectName.equals(currentProject.getName())) {
            project = currentProject;
        } else {
            project = getProjectByName(projectName);
        }
        if (project != null) {
            Module module = getModule(project, moduleName);
            if (module != null) {
                setCurrentModule(module);
                return;
            }
        }

        if (getAllProjects().size() > 0) {
            setCurrentModule(getAllProjects().get(0).getModules().get(0));
        }
    }

    public Module getModule(ProjectDescriptor project, String moduleName) {
        for (Module module : project.getModules()) {
            if (module.getName().equals(moduleName)) {
                return module;
            }
        }
        return null;
    }

    public String updateModule() {
        UploadedFile uploadedFile = getLastUploadedFile();
        if (uploadedFile == null) {
            // TODO Display message - e.getMessage()
            return null;
        }

        try {

            XlsWorkbookSourceHistoryListener historyListener = new XlsWorkbookSourceHistoryListener(
                    model.getHistoryManager());
            historyListener.beforeSave(model.getCurrentModuleWorkbook());

            Module module = getCurrentModule();
            OutputStream outputStream = null;
            InputStream inputStream = null;
            try {
                outputStream = new FileOutputStream(module.getRulesRootPath().getPath());
                inputStream = uploadedFile.getInputStream();
                IOUtils.copy(inputStream, outputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }

            historyListener.afterSave(model.getCurrentModuleWorkbook());
        } catch (Exception e) {
            log.error("Error updating file in user workspace.", e);
            // TODO Display message - e.getMessage()
        }

        reset(ReloadType.FORCED);
        rebuildModel();
        clearUploadedFiles();

        return null;
    }

    public String updateProject() {
        UploadedFile lastUploadedFile = getLastUploadedFile();
        if (lastUploadedFile == null) {
            // TODO Replace exceptions with FacesUtils.addErrorMessage()
            throw new IllegalArgumentException("No file was uploaded. Please upload .zip file to update project");
        }
        if (!FileTypeHelper.isZipFile(FilenameUtils.getName(lastUploadedFile.getName()))) {
            // TODO Replace exceptions with FacesUtils.addErrorMessage()
            throw new IllegalArgumentException("Wrong filename extension. Please upload .zip file");
        }
        try {
            ProjectDescriptor projectDescriptor = getCurrentProjectDescriptor();
            PathFilter filter = getZipFilter();

            String errorMessage = validateUploadedFiles(lastUploadedFile, filter, projectDescriptor);
            if (errorMessage != null) {
                // TODO Replace exceptions with FacesUtils.addErrorMessage()
                throw new ValidationException(errorMessage);
            }

            ZipWalker zipWalker = new ZipWalker(lastUploadedFile, filter);

            FilePathsCollector filesCollector = new FilePathsCollector();
            zipWalker.iterateEntries(filesCollector);
            List<String> filesInZip = filesCollector.getFilePaths();

            final File projectFolder = projectDescriptor.getProjectFolder();
            Collection<File> files = getProjectFiles(projectFolder, filter);

            // Delete absent files in project
            for (File file : files) {
                String relative = getRelativePath(projectFolder, file);
                if (!filesInZip.contains(relative)) {
                    FileUtils.deleteQuietly(file);
                }
            }

            // Update/create other files in project
            final XlsWorkbookSourceHistoryListener historyListener = new XlsWorkbookSourceHistoryListener(
                    model.getHistoryManager());
            zipWalker.iterateEntries(new DefaultZipEntryCommand() {
                @Override
                public boolean execute(String filePath, InputStream inputStream) throws IOException {
                    File outputFile = new File(projectFolder, filePath);
                    historyListener.beforeSave(outputFile);

                    OutputStream outputStream = null;
                    try {
                        File folder = outputFile.getParentFile();
                        if (folder != null && !folder.mkdirs() && !folder.isDirectory()) {
                            throw new IOException("Can't create parent folder");
                        }
                        outputStream = new FileOutputStream(outputFile);
                        IOUtils.copy(inputStream, outputStream);
                    } finally {
                        IOUtils.closeQuietly(outputStream);
                    }
                    historyListener.afterSave(outputFile);

                    return true;
                }
            });
        } catch (ValidationException e) {
            // TODO Replace exceptions with FacesUtils.addErrorMessage()
            throw e;
        } catch (Exception e) {
            log.error("Error while updating project in user workspace.", e);
            // TODO Replace exceptions with FacesUtils.addErrorMessage()
            throw new IllegalStateException("Error while updating project in user workspace.", e);
        }

        reset(ReloadType.FORCED);
        rebuildModel();
        clearUploadedFiles();

        return null;
    }

    public boolean isUploadedProjectStructureChanged() {
        UploadedFile lastUploadedFile = getLastUploadedFile();
        if (lastUploadedFile == null) {
            return false;
        }
        try {
            PathFilter filter = getZipFilter();
            ZipWalker zipWalker = new ZipWalker(lastUploadedFile, filter);

            FilePathsCollector filesCollector = new FilePathsCollector();
            zipWalker.iterateEntries(filesCollector);
            List<String> filesInZip = filesCollector.getFilePaths();

            final File projectFolder = getCurrentProjectDescriptor().getProjectFolder();
            Collection<File> files = getProjectFiles(projectFolder, filter);
            final List<String> filesInProject = new ArrayList<String>();
            for (File file : files) {
                filesInProject.add(getRelativePath(projectFolder, file));
            }

            for (String filePath : filesInProject) {
                if (!filesInZip.contains(filePath)) {
                    // Deleted file
                    return true;
                }
            }

            for (String filePath : filesInZip) {
                if (!filesInProject.contains(filePath)) {
                    // Added file
                    return true;
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    private String validateUploadedFiles(UploadedFile zipFile, PathFilter zipFilter, ProjectDescriptor oldProjectDescriptor) throws IOException, ProjectException {
        ProjectDescriptor newProjectDescriptor;
        try {
            newProjectDescriptor = ZipProjectDescriptorExtractor.getProjectDescriptorOrThrow(zipFile, zipFilter);
        } catch (XStreamException e) {
            return ProjectDescriptorUtils.getErrorMessage(e);
        }
        if (newProjectDescriptor != null && !newProjectDescriptor.getName().equals(oldProjectDescriptor.getName())) {
            return validateProjectName(newProjectDescriptor.getName());
        }

        return null;
    }

    private String validateProjectName(String projectName) throws ProjectException {
        String msg = null;
        if (StringUtils.isBlank(projectName)) {
            msg = "Project name must not be empty.";
        } else if (!NameChecker.checkName(projectName)) {
            msg = NameChecker.BAD_PROJECT_NAME_MSG;
        } else if (isProjectExists(projectName)) {
            msg = "Cannot update the project. Another project with the same name already exists in Repository.";
        }
        return msg;
    }

    private ProjectDescriptorArtefactResolver getProjectDescriptorResolver() {
        return (ProjectDescriptorArtefactResolver) WebApplicationContextUtils.
                getWebApplicationContext(FacesUtils.getServletContext()).getBean("projectDescriptorArtefactResolver");
    }

    private PathFilter getZipFilter() {
        return (PathFilter) WebApplicationContextUtils.getWebApplicationContext(FacesUtils.getServletContext()).getBean("zipFilter");
    }

    private Collection<File> getProjectFiles(File projectFolder, final PathFilter filter) {
        IOFileFilter fileFilter = new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                String path = file.getPath().replace(File.separator, "/");
                if (file.isDirectory() && !path.endsWith("/")) {
                    path += "/";
                }
                return filter.accept(path);
            }

            @Override
            public boolean accept(File dir, String name) {
                return accept(new File(dir, name));
            }
        };
        return FileUtils.listFiles(projectFolder, fileFilter, fileFilter);
    }

    private String getRelativePath(File baseFolder, File file) {
        return baseFolder.toURI().relativize(file.toURI()).getPath().replace("\\", "/");
    }

    private UploadedFile getLastUploadedFile() {
        if (!uploadedFiles.isEmpty()) {
            return uploadedFiles.get(uploadedFiles.size() - 1);
        }
        return null;
    }

    public ProjectDescriptor getProjectByName(final String name) {
        return CollectionUtils.find(getAllProjects(), new Predicate<ProjectDescriptor>() {
            public boolean evaluate(ProjectDescriptor project) {
                return project.getName().equals(name);
            }
        });
    }

    public ProjectDependencyDescriptor getProjectDependency(final String dependencyName) {
        List<ProjectDependencyDescriptor> dependencies = getCurrentProjectDescriptor().getDependencies();
        return CollectionUtils.find(dependencies, new Predicate<ProjectDependencyDescriptor>() {
            public boolean evaluate(ProjectDependencyDescriptor dependency) {
                return dependency.getName().equals(dependencyName);
            }
        });
    }

    /**
     * Checks if there is any project with specified name in repository.
     *
     * @param name physical or logical project name
     * @return true only if there is a project with specified name and it is not current project
     */
    public boolean isProjectExists(final String name) {
        HttpSession session = FacesUtils.getSession();
        UserWorkspace userWorkspace = WebStudioUtils.getUserWorkspace(session);

        if (userWorkspace.hasProject(name)) {
            try {
                if (getCurrentProject() != userWorkspace.getProject(name)) {
                    return true;
                }
            } catch (ProjectException e) {
                // Should not occur
                log.error(e.getMessage(), e);
            }
        }

        ProjectDescriptorArtefactResolver projectDescriptorResolver = getProjectDescriptorResolver();
        for (RulesProject rulesProject : userWorkspace.getProjects()) {
            if (getCurrentProject() == rulesProject) {
                continue;
            }
            if (projectDescriptorResolver.getLogicalName(rulesProject).equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param module The current module to set.
     * @throws Exception
     */
    public void setCurrentModule(Module module) throws Exception {
        if (currentModule == null || !getModuleId(currentModule).equals(getModuleId(module))) {
            model.setModuleInfo(module);
            model.getRecentlyVisitedTables().clear();
        }

        currentModule = module;
        currentProject = currentModule != null ? currentModule.getProject() : null;

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
        systemConfigManager.setProperty(AdministrationSettings.UPDATE_SYSTEM_PROPERTIES, updateSystemProperties);
    }

    public boolean isShowFormulas() {
        return showFormulas;
    }

    public void setShowFormulas(boolean showFormulas) {
        this.showFormulas = showFormulas;
        userSettingsManager.setProperty("table.formulas.show", showFormulas);
    }

    public int getTestsPerPage() {
        return testsPerPage;
    }

    public void setTestsPerPage(int testsPerPage) {
        this.testsPerPage = testsPerPage;
        userSettingsManager.setProperty("test.tests.perpage", testsPerPage);
    }

    public boolean isTestsFailuresOnly() {
        return testsFailuresOnly;
    }

    public void setTestsFailuresOnly(boolean testsFailuresOnly) {
        this.testsFailuresOnly = testsFailuresOnly;
        userSettingsManager.setProperty("test.failures.only", testsFailuresOnly);
    }

    public int getTestsFailuresPerTest() {
        return testsFailuresPerTest;
    }

    public void setTestsFailuresPerTest(int testsFailuresPerTest) {
        this.testsFailuresPerTest = testsFailuresPerTest;
        userSettingsManager.setProperty("test.failures.pertest", testsFailuresPerTest);
    }

    public boolean isCollapseProperties() {
        return collapseProperties;
    }

    public void setCollapseProperties(boolean collapseProperties) {
        this.collapseProperties = collapseProperties;
    }

    public boolean isShowComplexResult() {
        return showComplexResult;
    }

    public void setShowComplexResult(boolean showComplexResult) {
        this.showComplexResult = showComplexResult;
        userSettingsManager.setProperty("test.result.complex.show", showComplexResult);
    }

    public boolean isSingleModuleModeByDefault() {
        return singleModuleModeByDefault;
    }

    public void setSingleModuleModeByDefault(boolean singleModuleModeByDefault) {
        this.singleModuleModeByDefault = singleModuleModeByDefault;
        userSettingsManager.setProperty("project.dependency.modules.single", singleModuleModeByDefault);
    }

    public String getModuleId(Module module) {
        if (module != null) {
            return module.getProject().getName() + " - " + module.getName();
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

    public void setNeedRestart(boolean needRestart) {
        this.needRestart = needRestart;
    }

    public boolean isNeedRestart() {
        return needRestart;
    }

    public void uploadListener(FileUploadEvent event) {
        UploadedFile file = event.getUploadedFile();
        uploadedFiles.add(file);
    }

    public void destroy() {
        if (model != null) {
            model.destroy();
        }
    }

    public void clearUploadedFiles() {
        uploadedFiles.clear();
    }

    /**
     * Normalizes page urls: inserts project and module names.
     * <p/>
     * Example:
     * Page Url =       create/
     * Normalized Url = #tutorial1/rules/create/
     */
    public String url(String pageUrl) {
        return url(pageUrl, null);
    }

    public String url(String pageUrl, String moduleName) {
        String projectName = getCurrentProjectDescriptor().getName();
        if (moduleName == null) {
            moduleName = getCurrentModule().getName();
        }
        if (StringUtils.isBlank(pageUrl)) {
            pageUrl = StringUtils.EMPTY;
        }

        if ((StringUtils.isBlank(projectName) || StringUtils.isBlank(moduleName))
                && StringUtils.isNotBlank(pageUrl)) {
            return "#" + pageUrl;
        }

        return "#" + StringTool.encodeURL(projectName) + "/" + StringTool.encodeURL(moduleName) + "/" + pageUrl;
    }

    public String getModuleName(String uri) {
        return getCurrentProjectDescriptor().getModuleByUri(uri);
    }
}
