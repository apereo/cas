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
package org.jasig.cas.web.flow;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.LoginException;

import org.jasig.cas.ErrorMessageResolver;
import org.jasig.cas.Message;
import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.AggregateSecurityException;
import org.jasig.cas.authentication.InvalidLoginLocationException;
import org.jasig.cas.authentication.InvalidLoginTimeException;
import org.jasig.cas.authentication.UnsupportedCredentialException;

/**
 * Maps exceptions that arise from default {@link org.jasig.cas.authentication.AuthenticationManager} components onto
 * message descriptors that can be resolved into human-readable messages by Spring machinery.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class AuthenticationErrorMessageResolver implements ErrorMessageResolver {

    private static final Message CREDENTIAL_BAD = new Message("error.authentication.credential.bad");

    private static final Map<Class<? extends Throwable>, Message> MESSAGE_MAP =
            new HashMap<Class<? extends Throwable>, Message>();

    static {
        MESSAGE_MAP.put(AccountDisabledException.class, new Message("screen.accountdisabled.heading"));
        MESSAGE_MAP.put(AccountLockedException.class, new Message("screen.accountlocked.heading"));
        MESSAGE_MAP.put(CredentialExpiredException.class, new Message("screen.expiredpass.heading"));
        MESSAGE_MAP.put(InvalidLoginLocationException.class, new Message("screen.badworkstation.heading"));
        MESSAGE_MAP.put(InvalidLoginTimeException.class, new Message("screen.badhours.heading"));
        MESSAGE_MAP.put(UnsupportedCredentialException.class, new Message("error.authentication.credential.unsupported"));
    }

    @Override
    public Message resolve(final Throwable e) {
        if (e instanceof AggregateSecurityException) {
            final AggregateSecurityException aggregate = (AggregateSecurityException) e;
            if (aggregate.getErrors() != null && aggregate.getErrors().length > 0) {
                final GeneralSecurityException first = aggregate.getErrors()[0];
                final Message message = MESSAGE_MAP.get(first);
                if (message != null) {
                    return message;
                }
                if (first instanceof LoginException) {
                    return CREDENTIAL_BAD;
                }
            }
        }
        return new Message(e.getMessage());
    }
}
