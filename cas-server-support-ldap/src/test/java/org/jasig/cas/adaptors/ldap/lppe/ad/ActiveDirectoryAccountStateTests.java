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

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.LoginException;

import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.AccountPasswordMustChangeException;
import org.jasig.cas.authentication.InvalidLoginLocationException;
import org.jasig.cas.authentication.InvalidLoginTimeException;
import org.junit.Test;

/**
 * Unit test for {@link org.jasig.cas.adaptors.ldap.lppe.ad.ActiveDirectoryAccountState}.
 *
 * @author Misagh Moayyed
 * @since 4.0
 */
public class ActiveDirectoryAccountStateTests {

    @Test(expected=InvalidLoginTimeException.class)
    public void testAccountStateLoginTime() throws LoginException {
        final ActiveDirectoryAccountState state = new ActiveDirectoryAccountState(
                org.ldaptive.auth.ext.ActiveDirectoryAccountState.Error.INVALID_LOGON_HOURS);
        state.getError().throwSecurityException();
    }

    @Test(expected=AccountDisabledException.class)
    public void testAccountStateDisabled() throws LoginException {
        final ActiveDirectoryAccountState state = new ActiveDirectoryAccountState(
                org.ldaptive.auth.ext.ActiveDirectoryAccountState.Error.ACCOUNT_DISABLED);
        state.getError().throwSecurityException();
    }

    @Test(expected=InvalidLoginLocationException.class)
    public void testAccountStateInvalidLocation() throws LoginException {
        final ActiveDirectoryAccountState state = new ActiveDirectoryAccountState(
                org.ldaptive.auth.ext.ActiveDirectoryAccountState.Error.INVALID_WORKSTATION);
        state.getError().throwSecurityException();
    }

    @Test(expected=AccountPasswordMustChangeException.class)
    public void testAccountStatePswMustChange() throws LoginException {
        final ActiveDirectoryAccountState state = new ActiveDirectoryAccountState(
                org.ldaptive.auth.ext.ActiveDirectoryAccountState.Error.PASSWORD_MUST_CHANGE);
        state.getError().throwSecurityException();
    }

    @Test(expected=AccountLockedException.class)
    public void testAccountStateNormalException() throws LoginException {
        final ActiveDirectoryAccountState state = new ActiveDirectoryAccountState(
                org.ldaptive.auth.ext.ActiveDirectoryAccountState.Error.ACCOUNT_LOCKED_OUT);
        state.getError().throwSecurityException();
    }
}
