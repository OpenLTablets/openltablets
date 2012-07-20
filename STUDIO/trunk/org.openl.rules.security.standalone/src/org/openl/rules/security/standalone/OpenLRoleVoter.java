package org.openl.rules.security.standalone;

import java.util.Collection;

import org.openl.rules.security.PredefinedRole;
import org.openl.rules.security.Role;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * <p>
 * Based on {@link org.springframework.security.access.vote.RoleVoter}. If Authentication has
 * {@link org.openl.rules.security.PredefinedRole#ROLE_ADMIN} authority it will get
 * access even if it is not specified explicitly.
 * </p>
 * <p>
 * Votes if any {@link ConfigAttribute#getAttribute()} starts with a prefix
 * indicating that it is a role. The prefix string is <Code>ROLE_</code>.
 * </p>
 * <p>
 * Abstains from voting if no configuration attribute commences with the role
 * prefix. Votes to grant access if there is an exact matching
 * {@link org.springframework.security.core.GrantedAuthority} to a <code>ConfigAttribute</code>
 * starting with the role prefix. Votes to deny access if there is no exact
 * matching <code>GrantedAuthority</code> to a <code>ConfigAttribute</code>
 * starting with the role prefix ({@link org.openl.rules.security.Privileges#ROLE_PREFIX}).
 * </p>
 * <p>
 * All comparisons and prefixes are case sensitive.
 * </p>
 *
 * @author Aleh Bykhavets
 */
public class OpenLRoleVoter implements AccessDecisionVoter<Object> {
    /**
     * This implementation supports any type of class, because it does not query
     * the presented secure object.
     *
     * @param aClass the secure object
     *
     * @return always <code>true</code>
     */
    public boolean supports(Class<?> aClass) {
        return true;
    }

    public boolean supports(ConfigAttribute configAttribute) {
        return true;
        /*
         * String attr = configAttribute.getAttribute();
         *
         * if ((attr != null) && attr.startsWith(Roles.ROLE_PREFIX)) { return
         * true; } else { return false; }
         */
    }

    /**
     * Votes whether access should be granted.
     *
     * @param authentication Authentication (Principal)
     * @param object Ignored in current implementation
     * @param configAttributeDefinition attributes (required Authorities)
     * @return {@link #ACCESS_DENIED} or {@link #ACCESS_ABSTAIN} or
     *         {@link #ACCESS_GRANTED}
     */
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) {
        int result = ACCESS_ABSTAIN;

        for (ConfigAttribute attribute : configAttributes) {
            if (this.supports(attribute)) {
                result = ACCESS_DENIED;

                String attr = attribute.getAttribute();

                // Attempt to find a matching granted authority
                for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
                    String authority = grantedAuthority.getAuthority();
                    if (attr.equals(authority)) {
                        return ACCESS_GRANTED;
                    }
                    
                    Role role = PredefinedRole.findRole(authority);
                    if (role != null && role.hasAuthority(attr)) {
                        return ACCESS_GRANTED;
                    }
                }
            }
        }

        return result;
    }
}
