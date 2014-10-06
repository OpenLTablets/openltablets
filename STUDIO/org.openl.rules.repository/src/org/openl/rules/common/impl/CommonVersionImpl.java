package org.openl.rules.common.impl;

import org.openl.rules.common.CommonVersion;

public class CommonVersionImpl implements CommonVersion {
    private int major = MAX_MM_INT;
    private int minor = MAX_MM_INT;
    private int revision;

    private transient String versionName;

    public CommonVersionImpl(CommonVersion version) {
        major = version.getMajor();
        minor = version.getMinor();
        revision = version.getRevision();
    }

    public CommonVersionImpl(int major, int minor, int revision) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    public CommonVersionImpl(int revision) {
        this.revision = revision;
    }

    /**
     * x -> revision
     * x.y -> major.minor
     * x.y.z -> major.minor.revision
     */
    public CommonVersionImpl(String s) {
        String[] version = s.split("\\.");

        if (version.length == 1) {
            revision = Integer.parseInt(version[0]);
        } else {
            major = Integer.parseInt(version[0]);
            minor = Integer.parseInt(version[1]);
            if (version.length > 2) {
                revision = Integer.parseInt(version[2]);
            }
        }
    }

    public int compareTo(CommonVersion o) {
        /*Version with the same Revisions always equal*/
        if (revision == o.getRevision()) {
            return 0;
        }

        /*Revision with num 0 always should be at last place*/
        if (revision == 0) {
            return -1;
        } else if (o.getRevision() == 0) {
            return 1;
        }

        if (major != o.getMajor()) {
            return major < o.getMajor() ? -1 : 1;
        }

        if (minor != o.getMinor()) {
            return minor < o.getMinor() ? -1 : 1;
        }

        return revision < o.getRevision() ? -1 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommonVersion)) {
            return false;
        }

        return compareTo((CommonVersion) o) == 0;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRevision() {
        return revision;
    }

    public String getVersionName() {
        if (versionName == null) {
            if (major != MAX_MM_INT && minor != MAX_MM_INT && major != -1 && minor != -1) {
                versionName = new StringBuilder().append(major).append(".").append(minor).append(".").append(revision)
                        .toString();
            } else {
                versionName = new StringBuilder().append(revision).toString();
            }
        }

        return versionName;
    }

    @Override
    public int hashCode() {
        return (major << 22) ^ (minor << 11) ^ revision;
    }

    @Override
    public String toString() {
        return "versionName = " + getVersionName();
    }
}
