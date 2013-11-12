package org.openl.rules.common.impl;


import java.util.Date;

import org.openl.rules.common.VersionInfo;

public class RepositoryVersionInfoImpl implements VersionInfo {
    private static final long serialVersionUID = 5338481693656986251L;

    private Date createdAt;
    private String createdBy;
    
    private Date modifiedAt;
    private String modifiedBy;

    public RepositoryVersionInfoImpl(Date createdAt, String createdBy) {
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.modifiedAt = null;
        this.modifiedBy = null;
    }
    
    public RepositoryVersionInfoImpl(Date createdAt, String createdBy, Date modifiedAt, String modifiedBy) {
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.modifiedAt = modifiedAt;
        this.modifiedBy = modifiedBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }
    
    public Date getModifiedAt() {
        return modifiedAt;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }
}
