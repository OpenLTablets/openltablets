package org.openl.rules.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.openl.rules.project.model.MethodFilter;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.model.ModuleType;
import org.openl.rules.project.model.PathEntry;
import org.openl.rules.project.model.ProjectDescriptor;
import org.openl.rules.project.model.validation.ProjectDescriptorValidator;
import org.openl.rules.project.model.validation.ValidationException;
import org.openl.rules.project.xml.XmlProjectDescriptorSerializer;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.rits.cloning.Cloner;

public class ProjectDescriptorManager {

    private IProjectDescriptorSerializer serializer = new XmlProjectDescriptorSerializer();
    private ProjectDescriptorValidator validator = new ProjectDescriptorValidator();
    private PathMatcher pathMatcher = new AntPathMatcher();

    private Cloner cloner = new SafeCloner();

    public PathMatcher getPathMatcher() {
        return pathMatcher;
    }

    public void setPathMatcher(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    public IProjectDescriptorSerializer getSerializer() {
        return serializer;
    }

    public void setSerializer(IProjectDescriptorSerializer serializer) {
        this.serializer = serializer;
    }

    private ProjectDescriptor readDescriptorInternal(InputStream source) {
        return serializer.deserialize(source);
    }

    public ProjectDescriptor readDescriptor(File file) throws FileNotFoundException, ValidationException {
        FileInputStream inputStream = new FileInputStream(file);

        ProjectDescriptor descriptor = readDescriptorInternal(inputStream);
        IOUtils.closeQuietly(inputStream);

        postProcess(descriptor, file);
        validator.validate(descriptor);

        return descriptor;
    }

    public ProjectDescriptor readDescriptor(String filename) throws FileNotFoundException, ValidationException {
        File source = new File(filename);
        return readDescriptor(source);
    }

    public ProjectDescriptor readOriginalDescriptor(File filename) throws FileNotFoundException, ValidationException {
        FileInputStream inputStream = new FileInputStream(filename);

        ProjectDescriptor descriptor = readDescriptorInternal(inputStream);
        IOUtils.closeQuietly(inputStream);

        validator.validate(descriptor);

        return descriptor;
    }

    public void writeDescriptor(ProjectDescriptor descriptor, String filename) throws IOException, ValidationException {
        File file = new File(filename);
        writeDescriptor(descriptor, file);
    }

    public void writeDescriptor(ProjectDescriptor descriptor, File file) throws IOException, ValidationException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        writeDescriptor(descriptor, fileOutputStream);
    }

    public void writeDescriptor(ProjectDescriptor descriptor, OutputStream dest) throws IOException,
                                                                                ValidationException {
        validator.validate(descriptor);
        descriptor = cloner.deepClone(descriptor); // prevent changes argument
                                                   // object
        preProcess(descriptor);
        String serializedObject = serializer.serialize(descriptor);
        dest.write(serializedObject.getBytes("UTF-8"));
    }

    private boolean isModuleWithWildcard(Module module) {
        if (module.getRulesRootPath() != null) {
            return module.getRulesRootPath().getPath().contains("*") || module.getRulesRootPath()
                .getPath()
                .contains("?");
        }
        return false;
    }

