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

import org.jasig.cas.Message;

/**
 * Message conveying LDAP account expiration details.
 *
 * @author Misagh Moayyed
 * @since 4.0
 */
public class AccountPasswordExpiringMessage extends Message {
    private static final long serialVersionUID = -7827652078735517752L;

    private static final String CODE = "ldap.lppe.account.psw.expiring";

    private long numberOfDaysToPasswordExpiration;

    /**
     * Instantiates a new account password expiring message.
     *
     * @param defaultMsg the default msg
     * @param days the days
     */
    public AccountPasswordExpiringMessage(final String defaultMsg, final long days) {
        super(CODE, defaultMsg);
        this.numberOfDaysToPasswordExpiration = days;
    }

    public long getNumberOfDaysToPasswordExpiration() {
        return this.numberOfDaysToPasswordExpiration;
    }
}
