package org.apereo.cas.support.realm;

import lombok.extern.slf4j.Slf4j;
import org.apache.wss4j.common.ext.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import java.util.Arrays;

/**
 * This is {@link RealmPasswordVerificationCallbackHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class RealmPasswordVerificationCallbackHandler implements CallbackHandler {

    private final String psw;

    public RealmPasswordVerificationCallbackHandler(final String psw) {
        this.psw = psw;
    }

    @Override
    public void handle(final Callback[] callbacks) {
        Arrays.stream(callbacks)
                .filter(WSPasswordCallback.class::isInstance)
                .map(WSPasswordCallback.class::cast)
                .forEach(c -> {
                    LOGGER.debug("Evaluating [{}]", c.getIdentifier());
                    c.setPassword(this.psw);
                    LOGGER.debug("Authenticated [{}] successfully.", c.getIdentifier());
                });
    }
}
