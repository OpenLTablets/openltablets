package org.openl.rules.repository;

import java.util.Date;

import org.openl.rules.common.CommonUser;
import org.openl.rules.common.CommonVersion;
import org.openl.rules.common.impl.CommonUserImpl;

/**
 * OpenL Rules Entity Version.
 *
 * @author Aleh Bykhavets
 *
 */
public interface RVersion extends CommonVersion {
    
    public static RVersion NON_DEFINED_VERSION = new RVersion() {
        
        public int compareTo(CommonVersion o) {
            return -1;
        }
        
        public String getVersionName() {
            return "NO_VERSION";
        }
        
        public int getRevision() {
            return 0;
        }
        
        public Integer getMinor() {
            return null;
        }
        
        public Integer getMajor() {
            return null;
        }
        
        public CommonUser getCreatedBy() {
            return new CommonUserImpl(null);
        }
        
        public Date getCreated() {
            return null;
        }
    };

    /**
     * Gets date when the version was created.
     *
     * @return date of creation
     */
    Date getCreated();

    /**
     * Gets user who created the version.
     *
     * @return user who created it
     */
    CommonUser getCreatedBy();

}
