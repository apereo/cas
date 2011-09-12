/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.generic;

import java.util.Collections;
import java.util.Map;

import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import javax.validation.constraints.NotNull;

/**
 * Handler that contains a list of valid users and passwords. Useful if there is
 * a small list of users that we wish to allow. An example use case may be if
 * there are existing handlers that make calls to LDAP, etc. but there is a need
 * for additional users we don't want in LDAP. With the chain of command
 * processing of handlers, this handler could be added to check before LDAP and
 * provide the list of additional users. The list of acceptable users is stored
 * in a map. The key of the map is the username and the password is the object
 * retrieved from doing map.get(KEY).
 * <p>
 * Note that this class makes an unmodifiable copy of whatever map is provided
 * to it.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AcceptUsersAuthenticationHandler extends
    AbstractUsernamePasswordAuthenticationHandler {

    /** The list of users we will accept. */
    @NotNull
    private Map<String, String> users;

    protected final boolean authenticateUsernamePasswordInternal(final UsernamePasswordCredentials credentials) {
        final String transformedUsername = getPrincipalNameTransformer().transform(credentials.getUsername());
        final String cachedPassword = this.users.get(transformedUsername);

        if (cachedPassword == null) {
            if (log.isDebugEnabled()) {
                log.debug("The user [" + transformedUsername
                    + "] was not found in the map.");
            }
            return false;
        }

        final String encodedPassword = this.getPasswordEncoder().encode(
            credentials.getPassword());

        return (cachedPassword.equals(encodedPassword));
    }

    /**
     * @param users The users to set.
     */
    public final void setUsers(final Map<String, String> users) {
        this.users = Collections.unmodifiableMap(users);
    }
}
