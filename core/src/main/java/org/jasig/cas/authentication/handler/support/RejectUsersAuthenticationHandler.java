/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.util.Collection;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * Class to provide a list of users to automatically reject.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class RejectUsersAuthenticationHandler extends
    AbstractUsernamePasswordAuthenticationHandler {

    /** The collection of users to reject. */
    private Collection users;

    public boolean authenticateUsernamePasswordInternal(
        final UsernamePasswordCredentials credentials) {

        if (credentials.getUserName() == null) {
            return false;
        }
        return !this.users.contains(credentials.getUserName());
    }

    public void afterPropertiesSet() throws Exception {
        if (this.users == null) {
            throw new IllegalStateException(
                "You must provide a list of users that are not allowed to use the system.");
        }
    }

    /**
     * @param users The users to set.
     */
    public void setUsers(final Collection users) {
        this.users = users;
    }
}
