package com.yubico.core;

import com.yubico.webauthn.data.ByteArray;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class DefaultSessionManager implements SessionManager {
    private final WebSessionWebAuthnCache<ByteArray> sessionIdsToUsers;

    private final WebSessionWebAuthnCache<ByteArray> usersToSessionIds;

    @Override
    public ByteArray createSession(@NonNull final ByteArray userHandle) {
        var sessionId = usersToSessionIds.get(userHandle, __ -> SessionManager.generateRandom(32));
        sessionIdsToUsers.put(sessionId, userHandle);
        return sessionId;
    }

    @Override
    public Optional<ByteArray> getSession(
        @NonNull final ByteArray sessionId) {
        return Optional.ofNullable(sessionIdsToUsers.getIfPresent(sessionId));
    }
}
