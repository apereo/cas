package org.apereo.cas.support.events;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;

import lombok.val;
import org.junit.Test;

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
        val dto1 = getCasEvent();
        getRepositoryInstance().save(dto1);

        val dto2 = getCasEvent();
        getRepositoryInstance().save(dto2);

        val col = getRepositoryInstance().load();
        assertEquals(2, col.size());

        assertNotEquals(dto1.getId(), 0);
        assertNotEquals(dto2.getId(), 0);
        assertNotEquals(dto2.getId(), dto1.getId());

        val casEvent = col.stream().findFirst().get();
        assertFalse(casEvent.getProperties().isEmpty());
    }

    private CasEvent getCasEvent() {
        val ticket = new MockTicketGrantingTicket("casuser");
        val event = new CasTicketGrantingTicketCreatedEvent(this, ticket);

        val dto = new CasEvent();
        dto.setType(event.getClass().getCanonicalName());
        dto.putTimestamp(event.getTimestamp());
        dto.setCreationTime(event.getTicketGrantingTicket().getCreationTime().toString());
        dto.putId(event.getTicketGrantingTicket().getId());
        dto.setPrincipalId(event.getTicketGrantingTicket().getAuthentication().getPrincipal().getId());
        return dto;
    }

    public abstract CasEventRepository getRepositoryInstance();
}
