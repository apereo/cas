package org.apereo.cas.adaptors.gauth.repository.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * This is {@link GoogleAuthenticatorTokenRepositoryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GoogleAuthenticatorTokenRepositoryCleaner {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAuthenticatorTokenRepositoryCleaner.class);

    private final GoogleAuthenticatorTokenRepository tokenRepository;

    public GoogleAuthenticatorTokenRepositoryCleaner(final GoogleAuthenticatorTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Clean.
     */
    @Scheduled(initialDelayString = "20000", fixedDelayString = "15000")
    public void clean() {
        LOGGER.debug("Starting to clean expiring and previously used google authenticator tokens from {}", this.tokenRepository);
        tokenRepository.clean();
        LOGGER.info("Finished cleaning google authenticator tokens");
    }
}
