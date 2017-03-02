package org.apereo.cas.ticket;

import org.springframework.core.OrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link DefaultTicketCatalog}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultTicketCatalog implements TicketCatalog {
    private final Map<String, TicketDefinition> ticketMetadataMap = new HashMap<>();

    public DefaultTicketCatalog() {
    }

    @Override
    public TicketDefinition find(final String ticketId) {
        return ticketMetadataMap.values().stream().filter(md -> ticketId.startsWith(md.getPrefix())).findFirst().orElse(null);
    }

    @Override
    public TicketDefinition find(final Ticket ticket) {
        return find(ticket.getPrefix());
    }

    @Override
    public void register(final TicketDefinition ticketDefinition) {
        ticketMetadataMap.put(ticketDefinition.getPrefix(), ticketDefinition);
    }

    @Override
    public void update(final TicketDefinition metadata) {
        register(metadata);
    }

    @Override
    public boolean contains(final String ticketId) {
        return ticketMetadataMap.containsKey(ticketId);
    }

    @Override
    public Collection<TicketDefinition> findAll() {
        final List list = new ArrayList<>(ticketMetadataMap.values());
        OrderComparator.sort(list);
        return list;
    }
}
