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

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;

import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.AggregateSecurityException;
import org.jasig.cas.authentication.InvalidLoginLocationException;
import org.jasig.cas.authentication.InvalidLoginTimeException;

/**
 * Maps exceptions that arise from default {@link org.jasig.cas.authentication.AuthenticationManager} components onto
 * Webflow state names.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class AuthenticationErrorStateResolver implements ErrorStateResolver {

    private static final String DEFAULT_STATE = "error";

    private static final Map<Class<? extends Throwable>, String> STATE_MAP =
            new HashMap<Class<? extends Throwable>, String>();

    static {
        STATE_MAP.put(AccountDisabledException.class, "accountDisabled");
        STATE_MAP.put(AccountLockedException.class, "accountLocked");
        STATE_MAP.put(CredentialExpiredException.class, "passwordExpired");
        STATE_MAP.put(InvalidLoginLocationException.class, "badWorkstation");
        STATE_MAP.put(InvalidLoginTimeException.class, "badHours");
    }

    @Override
    public String resolve(final Throwable e) {
        if (e instanceof AggregateSecurityException) {
            final AggregateSecurityException aggregate = (AggregateSecurityException) e;
            if (aggregate.getErrors() != null && aggregate.getErrors().length > 0) {
                final String view = STATE_MAP.get(aggregate.getErrors()[0]);
                if (view != null) {
                    return view;
                }
            }
        }
        return DEFAULT_STATE;
    }
}
