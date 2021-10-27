/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.support.pac4j.authentication.handler.support;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.support.pac4j.authentication.principal.ClientCredential;
import org.pac4j.core.client.Clients;
import org.pac4j.core.profile.UserProfile;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * Specialized handler which builds the authenticated user directly from the retrieved user profile.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public class ClientAuthenticationHandler extends AbstractClientAuthenticationHandler {

    /**
     * Whether to use the typed identifier (by default) or just the identifier.
     */
    private boolean typedIdUsed = true;

    /**
     * Define the clients.
     *
     * @param theClients The clients for authentication
     */
    public ClientAuthenticationHandler(final Clients theClients) {
        super(theClients);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HandlerResult createResult(final ClientCredential credentials, final UserProfile profile)
        throws GeneralSecurityException, PreventedException {
        final String id;
        if (typedIdUsed) {
            id = profile.getTypedId();
        } else {
            id = profile.getId();
        }
        if (StringUtils.isNotBlank(id)) {
            credentials.setUserProfile(profile);
            credentials.setTypedIdUsed(typedIdUsed);
            return new DefaultHandlerResult(
                this,
                new BasicCredentialMetaData(credentials),
                this.principalFactory.createPrincipal(id, profile.getAttributes()));
        }
        throw new FailedLoginException("No identifier found for this user profile: " + profile);
    }

    public boolean isTypedIdUsed() {
        return typedIdUsed;
    }

    public void setTypedIdUsed(final boolean typedIdUsed) {
        this.typedIdUsed = typedIdUsed;
    }
}
