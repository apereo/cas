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
package org.jasig.cas.authentication;

import org.jasig.cas.authentication.handler.NamedAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;

/**
 * Adapts a CAS 3.x {@link org.jasig.cas.authentication.handler.AuthenticationHandler} onto a CAS 4.x
 * {@link AuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class LegacyAuthenticationHandlerAdapter implements AuthenticationHandler {

    /** Wrapped CAS 3.0 authentication handler. */
    @NotNull
    private final org.jasig.cas.authentication.handler.AuthenticationHandler legacyHandler;

    /** Adapts CAS 4.0 credentials onto CAS 3.0 credentials. */
    @NotNull
    private final CredentialsAdapter credentialsAdapter;


    /**
     * Creates a new instance that adapts the given legacy authentication handler.
     *
     * @param legacy CAS 3.0 authentication handler.
     */
    public LegacyAuthenticationHandlerAdapter(final org.jasig.cas.authentication.handler.AuthenticationHandler legacy) {
        if (!legacy.supports(new UsernamePasswordCredentials())) {
            throw new IllegalArgumentException(
                    "Cannot infer credential conversion strategy - specify CredentialsAdapter explicitly.");
        }
        this.legacyHandler = legacy;
        this.credentialsAdapter = new UsernamePasswordCredentialsAdapter();
    }

    /**
     * Creates a new instance that adapts the given legacy authentication handler.
     * Use this form for a handler that supports a credential type other than username/password credentials.
     *
     * @param legacy CAS 3.0 authentication handler.
     * @param adapter Adapts CAS 4.0 credential onto 3.0 credential.
     */
    public LegacyAuthenticationHandlerAdapter(
            final org.jasig.cas.authentication.handler.AuthenticationHandler legacy,
            final CredentialsAdapter adapter) {
        this.legacyHandler = legacy;
        this.credentialsAdapter = adapter;
    }

    @Override
    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException, PreventedException {
        try {
            if (this.legacyHandler.authenticate(credentialsAdapter.convert(credential))) {
                final CredentialMetaData md;
                if (credential instanceof CredentialMetaData) {
                    md = (CredentialMetaData) credential;
                } else {
                    md = new BasicCredentialMetaData(credential);
                }
                return new DefaultHandlerResult(this, md);
            } else {
                throw new FailedLoginException(
                        String.format("%s failed to authenticate %s", this.getName(), credential));
            }
        } catch (final org.jasig.cas.authentication.handler.AuthenticationException e) {
            throw new GeneralSecurityException(
                    String.format("%s failed to authenticate %s", this.getName(), credential), e);
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return this.legacyHandler.supports(credentialsAdapter.convert(credential));
    }

    @Override
    public String getName() {
        if (this.legacyHandler instanceof NamedAuthenticationHandler) {
            return ((NamedAuthenticationHandler) this.legacyHandler).getName();
        } else {
            return this.legacyHandler.getClass().getSimpleName();
        }
    }
}
