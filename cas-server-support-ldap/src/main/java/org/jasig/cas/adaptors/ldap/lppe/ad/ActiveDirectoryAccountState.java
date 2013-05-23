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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.AccountPasswordMustChangeException;
import org.jasig.cas.authentication.InvalidLoginLocationException;
import org.jasig.cas.authentication.InvalidLoginTimeException;
import org.ldaptive.auth.AccountState;

/**
 * An extension of ldaptive's ActiveDirectoryAccountState class that defines CAS-specific exceptions
 * to be thrown back for the following codes:
 * 
 * <ul>
 *  <li>INVALID_LOGON_HOURS</li>
 *  <li>INVALID_WORKSTATION</li>
 *  <li>PASSWORD_MUST_CHANGE</li>
 *  <li>ACCOUNT_DISABLED</li>
 * </ul>
 * @author Misagh Moayyed
 * @since 4.0
 */
class ActiveDirectoryAccountState extends org.ldaptive.auth.ext.ActiveDirectoryAccountState {
    private static final Map<org.ldaptive.auth.ext.ActiveDirectoryAccountState.Error, Class<? extends LoginException>> MAPPINGS
        = new HashMap<org.ldaptive.auth.ext.ActiveDirectoryAccountState.Error, Class<? extends LoginException>>();

    private final ActiveDirectoryError error;

    static {
        MAPPINGS.put(org.ldaptive.auth.ext.ActiveDirectoryAccountState.Error.INVALID_LOGON_HOURS, InvalidLoginTimeException.class);
        MAPPINGS.put(org.ldaptive.auth.ext.ActiveDirectoryAccountState.Error.INVALID_WORKSTATION, InvalidLoginLocationException.class);
        MAPPINGS.put(org.ldaptive.auth.ext.ActiveDirectoryAccountState.Error.PASSWORD_MUST_CHANGE, AccountPasswordMustChangeException.class);
        MAPPINGS.put(org.ldaptive.auth.ext.ActiveDirectoryAccountState.Error.ACCOUNT_DISABLED, AccountDisabledException.class);
    }

    public final class ActiveDirectoryError implements org.ldaptive.auth.AccountState.Error {
        private final AccountState.Error error = ActiveDirectoryAccountState.this.getActiveDirectoryError();

        @Override
        public int getCode() {
            return this.error.getCode();
        }

        @Override
        public String getMessage() {
            return this.error.getMessage();
        }

        @Override
        public void throwSecurityException() throws LoginException {
            if (MAPPINGS.containsKey(this.error)) {
                final LoginException e = createException();               
                throw e;
            } else {
                this.error.throwSecurityException();
            }
        }

        private LoginException createException() throws RuntimeException {
            try {
                final Class<?> exception = MAPPINGS.get(this.error);
                final Constructor<?> ctor = exception.getDeclaredConstructor(String.class);
                final LoginException e = (LoginException) ctor.newInstance(this.getMessage() + ":" + this.getCode());
                return e;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ActiveDirectoryAccountState(final Error error) {
        super(error);
        this.error = new ActiveDirectoryError();
    }

    @Override
    public org.ldaptive.auth.AccountState.Error getError() {
        return this.error;
    }
}
