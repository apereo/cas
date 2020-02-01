package org.apereo.cas.impl.token;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Optional;

/**
 * This is {@link InMemoryPasswordlessTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class InMemoryPasswordlessTokenRepository extends BasePasswordlessTokenRepository {
    private static final int INITIAL_CACHE_SIZE = 50;

    private static final long MAX_CACHE_SIZE = 100_000_000;

    private final LoadingCache<String, String> storage;

    public InMemoryPasswordlessTokenRepository(final int tokenExpirationInSeconds) {
        super(tokenExpirationInSeconds);
        this.storage = Caffeine.newBuilder()
            .initialCapacity(INITIAL_CACHE_SIZE)
            .maximumSize(MAX_CACHE_SIZE)
            .expireAfterWrite(Duration.ofSeconds(tokenExpirationInSeconds))
            .build(s -> {
                LOGGER.error("Load operation of the cache is not supported.");
                return null;
            });
    }

    @Override
    public Optional<String> findToken(final String username) {
        return Optional.ofNullable(this.storage.getIfPresent(username));
    }

    @Override
    public void deleteTokens(final String username) {
        this.storage.invalidate(username);
    }

    @Override
    public void deleteToken(final String username, final String token) {
        deleteTokens(username);
    }

    @Override
    public void saveToken(final String username, final String token) {
        this.storage.put(username, token);
    }

    @Override
    public void clean() {
        this.storage.cleanUp();
    }
}
