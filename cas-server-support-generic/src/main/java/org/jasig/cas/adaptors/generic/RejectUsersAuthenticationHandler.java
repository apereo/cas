/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.adaptors.generic;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.SimplePrincipal;
import org.jasig.cas.authentication.UsernamePasswordCredential;

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
 * @author Marvin S. Addison
 * @since 3.0
 */
public class RejectUsersAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    /** The collection of users to reject. */
    @NotNull
    private List<String> users;

    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credentials)
            throws GeneralSecurityException, IOException {

        final String transformedUsername = getPrincipalNameTransformer().transform(credentials.getUsername());

        if (this.users.contains(transformedUsername)) {
            throw new FailedLoginException();
        }
        return new HandlerResult(this, new SimplePrincipal(transformedUsername));
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
