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

final class LdapPasswordPolicyConfiguration {

    private String passwordExpirationDate;
    private String ignorePasswordExpirationWarning;
    private String userId;
    private int validPasswordNumberOfDays;
    private int passwordWarningNumberOfDays;
    private boolean accountDisabled;
    private boolean accountLocked;
    private boolean accountPasswordMustChange;
    private String userAccountControl;
        
    public LdapPasswordPolicyConfiguration(final String userId) {
        this.userId = userId;
    }

    public String getUserAccountControl() {
        return this.userAccountControl;
    }

    public void setUserAccountControl(String userAccountControl) {
        this.userAccountControl = userAccountControl;
    }
    
    public boolean isAccountDisabled() {
        return this.accountDisabled;
    }

    public void setAccountDisabled(boolean accountDisabled) {
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

    public String getUserId() {
        return this.userId;
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
}
