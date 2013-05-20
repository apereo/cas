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
package org.jasig.cas.adaptors.ldap.lppe;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.AccountPasswordExpiringException;
import org.jasig.cas.authentication.AccountPasswordMustChangeException;
import org.jasig.cas.authentication.LdapAuthenticationHandler;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.Authenticator;

/**
 * An extension of the {@link LdapAuthenticationHandler} that is aware of
 * the underlying password policy and can translate errors and warnings
 * back to the CAS flow.
 * @author Misagh Moayyed
 * @since 4.0
 */
public class LPPEAuthenticationHandler extends LdapAuthenticationHandler {

    /** The ldap configuration constructed based on given the policy. **/
    private final PasswordPolicyConfiguration configuration;

    public LPPEAuthenticationHandler(@NotNull final Authenticator authenticator,
            @NotNull final PasswordPolicyConfiguration configuration) {
        super(authenticator);
        this.configuration = configuration;
    }

    @Override
    protected final void doPostAuthentication(final AuthenticationResponse response)
            throws LoginException {
        
        if (!configuration.build(response.getLdapEntry())) {
            if (configuration.isCritical()) {
                throw new FailedLoginException("LPPE authentication failed. Configuration cannot be determined.");
            } else {
                log.warn("LPPE configuration cannot be determined.");
                return;
            }
        }
        
        try { 
            this.examineAccountStatus(response);
            this.validateAccountPasswordExpirationPolicy();
        } catch (final LoginException e) {
            if (configuration.isCritical()) {
                throw e;
            } else {
                log.warn("LPPE authentication failed. Skipped policy checks.");
            }
        }
    }
    
    protected void examineAccountStatus(final AuthenticationResponse response) throws LoginException {
        final String uid =  configuration.getDn();

        if (configuration.isUserAccountControlSetToDisableAccount()) {
            final String msg = String.format("User account control flag is set. Account %s is disabled", uid);
            throw new AccountDisabledException(msg);
        }

        if (configuration.isUserAccountControlSetToLockAccount()) {
            final String msg = String.format("User account control flag is set. Account %s is locked", uid);
            throw new AccountLockedException(msg);
        }

        if (configuration.isUserAccountControlSetToExpirePassword()) {
            final String msg = String.format("User account control flag is set. Account %s has expired", uid);
            throw new CredentialExpiredException(msg);
        }

        if (configuration.isAccountDisabled()) {
            final String msg = String.format("Password policy attribute %s is set. Account %s is disabled",
                    configuration.getAccountDisabledAttributeName() , uid);
            throw new AccountDisabledException(msg);
        }

        if (configuration.isAccountLocked()) {
            final String msg = String.format("Password policy attribute %s is set. Account %s is locked",
                    configuration.getAccountLockedAttributeName(), uid);
            throw new AccountLockedException(msg);
        }

        if (configuration.isAccountPasswordMustChange()) {
            final String msg = String.format("Password policy attribute %s is set. Account %s must change it password",
                                             configuration.getAccountPasswordMustChangeAttributeName(), uid);
            throw new AccountPasswordMustChangeException(msg);
        }
    }

    

    @Override
    protected void afterPropertiesSetInternal() {
        populatePrincipalAttributeMap();
    }

