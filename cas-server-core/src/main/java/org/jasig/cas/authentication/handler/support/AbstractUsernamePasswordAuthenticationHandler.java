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
package org.jasig.cas.authentication.handler.support;

import java.security.GeneralSecurityException;

import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.NoOpPrincipalNameTransformer;
import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.jasig.cas.authentication.handler.PlainTextPasswordEncoder;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Principal;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.constraints.NotNull;

/**
 * Abstract class to override supports so that we don't need to duplicate the
 * check for UsernamePasswordCredential.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 *
 * @since 3.0
 */
public abstract class AbstractUsernamePasswordAuthenticationHandler extends
    AbstractPreAndPostProcessingAuthenticationHandler {

    /**
     * PasswordEncoder to be used by subclasses to encode passwords for
     * comparing against a resource.
     */
    @NotNull
    private PasswordEncoder passwordEncoder = new PlainTextPasswordEncoder();

    @NotNull
    private PrincipalNameTransformer principalNameTransformer = new NoOpPrincipalNameTransformer();

    /** {@inheritDoc} */
    protected final HandlerResult doAuthentication(final Credential credential)
            throws GeneralSecurityException, PreventedException {
        final UsernamePasswordCredential userPass = (UsernamePasswordCredential) credential;
        if (userPass.getUsername() == null) {
            throw new AccountNotFoundException();
        }
        final String transformedUsername = this.principalNameTransformer.transform(userPass.getUsername());
        if (transformedUsername == null) {
            throw new AccountNotFoundException();
        }
        final Principal principal = authenticateUsernamePasswordInternal(
                transformedUsername,
                userPass.getPassword());
        return new HandlerResult(this, new BasicCredentialMetaData(credential), principal);
    }

    /**
     * Authenticates a username/password credential by an arbitrary strategy.
     *
     * @param username Non-null username produced by {@link #principalNameTransformer} acting on
     *                 {@link org.jasig.cas.authentication.UsernamePasswordCredential#getUsername()}.
     * @param password Password to authenticate.
     *
     * @return Principal resolved from credential on authentication success or null if no principal could be resolved
     * from the credential.
     *
     * @throws GeneralSecurityException On authentication failure.
     * @throws PreventedException On the indeterminate case when authentication is prevented.
     */
    protected abstract Principal authenticateUsernamePasswordInternal(String username, String password)
            throws GeneralSecurityException, PreventedException;

    /**
     * Method to return the PasswordEncoder to be used to encode passwords.
     *
     * @return the PasswordEncoder associated with this class.
     */
    protected final PasswordEncoder getPasswordEncoder() {
        return this.passwordEncoder;
    }

    protected final PrincipalNameTransformer getPrincipalNameTransformer() {
        return this.principalNameTransformer;
    }

    /**
     * Sets the PasswordEncoder to be used with this class.
     *
     * @param passwordEncoder the PasswordEncoder to use when encoding
     * passwords.
     */
    public final void setPasswordEncoder(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public final void setPrincipalNameTransformer(final PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }

    /**
     * @return true if the credential are not null and the credential class is
     * equal to the class defined in classToSupport.
     */
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }
}