    private void check(File folder, List<File> matched, String pathPattern, File rootFolder) {
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                check(file, matched, pathPattern, rootFolder);
            } else {
                String relativePath = file.getAbsolutePath().substring(rootFolder.getAbsolutePath().length() + 1);
                relativePath = relativePath.replace("\\", "/");
                if (pathMatcher.match(pathPattern, relativePath)) {
                    matched.add(file);
                }
            }
        }
    }

    private List<Module> getAllModulesMatchingPathPattern(ProjectDescriptor descriptor,
            Module module,
            String pathPattern) {
        List<Module> modules = new ArrayList<Module>();

        List<File> files = new ArrayList<File>();
        check(descriptor.getProjectFolder(), files, pathPattern.trim(), descriptor.getProjectFolder());

        for (File file : files) {
            Module m = new Module();
            m.setProject(descriptor);
            m.setRulesRootPath(new PathEntry(file.getAbsolutePath()));
            m.setName(FilenameUtils.getBaseName(file.getName()));
            m.setType(ModuleType.API);
            m.setMethodFilter(module.getMethodFilter());
            m.setWildcardRulesRootPath(pathPattern);
            m.setWildcardName(module.getName());
            modules.add(m);
        }
        return modules;
    }

    private boolean containsInProcessedModules(Collection<Module> modules, Module m, File projectRoot) {
        PathEntry pathEntry = m.getRulesRootPath();
        if (!new File(m.getRulesRootPath().getPath()).isAbsolute()) {
            pathEntry = new PathEntry(new File(projectRoot, m.getRulesRootPath().getPath()).getAbsolutePath());
        }

        for (Module module : modules) {
            PathEntry modulePathEntry = module.getRulesRootPath();
            if (!new File(module.getRulesRootPath().getPath()).isAbsolute()) {
                modulePathEntry = new PathEntry(new File(projectRoot, module.getRulesRootPath().getPath()).getAbsolutePath());
            }
            if (pathEntry.getPath().equals(modulePathEntry.getPath())) {
                return true;
            }
        }
        return false;
    }

    private void processModulePathPatterns(ProjectDescriptor descriptor, File projectRoot) {
        List<Module> modulesWasRead = descriptor.getModules();
        List<Module> processedModules = new ArrayList<Module>(modulesWasRead.size());
        // Process modules without wildcard path
        for (Module module : modulesWasRead) {
            if (!isModuleWithWildcard(module)) {
                processedModules.add(module);
            }
        }
        // Process modules with wildcard path
        for (Module module : modulesWasRead) {
            if (isModuleWithWildcard(module)) {
                List<Module> newModules = new ArrayList<Module>();
                List<Module> modules = getAllModulesMatchingPathPattern(descriptor, module, module.getRulesRootPath()
                    .getPath());
                for (Module m : modules) {
                    if (!containsInProcessedModules(processedModules, m, projectRoot)) {
                        newModules.add(m);
                    }
                }
                processedModules.addAll(newModules);
            }
        }

        descriptor.setModules(processedModules);
    }

    private void postProcess(ProjectDescriptor descriptor, File projectDescriptorFile) {

        File projectRoot = projectDescriptorFile.getParentFile();
        descriptor.setProjectFolder(projectRoot);
        processModulePathPatterns(descriptor, projectRoot);

        for (Module module : descriptor.getModules()) {
            module.setProject(descriptor);
            if (module.getMethodFilter() == null) {
                module.setMethodFilter(new MethodFilter());
            }
            if (module.getMethodFilter().getExcludes() == null) {
                module.getMethodFilter().setExcludes(new HashSet<String>());
            }
            if (module.getMethodFilter().getIncludes() == null) {
                module.getMethodFilter().setIncludes(new HashSet<String>());
            }

            if (!new File(module.getRulesRootPath().getPath()).isAbsolute()) {
                PathEntry absolutePath = new PathEntry(new File(projectRoot, module.getRulesRootPath().getPath()).getAbsolutePath());
                module.setRulesRootPath(absolutePath);
            }
        }
    }

    private void preProcess(ProjectDescriptor descriptor) {
        // processModulePathPatterns(descriptor);
        if (descriptor.getModules() == null || descriptor.getModules().isEmpty()) {
            return;
        }
        Set<String> wildcardPathSet = new HashSet<String>();
        Iterator<Module> itr = descriptor.getModules().iterator();
        while (itr.hasNext()) {
            Module module = itr.next();
            if (module.getWildcardRulesRootPath() == null || !wildcardPathSet.contains(module.getWildcardRulesRootPath())) {
                module.setProject(null);
                module.setProperties(null);
                if (module.getWildcardRulesRootPath() != null) {
                    wildcardPathSet.add(module.getWildcardRulesRootPath());
                    module.setRulesRootPath(new PathEntry(module.getWildcardRulesRootPath()));
                    module.setName(module.getWildcardName());
                } else {
                    PathEntry pathEntry = module.getRulesRootPath();
                    String path = pathEntry.getPath();
                    module.setRulesRootPath(new PathEntry(path.replaceAll("\\\\", "/")));
                }
                if (module.getMethodFilter() != null) {
                    boolean f = true;
                    if (module.getMethodFilter().getExcludes() != null && module.getMethodFilter()
                        .getExcludes()
                        .isEmpty()) {
                        module.getMethodFilter().setExcludes(null);
                        f = false;
                    }
                    if (module.getMethodFilter().getIncludes() != null && module.getMethodFilter()
                        .getIncludes()
                        .isEmpty()) {
                        if (f) {
                            module.getMethodFilter().setExcludes(null);
                        } else {
                            module.setMethodFilter(null);
                        }
                    }
                }
            } else {
                itr.remove();
            }
        }
    }

}
