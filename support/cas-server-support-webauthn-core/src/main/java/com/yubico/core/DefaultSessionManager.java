package com.yubico.core;

import com.yubico.webauthn.data.ByteArray;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class DefaultSessionManager implements SessionManager {
    private final WebAuthnCache<ByteArray> sessionIdsToUsers;

    private final WebAuthnCache<ByteArray> usersToSessionIds;

    @Override
    public ByteArray createSession(final HttpServletRequest request, @NonNull final ByteArray userHandle) {
        var sessionId = usersToSessionIds.get(request, userHandle, __ -> SessionManager.generateRandom(32));
        sessionIdsToUsers.put(request, sessionId, userHandle);
        return sessionId;
    }

    @Override
    public Optional<ByteArray> getSession(final HttpServletRequest request, @NonNull final ByteArray sessionId) {
        return Optional.ofNullable(sessionIdsToUsers.getIfPresent(request, sessionId));
    }
}
