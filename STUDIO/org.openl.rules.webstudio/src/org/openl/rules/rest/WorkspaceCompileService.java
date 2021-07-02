package org.openl.rules.rest;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openl.CompiledOpenClass;
import org.openl.dependency.CompiledDependency;
import org.openl.message.OpenLErrorMessage;
import org.openl.message.OpenLMessage;
import org.openl.message.OpenLMessagesUtils;
import org.openl.message.Severity;
import org.openl.rules.lang.xls.TableSyntaxNodeUtils;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.project.dependencies.ProjectExternalDependenciesHelper;
import org.openl.rules.project.instantiation.IDependencyLoader;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.model.ProjectDependencyDescriptor;
import org.openl.rules.project.model.ProjectDescriptor;
import org.openl.rules.table.IOpenLTable;
import org.openl.rules.testmethod.ProjectHelper;
import org.openl.rules.testmethod.TestSuiteMethod;
import org.openl.rules.ui.ProjectModel;
import org.openl.rules.ui.WebStudio;
import org.openl.rules.webstudio.dependencies.WebStudioWorkspaceRelatedDependencyManager;
import org.openl.rules.webstudio.web.MessageHandler;
import org.openl.rules.webstudio.web.tableeditor.TableBean;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.types.IOpenMethod;
import org.openl.util.CollectionUtils;
import org.openl.util.StringUtils;
import org.openl.validation.ValidatedCompiledOpenClass;
import org.springframework.stereotype.Service;

@Service
@Path("/compile/")
@Produces(MediaType.APPLICATION_JSON)
public class WorkspaceCompileService {

    private static final MessageHandler messageHandler = new MessageHandler();

    private static final int MAX_PROBLEMS = 100;

