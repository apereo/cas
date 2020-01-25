package com.yubico.webauthn;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yubico.webauthn.data.ByteArray;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;

public class SessionManager {

    private final SecureRandom random = new SecureRandom();

    private final Cache<ByteArray, ByteArray> sessionIdsToUsers = newCache();
    private final Cache<ByteArray, ByteArray> usersToSessionIds = newCache();

    private static <K, V> Cache<K, V> newCache() {
        return CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();
    }

    /**
     * @return Create a new session for the given user, or return the existing one.
     */
    public ByteArray createSession(@NonNull ByteArray userHandle) throws ExecutionException {
        ByteArray sessionId = usersToSessionIds.get(userHandle, () -> generateRandom(32));
        sessionIdsToUsers.put(sessionId, userHandle);
        return sessionId;
    }

    /**
     * @return the user handle of the given session, if any.
     */
    public Optional<ByteArray> getSession(@NonNull ByteArray token) {
        return Optional.ofNullable(sessionIdsToUsers.getIfPresent(token));
    }

    public boolean isSessionForUser(@NonNull ByteArray claimedUserHandle, @NonNull ByteArray token) {
        return getSession(token).map(claimedUserHandle::equals).orElse(false);
    }

    public boolean isSessionForUser(@NonNull ByteArray claimedUserHandle, @NonNull Optional<ByteArray> token) {
        return token.map(t -> isSessionForUser(claimedUserHandle, t)).orElse(false);
    }

    private ByteArray generateRandom(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return new ByteArray(bytes);
    }

}
