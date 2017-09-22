package org.apereo.cas.otp.repository.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * This is {@link BaseOneTimeTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class BaseOneTimeTokenRepository implements OneTimeTokenRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseOneTimeTokenRepository.class);

    @Override
    public final void clean() {
        LOGGER.debug("Starting to clean expiring and previously used google authenticator tokens");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        cleanInternal();
        LOGGER.info("Finished cleaning google authenticator tokens");
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
