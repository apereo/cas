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
package org.jasig.cas.authentication.handler.support;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class MockLoginModule implements LoginModule {
    private CallbackHandler callbackHandler;

    public void initialize(final Subject subject, final CallbackHandler handler, final Map<String, ?> arg2,
            final Map<String, ?> arg3) {
        this.callbackHandler = handler;
    }

    public boolean login() throws LoginException {
        final Callback[] callbacks = new Callback[] {new NameCallback("f"), new PasswordCallback("f", false)};
        try {
            this.callbackHandler.handle(callbacks);
        } catch (final Exception e) {
            throw new LoginException();
        }

        final String userName = ((NameCallback) callbacks[0]).getName();
        final String password = new String(((PasswordCallback) callbacks[1]).getPassword());

        if (userName.equals("test") && password.equals("test")) {
            return true;
        }

        throw new LoginException();
    }

    public boolean commit() throws LoginException {
        return true;
    }

    public boolean abort() throws LoginException {
        return true;
    }

    public boolean logout() throws LoginException {
        return true;
    }
}
