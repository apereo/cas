/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.util.Map;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * Handler that contains a list of valid users and passwords. Useful if there is
 * a small list of users that we wish to allow. An example use case may be if
 * there are existing handlers that make calls to LDAP, etc. but there is a need
 * for additional users we don't want in LDAP. With the chain of command
 * processing of handlers, this handler could be added to check before LDAP and
 * provide the list of additional users. The list of acceptable users is stored
 * in a map. The key of the map is the username and the password is the object
 * retrieved from doing map.get(KEY).
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AcceptUsersAuthenticationHandler extends
    AbstractUsernamePasswordAuthenticationHandler {

    private Map users;

    public boolean authenticateUsernamePasswordInternal(
        final UsernamePasswordCredentials credentials) {
        final String cachedPassword;

        if (!this.users.containsKey(credentials.getUserName())) {
            return false;
        }

        cachedPassword = (String)this.users.get(credentials.getUserName());

        return (cachedPassword.equals(credentials.getPassword()));
    }

    public void afterPropertiesSet() throws Exception {
        if (this.users == null) {
            throw new IllegalStateException("users must be set on "
                + this.getClass().getName());
        }
    }

    /**
     * @param users The users to set.
     */
    public void setUsers(final Map users) {
        this.users = users;
    }
}