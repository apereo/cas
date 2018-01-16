package org.apereo.cas.otp.repository.token;

import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

/**
 * This is {@link OneTimeTokenRepositoryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class OneTimeTokenRepositoryCleaner {


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
