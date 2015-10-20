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
package org.jasig.cas.integration.pac4j.authentication.handler.support;

import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.NoOpPrincipalNameTransformer;
import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.jasig.cas.authentication.handler.PlainTextPasswordEncoder;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.pac4j.http.credentials.UsernamePasswordCredentials;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;

/**
 * Pac4j authentication handler which works on a CAS username / password credential
 * and uses a pac4j authenticator and profile creator to play authentication.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
public class UsernamePasswordWrapperAuthenticationHandler
        extends AbstractWrapperAuthenticationHandler<UsernamePasswordCredential, UsernamePasswordCredentials> {

    /**
     * PasswordEncoder to be used by subclasses to encode passwords for
     * comparing against a resource.
     */
    @NotNull
    private PasswordEncoder passwordEncoder = new PlainTextPasswordEncoder();

    /**
     * PrincipalNameTransformer to be used by subclasses to tranform the principal name.
     */
    @NotNull
    private PrincipalNameTransformer principalNameTransformer = new NoOpPrincipalNameTransformer();

    /**
     * Default constructor.
     */
    public UsernamePasswordWrapperAuthenticationHandler() {
        setTypedIdUsed(false);
    }

    @Override
    protected UsernamePasswordCredentials convertToPac4jCredentials(final UsernamePasswordCredential casCredential)
            throws GeneralSecurityException, PreventedException {
        logger.debug("CAS credentials: {}", casCredential);
        final UsernamePasswordCredential credential = (UsernamePasswordCredential) casCredential;
        final String username = this.principalNameTransformer.transform(credential.getUsername());
        if (username == null) {
            throw new AccountNotFoundException("Username is null.");
        }
        final String password = this.passwordEncoder.encode(credential.getPassword());
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password, getClass().getSimpleName());
        logger.debug("pac4j credentials: {}", credentials);
        return credentials;
    }

    @Override
    protected Class<UsernamePasswordCredential> getCasCredentialsType() {
        return UsernamePasswordCredential.class;
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public PrincipalNameTransformer getPrincipalNameTransformer() {
        return principalNameTransformer;
    }

    public void setPrincipalNameTransformer(final PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }
}
