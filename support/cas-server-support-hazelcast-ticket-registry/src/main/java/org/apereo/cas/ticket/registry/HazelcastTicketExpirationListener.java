package org.apereo.cas.ticket.registry;

import module java.base;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryExpiredListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Hands ticket-granting tickets that Hazelcast expires to the {@link HazelcastTicketRegistry}, so an expired
 * single sign-on session is logged out like one the ticket registry cleaner removes. The listener is registered
 * as a local listener, so each expired session is handled once, by the member that owns it.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class HazelcastTicketExpirationListener implements EntryExpiredListener<String, HazelcastTicketDocument> {
    private final ObjectProvider<HazelcastTicketRegistry> ticketRegistry;

    @Override
    public void entryExpired(final EntryEvent<String, HazelcastTicketDocument> event) {
        val document = Objects.requireNonNullElse(event.getOldValue(), event.getValue());
        val registry = ticketRegistry.getIfAvailable();
        if (document != null && registry != null) {
            LOGGER.debug("Hazelcast expired ticket [{}]", event.getKey());
            registry.handleExpiredTicket(document);
        }
    }
}
