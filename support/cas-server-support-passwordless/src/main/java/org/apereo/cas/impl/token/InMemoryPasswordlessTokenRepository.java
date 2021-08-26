package org.apereo.cas.impl.token;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.Optional;

/**
 * This is {@link InMemoryPasswordlessTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class InMemoryPasswordlessTokenRepository extends BasePasswordlessTokenRepository {
    private static final int INITIAL_CACHE_SIZE = 50;

    private static final long MAX_CACHE_SIZE = 100_000_000;

    private final Cache<String, String> storage;

    public InMemoryPasswordlessTokenRepository(final int tokenExpirationInSeconds) {
        super(tokenExpirationInSeconds);
        this.storage = Caffeine.newBuilder()
            .initialCapacity(INITIAL_CACHE_SIZE)
            .maximumSize(MAX_CACHE_SIZE)
            .expireAfterWrite(Duration.ofSeconds(tokenExpirationInSeconds))
            .build();
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
