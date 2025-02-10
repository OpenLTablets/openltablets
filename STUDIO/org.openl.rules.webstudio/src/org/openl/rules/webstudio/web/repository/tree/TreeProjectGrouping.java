package org.openl.rules.webstudio.web.repository.tree;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import org.openl.rules.project.abstraction.AProjectArtefact;
import org.openl.rules.project.abstraction.RulesProject;
import org.openl.rules.project.resolving.ProjectDescriptorArtefactResolver;
import org.openl.rules.repository.api.BranchRepository;
import org.openl.rules.repository.api.Repository;
import org.openl.rules.security.standalone.persistence.ProjectGrouping;
import org.openl.rules.security.standalone.persistence.Tag;
import org.openl.rules.webstudio.filter.AllFilter;
import org.openl.rules.webstudio.filter.IFilter;
import org.openl.rules.webstudio.service.TagService;
import org.openl.rules.webstudio.web.repository.RepositoryUtils;
import org.openl.rules.webstudio.web.repository.UiConst;
import org.openl.util.StringUtils;

/**
 * Represents OpenL project in a tree.
 *
 * @author Aleh Bykhavets
 */
public class TreeProjectGrouping extends AbstractTreeNode {
    private static final IFilter<AProjectArtefact> ALL_FILTER = new AllFilter<>();

    public static final String GROUPING_REPOSITORY = "[Repository]";
    public static final String GROUPING_NONE = "[None]";
    private static final String GROUP_PREFIX = "grp_";

    private Map<Object, TreeNode> elements;
    private final int level;
    private final TagService tagService;
    private final Collection<RulesProject> projects;
    private final ProjectGrouping projectGrouping;
    private final boolean hideDeleted;
    private final ProjectDescriptorArtefactResolver projectDescriptorResolver;
    private final List<Repository> repositories;

    public TreeProjectGrouping(String id,
                               String name,
                               Collection<RulesProject> projects,
                               ProjectGrouping projectGrouping,
                               int level,
                               TagService tagService,
                               boolean hideDeleted,
                               ProjectDescriptorArtefactResolver projectDescriptorResolver,
                               List<Repository> repositories) {
        super("grp_" + id, name);
        this.projects = projects;
        this.projectGrouping = projectGrouping;
        this.level = level;
        this.tagService = tagService;
        this.hideDeleted = hideDeleted;
        this.projectDescriptorResolver = projectDescriptorResolver;
        this.repositories = repositories;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIcon() {
        return UiConst.ICON_FOLDER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconLeaf() {
        // in both cases we use the same icons
        return getIcon();
    }

    @Override
    public String getType() {
        return UiConst.TYPE_GROUP;
    }

    @Override
    public String getId() {
        return GROUP_PREFIX + super.getId();
    }

    @Override
    public Map<Object, TreeNode> getElements() {
        if (elements == null && !isLeafOnly()) {
            elements = new LinkedHashMap<>();
            Collection<RulesProject> projectsAtCurrentLevel = new ArrayList<>(projects);
            if (level < 3) {
                String nextGroupingType = level == 1 ? projectGrouping.getGroup2() : projectGrouping.getGroup3();
                if (StringUtils.isNotBlank(nextGroupingType)) {
                    if (GROUPING_REPOSITORY.equals(nextGroupingType)) {
                        projectsAtCurrentLevel.clear();

                        repositories.forEach(repository -> {
                            final String repoId = repository.getId();
                            final String name = "[" + repository.getName() + "]";
                            final String id = RepositoryUtils.getTreeNodeId(repoId);

                            final List<RulesProject> subProjects = projects.stream()
                                    .filter(project -> project.getRepository().getId().equals(repoId))
                                    .collect(Collectors.toList());

                            if (!subProjects.isEmpty()) {
                                add(new TreeProjectGrouping(id,
                                        name,
                                        subProjects,
                                        projectGrouping,
                                        level + 1,
                                        tagService,
                                        hideDeleted,
                                        projectDescriptorResolver,
                                        repositories));
                            }
                        });
                    } else {

                        final List<Tag> tags = tagService.getByTagType(nextGroupingType);
                        tags.forEach(tag -> {
                            final String name = tag.getName();
                            final String id = RepositoryUtils.getTreeNodeId(name);

                            String tagType = tag.getType().getName();
                            final List<RulesProject> subProjects = projects.stream()
                                    .filter(project -> project.getTags().getDesignTags().containsKey(tagType) && Objects.equals(project.getTags().getDesignTags().get(tagType), tag.getName()))
                                    .collect(Collectors.toList());
                            projectsAtCurrentLevel.removeAll(subProjects);

                            if (!subProjects.isEmpty()) {
                                add(new TreeProjectGrouping(id,
                                        name,
                                        subProjects,
                                        projectGrouping,
                                        level + 1,
                                        tagService,
                                        hideDeleted,
                                        projectDescriptorResolver,
                                        repositories));
                            }
                        });
                    }
                }

            }
            projectsAtCurrentLevel.forEach(project -> {
                String name = project.getMainBusinessName();
                String id = RepositoryUtils.getTreeNodeId(project);
                if (!project.isDeleted() || !hideDeleted) {
                    TreeProject prj = new TreeProject(id, name, ALL_FILTER, projectDescriptorResolver);
                    prj.setData(project);
                    add(prj);
                }
            });
        }
        return elements;
    }

    @Override
    public boolean isLeaf() {
        // If elements aren't initialized, consider it as not leaf
        return isLeafOnly() || elements != null && elements.isEmpty();
    }

    @Override
    public void refresh() {
        super.refresh();
        elements = null;
    }

}