    private void populatePrincipalAttributeMap() {
        if (!StringUtils.isBlank(configuration.getUserAccountControlAttributeName())) {
            principalAttributeMap.put(configuration.getUserAccountControlAttributeName(),
                                      configuration.getUserAccountControlAttributeName());
        }

        if (!StringUtils.isBlank(configuration.getAccountDisabledAttributeName())) {
            principalAttributeMap.put(configuration.getAccountDisabledAttributeName(),
                                      configuration.getAccountDisabledAttributeName());
        }

        if (!StringUtils.isBlank(configuration.getAccountLockedAttributeName())) {
            principalAttributeMap.put(configuration.getAccountLockedAttributeName(),
                                      configuration.getAccountLockedAttributeName());
        }

        if (!StringUtils.isBlank(configuration.getAccountPasswordMustChangeAttributeName())) {
            principalAttributeMap.put(configuration.getAccountPasswordMustChangeAttributeName(),
                                      configuration.getAccountPasswordMustChangeAttributeName());
        }

        if (!StringUtils.isBlank(configuration.getIgnorePasswordExpirationWarningAttributeName())) {
            principalAttributeMap.put(configuration.getIgnorePasswordExpirationWarningAttributeName(),
                                      configuration.getIgnorePasswordExpirationWarningAttributeName());
        }

        if (!StringUtils.isBlank(configuration.getPasswordExpirationDateAttributeName())) {
            principalAttributeMap.put(configuration.getPasswordExpirationDateAttributeName(),
                                      configuration.getPasswordExpirationDateAttributeName());
        }

        if (!StringUtils.isBlank(configuration.getPasswordWarningNumberOfDaysAttributeName())) {
            principalAttributeMap.put(configuration.getPasswordWarningNumberOfDaysAttributeName(),
                                      configuration.getPasswordWarningNumberOfDaysAttributeName());
        }

        if (!StringUtils.isBlank(configuration.getValidPasswordNumberOfDaysAttributeName())) {
            principalAttributeMap.put(configuration.getValidPasswordNumberOfDaysAttributeName(),
                                      configuration.getValidPasswordNumberOfDaysAttributeName());
        }
    }

    /**
     * Calculates the number of days left to the expiration date.
     * @return Number of days left to the expiration date or -1 if the no expiration warning is
     * calculated based on the defined policy.
     */
    protected int getDaysToExpirationDate(final DateTime expireDate) throws LoginException {
        final DateTimeZone timezone = configuration.getDateConverter().getTimeZone();
        final DateTime currentTime = new DateTime(timezone);
        log.debug("Current date is {}. Expiration date is {}", currentTime, expireDate);

        final Days d = Days.daysBetween(currentTime, expireDate);
        int daysToExpirationDate = d.getDays();

        log.debug("[{}] days left to the expiration date.", daysToExpirationDate);
        if (expireDate.equals(currentTime) || expireDate.isBefore(currentTime)) {
            final String msgToLog = String.format("Password expiration date %s is on/before the current time %s.",
                                          daysToExpirationDate, currentTime);
            log.debug(msgToLog);
            throw new CredentialExpiredException(msgToLog);
        }

        // Warning period begins from X number of days before the expiration date
        final DateTime warnPeriod = new DateTime(DateTime.parse(expireDate.toString()), timezone)
                                        .minusDays(configuration.getPasswordWarningNumberOfDays());
        log.debug("Warning period begins on [{}]", warnPeriod);

        if (configuration.isAlwaysDisplayPasswordExpirationWarning()) {
            log.debug("Warning all. The password for [{}] will expire in [{}] day(s).",
                    configuration.getDn(), daysToExpirationDate);
        } else if (currentTime.equals(warnPeriod) || currentTime.isAfter(warnPeriod)) {
            log.debug("Password will expire in [{}] day(s)", daysToExpirationDate);
        } else {
            log.debug("Password is not expiring. [{}] day(s) left to the warning.", daysToExpirationDate);
            daysToExpirationDate = -1;
        }

        return daysToExpirationDate;
    }

    /**
     * Determines the expiration date to use based on the password policy configuration.
     * @see #setLdapDateConverter(LdapDateConverter)
     */
    private DateTime getExpirationDateToUse() {
        final DateTime dateValue = configuration.convertPasswordExpirationDate();

        final DateTime expireDate = dateValue.plusDays(configuration.getValidPasswordNumberOfDays());
        log.debug("Retrieved date value [{}] for date attribute [{}] and added {} valid days. "
                    + "The final expiration date is [{}]", dateValue,
                configuration.getPasswordExpirationDateAttributeName(),
                configuration.getValidPasswordNumberOfDays(), expireDate);

        return expireDate;
    }

    private void validateAccountPasswordExpirationPolicy() throws LoginException {
        if (configuration.isAccountPasswordSetToNeverExpire()) {
            log.debug("Account password will never expire. Skipping password policy...");
            return;
        }

        final DateTime expireTime = getExpirationDateToUse();
        final int days = getDaysToExpirationDate(expireTime);
        if (days != -1) {
            final String msg = String.format("Password expires in [%d] days", days);
            log.debug(msg);
            throw new AccountPasswordExpiringException(msg, days);
        }
    }
}
