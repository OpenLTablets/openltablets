package org.openl.rules.ruleservice.core;

import org.openl.dependency.CompiledDependency;
import org.openl.exception.OpenLCompilationException;
import org.openl.rules.project.instantiation.AbstractDependencyManager;
import org.openl.rules.project.instantiation.SimpleDependencyLoader;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.model.ProjectDescriptor;

final class RuleServiceDependencyLoader extends SimpleDependencyLoader {

    public RuleServiceDependencyLoader(ProjectDescriptor project,
                                       Module module,
                                       RuleServiceDependencyManager dependencyManager) {
        super(project, module, true, dependencyManager);
    }

    @Override
    protected CompiledDependency compileDependency() throws OpenLCompilationException {
        AbstractDependencyManager dependencyManager = getDependencyManager();
        if (dependencyManager instanceof RuleServiceDependencyManager) {
            RuleServiceDependencyManager ruleServiceDeploymentRelatedDependencyManager = (RuleServiceDependencyManager) dependencyManager;
            ruleServiceDeploymentRelatedDependencyManager.compilationBegin();
            CompiledDependency compiledDependency = null;
            try {
                compiledDependency = super.compileDependency();
                ruleServiceDeploymentRelatedDependencyManager.compilationCompleted(this,
                        !compiledDependency.getCompiledOpenClass().hasErrors());
                return compiledDependency;
            } finally {
                if (compiledDependency == null) {
                    ruleServiceDeploymentRelatedDependencyManager
                            .compilationCompleted(this, false);
                }
            }
        } else {
            return super.compileDependency();
        }
    }
}