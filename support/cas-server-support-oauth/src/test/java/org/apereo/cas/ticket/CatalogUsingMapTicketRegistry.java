package org.apereo.cas.ticket;

import org.apereo.cas.ticket.registry.AbstractTicketRegistry;

import lombok.val;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link CatalogUsingMapTicketRegistry} for testing with ticket definitions.
 *
 * @author charlibot
 * @since 6.1.0
 */
public class CatalogUsingMapTicketRegistry extends AbstractTicketRegistry {

    private final Map<TicketDefinition, Map<String, Ticket>> ticketDefinitionMapMap = new HashMap<>();
    private final TicketCatalog ticketCatalog;

    public CatalogUsingMapTicketRegistry(final TicketCatalog ticketCatalog) {
        this.ticketCatalog = ticketCatalog;
        ticketCatalog.findAll().forEach(ticketDefinition -> {
            ticketDefinitionMapMap.put(ticketDefinition, new HashMap<>());
        });
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        val encTicketId = encodeTicketId(ticketId);
        val ticketDefinition = ticketCatalog.find(ticketId);
        val map = ticketDefinitionMapMap.get(ticketDefinition);
        if (map == null) {
            return false;
        }
        val removed = map.remove(encTicketId);
        return removed != null;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        val encTicketId = encodeTicketId(ticket.getId());
        val ticketDefinition = ticketCatalog.find(ticket.getId());
        val map = ticketDefinitionMapMap.get(ticketDefinition);
        if (map == null) {
            return;
        }
        map.put(encTicketId, encodeTicket(ticket));
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        val encTicketId = encodeTicketId(ticketId);
        val ticketDefinition = ticketCatalog.find(ticketId);
        val map = ticketDefinitionMapMap.get(ticketDefinition);
        if (map == null) {
            return null;
        }
        val ticket = map.get(encTicketId);
        if (ticket == null) {
            return null;
        }
        val decoded = decodeTicket(ticket);
        if (!predicate.test(decoded)) {
            return null;
        }
        return decoded;
    }

    @Override
    public long deleteAll() {
        ticketDefinitionMapMap.values().forEach(Map::clear);
        return 0;
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return ticketDefinitionMapMap.values().stream().flatMap(a -> a.values().stream()).collect(Collectors.toList());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }
}
