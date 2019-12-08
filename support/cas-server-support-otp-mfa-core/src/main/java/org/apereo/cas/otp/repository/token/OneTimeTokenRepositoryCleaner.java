package org.apereo.cas.otp.repository.token;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * This is {@link OneTimeTokenRepositoryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OneTimeTokenRepositoryCleaner {
    private final OneTimeTokenRepository tokenRepository;

    /**
     * Clean the repository.
     */
    @Synchronized
    public void clean() {
        val now = ZonedDateTime.now(ZoneId.systemDefault());
        LOGGER.debug("Starting to clean previously used authenticator tokens from [{}] at [{}]", this.tokenRepository, now);
        tokenRepository.clean();
        LOGGER.info("Finished cleaning authenticator tokens at [{}]", now);
    }
}
