/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.generic;

import java.util.Collection;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BlockedCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * AuthenticationHandler which fails to authenticate a user purporting to be one
 * of the blocked usernames, and blindly authenticates all other users.
 * <p>
 * Note that RejectUsersAuthenticationHandler throws an exception when the user
 * is found in the map. This is done to indicate that this is an extreme case
 * and any AuthenticationManager checking the RejectUsersAuthenticationHandler
 * should not continue checking other Authentication Handlers on the failure of
 * RejectUsersAuthenticationHandler to authenticate someone.
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
        final UsernamePasswordCredentials credentials)
        throws AuthenticationException {

        if (this.users.contains(credentials.getUsername())) {
            throw new BlockedCredentialsAuthenticationException();
        }

        return true;
    }

    public void afterPropertiesSetInternal() throws Exception {
        if (this.users == null) {
            throw new IllegalStateException(
                "You must provide a list of users that are not allowed to use the system.");
        }
    }

    /**
     * Set the Collection of usernames which we will fail to authenticate.
     * 
     * @param users The Collection of usernames we should not authenticate.
     */
    public void setUsers(final Collection users) {
        this.users = users;
    }
}
