package org.apereo.cas.authentication.handler.support;

import lombok.val;
import org.apache.hc.client5.http.auth.BasicUserPrincipal;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import java.util.Map;

/**
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class MockLoginModule implements LoginModule {
    private CallbackHandler callbackHandler;

    private Subject subject;

    @Override
    public void initialize(final Subject subject, final CallbackHandler handler, final Map<String, ?> arg2,
                           final Map<String, ?> arg3) {
        this.callbackHandler = handler;
        this.subject = subject;
    }

    @Override
    public boolean login() throws LoginException {
        val callbacks = new Callback[]{new NameCallback("f"), new PasswordCallback("f", false)};
        try {
            this.callbackHandler.handle(callbacks);
        } catch (final Exception e) {
            throw new LoginException();
        }

        val userName = ((NameCallback) callbacks[0]).getName();
        val password = new String(((PasswordCallback) callbacks[1]).getPassword());

        if ("test".equals(userName) && "test".equals(password)) {
            this.subject.getPrincipals().add(new BasicUserPrincipal(userName));
            return true;
        }

        throw new LoginException();
    }

    @Override
    public boolean commit() {
        return true;
    }

    @Override
    public boolean abort() {
        return true;
    }

    @Override
    public boolean logout() {
        return true;
    }
}
