package com.yubico.core;

import com.yubico.webauthn.data.ByteArray;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class DefaultSessionManager implements SessionManager {
    private final WebSessionWebAuthnCache<ByteArray> sessionIdsToUsers;

    private final WebSessionWebAuthnCache<ByteArray> usersToSessionIds;

    @Override
    public ByteArray createSession(@NonNull final ByteArray userHandle) {
        var sessionId = usersToSessionIds.get(userHandle, __ -> SessionManager.generateRandom(32));
        LOGGER.debug("createSession / [{}] -> [{}]", userHandle.getBase64(), sessionId.getBase64());
        sessionIdsToUsers.put(sessionId, userHandle);
        return sessionId;
    }

    @Override
    public Optional<ByteArray> getSession(
        @NonNull final ByteArray sessionId) {
        val o = Optional.ofNullable(sessionIdsToUsers.getIfPresent(sessionId));
        LOGGER.debug("getSession / [{}] -> [{}]", sessionId.getBase64(), o);
        return o;
    }
}
