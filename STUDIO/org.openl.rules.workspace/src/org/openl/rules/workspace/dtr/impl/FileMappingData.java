package org.openl.rules.workspace.dtr.impl;

import org.openl.rules.repository.api.AdditionalData;
import org.openl.rules.repository.api.PathConverter;
import org.openl.util.StringUtils;

public class FileMappingData implements AdditionalData<FileMappingData> {
    private String externalPath;
    private final String internalPath;

    public FileMappingData(String externalPath, String internalPath) {
        this.externalPath = externalPath;
        this.internalPath = StringUtils.trimToEmpty(internalPath);
    }

    @Override
    public FileMappingData convertPaths(PathConverter converter) {
        // We don't need to convert internalPath so return this.
        return this;
    }

    public String getInternalPath() {
        return internalPath;
    }

    public String getExternalPath() {
        return externalPath;
    }

    public void setExternalPath(String externalPath) {
        this.externalPath = externalPath;
    }
}
