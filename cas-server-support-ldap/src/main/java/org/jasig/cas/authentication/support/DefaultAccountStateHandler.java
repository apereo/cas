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
package org.jasig.cas.authentication.support;

import org.jasig.cas.DefaultMessageDescriptor;
import org.jasig.cas.MessageDescriptor;
import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.AccountPasswordMustChangeException;
import org.jasig.cas.authentication.InvalidLoginLocationException;
import org.jasig.cas.authentication.InvalidLoginTimeException;
import org.joda.time.Days;
import org.joda.time.Instant;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.ext.ActiveDirectoryAccountState;
import org.ldaptive.auth.ext.EDirectoryAccountState;
import org.ldaptive.auth.ext.PasswordExpirationAccountState;
import org.ldaptive.control.PasswordPolicyControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default account state handler.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class DefaultAccountStateHandler implements AccountStateHandler {
    /** Map of account state error to CAS authentication exception. */
    protected final Map<AccountState.Error, LoginException> errorMap;

    /** Logger instance. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Instantiates a new account state handler, that populates
     * the error map with LDAP error codes and corresponding exceptions.
     */
    public DefaultAccountStateHandler() {
        this.errorMap = new HashMap<>();
        this.errorMap.put(ActiveDirectoryAccountState.Error.ACCOUNT_DISABLED, new AccountDisabledException());
        this.errorMap.put(ActiveDirectoryAccountState.Error.ACCOUNT_LOCKED_OUT, new AccountLockedException());
        this.errorMap.put(ActiveDirectoryAccountState.Error.INVALID_LOGON_HOURS, new InvalidLoginTimeException());
        this.errorMap.put(ActiveDirectoryAccountState.Error.INVALID_WORKSTATION, new InvalidLoginLocationException());
        this.errorMap.put(ActiveDirectoryAccountState.Error.PASSWORD_MUST_CHANGE, new AccountPasswordMustChangeException());
        this.errorMap.put(ActiveDirectoryAccountState.Error.PASSWORD_EXPIRED, new CredentialExpiredException());
        this.errorMap.put(EDirectoryAccountState.Error.ACCOUNT_EXPIRED, new AccountExpiredException());
        this.errorMap.put(EDirectoryAccountState.Error.LOGIN_LOCKOUT, new AccountLockedException());
        this.errorMap.put(EDirectoryAccountState.Error.LOGIN_TIME_LIMITED, new InvalidLoginTimeException());
        this.errorMap.put(EDirectoryAccountState.Error.PASSWORD_EXPIRED, new CredentialExpiredException());
        this.errorMap.put(PasswordExpirationAccountState.Error.PASSWORD_EXPIRED, new CredentialExpiredException());
        this.errorMap.put(PasswordPolicyControl.Error.ACCOUNT_LOCKED, new AccountLockedException());
        this.errorMap.put(PasswordPolicyControl.Error.PASSWORD_EXPIRED, new CredentialExpiredException());
        this.errorMap.put(PasswordPolicyControl.Error.CHANGE_AFTER_RESET, new CredentialExpiredException());
    }

    @Override
    public List<MessageDescriptor> handle(final AuthenticationResponse response, final LdapPasswordPolicyConfiguration configuration)
            throws LoginException {

        final AccountState state = response.getAccountState();
        if (state == null) {
            logger.debug("Account state not defined. Returning empty list of messages.");
            return Collections.emptyList();
        }
        final List<MessageDescriptor> messages = new ArrayList<>();
        handleError(state.getError(), response, configuration, messages);
        handleWarning(state.getWarning(), response, configuration, messages);

        return messages;
    }

    /**
     * Handle an account state error produced by ldaptive account state machinery.
     * <p>
     * Override this method to provide custom error handling.
     *
     * @param error Account state error.
     * @param response Ldaptive authentication response.
     * @param configuration Password policy configuration.
     * @param messages Container for messages produced by account state error handling.
     *
     * @throws LoginException On errors that should be communicated as login exceptions.
     */
    protected void handleError(
            final AccountState.Error error,
            final AuthenticationResponse response,
            final LdapPasswordPolicyConfiguration configuration,
            final List<MessageDescriptor> messages)
            throws LoginException {

        logger.debug("Handling error {}", error);
        final LoginException ex = this.errorMap.get(error);
        if (ex != null) {
            throw ex;
        }
        logger.debug("No LDAP error mapping defined for {}", error);
    }


    /**
     * Handle an account state warning produced by ldaptive account state machinery.
     * <p>
     * Override this method to provide custom warning message handling.
     *
     * @param warning the account state warning messages.
     * @param response Ldaptive authentication response.
     * @param configuration Password policy configuration.
     * @param messages Container for messages produced by account state warning handling.
     */
    protected void handleWarning(
            final AccountState.Warning warning,
            final AuthenticationResponse response,
            final LdapPasswordPolicyConfiguration configuration,
            final List<MessageDescriptor> messages) {

        logger.debug("Handling warning {}", warning);
        if (warning == null) {
            logger.debug("Account state warning not defined");
            return;
        }

        final Calendar expDate = warning.getExpiration();
        final Days ttl = Days.daysBetween(Instant.now(), new Instant(expDate));
        logger.debug(
                "Password expires in {} days. Expiration warning threshold is {} days.",
                ttl.getDays(),
                configuration.getPasswordWarningNumberOfDays());
        if (configuration.isAlwaysDisplayPasswordExpirationWarning()
                || ttl.getDays() < configuration.getPasswordWarningNumberOfDays()) {
            messages.add(new PasswordExpiringWarningMessageDescriptor(
                    "Password expires in {0} days. Please change your password at <href=\"{1}\">{1}</a>",
                    ttl.getDays(),
                    configuration.getPasswordPolicyUrl()));
        }
        if (warning.getLoginsRemaining() > 0) {
            messages.add(new DefaultMessageDescriptor(
                    "password.expiration.loginsRemaining",
                    "You have {0} logins remaining before you MUST change your password.",
                    warning.getLoginsRemaining()));

        }
    }
}
