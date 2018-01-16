package org.apereo.cas.otp.repository.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * This is {@link BaseOneTimeTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public abstract class BaseOneTimeTokenRepository implements OneTimeTokenRepository {


    @Override
    public final void clean() {
        LOGGER.debug("Starting to clean expiring and previously used google authenticator tokens");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        cleanInternal();
        LOGGER.debug("Finished cleaning google authenticator tokens");
    }

    /**
     * Clean internal.
     */
    protected abstract void cleanInternal();

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
