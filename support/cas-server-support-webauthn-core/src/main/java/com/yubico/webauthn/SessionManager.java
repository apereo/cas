package com.yubico.webauthn;

import com.yubico.webauthn.data.ByteArray;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import lombok.val;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * This is {@link SessionManager}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class SessionManager {

    private final Cache<ByteArray, ByteArray> sessionIdsToUsers = newCache();

    private final Cache<ByteArray, ByteArray> usersToSessionIds = newCache();

    private static <K, V> Cache<K, V> newCache() {
        return CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(Duration.ofMinutes(5))
            .build();
    }

    /**
     * Create session byte array.
     *
     * @param userHandle the user handle
     * @return Create a new session for the given user, or return the existing one.
     * @throws ExecutionException the execution exception
     */
    public ByteArray createSession(@NonNull final ByteArray userHandle) throws ExecutionException {
        val sessionId = usersToSessionIds.get(userHandle, WebAuthnUtils::generateRandomId);
        sessionIdsToUsers.put(sessionId, userHandle);
        return sessionId;
    }

    /**
     * Gets session.
     *
     * @param token the token
     * @return the user handle of the given session, if any.
     */
    public Optional<ByteArray> getSession(@NonNull final ByteArray token) {
        return Optional.ofNullable(sessionIdsToUsers.getIfPresent(token));
    }

    public boolean isSessionForUser(@NonNull final ByteArray claimedUserHandle, @NonNull final ByteArray token) {
        return getSession(token).map(claimedUserHandle::equals).orElse(false);
    }

    public boolean isSessionForUser(@NonNull final ByteArray claimedUserHandle, @NonNull final Optional<ByteArray> token) {
        return token.map(t -> isSessionForUser(claimedUserHandle, t)).orElse(false);
    }
}
