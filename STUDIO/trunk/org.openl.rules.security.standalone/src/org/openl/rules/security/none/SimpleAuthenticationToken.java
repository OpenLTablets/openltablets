package org.openl.rules.security.none;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.providers.AbstractAuthenticationToken;

/**
 * @author Aliaksandr Antonik.
 */
public class SimpleAuthenticationToken extends AbstractAuthenticationToken {
    private final UserDetails principal;

    /**
     * Creates a token with the supplied array of authorities.
     * 
     * @param authorities the list of <tt>GrantedAuthority</tt>s for the
     *            principal represented by this authentication object. A
     *            <code>null</code> value indicates that no authorities have
     *            been granted (pursuant to the interface contract specified by
     *            {@link org.acegisecurity.Authentication#getAuthorities()}<code>null</code>
     *            should only be presented if the principal has not been
     *            authenticated).
     * @param userName user name
     */
    public SimpleAuthenticationToken(GrantedAuthority[] authorities, String userName) {
        super(authorities);
        this.principal = new User(userName, "", true, true, true, true, authorities);
    }

    /**
     * The credentials that prove the principal is correct. This is usually a
     * password, but could be anything relevant to the
     * <code>AuthenticationManager</code>. Callers are expected to populate
     * the credentials.
     * 
     * @return the credentials that prove the identity of the
     *         <code>Principal</code>
     */
    public Object getCredentials() {
        return "";
    }

    /**
     * The identity of the principal being authenticated. This is usually a
     * username. Callers are expected to populate the principal.
     * 
     * @return the <code>Principal</code> being authenticated
     */
    public Object getPrincipal() {
        return principal;
    }
}
