package org.apereo.cas.support.events;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * This is {@link AbstractCasEventRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractCasEventRepositoryTests {

    @Test
    public void verifySave() {
        final TicketGrantingTicket ticket = new MockTicketGrantingTicket("casuser");
        final CasTicketGrantingTicketCreatedEvent event = new CasTicketGrantingTicketCreatedEvent(this, ticket);

        final CasEvent dto = new CasEvent();
        dto.setType(event.getClass().getCanonicalName());
        dto.putTimestamp(event.getTimestamp());
        dto.putCreationTime(event.getTicketGrantingTicket().getCreationTime());
        dto.putId(event.getTicketGrantingTicket().getId());
        dto.setPrincipalId(event.getTicketGrantingTicket().getAuthentication().getPrincipal().getId());

        getRepositoryInstance().save(dto);

        final Collection<CasEvent> col = getRepositoryInstance().load();
        assertEquals(col.size(), 1);
        assertFalse(col.stream().findFirst().get().getProperties().isEmpty());
    }

    public abstract CasEventRepository getRepositoryInstance();
}