    @GET
    @Path("progress/{messageId}/{messageIndex}")
    public Map<String, Object> getCompile(@PathParam("messageId") final Long messageId,
            @PathParam("messageIndex") final Integer messageIndex) {
        Map<String, Object> compileModuleInfo = new HashMap<>();
        WebStudio webStudio = WebStudioUtils.getWebStudio(WebStudioUtils.getSession());
        if (webStudio != null) {
            ProjectModel model = webStudio.getModel();
            Module moduleInfo = model.getModuleInfo();
            WebStudioWorkspaceRelatedDependencyManager webStudioWorkspaceDependencyManager = model
                .getWebStudioWorkspaceDependencyManager();
            if (webStudioWorkspaceDependencyManager != null) {
                MessageCounter messageCounter = new MessageCounter();
                int compiledCount = 0;
                int modulesCount = 0;
                List<MessageDescription> newMessages = new ArrayList<>();
                Deque<ProjectDescriptor> queue = new ArrayDeque<>();
                queue.add(moduleInfo.getProject());
                while (!queue.isEmpty()) {
                    ProjectDescriptor projectDescriptor = queue.poll();
                    String dependencyName = ProjectExternalDependenciesHelper
                        .buildDependencyNameForProject(projectDescriptor.getName());
                    Collection<IDependencyLoader> loadersForProject = webStudioWorkspaceDependencyManager
                        .findDependencyLoadersByName(dependencyName);
                    for (IDependencyLoader dependencyLoader : loadersForProject) {
                        CompiledDependency compiledDependency = dependencyLoader.getRefToCompiledDependency();
                        if (compiledDependency != null) {
                            processMessages(compiledDependency.getCompiledOpenClass().getCurrentMessages(),
                                messageCounter,
                                model,
                                newMessages);
                        }
                    }
                    for (Module module : projectDescriptor.getModules()) {
                        Collection<IDependencyLoader> dependencyLoadersForModule = webStudioWorkspaceDependencyManager
                            .findDependencyLoadersByName(module.getName());
                        for (IDependencyLoader dependencyLoader : dependencyLoadersForModule) {
                            CompiledDependency compiledDependency = dependencyLoader.getRefToCompiledDependency();
                            if (compiledDependency != null) {
                                processMessages(compiledDependency.getCompiledOpenClass().getCurrentMessages(),
                                    messageCounter,
                                    model,
                                    newMessages);
                                compiledCount++;
                            }
                            modulesCount++;
                        }
                    }
                    if (projectDescriptor.getDependencies() != null) {
                        for (ProjectDependencyDescriptor pd : projectDescriptor.getDependencies()) {
                            String projectDependencyName = ProjectExternalDependenciesHelper
                                .buildDependencyNameForProject(pd.getName());
                            Collection<IDependencyLoader> dependencyLoadersForProject = webStudioWorkspaceDependencyManager
                                .findDependencyLoadersByName(projectDependencyName);
                            if (dependencyLoadersForProject != null) {
                                for (IDependencyLoader dependencyLoader : dependencyLoadersForProject) {
                                    if (dependencyLoader != null && dependencyLoader.isProjectLoader()) {
                                        queue.add(dependencyLoader.getProject());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                CompiledOpenClass compiledOpenClass = model.getCompiledOpenClass();
                if (compiledOpenClass instanceof ValidatedCompiledOpenClass) {
                    processMessages(((ValidatedCompiledOpenClass) compiledOpenClass).getValidationMessages(),
                        messageCounter,
                        model,
                        newMessages);
                }
                compileModuleInfo.put("dataType", "new");
                if (messageIndex != -1 && messageId != -1) {
                    MessageDescription messageDescription = newMessages.get(messageIndex);
                    if (messageDescription.getId() == messageId) {
                        newMessages = newMessages.subList(messageIndex + 1, newMessages.size());
                        compileModuleInfo.put("dataType", "add");
                    }
                }
                if (model.isProjectCompilationCompleted()) {
                    compiledCount = modulesCount;
                }
                compileModuleInfo.put("modulesCount", modulesCount);
                compileModuleInfo.put("modulesCompiled", compiledCount);
                compileModuleInfo.put("messages", newMessages);
                compileModuleInfo.put("messageId",
                    newMessages.isEmpty() ? -1 : newMessages.get(newMessages.size() - 1).getId());
                compileModuleInfo.put("messageIndex", newMessages.size() - 1);
                compileModuleInfo.put("errorsCount", messageCounter.errorsCount);
                compileModuleInfo.put("warningsCount", messageCounter.warningsCount);
                compileModuleInfo.put("compilationCompleted", model.isProjectCompilationCompleted());
            }
        }
        return compileModuleInfo;
    }

    @GET
    @Path("tests/{tableId}")
    public Map<String, Object> getCompile(@PathParam("tableId") final String tableId) {
        Map<String, Object> tableTestsInfo = new HashMap<>();
        WebStudio webStudio = WebStudioUtils.getWebStudio(WebStudioUtils.getSession());
        List<TableBean.TableDescription> tableDescriptions = new ArrayList<>();
        if (webStudio != null) {
            ProjectModel model = webStudio.getModel();
            IOpenLTable table = model.getTableById(tableId);
            IOpenMethod[] allTests = model.getTestAndRunMethods(table.getUri(), false);

            if (allTests != null) {
                for (IOpenMethod test : allTests) {
                    TableSyntaxNode syntaxNode = (TableSyntaxNode) test.getInfo().getSyntaxNode();
                    tableDescriptions.add(new TableBean.TableDescription(webStudio.url("table", syntaxNode.getUri()),
                        syntaxNode.getId(),
                        getTestName(test)));
                }
                tableDescriptions.sort(Comparator.comparing(TableBean.TableDescription::getName));
            }

            tableTestsInfo.put("allTests", tableDescriptions);
            tableTestsInfo.put("compiled", model.isProjectCompilationCompleted());
        }
        return tableTestsInfo;
    }

    @GET
    @Path("tests")
    public Map<String, Object> tests() {
        Map<String, Object> moduleTestsInfo = new HashMap<>();
        WebStudio studio = WebStudioUtils.getWebStudio(WebStudioUtils.getSession());
        ProjectModel model = studio.getModel();
        Module moduleInfo = model.getModuleInfo();
        TestSuiteMethod[] allTestMethods = model.getAllTestMethods();
        moduleTestsInfo.put("count", CollectionUtils.isNotEmpty(allTestMethods) ? allTestMethods.length : 0);
        moduleTestsInfo.put("compiled", model.isProjectCompilationCompleted());
        moduleTestsInfo.put("openCurrentModuleOnly",
            moduleInfo != null ? moduleInfo.getOpenCurrentModuleOnly() : false);
        return moduleTestsInfo;
    }

    @GET
    @Path("project")
    public boolean project() {
        WebStudio webStudio = WebStudioUtils.getWebStudio(WebStudioUtils.getSession());
        return webStudio.getModel().isProjectCompilationCompleted();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("error/{errorId}/{openedModule}")
    public String error(@PathParam("errorId") final long errorId,
            @PathParam("openedModule") final boolean openedModule) {
        ProjectModel model = WebStudioUtils.getWebStudio(WebStudioUtils.getSession()).getModel();
        Collection<OpenLMessage> errors = openedModule ? model.getOpenedModuleMessages() : model.getModuleMessages();
        Optional<OpenLMessage> error = errors.stream().filter(m -> m.getId() == errorId).findFirst();
        if (error.isPresent()) {
            OpenLMessage message = error.get();
            if (message instanceof OpenLErrorMessage) {
                OpenLErrorMessage errorMessage = (OpenLErrorMessage) message;
                return ExceptionUtils.getStackTrace((Throwable) errorMessage.getError());
            }
        }
        return null;
    }

    @GET
    @Path("table/{tableId}/{openedModule}")
    public Map<String, Object> table(@PathParam("tableId") final String tableId,
            @PathParam("openedModule") final boolean openedModule) {
        WebStudio webStudio = WebStudioUtils.getWebStudio(WebStudioUtils.getSession());
        ProjectModel model = webStudio.getModel();
        Module moduleInfo = model.getModuleInfo();
        IOpenLTable table = model.getTableById(tableId);
        String tableUri = table.getUri();
        List<OpenLMessage> errors;
        List<OpenLMessage> warnings;
        if (openedModule) {
            errors = new ArrayList<>(model.getOpenedModuleMessagesByTsn(tableUri, Severity.ERROR));
            warnings = new ArrayList<>(model.getOpenedModuleMessagesByTsn(tableUri, Severity.WARN));
        } else {
            errors = new ArrayList<>(model.getMessagesByTsn(tableUri, Severity.ERROR));
            warnings = new ArrayList<>(model.getMessagesByTsn(tableUri, Severity.WARN));
        }

        if (warnings.size() >= MAX_PROBLEMS) {
            warnings = warnings.subList(0, MAX_PROBLEMS);
            warnings.add(OpenLMessagesUtils
                .newErrorMessage("Only first " + MAX_PROBLEMS + " warnings are shown. Fix them first."));
        }
        if (errors.size() >= MAX_PROBLEMS) {
            errors = errors.subList(0, MAX_PROBLEMS);
            errors.add(OpenLMessagesUtils
                .newErrorMessage("Only first " + MAX_PROBLEMS + " errors are shown. Fix them first."));
        }

        // if the current table is a test then check tested target tables on errors.
        List<Pair<String, TableBean.TableDescription>> targetTableUrlPairs = new ArrayList<>();
        for (TableBean.TableDescription targetTable : OpenLTableLogic.getTargetTables(table, model)) {
            targetTableUrlPairs.add(Pair.of(webStudio.url("table", targetTable.getUri()), targetTable));
            if (!model.getErrorsByUri(targetTable.getUri()).isEmpty()) {
                warnings.add(new OpenLMessage("Tested rules have errors", Severity.WARN));
                break;
            }
        }

        Map<String, Object> tableInfo = new HashMap<>();
        tableInfo.put("errors", OpenLTableLogic.processTableProblems(errors, model, webStudio));
        tableInfo.put("warnings", OpenLTableLogic.processTableProblems(warnings, model, webStudio));
        tableInfo.put("targetTables", targetTableUrlPairs);
        tableInfo.put("tableUrl", webStudio.url("table"));
        tableInfo.put("openCurrentModuleOnly",
            moduleInfo != null ? moduleInfo.getOpenCurrentModuleOnly() : false);
        return tableInfo;
    }

    private String getTestName(IOpenMethod method) {
        String name = TableSyntaxNodeUtils.getTestName(method);
        String info = ProjectHelper.getTestInfo(method);
        return String.format("%s (%s)", name, info);
    }

    private void processMessages(Collection<OpenLMessage> messages,
            MessageCounter counter,
            ProjectModel model,
            List<MessageDescription> newMessages) {
        for (OpenLMessage message : messages) {
            switch (message.getSeverity()) {
                case WARN:
                    counter.warningsCount++;
                    break;
                case ERROR:
                    counter.errorsCount++;
                    break;
            }
            MessageDescription messageDescription = getMessageDescription(message, model);
            newMessages.add(messageDescription);
        }
    }

    private MessageDescription getMessageDescription(OpenLMessage message, ProjectModel model) {
        String url = messageHandler.getSourceUrl(message, model);
        if (StringUtils.isBlank(url)) {
            url = messageHandler.getUrlForEmptySource(message);
        }
        return new MessageDescription(message.getId(), message.getSummary(), message.getSeverity(), url);
    }

    private class MessageCounter {
        int warningsCount = 0;
        int errorsCount = 0;
    }
}