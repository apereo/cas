package com.yubico.core;

import module java.base;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import com.yubico.webauthn.data.ByteArray;
import lombok.RequiredArgsConstructor;
import lombok.val;
import jakarta.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
public class DefaultSessionManager implements SessionManager {
    private final WebAuthnCache<ByteArray> sessionIdsToUsers;

    private final WebAuthnCache<ByteArray> usersToSessionIds;

    private final CasReentrantLock lock = new CasReentrantLock();

    @Override
    public ByteArray createSession(final HttpServletRequest request, final ByteArray userHandle) {
        return Objects.requireNonNull(lock.tryLock(() -> {
            var sessionId = usersToSessionIds.get(request, userHandle, _ -> SessionManager.generateRandom(32));
            sessionIdsToUsers.put(request, sessionId, userHandle);
            return sessionId;
        }));
    }

    @Override
    public Optional<ByteArray> getSession(final HttpServletRequest request, final ByteArray sessionId) {
        return Optional.ofNullable(lock.tryLock(() -> sessionIdsToUsers.getIfPresent(request, sessionId)));
    }

    @Override
    public Optional<ByteArray> consumeSession(final HttpServletRequest request, final ByteArray sessionId) {
        val result = lock.tryLock(() -> {
            var userHandle = Optional.ofNullable(sessionIdsToUsers.getIfPresent(request, sessionId));
            userHandle.ifPresent(handle -> {
                sessionIdsToUsers.invalidate(request, sessionId);
                usersToSessionIds.invalidate(request, handle);
            });
            return userHandle;
        });
        return result != null ? result : Optional.empty();
    }
}
