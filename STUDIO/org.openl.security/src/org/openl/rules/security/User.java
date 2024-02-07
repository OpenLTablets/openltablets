package org.openl.rules.security;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Contains information about user that works with application. This class contains all fields that are specified by
 * {@link org.springframework.security.core.userdetails.UserDetails} interface.
 *
 * @author Andrei Astrouski
 */
public interface User extends UserDetails {

    String getFirstName();

    String getLastName();

    String getDisplayName();

    String getEmail();

    boolean hasPrivilege(String privilege);

    UserExternalFlags getExternalFlags();
}
