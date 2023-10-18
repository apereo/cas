package com.yubico.core;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yubico.webauthn.data.ByteArray;
import lombok.NonNull;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DefaultSessionManager implements SessionManager {
    private final Cache<ByteArray, ByteArray> sessionIdsToUsers = newCache();

    private final Cache<ByteArray, ByteArray> usersToSessionIds = newCache();

    private static <K, V> Cache<ByteArray, ByteArray> newCache() {
        return Caffeine.newBuilder()
            .maximumSize(100L)
            .expireAfterAccess(5L, TimeUnit.MINUTES)
            .build();
    }

    @Override
    public ByteArray createSession(@NonNull final ByteArray userHandle) throws ExecutionException {
        var sessionId = usersToSessionIds.get(userHandle, __ -> SessionManager.generateRandom(32));
        sessionIdsToUsers.put(sessionId, userHandle);
        return sessionId;
    }

    @Override
    public Optional<ByteArray> getSession(
        @NonNull final ByteArray token) {
        return Optional.ofNullable(sessionIdsToUsers.getIfPresent(token));
    }

}
