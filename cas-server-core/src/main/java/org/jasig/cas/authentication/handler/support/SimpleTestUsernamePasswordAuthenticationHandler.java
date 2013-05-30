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
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;

import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.InvalidLoginLocationException;
import org.jasig.cas.authentication.InvalidLoginTimeException;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Simple test implementation of a AuthenticationHandler that returns true if
 * the username and password match. This class should never be enabled in a
 * production environment and is only designed to facilitate unit testing and
 * load testing.
 *
 * @author Scott Battagliaa
 * @author Marvin S. Addison
 * @since 3.0
 */
public final class SimpleTestUsernamePasswordAuthenticationHandler implements AuthenticationHandler {
    /** Default mapping of special usernames to exceptions raised when that user attempts authentication. */
    private static final Map<String, Exception> DEFAULT_USERNAME_ERROR_MAP = new HashMap<String, Exception>();

    /** Instance of logging for subclasses. */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /** Map of special usernames to exceptions that are raised when a user with that name attempts authentication. */
    private Map<String, Exception> usernameErrorMap = DEFAULT_USERNAME_ERROR_MAP;


    static {
        DEFAULT_USERNAME_ERROR_MAP.put("accountDisabled", new AccountDisabledException("Account disabled"));
        DEFAULT_USERNAME_ERROR_MAP.put("accountLocked", new AccountLockedException("Account locked"));
        DEFAULT_USERNAME_ERROR_MAP.put("badHours", new InvalidLoginTimeException("Invalid logon hours"));
        DEFAULT_USERNAME_ERROR_MAP.put("badWorkstation", new InvalidLoginLocationException("Invalid workstation"));
        DEFAULT_USERNAME_ERROR_MAP.put("passwordExpired", new CredentialExpiredException("Password expired"));
    }

    public SimpleTestUsernamePasswordAuthenticationHandler() {
        log.warn(
                "{} is only to be used in a testing environment.  NEVER enable this in a production environment.",
                getName());
    }

    public void setUsernameErrorMap(final Map<String, Exception> map) {
        this.usernameErrorMap = map;
    }

    @Override
    public final HandlerResult authenticate(final Credentials credential)
            throws GeneralSecurityException, PreventedException {

        final UsernamePasswordCredentials passwordCredential = (UsernamePasswordCredentials) credential;
        final String username = passwordCredential.getUsername();
        final String password = passwordCredential.getPassword();

        final Exception exception = this.usernameErrorMap.get(username);
        if (exception instanceof GeneralSecurityException) {
            throw (GeneralSecurityException) exception;
        } else if (exception instanceof PreventedException) {
            throw (PreventedException) exception;
        } else if (exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        } else if (exception != null) {
            log.debug("Cannot throw checked exception {} since it is not declared by method signature.", exception);
        }

        if (StringUtils.hasText(username) && StringUtils.hasText(password) && username.equals(password)) {
            log.debug("User [{}] was successfully authenticated.", username);
            return new HandlerResult(this, new BasicCredentialMetaData(credential));
        }
        log.debug("User [{}] failed authentication", username);
        throw new FailedLoginException();
    }

    @Override
    public boolean supports(final Credentials credential) {
        return credential instanceof UsernamePasswordCredentials;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}