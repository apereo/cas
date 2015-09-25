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

package org.jasig.cas.adaptors.generic;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.RememberMeUsernamePasswordCredential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.springframework.core.io.Resource;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

/**
 * An authentication handler that routes requests to Apache Shiro.
 * Credentials are assumed to be username and password.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class ShiroAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private Set<String> requiredRoles = new HashSet<>();
    private Set<String> requiredPermissions = new HashSet<>();

    public void setRequiredRoles(final Set<String> requiredRoles) {
        this.requiredRoles = requiredRoles;
    }

    public void setRequiredPermissions(final Set<String> requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }

    /**
     * Sets shiro configuration to the path of the resource
     * that points to the <code>shiro.ini</code> file.
     *
     * @param resource the resource
     */
    public void setShiroConfiguration(final Resource resource) {
        try {
            final String location = resource.getURI().toString();
            logger.debug("Loading Shiro configuration from {}", location);

            final Factory<SecurityManager> factory = new IniSecurityManagerFactory(location);
            final SecurityManager securityManager = factory.getInstance();
            SecurityUtils.setSecurityManager(securityManager);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential)
            throws GeneralSecurityException, PreventedException {
        try {
            final RememberMeUsernamePasswordCredential credential =
                    (RememberMeUsernamePasswordCredential) transformedCredential;
            final UsernamePasswordToken token = new UsernamePasswordToken(credential.getUsername(),
                    this.getPasswordEncoder().encode(credential.getPassword()));
            token.setRememberMe(credential.isRememberMe());

            final Subject currentUser = getCurrentExecutingSubject();
            currentUser.login(token);

            checkSubjectRolesAndPermissions(currentUser);

            return createAuthenticatedSubjectResult(credential, currentUser);
        } catch (final UnknownAccountException uae) {
            throw new AccountNotFoundException(uae.getMessage());
        } catch (final IncorrectCredentialsException ice)  {
            throw new FailedLoginException(ice.getMessage());
        } catch (final LockedAccountException lae) {
            throw new AccountLockedException(lae.getMessage());
        } catch (final ExcessiveAttemptsException eae) {
            throw new AccountLockedException(eae.getMessage());
        } catch (final ExpiredCredentialsException eae) {
            throw new CredentialExpiredException(eae.getMessage());
        } catch (final DisabledAccountException eae) {
            throw new AccountDisabledException(eae.getMessage());
        } catch (final AuthenticationException ae){
            throw new FailedLoginException(ae.getMessage());
        }
    }

    /**
     * Check subject roles and permissions.
     *
     * @param currentUser the current user
     * @throws FailedLoginException the failed login exception in case roles or permissions are absent
     */
    protected void checkSubjectRolesAndPermissions(final Subject currentUser) throws FailedLoginException {
        for (final String role : this.requiredRoles) {
            if (!currentUser.hasRole(role)) {
                throw new FailedLoginException("Required role " + role + " does not exist");
            }
        }

        for (final String perm : this.requiredPermissions) {
            if (!currentUser.isPermitted(perm)) {
                throw new FailedLoginException("Required permission " + perm + " does not exist");
            }
        }
    }

    /**
     * Create authenticated subject result.
     *
     * @param credential the credential
     * @param currentUser the current user
     * @return the handler result
     */
    protected HandlerResult createAuthenticatedSubjectResult(final Credential credential,
                                                             final Subject currentUser) {
        final String username = currentUser.getPrincipal().toString();
        return createHandlerResult(credential, this.principalFactory.createPrincipal(username), null);
    }

    /**
     * Gets current executing subject.
     *
     * @return the current executing subject
     */
    protected Subject getCurrentExecutingSubject() {
        return SecurityUtils.getSubject();
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof RememberMeUsernamePasswordCredential;
    }
}

