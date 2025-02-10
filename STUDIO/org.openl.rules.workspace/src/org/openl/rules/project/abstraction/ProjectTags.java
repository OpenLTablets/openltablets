/*
 * Copyright © 2024 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.
 */
package org.openl.rules.project.abstraction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openl.rules.common.ProjectException;
import org.openl.rules.repository.api.BranchRepository;
import org.openl.util.PropertiesUtils;

public class ProjectTags {
    private final Logger log = LoggerFactory.getLogger(ProjectTags.class);
    public static final String TAGS_FILE_NAME = "tags.properties";
    private volatile Map<String, String> localTags;
    private volatile Map<String, String> designTags;
    private final RulesProject rulesProject;

    public ProjectTags(RulesProject rulesProject) {
        this.rulesProject = rulesProject;
    }

    public String getTagsFileName() {
        return TAGS_FILE_NAME;
    }

    private Map<String, String> readTagsFromStream(InputStream projectTagsFileStream) {
        var tags = new HashMap<String, String>();
        try {
            PropertiesUtils.load(projectTagsFileStream, tags::put);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return tags;
    }
    
    public Map<String, String> getLocalTags() {
        var tags = this.localTags;
        if (tags == null) {
            synchronized (this) {
                if (this.localTags == null) {
                    this.localTags = readLocalTags();
                }
                return this.localTags;
            }
        } else {
            return tags;
        }
    }

    private Map<String, String> readLocalTags() {

        if (rulesProject.hasArtefact(TAGS_FILE_NAME)) {
            try {
                AProjectResource resource = (AProjectResource) rulesProject.getArtefact(TAGS_FILE_NAME);
                try (InputStream projectTagsFileStream = resource.getContent()) {
                    return readTagsFromStream(projectTagsFileStream);
                }
            } catch (ProjectException| IOException e) {
                log.error(e.getMessage(), e);
                return Collections.emptyMap();
            }
        } else {
            return Collections.emptyMap();
        }
    }

    public Map<String, String> getDesignTags() {
        var tags = this.designTags;
        if (tags == null) {
            synchronized (this) {
                if (this.designTags == null) {
                    this.designTags = readDesignTags();
                }
                return this.designTags;
            }
        } else {
            return tags;
        }
    }

    public Map<String, String> readDesignTags() {

        var repository = rulesProject.getDesignRepository();
        if (repository.supports().branches()) {
            var branchRepository = (BranchRepository) repository;
            try {
                repository = branchRepository.forBranch(branchRepository.getBaseBranch());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return Map.of();
            }
        }
        var path = rulesProject.getRealPath();
        if (!path.endsWith("/")) {
            path += "/";
        }
        var tagsFileName = path + TAGS_FILE_NAME;
        try (var tagsFile = repository.read(tagsFileName)) {
            if (tagsFile != null) {
                var projectTagsFileStream = tagsFile.getStream();
                return readTagsFromStream(projectTagsFileStream);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return Map.of();
    }

    public void saveLocalTags(Map<String, String> tags) throws ProjectException {
        synchronized (this) {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                PropertiesUtils.store(byteArrayOutputStream, tags.entrySet());
                try (var inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
                    if (!rulesProject.hasArtefact(TAGS_FILE_NAME)) {
                        rulesProject.addResource(TAGS_FILE_NAME, inputStream);
                    } else {
                        AProjectResource artefact = (AProjectResource) rulesProject.getArtefact(TAGS_FILE_NAME);
                        artefact.setContent(inputStream);
                    }
                    this.localTags = Collections.unmodifiableMap(tags);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            } catch (IOException e) {
                throw new ProjectException("Cannot save tags", e);
            }
        }
    }
}
