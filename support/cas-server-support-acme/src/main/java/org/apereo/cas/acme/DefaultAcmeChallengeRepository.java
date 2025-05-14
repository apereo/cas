package org.apereo.cas.acme;

import org.apereo.cas.util.concurrent.CasReentrantLock;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link DefaultAcmeChallengeRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 * @deprecated since 7.3.0
 */
@Slf4j
@SuppressWarnings("removal")
@Deprecated(since = "7.3.0", forRemoval = true)
public class DefaultAcmeChallengeRepository implements AcmeChallengeRepository {
    private final CasReentrantLock lock = new CasReentrantLock();

    private final Cache<String, String> cache = Caffeine.newBuilder()
        .initialCapacity(100)
        .maximumSize(1000)
        .expireAfterAccess(2, TimeUnit.SECONDS)
        .build();


    @Override
    public void add(final String token, final String challenge) {
        lock.tryLock(__ -> {
            LOGGER.debug("Adding ACME token [{}] linked to challenge [{}]", token, challenge);
            cache.put(token, challenge);
        });
    }

    @Override
    public String get(final String token) {
        return lock.tryLock(() -> {
            LOGGER.debug("Fetching ACME token [{}]...", token);
            return cache.getIfPresent(token);
        });
    }
}
