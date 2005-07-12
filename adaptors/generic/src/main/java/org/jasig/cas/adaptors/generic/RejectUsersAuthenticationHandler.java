/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.generic;

import java.util.Collection;

import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * AuthenticationHandler which fails to authenticate a user purporting to be one
 * of the blocked usernames, and blindly authenticates all other users.
 * <p>
 * Note that the current implementation of AuthenticationManagerImpl tries all
 * AuthenitcationHandlers supporting a particular Credentials until it finds one
 * that succeeds. Since this implementation succeeds on all but those it is
 * configured to reject, adding it to the default AuthenticationManagerImpl has
 * the consequence that all users not explicitly rejected will be available for
 * authentication using any password (regardless of other handlers). Also note
 * that since this implementation does not throw any AuthenticationExceptions,
 * but only returns false for rejected users, rejecting a user using this
 * AuthenticationHandler will not prevent another AuthenticationHandler from
 * authenticating those credentials.
 * <p>
 * This is, under the current implementation of AuthenticationManagerImpl,
 * adding an instance of RejectUsersAuthenticationHandler to the
 * authenticationManager's List of AuthenticationHandlers will have no effect
 * for rejected users and will have the effect of allowing all other users to
 * authenticate regardless of presented password.
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

        return !this.users.contains(credentials.getUsername());
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
