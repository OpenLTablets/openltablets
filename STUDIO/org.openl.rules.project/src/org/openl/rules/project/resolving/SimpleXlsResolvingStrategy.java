package org.openl.rules.project.resolving;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.model.ModuleType;
import org.openl.rules.project.model.PathEntry;
import org.openl.rules.project.model.ProjectDescriptor;
import org.openl.util.FileTypeHelper;

/**
 * Resolver for simple OpenL project with only xls file.
 * 
 * ProjectDescriptor will be created with modules for each xls.
 * 
 * @author PUdalau
 */
public class SimpleXlsResolvingStrategy extends BaseResolvingStrategy {

    private final Log log = LogFactory.getLog(SimpleXlsResolvingStrategy.class);

    public boolean isRulesProject(File folder) {
        if (!folder.isDirectory()) {
            return false;
        }
        for (File f : folder.listFiles()) {
            if (!f.isHidden() && FileTypeHelper.isExcelFile(f.getName())) {
                log.debug(String.format("Project in %s folder was resolved as simple xls project", folder.getPath()));
                return true;
            }
        }
        log.debug(String.format("Simple xls strategy failed to resolve project folder:"
                + "there is no excel files in given folder %s", folder.getPath()));
        return false;
    }

    protected ProjectDescriptor internalResolveProject(File folder) {
        ProjectDescriptor project = createDescriptor(folder);
        Map<String, Module> modules = new TreeMap<String, Module>();
        for (File f : folder.listFiles()) {
            if (!f.isHidden() && f.isFile() && FileTypeHelper.isExcelFile(f.getName())) {

                String name = FilenameUtils.removeExtension(f.getName());
                if (!modules.containsKey(name)) {
                    PathEntry rootPath = new PathEntry(f.getAbsolutePath());
                    Module module = createModule(project, rootPath, name);
                    modules.put(name, module);
                } else {
                    log.error("A module with this name already exists: " + name);
                }
            }
        }
        project.setModules(new ArrayList<Module>(modules.values()));
        return project;
    }

    private Module createModule(ProjectDescriptor project, PathEntry rootPath, String name) {
        Module module = new Module();
        module.setProject(project);
        module.setRulesRootPath(rootPath);
        module.setType(ModuleType.API);
        // FIXME: classname just for webstudio
        module.setClassname(name);
        module.setName(name);
        return module;
    }

    private ProjectDescriptor createDescriptor(File folder) {
        ProjectDescriptor project = new ProjectDescriptor();
        project.setProjectFolder(folder);
        project.setName(folder.getName());
        return project;
    }
}
