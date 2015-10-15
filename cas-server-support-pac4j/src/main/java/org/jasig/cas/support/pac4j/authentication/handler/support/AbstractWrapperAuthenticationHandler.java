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

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.support.pac4j.authentication.principal.ClientCredential;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.http.credentials.authenticator.Authenticator;
import org.pac4j.http.profile.creator.AuthenticatorProfileCreator;
import org.pac4j.http.profile.creator.ProfileCreator;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * Abstract pac4j authentication handler which uses a pac4j authenticator and profile creator.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
@SuppressWarnings("unchecked")
public abstract class AbstractWrapperAuthenticationHandler extends AbstractPac4jAuthenticationHandler {

    /**
     * The pac4j authenticator used for authentication.
     */
    protected Authenticator authenticator;

    /**
     * The pac4j profile creator used for authentication.
     */
    protected ProfileCreator profileCreator = AuthenticatorProfileCreator.INSTANCE;

    @Override
    public final boolean supports(final Credential credential) {
        return credential != null && getCasCredentialsType().isAssignableFrom(credential.getClass());
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {

        CommonHelper.assertNotNull("authenticator", this.authenticator);
        CommonHelper.assertNotNull("profileCreator", this.profileCreator);

        final Credentials credentials = convertToPac4jCredentials(credential);
        logger.debug("credentials: {}", credentials);

        try {
            authenticator.validate(credentials);
        } catch (final CredentialsException e) {
            logger.error("Failed to validate credentials", e);
            throw new FailedLoginException("Failed to validate credentials: " + e.getMessage());
        }

        final UserProfile profile = profileCreator.create(credentials);
        logger.debug("profile: {}", profile);

        return createResult(new ClientCredential(credentials), profile);
    }

    /**
     * Convert a CAS credential into a pac4j credentials to play the authentication.
     *
     * @param casCredential the CAS credential
     * @return the pac4j credentials
     * @throws GeneralSecurityException On authentication failure.
     * @throws PreventedException On the indeterminate case when authentication is prevented.
     */
    protected abstract Credentials convertToPac4jCredentials(final Credential casCredential) throws GeneralSecurityException,
            PreventedException;

    /**
     * Return the CAS credential supported by this handler (to be converted in a pac4j credentials
     * by {@link #convertToPac4jCredentials(Credential)}).
     *
     * @return the CAS credential class
     */
    protected abstract Class<? extends Credential> getCasCredentialsType();

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(final Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public ProfileCreator getProfileCreator() {
        return profileCreator;
    }

    public void setProfileCreator(final ProfileCreator profileCreator) {
        this.profileCreator = profileCreator;
    }
}
