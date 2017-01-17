package org.apereo.cas.support;

import org.apache.wss4j.common.ext.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.Map;

/**
 * This is {@link UsernamePasswordCallbackHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class UsernamePasswordCallbackHandler implements CallbackHandler {

    private Map<String, String> passwords;

    public void setPasswords(final Map<String, String> passwords) {
        this.passwords = passwords;
    }

    public Map<String, String> getPasswords() {
        return passwords;
    }

    @Override
    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        if (getPasswords() == null || getPasswords().size() == 0) {
            return;
        }

        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof WSPasswordCallback) {
                final WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                final String pw = getPasswords().get(pc.getIdentifier());
                pc.setPassword(pw);
            }
        }
    }
}


