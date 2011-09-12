/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
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
    
    public void initialize(Subject subject, CallbackHandler handler, Map<String,?> arg2,
        Map<String,?> arg3) {
        this.callbackHandler = handler;
    }

    public boolean login() throws LoginException {
        final Callback[] callbacks = new Callback[] {new NameCallback("f"), new PasswordCallback("f", false)};
        try {
            this.callbackHandler.handle(callbacks);
        } catch (Exception e) {
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
