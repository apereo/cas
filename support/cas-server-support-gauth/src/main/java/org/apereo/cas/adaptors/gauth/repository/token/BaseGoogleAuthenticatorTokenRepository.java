package org.apereo.cas.adaptors.gauth.repository.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link BaseGoogleAuthenticatorTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class BaseGoogleAuthenticatorTokenRepository implements GoogleAuthenticatorTokenRepository {
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public final void clean() {
        logger.debug("Starting to clean expiring and previously used google authenticator tokens");
        cleanInternal();
        logger.info("Finished cleaning google authenticator tokens");
    }

    /**
     * Clean internal.
     */
    protected void cleanInternal() {
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
