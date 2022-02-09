package org.apereo.cas.otp.repository.token;

import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link BaseOneTimeTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@ToString
public abstract class BaseOneTimeTokenRepository implements OneTimeTokenRepository {
    @Override
    public final void clean() {
        LOGGER.debug("Starting to clean expiring and previously used google authenticator tokens");
        ApplicationContextProvider.processBeanInjections(this);
        cleanInternal();
        LOGGER.debug("Finished cleaning google authenticator tokens");
    }

    /**
     * Clean internal.
     */
    protected abstract void cleanInternal();
}
