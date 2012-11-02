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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * The password policy configuration defined by the ldap instance.
 * This is only constructed by the appropriate authentication handler
 * {@link LdapPasswordPolicyAwareAuthenticationHandler} and may be passed to {@link LdapPasswordPolicyExaminer} examiners.
 */
public final class LdapPasswordPolicyConfiguration {

    private UsernamePasswordCredentials credentials;
    
    private String passwordExpirationDate;
    private String ignorePasswordExpirationWarning;
    private String passwordExpirationDateAttributeName;
    
    private int validPasswordNumberOfDays;
    private int passwordWarningNumberOfDays;
    
    private boolean accountDisabled;
    private boolean accountLocked;
    private boolean accountPasswordMustChange;
    
    private long userAccountControl = -1;
        
    public LdapPasswordPolicyConfiguration(final UsernamePasswordCredentials credentials) {
        this.credentials = credentials;
    }

    public long getUserAccountControl() {
        return this.userAccountControl;
    }

    public void setUserAccountControl(final String userAccountControl) {
        if (!StringUtils.isBlank(userAccountControl) && NumberUtils.isNumber(userAccountControl)) {
            this.userAccountControl = Long.parseLong(userAccountControl);
        }
    }
    
    public boolean isAccountDisabled() {
        return this.accountDisabled;
    }

    public void setAccountDisabled(final boolean accountDisabled) {
        this.accountDisabled = accountDisabled;
    }

    public boolean isAccountLocked() {
        return this.accountLocked;
    }

    public void setAccountLocked(final boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public boolean isAccountPasswordMustChange() {
        return this.accountPasswordMustChange;
    }

    public void setAccountPasswordMustChange(final boolean accountPasswordMustChange) {
        this.accountPasswordMustChange = accountPasswordMustChange;
    }

    public String getPasswordExpirationDate() {
        return this.passwordExpirationDate;
    }

    public String getIgnorePasswordExpirationWarning() {
        return this.ignorePasswordExpirationWarning;
    }

    public UsernamePasswordCredentials getCredentials() {
        return this.credentials;
    }

    public int getValidPasswordNumberOfDays() {
        return this.validPasswordNumberOfDays;
    }

    public int getPasswordWarningNumberOfDays() {
        return this.passwordWarningNumberOfDays;
    }

    public void setPasswordExpirationDate(final String date) {
        this.passwordExpirationDate = date;
    }

    public void setIgnorePasswordExpirationWarning(final String value) {
        this.ignorePasswordExpirationWarning = value;
    }

    public void setValidPasswordNumberOfDays(final int valid) {
        this.validPasswordNumberOfDays = valid;
    }

    public void setPasswordWarningNumberOfDays(final int warn) {
        this.passwordWarningNumberOfDays = warn;
    }

    void setPasswordExpirationDateAttributeName(final String value) {
        this.passwordExpirationDateAttributeName = value;
    }

    public String getPasswordExpirationDateAttributeName() {
        return this.passwordExpirationDateAttributeName;
    }
}
