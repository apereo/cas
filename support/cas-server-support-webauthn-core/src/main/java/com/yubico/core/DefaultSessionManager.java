package com.yubico.core;

import com.yubico.webauthn.data.ByteArray;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultSessionManager implements SessionManager {
    private final WebAuthnCache<ByteArray> sessionIdsToUsers;

    private final WebAuthnCache<ByteArray> usersToSessionIds;

    @Override
    public ByteArray createSession(final HttpServletRequest request, @NonNull final ByteArray userHandle) {
        var sessionId = usersToSessionIds.get(request, userHandle, _ -> SessionManager.generateRandom(32));
        sessionIdsToUsers.put(request, sessionId, userHandle);
        return sessionId;
    }

    @Override
    public Optional<ByteArray> getSession(final HttpServletRequest request, @NonNull final ByteArray sessionId) {
        return Optional.ofNullable(sessionIdsToUsers.getIfPresent(request, sessionId));
    }
}
