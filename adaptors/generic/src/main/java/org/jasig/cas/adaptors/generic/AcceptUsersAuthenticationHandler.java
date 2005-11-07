/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.generic;

import java.util.Iterator;
import java.util.Map;

import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.util.Assert;

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
public final class AcceptUsersAuthenticationHandler extends
    AbstractUsernamePasswordAuthenticationHandler {

    /** The list of users we will accept. */
    private Map users;

    public boolean authenticateUsernamePasswordInternal(
        final UsernamePasswordCredentials credentials) {

        final String cachedPassword = (String) this.users.get(credentials
            .getUsername());

        if (cachedPassword == null) {
            return false;
        }

        final String encodedPassword = this.getPasswordEncoder().encode(
            credentials.getPassword());

        return (cachedPassword.equals(encodedPassword));
    }

    protected void afterPropertiesSetInternal() throws Exception {
        Assert.notNull(this.users);

        for (final Iterator iter = this.users.keySet().iterator(); iter.hasNext();) {
            final Object key = iter.next();
            final Object value = this.users.get(key);
            
            Assert.notNull(value, "Cannot have null password for user [" + key + "]");
        }
    }

    /**
     * @param users
     *            The users to set.
     */
    public void setUsers(final Map users) {
        this.users = users;
    }
}
