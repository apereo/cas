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
package org.jasig.cas.authentication.support;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.LoginException;

import org.jasig.cas.Message;
import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.AccountPasswordMustChangeException;
import org.jasig.cas.authentication.InvalidLoginLocationException;
import org.jasig.cas.authentication.InvalidLoginTimeException;
import org.joda.time.Days;
import org.joda.time.Instant;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.ext.ActiveDirectoryAccountState;
import org.ldaptive.auth.ext.EDirectoryAccountState;
import org.ldaptive.auth.ext.PasswordExpirationAccountState;
import org.ldaptive.control.PasswordPolicyControl;

/**
 * Default account state handler.
 *
 * @author Marvin S. Addison
 */
public class DefaultAccountStateHander implements AccountStateHandler {

    /** Map of account state error to CAS authentication exception. */
    private static final Map<AccountState.Error, LoginException> ERROR_MAP;

    static {
        ERROR_MAP = new HashMap<AccountState.Error, LoginException>();
        ERROR_MAP.put(ActiveDirectoryAccountState.Error.ACCOUNT_DISABLED, new AccountDisabledException());
        ERROR_MAP.put(ActiveDirectoryAccountState.Error.ACCOUNT_LOCKED_OUT, new AccountLockedException());
        ERROR_MAP.put(ActiveDirectoryAccountState.Error.INVALID_LOGON_HOURS, new InvalidLoginTimeException());
        ERROR_MAP.put(ActiveDirectoryAccountState.Error.INVALID_WORKSTATION, new InvalidLoginLocationException());
        ERROR_MAP.put(ActiveDirectoryAccountState.Error.PASSWORD_MUST_CHANGE, new AccountPasswordMustChangeException());
        ERROR_MAP.put(ActiveDirectoryAccountState.Error.PASSWORD_EXPIRED, new CredentialExpiredException());
        ERROR_MAP.put(EDirectoryAccountState.Error.ACCOUNT_EXPIRED, new AccountExpiredException());
        ERROR_MAP.put(EDirectoryAccountState.Error.LOGIN_LOCKOUT, new AccountLockedException());
        ERROR_MAP.put(EDirectoryAccountState.Error.LOGIN_TIME_LIMITED, new InvalidLoginTimeException());
        ERROR_MAP.put(EDirectoryAccountState.Error.PASSWORD_EXPIRED, new CredentialExpiredException());
        ERROR_MAP.put(PasswordExpirationAccountState.Error.PASSWORD_EXPIRED, new CredentialExpiredException());
        ERROR_MAP.put(PasswordPolicyControl.Error.ACCOUNT_LOCKED, new AccountLockedException());
        ERROR_MAP.put(PasswordPolicyControl.Error.PASSWORD_EXPIRED, new CredentialExpiredException());
    }

    @Override
    public List<Message> handle(final AccountState state, final LdapPasswordPolicyConfiguration configuration)
            throws LoginException {

        final LoginException error = ERROR_MAP.get(state.getError());
        if (error != null) {
            throw error;
        }
        final List<Message> messages = new ArrayList<Message>();
        if (state.getWarning() != null) {
            final Calendar expDate = state.getWarning().getExpiration();
            final Days ttl = Days.daysBetween(Instant.now(), new Instant(expDate));
            if (ttl.getDays() < configuration.getPasswordWarningNumberOfDays()) {
                messages.add(new PasswordExpiringWarningMessage(
                        "Password expires in {0} day(s). Please change your password at <href=\"{1}\">{1}</a>",
                        ttl.getDays(),
                        configuration.getPasswordPolicyUrl()));
            }
        }
        return messages;
    }
}
