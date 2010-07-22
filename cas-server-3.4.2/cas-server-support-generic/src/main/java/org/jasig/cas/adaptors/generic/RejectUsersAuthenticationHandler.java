/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.generic;

import java.util.List;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BlockedCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import javax.validation.constraints.NotNull;

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
public class RejectUsersAuthenticationHandler extends
    AbstractUsernamePasswordAuthenticationHandler {

    /** The collection of users to reject. */
    @NotNull
    private List<String> users;

    protected final boolean authenticateUsernamePasswordInternal(final UsernamePasswordCredentials credentials) throws AuthenticationException {

        final String transformedUsername = getPrincipalNameTransformer().transform(credentials.getUsername());

        if (this.users.contains(transformedUsername)) {
            throw new BlockedCredentialsAuthenticationException();
        }

        return true;
    }

    /**
     * Set the Collection of usernames which we will fail to authenticate.
     * 
     * @param users The Collection of usernames we should not authenticate.
     */
    public final void setUsers(final List<String> users) {
        this.users = users;
    }
}
