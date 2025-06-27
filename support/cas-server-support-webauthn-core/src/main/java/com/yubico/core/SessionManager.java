package com.yubico.core;

import jakarta.servlet.http.HttpServletRequest;
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

    ByteArray createSession(HttpServletRequest request, @NonNull final ByteArray userHandle);

    Optional<ByteArray> getSession(HttpServletRequest request, @NonNull final ByteArray sessionId);

    default boolean isSessionForUser(
            final HttpServletRequest request,
            @NonNull final ByteArray claimedUserHandle,
            @NonNull final ByteArray token) {
        Objects.requireNonNull(claimedUserHandle);
        return getSession(request, token).map(claimedUserHandle::equals).orElse(false);
    }

    default boolean isSessionForUser(
            final HttpServletRequest request,
            @NonNull final ByteArray claimedUserHandle,
            @NonNull final Optional<ByteArray> token) {
        return token.map(givenToken -> isSessionForUser(request, claimedUserHandle, givenToken)).orElse(false);
    }
}
