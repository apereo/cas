package org.apereo.cas.adaptors.gauth.repository.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.ZonedDateTime;

/**
 * This is {@link GoogleAuthenticatorTokenRepositoryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GoogleAuthenticatorTokenRepositoryCleaner {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAuthenticatorTokenRepositoryCleaner.class);

    private final GoogleAuthenticatorTokenRepository tokenRepository;
    private final Object lock = new Object();
    
    public GoogleAuthenticatorTokenRepositoryCleaner(final GoogleAuthenticatorTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Clean the repository.
     */
    @Scheduled(initialDelayString = "${cas.authn.mfa.gauth.cleaner.startDelay:20000}",
            fixedDelayString = "${cas.authn.mfa.gauth.cleaner.repeatInterval:60000}")
    public void clean() {
        LOGGER.debug("Starting to clean previously used google authenticator tokens from {} at {}", this.tokenRepository, ZonedDateTime.now());
        synchronized (this.lock) {
            tokenRepository.clean();
        }
        LOGGER.info("Finished cleaning google authenticator tokens at {}", ZonedDateTime.now());
    }
}
