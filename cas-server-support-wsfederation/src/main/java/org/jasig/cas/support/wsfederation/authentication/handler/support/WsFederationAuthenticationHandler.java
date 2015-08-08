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
package org.jasig.cas.support.wsfederation.authentication.handler.support;

import org.jasig.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.jasig.cas.MessageDescriptor;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Principal;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * This handler authenticates Security token/credentials.
 *
 * @author John Gasper
 * @since 4.2.0
 */
public final class WsFederationAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    /**
     * Determines if this handler can support the credentials provided.
     *
     * @param credentials the credentials to test
     * @return true if supported, otherwise false
     */
    @Override
    public boolean supports(final Credential credentials) {
        return credentials != null && WsFederationCredential.class.isAssignableFrom(credentials.getClass());
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        final WsFederationCredential wsFederationCredentials = (WsFederationCredential) credential;
        if (wsFederationCredentials != null) {
            final Principal principal = this.principalFactory.createPrincipal(wsFederationCredentials.getId(),
                    wsFederationCredentials.getAttributes());

            return this.createHandlerResult(wsFederationCredentials, principal, new ArrayList<MessageDescriptor>());
        }
        throw new FailedLoginException();
    }

}
