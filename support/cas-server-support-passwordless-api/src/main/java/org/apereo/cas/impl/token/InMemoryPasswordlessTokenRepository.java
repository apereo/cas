package org.apereo.cas.impl.token;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.util.crypto.CipherExecutor;

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
    private static final int INITIAL_CACHE_SIZE = 1_000;

    private static final long MAX_CACHE_SIZE = 100_000_000;

    private final Cache<String, String> storage;

    public InMemoryPasswordlessTokenRepository(final long tokenExpirationInSeconds,
                                               final CipherExecutor cipherExecutor) {
        super(tokenExpirationInSeconds, cipherExecutor);
        storage = Caffeine.newBuilder()
            .initialCapacity(INITIAL_CACHE_SIZE)
            .maximumSize(MAX_CACHE_SIZE)
            .expireAfterWrite(Duration.ofSeconds(tokenExpirationInSeconds))
            .build();
    }

    @Override
    public Optional<PasswordlessAuthenticationToken> findToken(final String username) {
        return Optional.ofNullable(storage.getIfPresent(username))
            .map(this::decodePasswordlessAuthenticationToken);
    }

    @Override
    public void deleteTokens(final String username) {
        storage.invalidate(username);
    }

    @Override
    public void deleteToken(final PasswordlessAuthenticationToken token) {
        deleteTokens(token.getUsername());
    }

    @Override
    public PasswordlessAuthenticationToken saveToken(final PasswordlessUserAccount passwordlessAccount,
                                                     final PasswordlessAuthenticationRequest passwordlessRequest,
                                                     final PasswordlessAuthenticationToken token) {
        storage.put(passwordlessAccount.getUsername(), encodeToken(token));
        return token;
    }

    @Override
    public void clean() {
        storage.cleanUp();
    }
}
