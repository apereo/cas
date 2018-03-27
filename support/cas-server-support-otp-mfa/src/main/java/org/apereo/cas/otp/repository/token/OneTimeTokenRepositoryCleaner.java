package org.apereo.cas.otp.repository.token;

import lombok.AllArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

/**
 * This is {@link OneTimeTokenRepositoryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@AllArgsConstructor
public class OneTimeTokenRepositoryCleaner {
    private final OneTimeTokenRepository tokenRepository;

    /**
     * Clean the repository.
     */
    @Synchronized
    public void clean() {
        LOGGER.debug("Starting to clean previously used authenticator tokens from [{}] at [{}]", this.tokenRepository, ZonedDateTime.now());
        tokenRepository.clean();
        LOGGER.info("Finished cleaning authenticator tokens at [{}]", ZonedDateTime.now());
    }
}
