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
import org.springframework.beans.factory.InitializingBean;

/**
 * An extension of the {@link LdapAuthenticationHandler} that is aware of
 * the underlying password policy and can translate errors and warnings
 * back to the CAS flow.
 * @author Misagh Moayyed
 * @since 4.0
 */
public class LPPEAuthenticationHandler extends LdapAuthenticationHandler implements InitializingBean {

    private final PasswordPolicyConfiguration configuration;

    public LPPEAuthenticationHandler(@NotNull final Authenticator authenticator, @NotNull final PasswordPolicyConfiguration configuration) {
        super(authenticator);
        this.configuration = configuration;
    }

    @Override
    protected void examineAccountStatePostAuthentication(final AuthenticationResponse response) throws LoginException {
        super.examineAccountStatePostAuthentication(response);
        this.configuration.build(response.getLdapEntry());
        this.examineAccountStatus(response);
        this.validateAccountPasswordExpirationPolicy();
    }

    protected void examineAccountStatus(final AuthenticationResponse response) throws LoginException {
        final String uid =  this.configuration.getDn();
        
        if (this.configuration.isUserAccountControlSetToDisableAccount()) {
            final String msg = String.format("User account control flag is set. Account %s is disabled", uid);
            throw new AccountDisabledException(msg);
        }

        if (this.configuration.isUserAccountControlSetToLockAccount()) {
            final String msg = String.format("User account control flag is set. Account %s is locked", uid);
            throw new AccountLockedException(msg);
        }

        if (this.configuration.isUserAccountControlSetToExpirePassword()) {
            final String msg = String.format("User account control flag is set. Account %s has expired", uid);
            throw new CredentialExpiredException(msg);
        }

        if (this.configuration.isAccountDisabled()) {
            final String msg = String.format("Password policy attribute %s is set. Account %s is disabled",
                    this.configuration.getAccountDisabledAttributeName() , uid);
            throw new AccountDisabledException(msg);
        }
        
        if (this.configuration.isAccountLocked()) {
            final String msg = String.format("Password policy attribute %s is set. Account %s is locked",
                    this.configuration.getAccountLockedAttributeName(), uid);
            throw new AccountLockedException(msg);
        }
        
        if (this.configuration.isAccountPasswordMustChange()) {
            final String msg = String.format("Password policy attribute %s is set. Account %s must change it password", 
                                             this.configuration.getAccountPasswordMustChangeAttributeName(), uid);
            throw new AccountPasswordMustChangeException(msg);
        }
    }
        
    @Override
    public void afterPropertiesSet() throws Exception {
        populatePrincipalAttributeMap();
    }

    private void populatePrincipalAttributeMap() {
        if (!StringUtils.isBlank(this.configuration.getUserAccountControlAttributeName())) {
            principalAttributeMap.put(this.configuration.getUserAccountControlAttributeName(),
                                      this.configuration.getUserAccountControlAttributeName());
        }
        
        if (!StringUtils.isBlank(this.configuration.getAccountDisabledAttributeName())) {
            principalAttributeMap.put(this.configuration.getAccountDisabledAttributeName(),
                                      this.configuration.getAccountDisabledAttributeName());
        }
        
        if (!StringUtils.isBlank(this.configuration.getAccountLockedAttributeName())) {
            principalAttributeMap.put(this.configuration.getAccountLockedAttributeName(),
                                      this.configuration.getAccountLockedAttributeName());
        }
        
        if (!StringUtils.isBlank(this.configuration.getAccountPasswordMustChangeAttributeName())) {
            principalAttributeMap.put(this.configuration.getAccountPasswordMustChangeAttributeName(),
                                      this.configuration.getAccountPasswordMustChangeAttributeName());
        }
        
        if (!StringUtils.isBlank(this.configuration.getIgnorePasswordExpirationWarningAttributeName())) {
            principalAttributeMap.put(this.configuration.getIgnorePasswordExpirationWarningAttributeName(),
                                      this.configuration.getIgnorePasswordExpirationWarningAttributeName());
        }
        
        if (!StringUtils.isBlank(this.configuration.getPasswordExpirationDateAttributeName())) {
            principalAttributeMap.put(this.configuration.getPasswordExpirationDateAttributeName(),
                                      this.configuration.getPasswordExpirationDateAttributeName());
        }
        
        if (!StringUtils.isBlank(this.configuration.getPasswordWarningNumberOfDaysAttributeName())) {
            principalAttributeMap.put(this.configuration.getPasswordWarningNumberOfDaysAttributeName(),
                                      this.configuration.getPasswordWarningNumberOfDaysAttributeName());
        }
        
        if (!StringUtils.isBlank(this.configuration.getValidPasswordNumberOfDaysAttributeName())) {
            principalAttributeMap.put(this.configuration.getValidPasswordNumberOfDaysAttributeName(),
                                      this.configuration.getValidPasswordNumberOfDaysAttributeName());
        }
    }
    
