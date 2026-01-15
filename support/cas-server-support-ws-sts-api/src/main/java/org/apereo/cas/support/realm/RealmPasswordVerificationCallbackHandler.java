package org.apereo.cas.support.realm;

import module java.base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.wss4j.common.ext.WSPasswordCallback;

/**
 * This is {@link RealmPasswordVerificationCallbackHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class RealmPasswordVerificationCallbackHandler implements CallbackHandler {

    private final char[] password;
    
    @Override
    public void handle(final Callback[] callbacks) {
        Arrays.stream(callbacks)
            .filter(WSPasswordCallback.class::isInstance)
            .map(WSPasswordCallback.class::cast)
            .forEach(callback -> {
                val identifier = callback.getIdentifier();
                LOGGER.trace("Evaluating [{}]", identifier);
                callback.setPassword(new String(this.password));
                LOGGER.debug("Authenticated [{}] successfully.", identifier);
            });
    }
}
