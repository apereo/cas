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
package org.jasig.cas.authentication;

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;

import org.jasig.cas.ErrorMessageResolver;
import org.jasig.cas.Message;

/**
 * Resolves exceptions that come out of LDAP/LPPE components into an abstract message representation that can be
 * converted by Spring components into human-readable text.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class LdapPasswordPolicyErrorMessageResolver implements ErrorMessageResolver {

    private static final Map<Class<? extends Exception>, Message> MESSAGE_MAP =
            new HashMap<Class<? extends Exception>, Message>();

    static {
        MESSAGE_MAP.put(AccountException.class, new Message("screen.badhours.heading"));
        MESSAGE_MAP.put(AccountDisabledException.class, new Message("screen.accountdisabled.heading"));
        MESSAGE_MAP.put(AccountLockedException.class, new Message("screen.accountlocked.heading"));
        MESSAGE_MAP.put(CredentialExpiredException.class, new Message("screen.expiredpass.heading"));
        MESSAGE_MAP.put(InvalidLoginLocationException.class, new Message("screen.badworkstation.heading"));
        MESSAGE_MAP.put(InvalidLoginTimeException.class, new Message("screen.badhours.heading"));
        MESSAGE_MAP.put(
                LdapPasswordPolicyEnforcementException.class, new Message("screen.accounterror.password.message"));
    }

    @Override
    public Message resolve(final Exception e) {
        final Message message = MESSAGE_MAP.get(e);
        if (message != null) {
            return message;
        }
        return new Message(e.getMessage());
    }
}