    /**
     * Calculates the number of days left to the expiration date.
     * @return Number of days left to the expiration date or -1 if the no expiration warning is 
     * calculated based on the defined policy. 
     */
    protected int getDaysToExpirationDate(final DateTime expireDate) throws LoginException {
        final DateTimeZone timezone = this.configuration.getDateConverter().getTimeZone();
        final DateTime currentTime = new DateTime(this.configuration.getDateConverter().getTimeZone());
        log.debug("Current date is {}. Expiration date is {}", currentTime, expireDate);

        final Days d = Days.daysBetween(currentTime, expireDate);
        int daysToExpirationDate = d.getDays();

        log.debug("Days left to the expiration date: {}", daysToExpirationDate);
        if (expireDate.equals(currentTime) || expireDate.isBefore(currentTime)) {
            final String msgToLog = String.format("Password expiration date %s is on/before the current time %s and has expired.",
                                          daysToExpirationDate, currentTime);
            log.debug(msgToLog);
            return 0;
        }

        // Warning period begins from X number of days before the expiration date
        final DateTime warnPeriod = new DateTime(DateTime.parse(expireDate.toString()), timezone)
                                        .minusDays(this.configuration.getPasswordWarningNumberOfDays());
        log.debug("Warning period begins on {}", warnPeriod);

        if (this.configuration.isAlwaysDisplayPasswordExpirationWarning()) {
            log.debug("Warning all. The password for {} will expire in {} day(s)", this.configuration.getDn(), daysToExpirationDate);
        } else if (currentTime.equals(warnPeriod) || currentTime.isAfter(warnPeriod)) {
            log.debug("Password will expire in {} day(s)", daysToExpirationDate);
        } else {
            log.debug("Password is not expiring. {} day(s) left to the warning", daysToExpirationDate);
            daysToExpirationDate = -1;
        }

        return daysToExpirationDate;
    }
    
    /**
     * Determines the expiration date to use based on the password policy configuration.
     * @see #setLdapDateConverter(LdapDateConverter)
     */
    private DateTime getExpirationDateToUse() {
        final DateTime dateValue = this.configuration.convertPasswordExpirationDate(); 
                
        final DateTime expireDate = dateValue.plusDays(this.configuration.getValidPasswordNumberOfDays());
        log.debug("Retrieved date value {} for date attribute {} and added {} days. The final expiration date is {}", dateValue,
                this.configuration.getPasswordExpirationDateAttributeName(), this.configuration
                        .getValidPasswordNumberOfDays(), expireDate);

        return expireDate;
    }

    private void validateAccountPasswordExpirationPolicy() throws LoginException {
        if (this.configuration.isAccountPasswordSetToNeverExpire()) {
            log.debug("Account password will never expire. Skipping password policy...");
            return;
        }

        final DateTime expireTime = getExpirationDateToUse();
        final int days = getDaysToExpirationDate(expireTime);
        if (days != -1) {
            final String msg = String.format("Password expires in %d days", days);
            log.debug(msg);
            throw new AccountPasswordExpiringException(msg, days);
        }
    }
}
