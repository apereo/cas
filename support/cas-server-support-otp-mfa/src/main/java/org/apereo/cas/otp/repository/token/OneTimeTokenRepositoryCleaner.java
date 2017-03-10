package org.apereo.cas.otp.repository.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

/**
 * This is {@link OneTimeTokenRepositoryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OneTimeTokenRepositoryCleaner {
    private static final Logger LOGGER = LoggerFactory.getLogger(OneTimeTokenRepositoryCleaner.class);

    private final OneTimeTokenRepository tokenRepository;
    private final Object lock = new Object();
    
    public OneTimeTokenRepositoryCleaner(final OneTimeTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Clean the repository.
     */
    public void clean() {
        LOGGER.debug("Starting to clean previously used authenticator tokens from [{}] at [{}]", this.tokenRepository, ZonedDateTime.now());
        synchronized (this.lock) {
            tokenRepository.clean();
        }
        LOGGER.info("Finished cleaning authenticator tokens at [{}]", ZonedDateTime.now());
    }
}
