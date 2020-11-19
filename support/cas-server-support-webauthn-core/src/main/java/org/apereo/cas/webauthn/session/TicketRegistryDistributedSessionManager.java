package org.apereo.cas.webauthn.session;

import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;

import com.yubico.core.SessionManager;
import com.yubico.webauthn.data.ByteArray;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * This is {@link TicketRegistryDistributedSessionManager}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class TicketRegistryDistributedSessionManager implements SessionManager {
    private static final int SESSION_ID_LENGTH = 32;

    private final TicketFactory ticketFactory;
    
    private final CentralAuthenicationService centralAuthenicationService;

    @Override
    public ByteArray createSession(final ByteArray userHandle) throws ExecutionException {
        val sessionId = SessionManager.generateRandom(SESSION_ID_LENGTH);
        val ticketId = TransientSessionTicketFactory.normalizeTicketId(userHandle.getHex());


        ByteArray sessionId = this.usersToSessionIds.get(userHandle, () -> );
        this.sessionIdsToUsers.put(sessionId, userHandle);
    }

    @Override
    public Optional<ByteArray> getSession(final ByteArray token) {
    }

    @Override
    public boolean isSessionForUser(final ByteArray claimedUserHandle, final ByteArray token) {
        return getSession(token).map(claimedUserHandle::equals).orElse(Boolean.valueOf(false)).booleanValue();
    }

    @Override
    public boolean isSessionForUser(final ByteArray claimedUserHandle, final Optional<ByteArray> token) {
        return token.map(t -> Boolean.valueOf(isSessionForUser(claimedUserHandle, t)))
            .orElse(Boolean.valueOf(false)).booleanValue();
    }
}
