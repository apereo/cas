package com.yubico.core;

import org.apereo.cas.util.RandomUtils;
import com.yubico.webauthn.data.ByteArray;
import lombok.NonNull;
import java.util.Objects;
import java.util.Optional;

public interface SessionManager {
    /**
     * The bean name.
     */
    String BEAN_NAME = "webAuthnSessionManager";

    static ByteArray generateRandom(final int length) {
        var random = RandomUtils.getNativeInstance();
        var bytes = new byte[length];
        random.nextBytes(bytes);
        return new ByteArray(bytes);
    }

    ByteArray createSession(@NonNull final ByteArray userHandle);

    Optional<ByteArray> getSession(@NonNull final ByteArray sessionId);
    
    default boolean isSessionForUser(
        @NonNull final ByteArray claimedUserHandle,
        @NonNull final ByteArray token) {
        Objects.requireNonNull(claimedUserHandle);
        return getSession(token).map(claimedUserHandle::equals).orElse(false);
    }

    default boolean isSessionForUser(
        @NonNull final ByteArray claimedUserHandle,
        @NonNull final Optional<ByteArray> token) {
        return token.map(givenToken -> isSessionForUser(claimedUserHandle, givenToken)).orElse(false);
    }
}

