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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for password policy configuration.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PasswordPolicyConfiguration {

    private static final int DEFAULT_PASSWORD_WARNING_NUMBER_OF_DAYS = 30;

    /** Logger instance. */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Disregard the warning period and warn all users of password expiration. */
    private boolean alwaysDisplayPasswordExpirationWarning;

    /** Threshold number of days till password expiration below which a warning is displayed. **/
    private int passwordWarningNumberOfDays = DEFAULT_PASSWORD_WARNING_NUMBER_OF_DAYS;

    /** Url to the password policy application. **/
    private String passwordPolicyUrl;


    public boolean isAlwaysDisplayPasswordExpirationWarning() {
        return this.alwaysDisplayPasswordExpirationWarning;
    }

    public void setAlwaysDisplayPasswordExpirationWarning(final boolean alwaysDisplayPasswordExpirationWarning) {
        this.alwaysDisplayPasswordExpirationWarning = alwaysDisplayPasswordExpirationWarning;
    }

    public String getPasswordPolicyUrl() {
        return this.passwordPolicyUrl;
    }

    public void setPasswordPolicyUrl(final String passwordPolicyUrl) {
        this.passwordPolicyUrl = passwordPolicyUrl;
    }

    public int getPasswordWarningNumberOfDays() {
        return passwordWarningNumberOfDays;
    }

    public void setPasswordWarningNumberOfDays(final int days) {
        this.passwordWarningNumberOfDays = days;
    }
}
