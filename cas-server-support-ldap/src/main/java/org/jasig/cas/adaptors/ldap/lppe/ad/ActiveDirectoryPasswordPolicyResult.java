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
package org.jasig.cas.adaptors.ldap.lppe.ad;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jasig.cas.adaptors.ldap.lppe.PasswordPolicyConfiguration;
import org.jasig.cas.adaptors.ldap.lppe.PasswordPolicyResult;

/**
 * The password policy configuration defined by the underlying data source.
 * @author Misagh Moayyed
 * @version 4.0.0
 */
public class ActiveDirectoryPasswordPolicyResult extends PasswordPolicyResult {
    private long userAccountControl = -1;

    /**
     * Instantiates a new active directory password policy result.
     *
     * @param configuration the configuration
     */
    public ActiveDirectoryPasswordPolicyResult(final PasswordPolicyConfiguration configuration) {
        super(configuration);
    }

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

        /**
         * Instantiates a new active directory user account control flags.
         *
         * @param id the account control id
         */
        ActiveDirectoryUserAccountControlFlags(final long id) {
            this.value = id;
        }

        public long getValue() {
            return this.value;
        }
    }

    /**
     * Checks if is user account control set to disable account.
     *
     * @return true, if is user account control set to disable account
     */
    private boolean isUserAccountControlSetToDisableAccount() {
        if (isUserAccountControlBitSet(ActiveDirectoryUserAccountControlFlags.ADS_UF_ACCOUNT_DISABLE)) {
            logger.debug("User account control flag is set. Account [{}] is disabled", getDn());
            return true;
        }
        return false;
    }

    /**
     * Checks if is user account control set to lock account.
     *
     * @return true, if is user account control set to lock account
     */
    private boolean isUserAccountControlSetToLockAccount() {
        if (isUserAccountControlBitSet(ActiveDirectoryUserAccountControlFlags.ADS_UF_LOCKOUT)) {
            logger.debug("User account control flag is set. Account [{}] is locked", getDn());
            return true;
        }
        return false;
    }

    @Override
    protected boolean isAccountExpired() {
        return super.isAccountExpired() || isUserAccountControlSetToExpirePassword();
    }

    private boolean isUserAccountControlSetToExpirePassword() {
        return isUserAccountControlBitSet(ActiveDirectoryUserAccountControlFlags.ADS_UF_PASSWORD_EXPIRED);
    }

    private long getUserAccountControl() {
        return this.userAccountControl;
    }

    /**
     * Sets the user account control to the given flag.
     *
     * @param userAccountControl the new user account control
     */
    protected void setUserAccountControl(final String userAccountControl) {
        if (!StringUtils.isBlank(userAccountControl) && NumberUtils.isNumber(userAccountControl)) {
            this.userAccountControl = Long.parseLong(userAccountControl);
        }
    }

    /**
     * Checks if is user account control bit set.
     *
     * @param bit the bit
     * @return true, if is user account control bit set
     */
    private boolean isUserAccountControlBitSet(final ActiveDirectoryUserAccountControlFlags bit) {
        if (getUserAccountControl() > 0) {
            return ((getUserAccountControl() & bit.getValue()) == bit.getValue());
        }
        return false;
    }

    @Override
    protected boolean isAccountLocked() {
        return super.isAccountLocked() || isUserAccountControlSetToLockAccount();
    }

    @Override
    protected boolean isAccountDisabled() {
        return super.isAccountDisabled() || isUserAccountControlSetToDisableAccount();
    }

    /**
     * {@inheritDoc}
     * <p>Additionally, checks for AD-specific user account control values
     * {@link ActiveDirectoryUserAccountControlFlags#ADS_UF_DONT_EXPIRE_PASSWD} and
     * {@link ActiveDirectoryUserAccountControlFlags#ADS_UF_PASSWD_CANT_CHANGE}.
     */
    @Override
    public boolean isAccountPasswordSetToNeverExpire() {
        boolean ignoreChecks = super.isAccountPasswordSetToNeverExpire();

        if (!ignoreChecks) {
            ignoreChecks = isUserAccountControlBitSet(ActiveDirectoryUserAccountControlFlags.ADS_UF_DONT_EXPIRE_PASSWD)
                    || isUserAccountControlBitSet(ActiveDirectoryUserAccountControlFlags.ADS_UF_PASSWD_CANT_CHANGE);
        }
        return ignoreChecks;
    }
}
