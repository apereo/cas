package org.apereo.cas.support.realm;

import org.apache.wss4j.common.ext.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.Arrays;

/**
 * This is {@link RealmVerificationCallbackHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RealmVerificationCallbackHandler implements CallbackHandler {
    private final String realm;

    public RealmVerificationCallbackHandler(final String realm) {
        this.realm = realm;
    }

    @Override
    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        Arrays.stream(callbacks)
                .filter(WSPasswordCallback.class::isInstance)
                .map(WSPasswordCallback.class::cast)
                .forEach(c -> {
                    if (realm.equalsIgnoreCase(c.getIdentifier())) {
                        c.setPassword(c.getIdentifier());
                    }
                });
    }
}
