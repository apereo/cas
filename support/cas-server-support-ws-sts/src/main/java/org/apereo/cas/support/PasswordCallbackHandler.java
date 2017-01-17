package org.apereo.cas.support;

import org.apache.wss4j.common.ext.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

/**
 * This is {@link PasswordCallbackHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class PasswordCallbackHandler implements CallbackHandler {

    @Override
    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof WSPasswordCallback) { 
                final WSPasswordCallback pc = (org.apache.wss4j.common.ext.WSPasswordCallback) callbacks[i];
                if ("realma".equals(pc.getIdentifier())) {
                    pc.setPassword("realma");
                    break;
                } else if ("realmb".equals(pc.getIdentifier())) {
                    pc.setPassword("realmb");
                    break;
                }
                if ("mystskey".equals(pc.getIdentifier())) {
                    pc.setPassword("stskpass");
                    break;
                }
            }
        }
    }

}
