package org.apereo.cas.support.events;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AbstractCasEventRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractCasEventRepositoryTests {

    @Test
    protected void verifyLoadOps() throws Throwable {
        val eventRepository = getEventRepository();
        eventRepository.removeAll();
        
        val dto1 = getCasEvent("example1");

        eventRepository.save(dto1);
        val dt = ZonedDateTime.now(ZoneOffset.UTC).minusMonths(12);
        val loaded = eventRepository.load(dt);
        assertTrue(loaded.findAny().isPresent());

        assertFalse(eventRepository.getEventsOfTypeForPrincipal(dto1.getType(), dto1.getPrincipalId()).findAny().isEmpty());
        assertFalse(eventRepository.getEventsOfTypeForPrincipal(dto1.getType(), dto1.getPrincipalId(), dt).findAny().isEmpty());

        assertFalse(eventRepository.getEventsOfType(dto1.getType(), dt).findAny().isEmpty());
        assertFalse(eventRepository.getEventsOfType(dto1.getType()).findAny().isEmpty());

        assertFalse(eventRepository.getEventsForPrincipal(dto1.getPrincipalId()).findAny().isEmpty());
        assertFalse(eventRepository.getEventsForPrincipal(dto1.getPrincipalId(), dt).findAny().isEmpty());
    }

    @Test
    protected void verifySave() throws Throwable {
        getEventRepository().removeAll();
        
        val dto1 = getCasEvent("casuser");
        getEventRepository().save(dto1);

        val dto2 = getCasEvent("casuser");
        getEventRepository().save(dto2);

        val col = getEventRepository().load().toList();
        assertEquals(2, col.size());

        assertNotEquals(dto2.getEventId(), dto1.getEventId(), "Created event IDs are equal but they should not be");

        val load2 = getEventRepository().load();
        val loadedEvents = load2.map(CasEvent::getEventId).distinct().toList();
        assertTrue(loadedEvents.stream().anyMatch(dto1.getEventId()::equals));
        assertTrue(loadedEvents.stream().anyMatch(dto2.getEventId()::equals));

        val load3 = getEventRepository().load();
        load3.forEach(event -> {
            assertFalse(event.getProperties().isEmpty());
            if (event.getEventId().equals(dto1.getEventId())) {
                assertEquals(dto1.getType(), event.getType());
                assertEquals(dto1.getTimestamp(), event.getTimestamp());
                assertEquals(dto1.getCreationTime(), event.getCreationTime());
                assertEquals(dto1.getPrincipalId(), event.getPrincipalId());
                assertEquals(dto1.getGeoLocation(), event.getGeoLocation());
                assertEquals(dto1.getClientIpAddress(), event.getClientIpAddress());
                assertEquals(dto1.getServerIpAddress(), event.getServerIpAddress());
            } else if (event.getEventId().equals(dto2.getEventId())) {
                assertEquals(dto2.getType(), event.getType());
                assertEquals(dto2.getTimestamp(), event.getTimestamp());
                assertEquals(dto2.getCreationTime(), event.getCreationTime());
                assertEquals(dto2.getPrincipalId(), event.getPrincipalId());
                assertEquals(dto2.getGeoLocation(), event.getGeoLocation());
                assertEquals(dto2.getClientIpAddress(), event.getClientIpAddress());
                assertEquals(dto2.getServerIpAddress(), event.getServerIpAddress());
            } else {
                fail("Unexpected event");
            }
        });
    }

    public abstract CasEventRepository getEventRepository();

    private CasEvent getCasEvent(final String user) {
        val ticket = new MockTicketGrantingTicket(user);
        val event = new CasTicketGrantingTicketCreatedEvent(this, ticket, null);
        val dto = new CasEvent();
        dto.setType(event.getClass().getCanonicalName());
        dto.putTimestamp(event.getTimestamp());
        dto.setCreationTime(event.getTicketGrantingTicket().getCreationTime().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        dto.putEventId(event.getTicketGrantingTicket().getId());
        dto.putClientIpAddress("1.2.3.4");
        dto.putServerIpAddress("1.2.3.4");
        val location = new GeoLocationRequest(1234, 1234);
        location.setAccuracy("80");
        location.setTimestamp(String.valueOf(event.getTimestamp()));
        dto.putGeoLocation(location);
        dto.setPrincipalId(event.getTicketGrantingTicket().getAuthentication().getPrincipal().getId());
        return dto;
    }
}
