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

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * An implementation of the {@link LdapPasswordPolicyExaminer} that determines whether
 * an ldap account's password has expired. 
 */
public class LdapPasswordExpirationPolicyExaminer implements LdapPasswordPolicyExaminer {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /** The ldap converter used in calculating the expiration date attribute value.*/
    @NotNull
    private LdapDateConverter ldapDateConverter = null;
    
    /** The value that will cause password warning to be bypassed  */
    private List<String> ignorePasswordExpirationWarningFlags;
    
    /** An instance of the password policy configuration retrieved from the ldap instance.*/
    private LdapPasswordPolicyConfiguration configuration = null;
    
    /** Disregard the warning period and warn all users of password expiration */
    private boolean alwaysDisplayPasswordExpirationWarning = false;
    
    /**
     * Set the flag values which will used to calculate whether the password expiration
     * warning should be ignored for this account. 
     * @see #isAccountPasswordSetToNeverExpire()
     */
    public void setIgnorePasswordExpirationWarningFlags(final List<String> ignorePasswordWarningFlags) {
        this.ignorePasswordExpirationWarningFlags = ignorePasswordWarningFlags;
    }

    /** Set the ldap converter used in calculating the expiration date attribute value.*/
    @Required
    public void setLdapDateConverter(final LdapDateConverter ldapDateConverter) {
        this.ldapDateConverter = ldapDateConverter;
    }
    
    protected LdapPasswordPolicyConfiguration getPasswordPolicyConfiguration() {
        return this.configuration;
    }
    
    @Override
    public final void examinePasswordPolicy(final LdapPasswordPolicyConfiguration configuration) throws LdapPasswordPolicyAuthenticationException {
        this.configuration = configuration;
        validateAccountPasswordExpirationPolicy();
    }

    public void setAlwaysDisplayPasswordExpirationWarning(final boolean warnAll) {
        this.alwaysDisplayPasswordExpirationWarning = warnAll;
    }
    
    /**
     * Calculates the number of days left to the expiration date 
     * @return Number of days left to the expiration date or -1 if the no expiration warning is 
     * calculated based on the defined policy. 
     */
    private int getDaysToExpirationDate(final DateTime expireDate) throws LdapPasswordPolicyAuthenticationException {

        log.debug("Calculating number of days left to the expiration date for user {}", getPasswordPolicyConfiguration().getCredentials().getUsername());

        final DateTime currentTime = new DateTime(this.ldapDateConverter.getTimeZone());
        log.debug("Current date is {}. Expiration date is {}" + currentTime, expireDate);

        final Days d = Days.daysBetween(currentTime, expireDate);
        int daysToExpirationDate = d.getDays();

        if (expireDate.equals(currentTime) || expireDate.isBefore(currentTime)) {
            final String msgToLog = String.format("Password expiration date %s is on/before the current time %s. The account password has expired.",
                                            daysToExpirationDate, currentTime);
            log.debug(msgToLog);
            return 0;
        }

        // Warning period begins from X number of days before the expiration date
        final DateTime warnPeriod = new DateTime(DateTime.parse(expireDate.toString()), 
                                     this.ldapDateConverter.getTimeZone()).minusDays(getPasswordPolicyConfiguration().getPasswordWarningNumberOfDays());
        log.debug("Warning period begins on {}", warnPeriod.toString());

        if (this.alwaysDisplayPasswordExpirationWarning) {
            log.debug("Warning all. The password for {} will expire in {} day(s)", getPasswordPolicyConfiguration().getCredentials().getUsername(), daysToExpirationDate);
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
        final DateTime dateValue = this.ldapDateConverter.convert(getPasswordPolicyConfiguration().getPasswordExpirationDate());
        final DateTime expireDate = dateValue.plusDays(getPasswordPolicyConfiguration().getValidPasswordNumberOfDays());
        log.debug("Retrieved date value {} for date attribute {} and added {} days. The final expiration date is {}", dateValue,
                getPasswordPolicyConfiguration().getPasswordExpirationDate(), getPasswordPolicyConfiguration().getValidPasswordNumberOfDays(), expireDate);

        return expireDate;
    }
    
    private void validateAccountPasswordExpirationPolicy() throws LdapPasswordPolicyAuthenticationException {
        if (isAccountPasswordSetToNeverExpire()) {
            log.debug("Account password will never expire. Skipping password warning checks...");
            return;
        }

        final DateTime expireTime = getExpirationDateToUse();
        final int days = getDaysToExpirationDate(expireTime);
        if (days != -1) {
            final String msg = String.format("Password expires in %d days", days);
            throw new LdapPasswordPolicyExpirationException(msg, days); 
        }
    }
    
    /**
     * Determines if the password value is set to never expire. Takes into account {@link #setIgnorePasswordExpirationWarningFlags(List)}
     * and the policy defined for {@link LdapPasswordPolicyConfiguration#getUserAccountControl()}, if any.
     */
    private boolean isAccountPasswordSetToNeverExpire() {
        final String ignoreCheckValue = getPasswordPolicyConfiguration().getIgnorePasswordExpirationWarning();
        boolean ignoreChecks = false;

       
        if (!StringUtils.isBlank(ignoreCheckValue) && this.ignorePasswordExpirationWarningFlags != null) {
            ignoreChecks = this.ignorePasswordExpirationWarningFlags.contains(ignoreCheckValue);
        }
    
        final long uacValue  = getPasswordPolicyConfiguration().getUserAccountControl();
        if (!ignoreChecks && uacValue > 0) {
            ignoreChecks = ((uacValue & ActiveDirectoryUserAccountControlFlags.UAC_FLAG_DONT_EXPIRE_PASSWD.getValue()) == 
                           ActiveDirectoryUserAccountControlFlags.UAC_FLAG_DONT_EXPIRE_PASSWD.getValue());
        }
        return ignoreChecks;
    }

}
