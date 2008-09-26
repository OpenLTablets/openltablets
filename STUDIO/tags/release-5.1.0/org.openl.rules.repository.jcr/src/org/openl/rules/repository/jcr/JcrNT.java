package org.openl.rules.repository.jcr;

/**
 * Node related constants.
 * 
 * @author Aleh Bykhavets
 *
 */
public interface JcrNT {
	// Node Types
	public static final String NT_PROJECT = "openl:project";
	public static final String NT_PROD_PROJECT = "openl:dproject";
	public static final String NT_FILES = "openl:files";
	public static final String NT_PROD_FILES = "openl:dfiles";
	public static final String NT_FOLDER = "openl:folder";
	public static final String NT_PROD_FOLDER = "openl:dfolder";
	public static final String NT_FILE = "openl:file";
	public static final String NT_PROD_FILE = "openl:dfile";
    public static final String NT_DEPLOYMENT = "openl:deployment";
    public static final String NT_RESOURCE = "nt:resource";

    public static final String NT_DEPENDENCIES = "openl:dependencies";
    public static final String NT_DEPENDENCY = "openl:dependency";

    public static final String NT_DEPLOYMENT_PROJECT = "openl:deploymentProject";
    public static final String NT_PROJECT_DESCRIPTOR = "openl:projectDescriptor";

    public static final String NT_LOCK = "openl:lock";

	public static final String MIX_VERSIONABLE = "mix:versionable";

    public static final String NT_FROZEN_NODE = "nt:frozenNode";
    public static final String FROZEN_NODE = "jcr:frozenNode";

	// Global Properties to track security issues
// take time from version
//	public static final String PROP_MODIFIED_TIME = "modifiedTime";
	public static final String PROP_MODIFIED_BY = "modifiedBy";
    public static final String PROP_LOCKED_AT = "lockedAt";
    public static final String PROP_LOCKED_BY = "lockedBy";

    // Common Rules properties
    public static final String PROP_EFFECTIVE_DATE = "effectiveDate";
    public static final String PROP_EXPIRATION_DATE = "expirationDate";
    public static final String PROP_LINE_OF_BUSINESS = "LOB";

    // Node properties
    public static final String PROP_ATTRIBUTE1 = "attribute1";
    public static final String PROP_ATTRIBUTE2 = "attribute2";
    public static final String PROP_ATTRIBUTE3 = "attribute3";
    public static final String PROP_ATTRIBUTE4 = "attribute4";
    public static final String PROP_ATTRIBUTE5 = "attribute5";
    public static final String PROP_ATTRIBUTE6 = "attribute6";
    public static final String PROP_ATTRIBUTE7 = "attribute7";
    public static final String PROP_ATTRIBUTE8 = "attribute8";
    public static final String PROP_ATTRIBUTE9 = "attribute9";
    public static final String PROP_ATTRIBUTE10 = "attribute10";
    public static final String PROP_ATTRIBUTE11 = "attribute11";
    public static final String PROP_ATTRIBUTE12 = "attribute12";
    public static final String PROP_ATTRIBUTE13 = "attribute13";
    public static final String PROP_ATTRIBUTE14 = "attribute14";
    public static final String PROP_ATTRIBUTE15 = "attribute15";

	// Properties for File->Resource
	public static final String PROP_RES_CONTENT = "jcr:content";
	public static final String PROP_RES_MIMETYPE = "jcr:mimeType";
	public static final String PROP_RES_ENCODING = "jcr:encoding";
	public static final String PROP_RES_DATA = "jcr:data";
	public static final String PROP_RES_LASTMODIFIED = "jcr:lastModified";

	// Properties for Project
	public static final String PROP_PRJ_DESCR = "description";
	public static final String PROP_PRJ_MARKED_4_DELETION = "marked4deletion";

    public static final String PROP_VERSION = "rVersion";
    public static final String PROP_REVISION = "rRevision";

    // Deployment Descriptor
    public static final String PROP_PROJECT_NAME = "projectName";
}
