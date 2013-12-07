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
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The password policy configuration defined by the underlying data source.
 * @author Misagh Moayyed
 * @version 4.0.0
 */
public class PasswordPolicyResult {

    /** The logger. */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Password expiration date in raw format. **/
    private String passwordExpirationDate;

    /**
     * Password expiration ignore warning flag that will be compared
     * against the list of configured ignore flags in order to determine
     * whether the expiration policy should be ignored for the account.
     * @see #isAccountPasswordSetToNeverExpire()
     */
    private String ignorePasswordExpirationWarning;

    /** Number of valid password days. **/
    private int validPasswordNumberOfDays;

    /** Number of password warning days. **/
    private int passwordWarningNumberOfDays;

    private boolean accountDisabled = false;

    private boolean accountLocked = false;

    private boolean accountPasswordMustChange = false;

    private boolean accountExpired = false;

    /** Authenticated DN entry via ldap. **/
    private String dn;

    /** The computed and converted password expiration date time object. **/
    private DateTime passwordExpirationDateTime;

    /**
     * Reference to the password warning expiration ignore flags stored locally
     * to avoid keeping track of the {@link PasswordPolicyConfiguration} object
     * for easier serialization reasons.
     */
    private final List<String> ignorePasswordExpirationWarningFlags;

    /**
     * Instantiates a new password policy result.
     *
     * @param configuration the configuration
     */
    public PasswordPolicyResult(@NotNull final PasswordPolicyConfiguration configuration) {
        this.passwordExpirationDateTime = configuration.getDateConverter().convert(getPasswordExpirationDate());
        this.ignorePasswordExpirationWarningFlags = configuration.getIgnorePasswordExpirationWarningFlags();
    }

    protected boolean isAccountDisabled() {
        return this.accountDisabled;
    }

    public String getDn() {
        return this.dn;
    }

    protected void setDn(final String dn) {
        this.dn = dn;
    }

    protected void setAccountDisabled(final boolean accountDisabled) {
        this.accountDisabled = accountDisabled;
    }

    protected boolean isAccountLocked() {
        return this.accountLocked;
    }

    protected boolean isAccountExpired() {
        return this.accountExpired;
    }

    protected void setAccountLocked(final boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    protected boolean isAccountPasswordMustChange() {
        return this.accountPasswordMustChange;
    }

    protected void setAccountPasswordMustChange(final boolean accountPasswordMustChange) {
        this.accountPasswordMustChange = accountPasswordMustChange;
    }

    /**
     * Password expiration date in its raw format.
     * @return password expiration date.
     */
    private String getPasswordExpirationDate() {
        return this.passwordExpirationDate;
    }

    public int getValidPasswordNumberOfDays() {
        return this.validPasswordNumberOfDays;
    }

    public int getPasswordWarningNumberOfDays() {
        return this.passwordWarningNumberOfDays;
    }

    protected void setPasswordExpirationDate(final String date) {
        this.passwordExpirationDate = date;
    }

    public void setValidPasswordNumberOfDays(final int valid) {
        this.validPasswordNumberOfDays = valid;
    }

    public void setPasswordWarningNumberOfDays(final int days) {
        this.passwordWarningNumberOfDays = days;
    }

    public String getIgnorePasswordExpirationWarning() {
        return this.ignorePasswordExpirationWarning;
    }

    protected void setIgnorePasswordExpirationWarning(final String warning) {
        this.ignorePasswordExpirationWarning = warning;
    }

    /**
     * Evaluate whether an account is set to never expire.
     * Compares the account against configured ignore values for password expiration warning.
     * Finally, checks the value of password expiration date (if numeric) to be greater than zero.
     * @return true, if the any of the above conditions return true.
     */
    protected boolean isAccountPasswordSetToNeverExpire() {
        final String ignoreCheckValue = getIgnorePasswordExpirationWarning();
        boolean ignoreChecks = false;

        if (!StringUtils.isBlank(ignoreCheckValue)) {
            ignoreChecks = this.ignorePasswordExpirationWarningFlags.contains(ignoreCheckValue);
        }

        if (!ignoreChecks) {
            ignoreChecks = NumberUtils.isNumber(getPasswordExpirationDate())
                    && NumberUtils.toLong(getPasswordExpirationDate()) <= 0;
        }
        return ignoreChecks;
    }

    public final DateTime getPasswordExpirationDateTime() {
        return this.passwordExpirationDateTime;
    }
}
