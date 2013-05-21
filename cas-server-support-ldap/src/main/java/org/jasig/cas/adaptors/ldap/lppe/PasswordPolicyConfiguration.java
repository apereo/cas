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

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * The password policy configuration defined by the underlying data source.
 * @author Misagh Moayyed
 * @version 4.0.0
 */
public final class PasswordPolicyConfiguration implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(PasswordPolicyConfiguration.class);

    /**
     * This enumeration defines a selective limited set of ldap user account control flags
     * that indicate various statuses of the user account. The account status
     * is a bitwise flag that may contain one of more of the following values.
     */
    private enum ActiveDirectoryUserAccountControlFlags {
        ADS_UF_ACCOUNT_DISABLE(0x00000002),
        ADS_UF_LOCKOUT(0x00000010),
        ADS_UF_PASSWORD_NOTREQUIRED(0x00000020),
        ADS_UF_PASSWD_CANT_CHANGE(0x00000040),
        ADS_UF_NORMAL_ACCOUNT(0x00000200),
        ADS_UF_DONT_EXPIRE_PASSWD(0x00010000),
        ADS_UF_PASSWORD_EXPIRED(0x00800000);

        private long value;

        ActiveDirectoryUserAccountControlFlags(final long id) {
            this.value = id;
        }

        public long getValue() {
            return this.value;
        }
    }

    /** The ldap converter used in calculating the expiration date attribute value.*/
    @NotNull
    private LdapDateConverter ldapDateConverter = null;

    /** the date/time formatter object used to parse date patterns in dates. **/
    private DateTimeFormatter datetimeFormatter = DateTimeFormat.fullDate();
    
    /** The value that will cause password warning to be bypassed.  */
    private List<String> ignorePasswordExpirationWarningFlags = new ArrayList<String>();

    /** Disregard the warning period and warn all users of password expiration. */
    private boolean alwaysDisplayPasswordExpirationWarning = false;

    private String passwordExpirationDate;

    private String ignorePasswordExpirationWarning;

    private String passwordExpirationDateAttributeName;

    private int validPasswordNumberOfDays;
    
    private int passwordWarningNumberOfDays;

    private boolean accountDisabled;
    
    private boolean accountLocked;
    
    private boolean accountPasswordMustChange;

    private long userAccountControl = -1;

    /** The custom attribute that indicates the account is disabled. **/
    private String accountDisabledAttributeName = null;

    /** The custom attribute that indicates the account is locked. **/
    private String accountLockedAttributeName = null;

    /** The custom attribute that indicates the account password must change. **/
    private String accountPasswordMustChangeAttributeName = null;

    /** The attribute that indicates the user account status. **/
    private String userAccountControlAttributeName = "userAccountControl";

    /** The attribute that contains the data that will determine if password warning is skipped.  */
    private String ignorePasswordExpirationWarningAttributeName = null;

    /** Default number of days which the password may be considered valid. **/
    private int defaultValidPasswordNumberOfDays = 90;

    /** Default number of days to use when calculating the warning period. **/
    private int defaultPasswordWarningNumberOfDays = 30;

    /** Url to the password policy application. **/
    private String passwordPolicyUrl;

    /** The attribute that contains the user's warning days. */
    private String passwordWarningNumberOfDaysAttributeName = null;

    /** The attribute that contains the number of days the user's password is valid. */
    private String validPasswordNumberOfDaysAttributeName = null;

    private String staticPasswordExpirationDate = null;
   
    private String dn;

    private boolean isCritical;

    public boolean isAlwaysDisplayPasswordExpirationWarning() {
        return this.alwaysDisplayPasswordExpirationWarning;
    }

    public void setAlwaysDisplayPasswordExpirationWarning(final boolean alwaysDisplayPasswordExpirationWarning) {
        this.alwaysDisplayPasswordExpirationWarning = alwaysDisplayPasswordExpirationWarning;
    }

    public PasswordPolicyConfiguration() {
    }

    public String getPasswordPolicyUrl() {
        return this.passwordPolicyUrl;
    }

    public void setPasswordPolicyUrl(final String passwordPolicyUrl) {
        this.passwordPolicyUrl = passwordPolicyUrl;
    }

    public void setAccountDisabledAttributeName(final String accountDisabledAttributeName) {
        this.accountDisabledAttributeName = accountDisabledAttributeName;
    }

    public String getAccountDisabledAttributeName() {
        return this.accountDisabledAttributeName;
    }

    public void setAccountLockedAttributeName(final String accountLockedAttributeName) {
        this.accountLockedAttributeName = accountLockedAttributeName;
    }

    public String getAccountLockedAttributeName() {
        return this.accountLockedAttributeName;
    }

    public void setAccountPasswordMustChangeAttributeName(final String accountPasswordMustChange) {
        this.accountPasswordMustChangeAttributeName = accountPasswordMustChange;
    }

    public String getAccountPasswordMustChangeAttributeName() {
        return this.accountPasswordMustChangeAttributeName;
    }

    public long getUserAccountControl() {
        return this.userAccountControl;
    }

    public void setUserAccountControlAttributeName(final String attr) {
        this.userAccountControlAttributeName = attr;
    }

    public String getUserAccountControlAttributeName() {
        return this.userAccountControlAttributeName;
    }

    public void setValidPasswordNumberOfDaysAttributeName(final String validDaysAttributeName) {
        this.validPasswordNumberOfDaysAttributeName = validDaysAttributeName;
    }

    public String getValidPasswordNumberOfDaysAttributeName() {
        return this.validPasswordNumberOfDaysAttributeName;
    }

    public void setPasswordWarningNumberOfDaysAttributeName(final String warningDaysAttributeName) {
        this.passwordWarningNumberOfDaysAttributeName = warningDaysAttributeName;
    }

    public String getPasswordWarningNumberOfDaysAttributeName() {
        return this.passwordWarningNumberOfDaysAttributeName;
    }

    public void setDefaultValidPasswordNumberOfDays(final int days) {
        this.defaultValidPasswordNumberOfDays = days;
    }

    public void setDefaultPasswordWarningNumberOfDays(final int days) {
        this.defaultPasswordWarningNumberOfDays = days;
    }
    
    /**
     * Provides a constant password expiration date value, beyond which
     * the account will be considered expired. When provided, the 
     * number of days the password may remain valid is set to zero
     * via {@link #setValidPasswordNumberOfDays(int)}.
     * @param date
     * @see #setDateTimeFormatter(DateTimeFormatter)
     */
    public void setStaticPasswordExpirationDate(final String date) {
        this.staticPasswordExpirationDate = date;
    }
    
    public void setDateTimeFormatter(final DateTimeFormatter fmt) {
        this.datetimeFormatter = fmt;
    }
    
    /**
     * Construct the static password expiration date based on the formatter defined.
     * @return the static password expiration date.
     * @see #setDateTimeFormatter(DateTimeFormatter)
     * @see #setStaticPasswordExpirationDate(String)
     */
    public DateTime getStaticPasswordExpirationDate() {
        if (staticPasswordExpirationDate != null) {
            return DateTime.parse(this.staticPasswordExpirationDate, this.datetimeFormatter);
        }
        return null;
    }

    private void setUserAccountControl(final String userAccountControl) {
        if (!StringUtils.isBlank(userAccountControl) && NumberUtils.isNumber(userAccountControl)) {
            this.userAccountControl = Long.parseLong(userAccountControl);
        }
    }

    public boolean isAccountDisabled() {
        return this.accountDisabled;
    }

    private void setAccountDisabled(final boolean accountDisabled) {
        this.accountDisabled = accountDisabled;
    }

    public boolean isAccountLocked() {
        return this.accountLocked;
    }

    private void setAccountLocked(final boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public boolean isAccountPasswordMustChange() {
        return this.accountPasswordMustChange;
    }

    private void setAccountPasswordMustChange(final boolean accountPasswordMustChange) {
        this.accountPasswordMustChange = accountPasswordMustChange;
    }

    public String getPasswordExpirationDate() {
        return this.passwordExpirationDate;
    }

    public String getIgnorePasswordExpirationWarningAttributeName() {
        return this.ignorePasswordExpirationWarningAttributeName;
    }

    public int getValidPasswordNumberOfDays() {
        return this.validPasswordNumberOfDays;
    }

    public int getPasswordWarningNumberOfDays() {
        return this.passwordWarningNumberOfDays;
    }

    private void setPasswordExpirationDate(final String date) {
        this.passwordExpirationDate = date;
    }

    public void setIgnorePasswordExpirationWarningAttributeName(final String value) {
        this.ignorePasswordExpirationWarningAttributeName = value;
    }

    public void setValidPasswordNumberOfDays(final int valid) {
        this.validPasswordNumberOfDays = valid;
    }

    public void setPasswordWarningNumberOfDays(final int days) {
        this.passwordWarningNumberOfDays = days;
    }

    public void setPasswordExpirationDateAttributeName(final String value) {
        this.passwordExpirationDateAttributeName = value;
    }

    public String getPasswordExpirationDateAttributeName() {
        return this.passwordExpirationDateAttributeName;
    }

    public String getIgnorePasswordExpirationWarning() {
        return this.ignorePasswordExpirationWarning;
    }

    private void setIgnorePasswordExpirationWarning(final String warning) {
        this.ignorePasswordExpirationWarning = warning;
    }

    public LdapDateConverter getDateConverter() {
        return this.ldapDateConverter;
    }

    public void setDateConverter(final LdapDateConverter converter) {
        this.ldapDateConverter = converter;
    }

    public String getDn() {
        return this.dn;
    }

    private void setDn(final String dn) {
        this.dn = dn;
    }

    public boolean isCritical() {
        return this.isCritical;
    }

    /** Determines whether password policy errors should simply be ignored,
     * or they should be treated as critical issues failing the authentication flow.
     * @param critical if policy should be critical.
     */
    public void setCritical(final boolean critical) {
        this.isCritical = critical;
    }

    /**
     * Evaluate whether an account is set to never expire. 
     * Compares the account against configured ignore values for password expiration warning.
     * Then checks for AD-specific user account control values
     * {@link ActiveDirectoryUserAccountControlFlags#ADS_UF_DONT_EXPIRE_PASSWD} and 
     * {@link ActiveDirectoryUserAccountControlFlags#ADS_UF_PASSWD_CANT_CHANGE}. Finally,
     * checks the value of password expiration date (if numeric) to be greater than zero.
     * @return true, if the any of the above conditions return true.
     */
    public boolean isAccountPasswordSetToNeverExpire() {
        final String ignoreCheckValue = getIgnorePasswordExpirationWarning();
        boolean ignoreChecks = false;

        if (!StringUtils.isBlank(ignoreCheckValue) && this.ignorePasswordExpirationWarningFlags != null) {
            ignoreChecks = this.ignorePasswordExpirationWarningFlags.contains(ignoreCheckValue);
        }

        if (!ignoreChecks) {
            ignoreChecks = isUserAccountControlBitSet(ActiveDirectoryUserAccountControlFlags.ADS_UF_DONT_EXPIRE_PASSWD)
                    || isUserAccountControlBitSet(ActiveDirectoryUserAccountControlFlags.ADS_UF_PASSWD_CANT_CHANGE);
        }
        if (!ignoreChecks) {
            ignoreChecks = NumberUtils.isNumber(getPasswordExpirationDate()) &&
                            NumberUtils.toLong(getPasswordExpirationDate()) <= 0;
        }
        return ignoreChecks;
    }

    public boolean isUserAccountControlSetToDisableAccount() {
        return isUserAccountControlBitSet(ActiveDirectoryUserAccountControlFlags.ADS_UF_ACCOUNT_DISABLE);
    }

    public boolean isUserAccountControlSetToLockAccount() {
        return isUserAccountControlBitSet(ActiveDirectoryUserAccountControlFlags.ADS_UF_LOCKOUT);
    }

    public boolean isUserAccountControlSetToExpirePassword() {
        return isUserAccountControlBitSet(ActiveDirectoryUserAccountControlFlags.ADS_UF_PASSWORD_EXPIRED);
    }

    public boolean isUserAccountControlBitSet(final ActiveDirectoryUserAccountControlFlags bit) {
        if (getUserAccountControl() > 0) {
            return ((getUserAccountControl() & bit.getValue()) == bit.getValue());
        }
        return false;
    }

    /**
     * Construct the internal state of the password policy configuration based on
     * attributes and defined settings.
     * @param entry the authenticated ldap account entry.
     * @return false if password expiration date value is blank. Otherwise, true.
     */
    public boolean build(final LdapEntry entry) {

        final String expirationDate = getPasswordPolicyAttributeValue(entry, getPasswordExpirationDateAttributeName());
        if (StringUtils.isBlank(expirationDate)) {
            log.warn("Password expiration date [{}] is null.", getPasswordExpirationDateAttributeName());
            return false;
        }

        setDn(entry.getDn());
        setPasswordExpirationDate(expirationDate);
        setPasswordWarningNumberOfDays(this.defaultPasswordWarningNumberOfDays);
        setValidPasswordNumberOfDays(this.defaultValidPasswordNumberOfDays);

        String attributeValue = getPasswordPolicyAttributeValue(entry, getPasswordWarningNumberOfDaysAttributeName());
        if (attributeValue != null) {
            if (NumberUtils.isNumber(attributeValue)) {
                setPasswordWarningNumberOfDays(Integer.parseInt(attributeValue));
            }
        }

        attributeValue = getPasswordPolicyAttributeValue(entry, getIgnorePasswordExpirationWarningAttributeName());
        if (attributeValue != null) {
            setIgnorePasswordExpirationWarning(attributeValue);
        }

        attributeValue = getPasswordPolicyAttributeValue(entry, getValidPasswordNumberOfDaysAttributeName());
        if (attributeValue != null) {
            setValidPasswordNumberOfDays(NumberUtils.toInt(attributeValue));
        }

        attributeValue = getPasswordPolicyAttributeValue(entry, getAccountDisabledAttributeName());
        if (attributeValue != null) {
            setAccountDisabled(translateValueToBoolean(attributeValue));
        }

        attributeValue = getPasswordPolicyAttributeValue(entry, getAccountLockedAttributeName());
        if (attributeValue != null) {
            setAccountLocked(translateValueToBoolean(attributeValue));
        }

        attributeValue = getPasswordPolicyAttributeValue(entry, getAccountPasswordMustChangeAttributeName());
        if (attributeValue != null) {
            setAccountPasswordMustChange(translateValueToBoolean(attributeValue));
        }

        attributeValue = getPasswordPolicyAttributeValue(entry, getUserAccountControlAttributeName());
        if (attributeValue != null) {
            setUserAccountControl(attributeValue);
        }

        return true;
    }

    /**
     * Translate a value to its corresponding boolean value. 
     * @param value the attribute value to translate
     * @return true, if the value {@link Boolean#valueOf(String)} returns true or if the value is an integer greater than zero.
     */
    private boolean translateValueToBoolean(final String value) {
        return Boolean.valueOf(value) || (NumberUtils.toLong(value) > 0);
    }
    
    private String getPasswordPolicyAttributeValue(final LdapEntry entry, final String attrName) {
        if (attrName != null) {
            log.debug("Retrieving attribute [{}]", attrName);
            final LdapAttribute attribute = entry.getAttribute(attrName);

            if (attribute != null) {
                log.debug("Retrieved attribute [{}] with value [{}]", attrName, attribute.getStringValue());
                return attribute.getStringValue();
            }
        }
        return null;
    }

    public DateTime convertPasswordExpirationDate() {
        return getDateConverter().convert(getPasswordExpirationDate());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (getStaticPasswordExpirationDate() != null) {
            
            setValidPasswordNumberOfDays(0);
            log.debug("Static password expiration date is configured. Number of valid "
            		+ "password days is now set to [{}]", getValidPasswordNumberOfDays());
        }
    }
}
