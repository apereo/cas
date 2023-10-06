package org.apereo.cas.otp.repository.token;

import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.thread.Cleanable;
import lombok.RequiredArgsConstructor;
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
public class OneTimeTokenRepositoryCleaner implements Cleanable {
    private final CasReentrantLock lock = new CasReentrantLock();

    private final OneTimeTokenRepository tokenRepository;

    @Override
    public void clean() {
        lock.tryLock(__ -> {
            val now = ZonedDateTime.now(ZoneId.systemDefault());
            LOGGER.debug("Starting to clean previously used authenticator tokens from [{}] at [{}]", tokenRepository, now);
            tokenRepository.clean();
            LOGGER.debug("Finished cleaning authenticator tokens at [{}]", now);
        });

    }
}
